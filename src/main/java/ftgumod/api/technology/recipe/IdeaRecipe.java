package ftgumod.api.technology.recipe;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ftgumod.api.FTGUAPI;
import ftgumod.api.util.JsonContextPublic;
import ftgumod.api.util.predicate.ItemPredicate;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.tuple.Pair;

public class IdeaRecipe implements IIdeaRecipe {

	private final NonNullList<Pair<ItemPredicate, Boolean>> recipe;
	private final int needed;

	public IdeaRecipe(NonNullList<Pair<ItemPredicate, Boolean>> recipe, int needed) {
		this.needed = needed;
		this.recipe = recipe;
	}

	public static IdeaRecipe deserialize(JsonObject object, JsonContextPublic context) {
		int amount = JsonUtils.getInt(object, "amount");
		JsonArray ingredients = JsonUtils.getJsonArray(object, "ingredients");

		NonNullList<Pair<ItemPredicate, Boolean>> recipe = NonNullList.create();
		for (JsonElement element : ingredients) {
			ItemPredicate predicate = FTGUAPI.stackUtils.getItemPredicate(element, context);
			JsonElement first = element;
			while (first.isJsonArray())
				first = first.getAsJsonArray().get(0); // TODO: Make consume not be first
			recipe.add(Pair.of(predicate,
					first.isJsonObject() && first.getAsJsonObject().has("consume")
							? JsonUtils.getBoolean(first.getAsJsonObject(), "consume")
							: null));
		}

		return new IdeaRecipe(recipe, amount);
	}

	@Override
	public NonNullList<ItemStack> test(InventoryCrafting inventory) {
		NonNullList<ItemStack> remaining = ForgeHooks.defaultRecipeGetRemainingItems(inventory);

		Set<Pair<ItemPredicate, Boolean>> copy = new HashSet<>(recipe);

		loop: for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty())
				continue;

			Iterator<Pair<ItemPredicate, Boolean>> iterator = copy.iterator();
			while (iterator.hasNext()) {
				Pair<ItemPredicate, Boolean> match = iterator.next();
				if (match.getLeft().test(stack)) {
					iterator.remove();
					if (match.getRight() != null) {
						if (match.getRight())
							remaining.set(i, ItemStack.EMPTY);
						else
							remaining.set(i, stack.copy());
					}
					continue loop;
				}
			}
			return null;
		}

		return recipe.size() - copy.size() >= needed ? remaining : null;
	}

}
