package ftgumod.compat.jei;

import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.api.technology.unlock.IUnlock;
import ftgumod.api.util.predicate.ItemPredicate;
import ftgumod.compat.ICompat;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import mezz.jei.Internal;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Config;
import mezz.jei.recipes.RecipeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@JEIPlugin
public class CompatJEI implements ICompat, IModPlugin {

	private static IRecipeRegistry recipe;
	private static IIngredientRegistry ingredient;

	private static boolean config = Config.isCheatItemsEnabled();

	private List<ItemStack> lockedLast = new LinkedList<>();
	private static List<IRecipeWrapper> wrappers;

	@Override
	public void register(IModRegistry registry) {
		IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
		blacklist.addIngredientToBlacklist(new ItemStack(Content.i_parchmentIdea));
		blacklist.addIngredientToBlacklist(new ItemStack(Content.i_parchmentResearch));

		ingredient = registry.getIngredientRegistry();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		recipe = runtime.getRecipeRegistry();
		wrappers = recipe.getRecipeWrappers(recipe.getRecipeCategory(VanillaRecipeCategoryUid.CRAFTING));
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean run(Object... arg) {
		if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
			Minecraft.getMinecraft().addScheduledTask(() -> run(arg));
			return true;
		}

		if (Minecraft.getMinecraft().player == null)
			return true;

		if (arg.length > 0 && (boolean) arg[0]) {
			if (config != Config.isCheatItemsEnabled())
				config = Config.isCheatItemsEnabled();
			else
				return true;
		}

		// Get locked stacks
		List<ItemStack> stacks = new LinkedList<>();

		for (Technology tech : TechnologyManager.INSTANCE)
			if (!tech.isResearched(Minecraft.getMinecraft().player))
				for (IUnlock unlock : tech.getUnlock())
					if (unlock.isDisplayed())
						Collections.addAll(stacks, unlock.getIcon().getMatchingStacks());

		// Lock those inside JEI
		if (!lockedLast.isEmpty()) {
			ingredient.addIngredientsAtRuntime(VanillaTypes.ITEM, lockedLast);
		}
		if (!config && !stacks.isEmpty() && FTGU.hide == 2) {
			ingredient.removeIngredientsAtRuntime(VanillaTypes.ITEM, stacks);
		}

		// Lock every recipe with those outputs
		Ingredient locked = new ItemPredicate(stacks.toArray(new ItemStack[0]));

		for (IRecipeWrapper wrapper : wrappers) {
			IIngredients ingredients = ((RecipeRegistry) recipe).getIngredients(wrapper);
			List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);

			if (FTGU.hide > 0 && outputs.size() > 0 && outputs.get(0).size() > 0 && locked.apply(outputs.get(0).get(0))) {
				recipe.hideRecipe(wrapper, VanillaRecipeCategoryUid.CRAFTING);
			} else {
				recipe.unhideRecipe(wrapper, VanillaRecipeCategoryUid.CRAFTING);
			}
		}

		// Refresh ingredient list
		Internal.getRuntime().getIngredientListOverlay().rebuildItemFilter();
		lockedLast = stacks;
		return true;
	}

}
