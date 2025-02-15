package cy.jdkdigital.productivebees.integrations.jei;

import cy.jdkdigital.productivebees.ProductiveBees;
import cy.jdkdigital.productivebees.init.ModBlocks;
import cy.jdkdigital.productivebees.init.ModItems;
import cy.jdkdigital.productivebees.integrations.jei.ingredients.BeeIngredient;
import cy.jdkdigital.productivebees.integrations.jei.ingredients.BeeIngredientFactory;
import cy.jdkdigital.productivebees.integrations.jei.ingredients.BeeIngredientHelper;
import cy.jdkdigital.productivebees.integrations.jei.ingredients.BeeIngredientRenderer;
import cy.jdkdigital.productivebees.recipe.*;
import cy.jdkdigital.productivebees.setup.BeeReloadListener;
import cy.jdkdigital.productivebees.util.BeeCreator;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;

@JeiPlugin
public class ProductiveBeesJeiPlugin implements IModPlugin
{
    private static final ResourceLocation pluginId = new ResourceLocation(ProductiveBees.MODID, ProductiveBees.MODID);
    public static final ResourceLocation CATEGORY_ADVANCED_BEEHIVE_UID = new ResourceLocation(ProductiveBees.MODID, "advanced_beehive");
    public static final ResourceLocation CATEGORY_BEE_BREEDING_UID = new ResourceLocation(ProductiveBees.MODID, "bee_breeding");
    public static final ResourceLocation CATEGORY_BEE_CONVERSION_UID = new ResourceLocation(ProductiveBees.MODID, "bee_conversion");
    public static final ResourceLocation CATEGORY_BEE_SPAWNING_UID = new ResourceLocation(ProductiveBees.MODID, "bee_spawning");
    public static final ResourceLocation CATEGORY_BEE_SPAWNING_BIG_UID = new ResourceLocation(ProductiveBees.MODID, "bee_spawning_big");
    public static final ResourceLocation CATEGORY_CENTRIFUGE_UID = new ResourceLocation(ProductiveBees.MODID, "centrifuge");
    public static final ResourceLocation CATEGORY_BEE_FLOWERING_UID = new ResourceLocation(ProductiveBees.MODID, "bee_flowering");
    public static final ResourceLocation CATEGORY_INCUBATION_UID = new ResourceLocation(ProductiveBees.MODID, "incubation");
    public static final ResourceLocation CATEGORY_BLOCK_CONVERSION_UID = new ResourceLocation(ProductiveBees.MODID, "block_conversion");
    public static final ResourceLocation CATEGORY_BOTTLER_UID = new ResourceLocation(ProductiveBees.MODID, "bottler");

    public static final IIngredientType<BeeIngredient> BEE_INGREDIENT = () -> BeeIngredient.class;

    public ProductiveBeesJeiPlugin() {
        BeeIngredientFactory.getOrCreateList();
    }

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return pluginId;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ADVANCED_OAK_BEEHIVE.get()), CATEGORY_ADVANCED_BEEHIVE_UID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CENTRIFUGE.get()), CATEGORY_CENTRIFUGE_UID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.POWERED_CENTRIFUGE.get()), CATEGORY_CENTRIFUGE_UID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.COARSE_DIRT_NEST.get()), CATEGORY_BEE_SPAWNING_UID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.OAK_WOOD_NEST.get()), CATEGORY_BEE_SPAWNING_BIG_UID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.INCUBATOR.get()), CATEGORY_INCUBATION_UID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BOTTLER.get()), CATEGORY_BOTTLER_UID);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registration.addRecipeCategories(new AdvancedBeehiveRecipeCategory(guiHelper));
        registration.addRecipeCategories(new BeeBreedingRecipeCategory(guiHelper));
        registration.addRecipeCategories(new BeeConversionRecipeCategory(guiHelper));
        registration.addRecipeCategories(new CentrifugeRecipeCategory(guiHelper));
        registration.addRecipeCategories(new BeeSpawningRecipeCategory(guiHelper));
        registration.addRecipeCategories(new BeeSpawningRecipeBigCategory(guiHelper));
        registration.addRecipeCategories(new BeeFloweringRecipeCategory(guiHelper));
        registration.addRecipeCategories(new IncubationRecipeCategory(guiHelper));
        registration.addRecipeCategories(new BlockConversionRecipeCategory(guiHelper));
        registration.addRecipeCategories(new BottlerRecipeCategory(guiHelper));
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        Collection<BeeIngredient> ingredients = BeeIngredientFactory.getOrCreateList(true).values();
        registration.register(BEE_INGREDIENT, new ArrayList<>(ingredients), new BeeIngredientHelper(), new BeeIngredientRenderer());
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(ModItems.WOOD_CHIP.get());
        registration.useNbtForSubtypes(ModItems.STONE_CHIP.get());
        registration.useNbtForSubtypes(ModItems.CONFIGURABLE_HONEYCOMB.get());
        registration.useNbtForSubtypes(ModItems.CONFIGURABLE_SPAWN_EGG.get());
        registration.useNbtForSubtypes(ModItems.CONFIGURABLE_COMB_BLOCK.get());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        // Beehive bee produce recipes
        Map<ResourceLocation, IRecipe<IInventory>> advancedBeehiveRecipesMap = recipeManager.byType(AdvancedBeehiveRecipe.ADVANCED_BEEHIVE);
        registration.addRecipes(advancedBeehiveRecipesMap.values(), CATEGORY_ADVANCED_BEEHIVE_UID);
        // Centrifuge recipes
        Map<ResourceLocation, IRecipe<IInventory>> centrifugeRecipesMap = recipeManager.byType(CentrifugeRecipe.CENTRIFUGE);
        registration.addRecipes(centrifugeRecipesMap.values(), CATEGORY_CENTRIFUGE_UID);
        // Spawning recipes
        Map<ResourceLocation, IRecipe<IInventory>> beeSpawningRecipesMap = recipeManager.byType(BeeSpawningRecipe.BEE_SPAWNING);
        registration.addRecipes(beeSpawningRecipesMap.values(), CATEGORY_BEE_SPAWNING_UID);
        Map<ResourceLocation, IRecipe<IInventory>> beeSpawningRecipesBigMap = recipeManager.byType(BeeSpawningBigRecipe.BEE_SPAWNING);
        registration.addRecipes(beeSpawningRecipesBigMap.values(), CATEGORY_BEE_SPAWNING_BIG_UID);
        // Breeding recipes
        Map<ResourceLocation, IRecipe<IInventory>> beeBreedingRecipeMap = recipeManager.byType(BeeBreedingRecipe.BEE_BREEDING);
        registration.addRecipes(beeBreedingRecipeMap.values(), CATEGORY_BEE_BREEDING_UID);
        // Bee conversion recipes
        Map<ResourceLocation, IRecipe<IInventory>> beeConversionRecipeMap = recipeManager.byType(BeeConversionRecipe.BEE_CONVERSION);
        registration.addRecipes(beeConversionRecipeMap.values(), CATEGORY_BEE_CONVERSION_UID);
        // Block conversion recipes
        Map<ResourceLocation, IRecipe<IInventory>> blockConversionRecipeMap = recipeManager.byType(BlockConversionRecipe.BLOCK_CONVERSION);
        registration.addRecipes(blockConversionRecipeMap.values(), CATEGORY_BLOCK_CONVERSION_UID);
        // Bottler recipes
        Map<ResourceLocation, IRecipe<IInventory>> bottlerRecipeMap = recipeManager.byType(BottlerRecipe.BOTTLER);
        registration.addRecipes(bottlerRecipeMap.values(), CATEGORY_BOTTLER_UID);

        // Bee ingredient descriptions
        List<String> notInfoBees = Arrays.asList("minecraft:bee", "configurable_bee");
        Map<String, BeeIngredient> beeList = BeeIngredientFactory.getOrCreateList();
        for (Map.Entry<String, BeeIngredient> entry : beeList.entrySet()) {
            String beeId = entry.getKey().replace("productivebees:", "");
            if (!notInfoBees.contains(beeId)) {
                if (entry.getValue().isConfigurable()) {
                    CompoundNBT nbt = BeeReloadListener.INSTANCE.getData(entry.getKey());
                    String description = "";
                    if (nbt.contains("description")) {
                        description = new TranslationTextComponent(nbt.getString("description")).getString();
                    }
                    if (!nbt.getBoolean("selfbreed")) {
                        description = new TranslationTextComponent("productivebees.ingredient.description.selfbreed", description).getString();
                    }
                    if (!description.isEmpty()) {
                        registration.addIngredientInfo(entry.getValue(), BEE_INGREDIENT, description);
                    }
                } else {
                    registration.addIngredientInfo(entry.getValue(), BEE_INGREDIENT, "productivebees.ingredient.description." + (beeId));
                }
            }

        }
        // Bee flowering requirements
        registration.addRecipes(BeeFloweringRecipeCategory.getFlowersRecipes(beeList), CATEGORY_BEE_FLOWERING_UID);

        // Incubation recipes
        registration.addRecipes(IncubationRecipeCategory.getRecipes(beeList), CATEGORY_INCUBATION_UID);

        // Bee nest descriptions
        List<String> itemInfos = Arrays.asList(
                "inactive_dragon_egg",
                "dragon_egg_hive",
                "bumble_bee_nest",
                "sugar_cane_nest",
                "slimy_nest",
                "stone_nest",
                "sand_nest",
                "snow_nest",
                "gravel_nest",
                "coarse_dirt_nest",
                "oak_wood_nest",
                "spruce_wood_nest",
                "acacia_wood_nest",
                "dark_oak_wood_nest",
                "jungle_wood_nest",
                "birch_wood_nest",
                "end_stone_nest",
                "obsidian_nest",
                "glowstone_nest",
                "soul_sand_nest",
                "nether_brick_nest",
                "nether_quartz_nest"
        );
        for (String itemName : itemInfos) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ProductiveBees.MODID, itemName));
            registration.addIngredientInfo(new ItemStack(item), VanillaTypes.ITEM, "productivebees.ingredient.description." + itemName);
        }

        // Chip information
        registration.addIngredientInfo(new ItemStack(ModItems.WOOD_CHIP.get()), VanillaTypes.ITEM, "productivebees.ingredient.description.wood_chip");
        registration.addIngredientInfo(new ItemStack(ModItems.STONE_CHIP.get()), VanillaTypes.ITEM, "productivebees.ingredient.description.stone_chip");

        // Configurable combs
        Optional<? extends IRecipe<?>> honeycombRecipe = recipeManager.byKey(new ResourceLocation(ProductiveBees.MODID, "comb_block/configurable_honeycomb"));
        int count = 4;
        if (honeycombRecipe.isPresent()) {
            count = ((ConfigurableHoneycombRecipe) honeycombRecipe.get()).count;
        }
        Map<ResourceLocation, ShapelessRecipe> recipes = new HashMap<>();
        for (Map.Entry<String, CompoundNBT> entry : BeeReloadListener.INSTANCE.getData().entrySet()) {
            String beeType = entry.getKey();
            ResourceLocation idComb = new ResourceLocation(beeType + "_honeycomb");
            ResourceLocation idCombBlock = new ResourceLocation(beeType + "_comb");

            // Add comb item
            ItemStack comb = new ItemStack(ModItems.CONFIGURABLE_HONEYCOMB.get());
            BeeCreator.setTag(beeType, comb);
            NonNullList<Ingredient> combInput = NonNullList.create();
            for (int i = 0; i < count; i++) {
                combInput.add(Ingredient.of(comb));
            }

            // Add comb block
            ItemStack combBlock = new ItemStack(ModItems.CONFIGURABLE_COMB_BLOCK.get());
            BeeCreator.setTag(beeType, combBlock);
            NonNullList<Ingredient> combBlockInput = NonNullList.create();
            combBlockInput.add(Ingredient.of(combBlock));

            recipes.put(idComb, new ShapelessRecipe(idComb, "", combBlock, combInput));
            ItemStack combOutput = comb.copy();
            combOutput.setCount(count);
            recipes.put(idCombBlock, new ShapelessRecipe(idCombBlock, "", combOutput, combBlockInput));
        }
        registration.addRecipes(recipes.values(), VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        // Hide RBees
        Collection<BeeIngredient> ingredients = BeeIngredientFactory.getRBeesIngredients().values();
        if (ingredients.size() > 0) {
            jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(BEE_INGREDIENT, new ArrayList<>(ingredients));
        }
    }
}
