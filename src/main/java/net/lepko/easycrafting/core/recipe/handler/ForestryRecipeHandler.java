package net.lepko.easycrafting.core.recipe.handler;

import net.minecraftforge.fml.common.Loader;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.recipe.RecipeManager;
import net.lepko.easycrafting.core.recipe.WrappedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class ForestryRecipeHandler implements IRecipeHandler {

    private static Class<? super IRecipe> shapedRecipeClass = null;
    private static Method inputMethod = null;
    private static Method checkItemMatch = null;

    static {
        if (Loader.isModLoaded("Forestry")) {
            try {
                shapedRecipeClass = (Class<? super IRecipe>) Class.forName("forestry.core.utils.ShapedRecipeCustom");
                inputMethod = shapedRecipeClass.getMethod("getIngredients");
                checkItemMatch = shapedRecipeClass.getDeclaredMethod("checkItemMatch", ItemStack.class, ItemStack.class);
                checkItemMatch.setAccessible(true);
            } catch (Exception e) {
                Ref.LOGGER.warn("[Forestry Recipe Scan] Forestry ShapedRecipeCustom.class could not be obtained!", e);
            }
        } else {
            Ref.LOGGER.info("[Forestry Recipe Scan] Disabled.");
        }
    }

    @Override
    public List<Object> getInputs(IRecipe recipe) {
        List<Object> ingredients = null;
        if (shapedRecipeClass != null && shapedRecipeClass.isInstance(recipe) && inputMethod != null && checkItemMatch != null) {
            try {
                Object[] input = (Object[]) inputMethod.invoke(recipe);
                ingredients = new ArrayList<Object>(Arrays.asList(input));
            } catch (Exception e) {
                Ref.LOGGER.warn("[Forestry Recipe Scan] " + recipe.getClass().getName() + " failed!", e);
                return null;
            }
        }
        return ingredients;
    }

    @Override
    public boolean matchItem(ItemStack target, ItemStack candidate, WrappedRecipe recipe) {
        boolean b;
        try {
            b = (Boolean) checkItemMatch.invoke(recipe.recipe, target, candidate);
        } catch (Exception e) {
            Ref.LOGGER.warn("[Forestry Recipe Handler] failed to match item!", e);
            return false;
        }
        return b;
    }

    @Override
    public ItemStack getCraftingResult(WrappedRecipe recipe, List<ItemStack> usedIngredients) {
        return recipe.recipe.getCraftingResult(RecipeManager.getCraftingInventory(usedIngredients));
    }
}
