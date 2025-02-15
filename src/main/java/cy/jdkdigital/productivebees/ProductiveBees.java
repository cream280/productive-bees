package cy.jdkdigital.productivebees;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import cy.jdkdigital.productivebees.common.block.AdvancedBeehive;
import cy.jdkdigital.productivebees.common.block.DragonEggHive;
import cy.jdkdigital.productivebees.common.crafting.conditions.FluidTagEmptyCondition;
import cy.jdkdigital.productivebees.common.entity.bee.ProductiveBeeEntity;
import cy.jdkdigital.productivebees.common.entity.bee.solitary.BlueBandedBeeEntity;
import cy.jdkdigital.productivebees.common.item.BeeCage;
import cy.jdkdigital.productivebees.handler.bee.CapabilityBee;
import cy.jdkdigital.productivebees.init.*;
import cy.jdkdigital.productivebees.integrations.top.TopPlugin;
import cy.jdkdigital.productivebees.network.PacketHandler;
import cy.jdkdigital.productivebees.network.packets.Messages;
import cy.jdkdigital.productivebees.setup.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.OptionalDispenseBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.Set;

@Mod(ProductiveBees.MODID)
@EventBusSubscriber(modid = ProductiveBees.MODID)
public final class ProductiveBees
{
    public static final String MODID = "productivebees";
    public static final Random rand = new Random();

    public static final IProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public static final Logger LOGGER = LogManager.getLogger();

    public ProductiveBees() {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(this::onBiomeLoad);
        MinecraftForge.EVENT_BUS.addListener(this::onDataSync);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModPointOfInterestTypes.POINT_OF_INTEREST_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModEntities.HIVE_BEES.register(modEventBus);
        ModEntities.SOLITARY_BEES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        ModContainerTypes.CONTAINER_TYPES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModRecipeTypes.RECIPE_SERIALIZERS.register(modEventBus);
        ModParticles.PARTICLE_TYPES.register(modEventBus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(ClientSetup::init);
            modEventBus.addListener(EventPriority.LOWEST, ClientSetup::registerParticles);
        });

        modEventBus.addListener(this::onInterModEnqueue);
        modEventBus.addGenericListener(Feature.class, this::onRegisterFeatures);
        modEventBus.addListener(this::onCommonSetup);

        // Config loading
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ProductiveBeesConfig.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ProductiveBeesConfig.CLIENT_CONFIG);

        CraftingHelper.register(FluidTagEmptyCondition.Serializer.INSTANCE);
    }

    public void onInterModEnqueue(InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", TopPlugin::new);
        }
    }

    public void onServerStarting(AddReloadListenerEvent event) {
        BeeReloadListener.recipeManager = event.getDataPackRegistries().getRecipeManager();
        event.addListener(BeeReloadListener.INSTANCE);
    }

    public void onRegisterFeatures(final RegistryEvent.Register<Feature<?>> event) {
        ModConfiguredFeatures.registerConfiguredFeatures();
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {
        CapabilityBee.register();
        PacketHandler.init();
        ModAdvancements.register();

        DefaultDispenseItemBehavior cageDispenseBehavior = new OptionalDispenseBehavior()
        {
            private final DefaultDispenseItemBehavior fallbackDispenseBehavior = new DefaultDispenseItemBehavior();

            @Override
            public ItemStack execute(IBlockSource source, ItemStack stack) {
                if (stack.getItem() instanceof BeeCage && BeeCage.isFilled(stack)) {
                    Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);

                    BeeEntity entity = BeeCage.getEntityFromStack(stack, source.getLevel(), true);
                    if (entity != null) {
                        entity.hivePos = null;

                        BlockPos spawnPos = source.getPos().relative(direction);

                        entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

                        if (source.getLevel().addFreshEntity(entity)) {
                            if (stack.getItem().equals(ModItems.BEE_CAGE.get())) {
                                stack.shrink(1);
                            } else if (stack.getItem().equals(ModItems.STURDY_BEE_CAGE.get())) {
                                stack.setTag(null);
                            }
                        }
                        return stack;
                    }
                }
                return fallbackDispenseBehavior.dispense(source, stack);
            }
        };
        DispenserBlock.registerBehavior(ModItems.BEE_CAGE.get(), cageDispenseBehavior);
        DispenserBlock.registerBehavior(ModItems.STURDY_BEE_CAGE.get(), cageDispenseBehavior);

        DeferredWorkQueue.runLater(() -> {
            //Entity attribute assignments
            for (RegistryObject<EntityType<?>> registryObject : ModEntities.HIVE_BEES.getEntries()) {
                EntityType<ProductiveBeeEntity> bee = (EntityType<ProductiveBeeEntity>) registryObject.get();
                GlobalEntityTypeAttributes.put(bee, ProductiveBeeEntity.getDefaultAttributes().build());
            }
            for (RegistryObject<EntityType<?>> registryObject : ModEntities.SOLITARY_BEES.getEntries()) {
                EntityType<ProductiveBeeEntity> bee = (EntityType<ProductiveBeeEntity>) registryObject.get();
                GlobalEntityTypeAttributes.put(bee, ProductiveBeeEntity.getDefaultAttributes().build());
            }
            GlobalEntityTypeAttributes.put(ModEntities.BLUE_BANDED_BEE.get(), BlueBandedBeeEntity.getDefaultAttributes().build());
        });

        this.fixPOI(event);
    }

    private void onBiomeLoad(BiomeLoadingEvent event) {
        ModFeatures.registerFeatures(event);
    }

    private void onDataSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            PacketHandler.sendToAllPlayers(new Messages.BeeDataMessage(BeeReloadListener.INSTANCE.getData()));
        } else {
            PacketHandler.sendBeeDataToPlayer(new Messages.BeeDataMessage(BeeReloadListener.INSTANCE.getData()), event.getPlayer());
        }
    }

    private void fixPOI(final FMLCommonSetupEvent event) {
        for (RegistryObject<PointOfInterestType> poi : ModPointOfInterestTypes.POINT_OF_INTEREST_TYPES.getEntries()) {
            ModPointOfInterestTypes.fixPOITypeBlockStates(poi.get());
        }

        PointOfInterestType.BEEHIVE.matchingStates = this.makePOIStatesMutable(PointOfInterestType.BEEHIVE.matchingStates);
        ImmutableList<Block> beehives = ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block instanceof AdvancedBeehive && !(block instanceof DragonEggHive)).collect(ImmutableList.toImmutableList());
        for (Block block : beehives) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                PointOfInterestType.TYPE_BY_STATE.put(state, PointOfInterestType.BEEHIVE);
                try {
                    PointOfInterestType.BEEHIVE.matchingStates.add(state);
                } catch (Exception e) {
                    LOGGER.warn("Could not add blockstate to beehive POI " + state);
                }
            }
        }
    }

    private Set<BlockState> makePOIStatesMutable(Set<BlockState> toCopy) {
        Set<BlockState> copy = Sets.newHashSet();
        copy.addAll(toCopy);
        return copy;
    }
}
