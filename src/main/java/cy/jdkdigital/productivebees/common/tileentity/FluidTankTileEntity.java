package cy.jdkdigital.productivebees.common.tileentity;

import cy.jdkdigital.productivebees.recipe.BottlerRecipe;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class FluidTankTileEntity extends CapabilityTileEntity implements ITickableTileEntity
{
    private int tankTick = 0;

    public FluidTankTileEntity(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public void tick() {
        if (level != null && !level.isClientSide) {
            if (++this.tankTick > 21) {
                this.tankTick = 0;
                tickFluidTank();
            }
        }
    }

    public void tickFluidTank() {
        this.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(fluidHandler -> {
            FluidStack fluidStack = fluidHandler.getFluidInTank(0);
            if (fluidStack.getAmount() >= 0 && level instanceof ServerWorld) {
                this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(invHandler -> {
                    ItemStack fluidContainerItem = invHandler.getStackInSlot(InventoryHandlerHelper.BOTTLE_SLOT);
                    ItemStack existingOutput = invHandler.getStackInSlot(InventoryHandlerHelper.FLUID_ITEM_OUTPUT_SLOT);
                    if (fluidContainerItem.getCount() > 0 && (existingOutput.isEmpty() || (existingOutput.getCount() < existingOutput.getMaxStackSize()))) {
                        // Loop up bottler recipes from input
                        List<BottlerRecipe> recipes = new ArrayList<>();
                        Map<ResourceLocation, IRecipe<IInventory>> allRecipes = level.getRecipeManager().byType(BottlerRecipe.BOTTLER);
                        for (Map.Entry<ResourceLocation, IRecipe<IInventory>> entry : allRecipes.entrySet()) {
                            BottlerRecipe recipe = (BottlerRecipe) entry.getValue();
                            if (recipe.matches(fluidStack, fluidContainerItem)) {
                                recipes.add(recipe);
                            }
                        }

                        if (recipes.size() > 0) {
                            BottlerRecipe recipe = recipes.iterator().next();
                            if (existingOutput.isEmpty() || existingOutput.getItem().equals(recipe.getResultItem().getItem())) {
                                processOutput(fluidHandler, invHandler, recipe.getResultItem(), recipe.fluidInput.getSecond(), true);
                            }
                        } else if (fluidContainerItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).isPresent()) {
                            // try filling fluid container
                            fluidContainerItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(h -> {
                                int amount = h.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                processOutput(fluidHandler, invHandler, h.getFluidInTank(0).getAmount() == h.getTankCapacity(0) ? fluidContainerItem : null, amount, false);
                            });
                        } else {
                            // try to fill bucket
                            FluidActionResult fillResult = FluidUtil.tryFillContainer(fluidContainerItem, fluidHandler, Integer.MAX_VALUE, null, true);
                            if (fillResult.isSuccess()) {
                                processOutput(fluidHandler, invHandler, fillResult.getResult(), 0, true);
                            }
                        }
                    }
                });
            }
        });
    }

    private static void processOutput(IFluidHandler fluidHandler, IItemHandler itemHandler, ItemStack outputItem, int drainedAmount, boolean shrinkInputStack) {
        if (shrinkInputStack) {
            itemHandler.getStackInSlot(InventoryHandlerHelper.BOTTLE_SLOT).shrink(1);
        }
        if (outputItem != null) {
            itemHandler.insertItem(InventoryHandlerHelper.FLUID_ITEM_OUTPUT_SLOT, outputItem, false);
        }
        fluidHandler.drain(drainedAmount, IFluidHandler.FluidAction.EXECUTE);
    }
}
