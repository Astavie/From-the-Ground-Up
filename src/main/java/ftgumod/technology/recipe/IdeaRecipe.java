package ftgumod.technology.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ftgumod.api.recipe.IIdeaRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
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
	public boolean test(NonNullList<ItemStack> inventory) {
		if (inventory.size() >= needed) {
			Set<Ingredient> copy = new HashSet<>(recipe);

			loop:
			for (ItemStack stack : inventory) {
				Iterator<Ingredient> iterator = copy.iterator();
				while (iterator.hasNext())
					if (iterator.next().test(stack)) {
						iterator.remove();
						continue loop;
					}
				return false;
			}

			return true;
		}
		return false;
	}

}
