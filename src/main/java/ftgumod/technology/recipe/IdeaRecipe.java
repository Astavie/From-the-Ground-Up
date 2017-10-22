package ftgumod.technology.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ftgumod.api.technology.recipe.IIdeaRecipe;
import ftgumod.crafting.IngredientFluid;
import ftgumod.util.StackUtils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class IdeaRecipe implements IIdeaRecipe {

	private final NonNullList<Ingredient> recipe;
	private final int needed;

	public IdeaRecipe(NonNullList<Ingredient> recipe, int needed) {
		this.needed = needed;
		this.recipe = recipe;
	}

	public static IdeaRecipe deserialize(JsonObject object, JsonContext context) {
		int amount = JsonUtils.getInt(object, "amount");
		JsonArray ingredients = JsonUtils.getJsonArray(object, "ingredients");

		NonNullList<Ingredient> recipe = NonNullList.create();
		for (JsonElement element : ingredients)
			recipe.add(CraftingHelper.getIngredient(element, context));

		return new IdeaRecipe(recipe, amount);
	}

	@Override
	public NonNullList<ItemStack> test(InventoryCrafting inventory) {
		NonNullList<ItemStack> remaining = ForgeHooks.defaultRecipeGetRemainingItems(inventory);

		Set<Ingredient> copy = new HashSet<>(recipe);

		loop:
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty())
				continue;

			Iterator<Ingredient> iterator = copy.iterator();
			while (iterator.hasNext()) {
				Ingredient match = iterator.next();
				if (match.test(stack)) {
					iterator.remove();

					if (match instanceof IngredientFluid)
						remaining.set(i, StackUtils.INSTANCE.drain(stack.copy(), ((IngredientFluid) match).getFluid()));

					continue loop;
				}
			}
			return null;
		}

		return recipe.size() - copy.size() >= needed ? remaining : null;
	}

}
