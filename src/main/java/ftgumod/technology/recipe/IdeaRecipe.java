package ftgumod.technology.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ftgumod.api.technology.recipe.IIdeaRecipe;
import ftgumod.util.FluidPredicate;
import ftgumod.util.StackUtils;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.JsonContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class IdeaRecipe implements IIdeaRecipe {

	private final NonNullList<Pair<Set<ItemPredicate>, Boolean>> recipe;
	private final int needed;

	public IdeaRecipe(NonNullList<Pair<Set<ItemPredicate>, Boolean>> recipe, int needed) {
		this.needed = needed;
		this.recipe = recipe;
	}

	public static IdeaRecipe deserialize(JsonObject object, JsonContext context) {
		int amount = JsonUtils.getInt(object, "amount");
		JsonArray ingredients = JsonUtils.getJsonArray(object, "ingredients");

		NonNullList<Pair<Set<ItemPredicate>, Boolean>> recipe = NonNullList.create();
		for (JsonElement element : ingredients) {
			Set<ItemPredicate> predicate = StackUtils.INSTANCE.getItemPredicate(element, context);
			JsonElement first = element;
			while (first.isJsonArray())
				first = first.getAsJsonArray().get(0); // TODO: Make consume not be first
			recipe.add(Pair.of(predicate, first.isJsonObject() && first.getAsJsonObject().has("consume") ? JsonUtils.getBoolean(first.getAsJsonObject(), "consume") : null));
		}

		return new IdeaRecipe(recipe, amount);
	}

	@Override
	public NonNullList<ItemStack> test(InventoryCrafting inventory) {
		NonNullList<ItemStack> remaining = ForgeHooks.defaultRecipeGetRemainingItems(inventory);

		Set<Pair<Set<ItemPredicate>, Boolean>> copy = new HashSet<>(recipe);

		loop:
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty())
				continue;

			Iterator<Pair<Set<ItemPredicate>, Boolean>> iterator = copy.iterator();
			while (iterator.hasNext()) {
				Pair<Set<ItemPredicate>, Boolean> match = iterator.next();
				for (ItemPredicate predicate : match.getLeft())
					if (predicate.test(stack)) {
						iterator.remove();
						if (match.getRight() != null) {
							if (match.getRight())
								remaining.set(i, ItemStack.EMPTY);
							else
								remaining.set(i, stack.copy());
						} else if (predicate instanceof FluidPredicate)
							remaining.set(i, ((FluidPredicate) predicate).drain(stack.copy()));
						continue loop;
					}
			}
			return null;
		}

		return recipe.size() - copy.size() >= needed ? remaining : null;
	}

}
