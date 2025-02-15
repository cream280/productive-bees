package cy.jdkdigital.productivebees.init;

import cy.jdkdigital.productivebees.ProductiveBees;
import cy.jdkdigital.productivebees.common.entity.BeeBombEntity;
import cy.jdkdigital.productivebees.common.entity.bee.ConfigurableBeeEntity;
import cy.jdkdigital.productivebees.common.entity.bee.ProductiveBeeEntity;
import cy.jdkdigital.productivebees.common.entity.bee.SolitaryBeeEntity;
import cy.jdkdigital.productivebees.common.entity.bee.hive.*;
import cy.jdkdigital.productivebees.common.entity.bee.solitary.*;
import cy.jdkdigital.productivebees.common.item.SpawnEgg;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;

@EventBusSubscriber(modid = ProductiveBees.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, ProductiveBees.MODID);
    public static final DeferredRegister<EntityType<?>> HIVE_BEES = DeferredRegister.create(ForgeRegistries.ENTITIES, ProductiveBees.MODID);
    public static final DeferredRegister<EntityType<?>> SOLITARY_BEES = DeferredRegister.create(ForgeRegistries.ENTITIES, ProductiveBees.MODID);

    public static RegistryObject<EntityType<ProjectileItemEntity>> BEE_BOMB = createEntity("bee_bomb", BeeBombEntity::new);

    public static RegistryObject<EntityType<BeeEntity>> DYE_BEE = createHiveBee("dye_bee", ProductiveBeeEntity::new, 16768648, 6238757, ModItemGroups.PRODUCTIVE_BEES);
    public static RegistryObject<EntityType<BeeEntity>> LUMBER_BEE = createHiveBee("lumber_bee", LumberBeeEntity::new, 8306542, 6238757, ModItemGroups.PRODUCTIVE_BEES);
    public static RegistryObject<EntityType<BeeEntity>> QUARRY_BEE = createHiveBee("quarry_bee", QuarryBeeEntity::new, 7566195, 6238757, ModItemGroups.PRODUCTIVE_BEES);
    public static RegistryObject<EntityType<BeeEntity>> RANCHER_BEE = createHiveBee("rancher_bee", RancherBeeEntity::new, 9615358, 6238757, ModItemGroups.PRODUCTIVE_BEES);
    public static RegistryObject<EntityType<BeeEntity>> COLLECTOR_BEE = createHiveBee("collector_bee", HoarderBeeEntity::new, 8306149, 6238757, ModItemGroups.PRODUCTIVE_BEES);
    public static RegistryObject<EntityType<BeeEntity>> HOARDER_BEE = createHiveBee("hoarder_bee", HoarderBeeEntity::new, 8306149, 6238757, ModItemGroups.PRODUCTIVE_BEES);
    public static RegistryObject<EntityType<BeeEntity>> FARMER_BEE = createHiveBee("farmer_bee", FarmerBeeEntity::new, 9615358, 6238757, ModItemGroups.PRODUCTIVE_BEES);
    public static RegistryObject<EntityType<BeeEntity>> CREEPER_BEE = createHiveBee("creeper_bee", CreeperBeeEntity::new, 894731, 6238757, ModItemGroups.PRODUCTIVE_BEES);
    public static RegistryObject<EntityType<BeeEntity>> CUPID_BEE = createHiveBee("cupid_bee", CupidBeeEntity::new, 894731, 6238757, ModItemGroups.PRODUCTIVE_BEES);

    public static RegistryObject<EntityType<BeeEntity>> ASHY_MINING_BEE = createSolitaryBee("ashy_mining_bee", MiningBeeEntity::new, 11709345, 6238757);
    public static RegistryObject<EntityType<BeeEntity>> BLUE_BANDED_BEE = createSolitaryBee("blue_banded_bee", BlueBandedBeeEntity::new, 9615358, 6238757);
    public static RegistryObject<EntityType<BeeEntity>> GREEN_CARPENTER_BEE = createSolitaryBee("green_carpenter_bee", GreenCarpenterBeeEntity::new, 9615358, 6238757);
    public static RegistryObject<EntityType<BeeEntity>> YELLOW_BLACK_CARPENTER_BEE = createSolitaryBee("yellow_black_carpenter_bee", YellowBlackCarpenterBeeEntity::new, 15582019, 6238757);
    public static RegistryObject<EntityType<BeeEntity>> CHOCOLATE_MINING_BEE = createSolitaryBee("chocolate_mining_bee", MiningBeeEntity::new, 11709345, 6238757);
    public static RegistryObject<EntityType<BeeEntity>> DIGGER_BEE = createSolitaryBee("digger_bee", DiggerBeeEntity::new, 8875079, 6238757);
    public static RegistryObject<EntityType<BeeEntity>> LEAFCUTTER_BEE = createSolitaryBee("leafcutter_bee", SolitaryBeeEntity::new, 2057258, 6238757);
    public static RegistryObject<EntityType<BeeEntity>> MASON_BEE = createSolitaryBee("mason_bee", MasonBeeEntity::new, 2226382, 6238757);
    public static RegistryObject<EntityType<BeeEntity>> NEON_CUCKOO_BEE = createSolitaryBee("neon_cuckoo_bee", NeonCuckooBeeEntity::new, 9615358, 6238757);
    public static RegistryObject<EntityType<BeeEntity>> NOMAD_BEE = createSolitaryBee("nomad_bee", NomadBeeEntity::new, 14529911, 6238757);
    public static RegistryObject<EntityType<BeeEntity>> REED_BEE = createSolitaryBee("reed_bee", ReedBeeEntity::new, 13806336, 6238757);
    public static RegistryObject<EntityType<BeeEntity>> RESIN_BEE = createSolitaryBee("resin_bee", ResinBeeEntity::new, 13939231, 6238757);
    public static RegistryObject<EntityType<BeeEntity>> SWEATY_BEE = createSolitaryBee("sweaty_bee", SweatyBeeEntity::new, 9748939, 6238757);
    public static RegistryObject<EntityType<BumbleBeeEntity>> BUMBLE = createSolitaryBee("bumble_bee", BumbleBeeEntity::new, 9748939, 6238757);

    public static RegistryObject<EntityType<ConfigurableBeeEntity>> CONFIGURABLE_BEE = createColoredHiveBee("configurable_bee", ConfigurableBeeEntity::new, "#73ffb9", "#0f5c7a", ModItemGroups.PRODUCTIVE_BEES);

    public static <E extends BeeEntity> RegistryObject<EntityType<E>> createColoredHiveBee(String name, EntityType.IFactory<E> supplier, String primaryColor, String secondaryColor, ItemGroup itemGroup) {
        Color primary = Color.decode(primaryColor);
        Color secondary = Color.decode(secondaryColor);
        return createHiveBee(name, (entityType, world) -> {
            ProductiveBeeEntity bee = (ProductiveBeeEntity) supplier.create(entityType, world);
            bee.setColor(primary, secondary);
            return (E) bee;
        }, primary.getRGB(), secondary.getRGB(), itemGroup);
    }

    public static <E extends BeeEntity> RegistryObject<EntityType<E>> createHiveBee(String name, EntityType.IFactory<E> supplier, int primaryColor, int secondaryColor, ItemGroup itemGroup) {
        return createBee(HIVE_BEES, name, supplier, primaryColor, secondaryColor, itemGroup);
    }

    public static <E extends BeeEntity> RegistryObject<EntityType<E>> createSolitaryBee(String name, EntityType.IFactory<E> supplier, int primaryColor, int secondaryColor) {
        return createBee(SOLITARY_BEES, name, supplier, primaryColor, secondaryColor, ModItemGroups.PRODUCTIVE_BEES);
    }

    public static <E extends BeeEntity> RegistryObject<EntityType<E>> createBee(DeferredRegister<EntityType<?>> registry, String name, EntityType.IFactory<E> supplier, int primaryColor, int secondaryColor, ItemGroup itemGroup) {
        EntityType.Builder<E> builder = EntityType.Builder.of(supplier, EntityClassification.CREATURE).sized(0.7F, 0.6F).setTrackingRange(8);

        RegistryObject<EntityType<E>> entity = registry.register(name, () -> builder.build(ProductiveBees.MODID + ":" + name));

        if (itemGroup != null) {
            RegistryObject<Item> spawnEgg = ModItems.ITEMS.register("spawn_egg_" + name, () -> new SpawnEgg(entity::get, secondaryColor, primaryColor, new Item.Properties().tab(itemGroup)));
            if (name.equals("configurable_bee")) {
                ModItems.CONFIGURABLE_SPAWN_EGG = spawnEgg;
            }
            ModItems.SPAWN_EGGS.add(spawnEgg);
        }


        return entity;
    }

    public static <E extends Entity> RegistryObject<EntityType<E>> createEntity(String name, EntityType.IFactory<E> supplier) {
        EntityType.Builder<E> builder = EntityType.Builder.of(supplier, EntityClassification.MISC).sized(0.25F, 0.25F);

        RegistryObject<EntityType<E>> entity = ENTITIES.register(name, () -> builder.build(ProductiveBees.MODID + ":" + name));

        return entity;
    }
}
