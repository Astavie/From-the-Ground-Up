package ftgumod.compat.jei;

import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.api.technology.unlock.IUnlock;
import ftgumod.compat.ICompat;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import mezz.jei.Internal;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.collect.Table;
import mezz.jei.config.Config;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.util.IngredientSet;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.*;

@JEIPlugin
public class CompatJEI implements ICompat, IModPlugin {

	private static final Table<IIngredientType, String, Object> original = Table.hashBasedTable();
	private static List<IRecipeCategory> categories;

	private static IRecipeRegistry recipe;
	private static IIngredientRegistry ingredient;
	private static IIngredientHelper<ItemStack> helper;

	private static boolean config = Config.isCheatItemsEnabled();

	@Override
	public void register(IModRegistry registry) {
		IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
		blacklist.addIngredientToBlacklist(new ItemStack(Content.i_parchmentIdea));
		blacklist.addIngredientToBlacklist(new ItemStack(Content.i_parchmentResearch));

		ingredient = registry.getIngredientRegistry();
		helper = ingredient.getIngredientHelper(VanillaTypes.ITEM);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		recipe = runtime.getRecipeRegistry();
		categories = new LinkedList<>(recipe.getRecipeCategories()); // Save a copy since the list might change

		for (IIngredientType type : ingredient.getRegisteredIngredientTypes()) {
			IIngredientHelper helper = ingredient.getIngredientHelper(type);
			for (Object object : ingredient.getAllIngredients(type))
				original.put(type, helper.getUniqueId(object), object);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean run(Object... arg) {
		if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
			Minecraft.getMinecraft().addScheduledTask(() -> run(arg));
			return true;
		}

		if (arg.length > 0 && (boolean) arg[0]) {
			if (config != Config.isCheatItemsEnabled())
				config = Config.isCheatItemsEnabled();
			else return true;
		}

		IngredientSet<ItemStack> itemWhitelist = IngredientSet.create(VanillaTypes.ITEM, helper);
		IngredientSet<ItemStack> itemBlacklist = IngredientSet.create(VanillaTypes.ITEM, helper);

		for (Technology tech : TechnologyManager.INSTANCE) {
			if (FTGU.hide == 0 || Config.isCheatItemsEnabled() || tech.isResearched(Minecraft.getMinecraft().player)) {
				for (IUnlock unlock : tech.getUnlock())
					if (unlock.isDisplayed())
						itemWhitelist.addAll(Arrays.asList(unlock.getIcon().getMatchingStacks()));
			} else {
				for (IUnlock unlock : tech.getUnlock())
					if (unlock.isDisplayed())
						itemBlacklist.addAll(Arrays.asList(unlock.getIcon().getMatchingStacks()));
			}
		}

		if (!itemWhitelist.isEmpty())
			ingredient.addIngredientsAtRuntime(VanillaTypes.ITEM, itemWhitelist);
		if (!itemBlacklist.isEmpty())
			ingredient.removeIngredientsAtRuntime(VanillaTypes.ITEM, itemBlacklist);

		for (IIngredientType type : ingredient.getRegisteredIngredientTypes()) {
			Collection collection = new LinkedList();
			for (Object object : original.getRow(type).values()) {
				if (object == null || (object instanceof ItemStack && (itemWhitelist.contains(object) || itemBlacklist.contains(object))))
					continue;
				if (recipe.getRecipeCategories(recipe.createFocus(IFocus.Mode.OUTPUT, object)).isEmpty())
					collection.add(object);
			}

			if (!collection.isEmpty()) {
				if (FTGU.hide == 2 && !Config.isCheatItemsEnabled())
					ingredient.removeIngredientsAtRuntime(type, collection);
				else
					ingredient.addIngredientsAtRuntime(type, collection);
			}
		}

		List<IRecipeCategory> categories = new LinkedList<>(CompatJEI.categories);
		Table<IIngredientType, String, Object> add = Table.hashBasedTable();
		for (IRecipeCategory category : categories) {
			List<IRecipeWrapper> wrappers = recipe.getRecipeWrappers(category);
			for (IRecipeWrapper wrapper : wrappers) {
				Ingredients ingredients = new Ingredients();
				wrapper.getIngredients(ingredients);

				for (Map.Entry<IIngredientType, List> entry : ingredients.getOutputIngredients().entrySet()) {
					for (Object object : entry.getValue()) {
						String id = ingredient.getIngredientHelper(object).getUniqueId(object);
						if (add.get(entry.getKey(), id) != null)
							continue;

						object = original.get(entry.getKey(), id);
						if (object == null || (object instanceof ItemStack && (itemWhitelist.contains(object) || itemBlacklist.contains(object))))
							continue;

						add.put(entry.getKey(), id, object);
					}
				}
			}
		}
		for (IIngredientType type : ingredient.getRegisteredIngredientTypes())
			if (!add.getRow(type).values().isEmpty())
				ingredient.addIngredientsAtRuntime(type, add.getRow(type).values());

		if (FTGU.hide != 0 && !Config.isCheatItemsEnabled()) {
			boolean change = true;
			while (change) {
				change = false;
				Table<IIngredientType, String, Object> remove = Table.hashBasedTable();

				for (Iterator<IRecipeCategory> iterator = categories.iterator(); iterator.hasNext(); ) {
					IRecipeCategory category = iterator.next();
					if (recipe.getRecipeCatalysts(category).isEmpty()) {
						List<IRecipeWrapper> wrappers = recipe.getRecipeWrappers(category);
						for (IRecipeWrapper wrapper : wrappers) {
							Ingredients ingredients = new Ingredients();
							wrapper.getIngredients(ingredients);

							for (Map.Entry<IIngredientType, List> entry : ingredients.getOutputIngredients().entrySet()) {
								a:
								for (Object object : entry.getValue()) {
									String id = ingredient.getIngredientHelper(object).getUniqueId(object);
									if (remove.get(entry.getKey(), id) != null)
										continue;

									object = original.get(entry.getKey(), id);
									if (object == null || (object instanceof ItemStack && (itemWhitelist.contains(object) || itemBlacklist.contains(object))))
										continue;

									for (IRecipeCategory crafter : recipe.getRecipeCategories(recipe.createFocus(IFocus.Mode.OUTPUT, object)))
										if (!recipe.getRecipeCatalysts(crafter).isEmpty())
											continue a;

									remove.put(entry.getKey(), id, object);
									change = true;
								}
							}
						}
						iterator.remove();
					}
				}
				for (IIngredientType type : ingredient.getRegisteredIngredientTypes())
					if (!remove.getRow(type).values().isEmpty())
						ingredient.removeIngredientsAtRuntime(type, remove.getRow(type).values());
			}
		}

		Internal.getRuntime().getIngredientListOverlay().rebuildItemFilter();
		return true;
	}

}
