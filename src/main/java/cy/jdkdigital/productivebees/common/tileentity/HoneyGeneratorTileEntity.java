package cy.jdkdigital.productivebees.common.tileentity;

import cy.jdkdigital.productivebees.ProductiveBeesConfig;
import cy.jdkdigital.productivebees.common.block.HoneyGenerator;
import cy.jdkdigital.productivebees.container.HoneyGeneratorContainer;
import cy.jdkdigital.productivebees.init.ModBlocks;
import cy.jdkdigital.productivebees.init.ModFluids;
import cy.jdkdigital.productivebees.init.ModTags;
import cy.jdkdigital.productivebees.init.ModTileEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HoneyGeneratorTileEntity extends FluidTankTileEntity implements INamedContainerProvider, ITickableTileEntity, UpgradeableTileEntity
{
    protected int tickCounter = 0;

    private LazyOptional<IItemHandlerModifiable> inventoryHandler = LazyOptional.of(() -> new InventoryHandlerHelper.ItemHandler(2, this)
    {
        @Override
        public boolean isBottleItem(Item item) {
            return item.equals(Items.HONEY_BOTTLE) || item.is(ModTags.HONEY_BUCKETS) || item.equals(Items.HONEY_BLOCK);
        }

        @Override
        public boolean isInputSlot(int slot) {
            return slot == InventoryHandlerHelper.BOTTLE_SLOT;
        }
    });

    protected LazyOptional<IFluidHandler> fluidInventory = LazyOptional.of(() -> new InventoryHandlerHelper.FluidHandler(10000)
    {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.HONEY.get());
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            if (fluid.getAmount() > 0) {
                HoneyGeneratorTileEntity.this.setFilled(true);
            } else {
                HoneyGeneratorTileEntity.this.setFilled(false);
            }
            HoneyGeneratorTileEntity.this.setChanged();
        }
    });
    private List<IEnergyStorage> recipients = new ArrayList<>();

    private void setFilled(boolean filled) {
        if (level != null && !level.isClientSide) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(HoneyGenerator.FULL, filled));
        }
    }

    private void setOn(boolean filled) {
        if (level != null && !level.isClientSide) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(HoneyGenerator.ON, filled));
        }
    }

    protected LazyOptional<IItemHandlerModifiable> upgradeHandler = LazyOptional.of(() -> new InventoryHandlerHelper.UpgradeHandler(4, this));

    protected LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> new EnergyStorage(100000));

    public HoneyGeneratorTileEntity() {
        super(ModTileEntityTypes.HONEY_GENERATOR.get());
    }

    @Override
    public void tick() {
        int tickRate = 10;
        if (level != null && !level.isClientSide) {
            if (++tickCounter % tickRate == 0) {
                int inputPowerAmount = ProductiveBeesConfig.GENERAL.generatorPowerGen.get() * tickRate;
                int fluidConsumeAmount = ProductiveBeesConfig.GENERAL.generatorHoneyUse.get() * tickRate;
                fluidInventory.ifPresent(fluidHandler -> energyHandler.ifPresent(handler -> {
                    if (fluidHandler.getFluidInTank(0).getAmount() >= fluidConsumeAmount && handler.receiveEnergy(inputPowerAmount, true) > 0) {
                        handler.receiveEnergy(inputPowerAmount, false);
                        fluidHandler.drain(fluidConsumeAmount, IFluidHandler.FluidAction.EXECUTE);
                        setOn(true);
                    } else {
                        setOn(false);
                    }
                }));
            }
            this.sendOutPower(tickRate);
        }
        super.tick();
    }

    private void sendOutPower(int modifier) {
        if (this.level != null) {
            energyHandler.ifPresent(energyHandler -> {
                AtomicInteger capacity = new AtomicInteger(energyHandler.getEnergyStored());
                if (capacity.get() > 0) {
                    AtomicBoolean dirty = new AtomicBoolean(false);
                    for (IEnergyStorage handler : recipients) {
                        boolean doContinue = true;
                        if (handler.canReceive()) {
                            int received = handler.receiveEnergy(Math.min(capacity.get(), 100 * modifier), false);
                            capacity.addAndGet(-received);
                            energyHandler.extractEnergy(received, false);
                            dirty.set(true);
                            doContinue = capacity.get() > 0;
                        }

                        if (!doContinue) {
                            break;
                        }
                    }
                    if (dirty.get()) {
                        this.setChanged();
                    }
                }
            });
        }
    }

    @Override
    public void tickFluidTank() {
        fluidInventory.ifPresent(fluidHandler -> {
            inventoryHandler.ifPresent(invHandler -> {
                int fluidSpace = fluidInventory.map(h -> h.getTankCapacity(0) - h.getFluidInTank(0).getAmount()).orElse(0);
                if (!invHandler.getStackInSlot(0).isEmpty()) {
                    ItemStack invItem = invHandler.getStackInSlot(0);
                    ItemStack outputInvItem = invHandler.getStackInSlot(1);
                    ItemStack outputItem = ItemStack.EMPTY;

                    LazyOptional<IFluidHandler> itemFluidHandler = invItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    boolean isHoneyBottle = invItem.getItem().equals(Items.HONEY_BOTTLE);
                    boolean isHoneyBlock = invItem.getItem().equals(Items.HONEY_BLOCK);
                    boolean isHoneyBucket = invItem.getItem().is(ModTags.HONEY_BUCKETS);

                    int addAmount = 0;
                    if (isHoneyBottle) {
                        addAmount = 250;
                        outputItem = new ItemStack(Items.GLASS_BOTTLE);
                    } else if (isHoneyBlock) {
                        addAmount = 1000;
                    } else if (isHoneyBucket) {
                        addAmount = 1000;
                        outputItem = new ItemStack(Items.BUCKET);
                    } else if (itemFluidHandler.isPresent()) {
                        addAmount = fluidSpace;
                    }

                    // Check if output has room
                    if (!outputItem.equals(ItemStack.EMPTY) && !outputInvItem.isEmpty() && (!outputInvItem.getItem().equals(outputItem.getItem()) || outputInvItem.getMaxStackSize() == outputInvItem.getCount())) {
                        return;
                    }

                    // Move empty containers to output
                    if (itemFluidHandler.isPresent() && itemFluidHandler.map(h -> h.getFluidInTank(0).isEmpty()).orElse(false)) {
                        if (outputInvItem.isEmpty()) {
                            if (!invHandler.insertItem(1, invItem, false).isEmpty()) {
                                invHandler.setStackInSlot(0, ItemStack.EMPTY);
                            }
                        }
                        return;
                    }

                    if (addAmount > 0 && addAmount <= fluidSpace) {
                        int fillAmount = fluidHandler.fill(new FluidStack(ModFluids.HONEY.get(), addAmount), IFluidHandler.FluidAction.EXECUTE);
                        if (itemFluidHandler.isPresent()) {
                            FluidUtil.tryEmptyContainer(invItem, fluidHandler, fillAmount, null, true);
                        } else {
                            invItem.shrink(1);
                            if (!outputItem.equals(ItemStack.EMPTY)) {
                                if (outputInvItem.isEmpty()) {
                                    invHandler.setStackInSlot(1, outputItem);
                                } else {
                                    outputInvItem.grow(1);
                                }
                            }
                        }
                    }
                }
            });
        });
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        // Rebuild cached attached TEs
        refreshConnectedTileEntityCache();
    }

    @Override
    public LazyOptional<IItemHandlerModifiable> getUpgradeHandler() {
        return upgradeHandler;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return inventoryHandler.cast();
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidInventory.cast();
        } else if (cap == CapabilityEnergy.ENERGY) {
            return energyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(ModBlocks.HONEY_GENERATOR.get().getDescriptionId());
    }

    @Nullable
    @Override
    public Container createMenu(final int windowId, final PlayerInventory playerInventory, final PlayerEntity player) {
        return new HoneyGeneratorContainer(windowId, playerInventory, this);
    }

    public void refreshConnectedTileEntityCache() {
        if (level instanceof ServerWorld) {
            List<IEnergyStorage> recipients = new ArrayList<>();
            Direction[] directions = Direction.values();
            for (Direction direction : directions) {
                TileEntity te = level.getBlockEntity(worldPosition.relative(direction));
                if (te != null) {
                    te.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite()).ifPresent(recipients::add);
                }
            }
            this.recipients = recipients;
        }
    }
}
