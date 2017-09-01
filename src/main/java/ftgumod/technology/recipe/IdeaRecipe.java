package ftgumod.technology.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class IdeaRecipe {

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

	public boolean test(Collection<ItemStack> inventory) {
		Set<Ingredient> copy = new HashSet<>(recipe);
		int count = 0;

		for (ItemStack stack : inventory) {
			Iterator<Ingredient> iterator = copy.iterator();
			while (iterator.hasNext())
				if (iterator.next().test(stack)) {
					iterator.remove();
					count++;
					break;
				}
		}

		return count >= needed;
	}

}
