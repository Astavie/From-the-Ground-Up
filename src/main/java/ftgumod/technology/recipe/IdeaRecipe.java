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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class IdeaRecipe implements IIdeaRecipe {

	private final NonNullList<Set<ItemPredicate>> recipe;
	private final int needed;

	public IdeaRecipe(NonNullList<Set<ItemPredicate>> recipe, int needed) {
		this.needed = needed;
		this.recipe = recipe;
	}

	public static IdeaRecipe deserialize(JsonObject object, JsonContext context) {
		int amount = JsonUtils.getInt(object, "amount");
		JsonArray ingredients = JsonUtils.getJsonArray(object, "ingredients");

		NonNullList<Set<ItemPredicate>> recipe = NonNullList.create();
		for (JsonElement element : ingredients)
			recipe.add(StackUtils.INSTANCE.getItemPredicate(element, context));

		return new IdeaRecipe(recipe, amount);
	}

	@Override
	public NonNullList<ItemStack> test(InventoryCrafting inventory) {
		NonNullList<ItemStack> remaining = ForgeHooks.defaultRecipeGetRemainingItems(inventory);

		Set<Set<ItemPredicate>> copy = new HashSet<>(recipe);

		loop:
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty())
				continue;

			Iterator<Set<ItemPredicate>> iterator = copy.iterator();
			while (iterator.hasNext()) {
				Set<ItemPredicate> match = iterator.next();
				for (ItemPredicate predicate : match)
					if (predicate.test(stack)) {
						iterator.remove();
						if (predicate instanceof FluidPredicate)
							remaining.set(i, ((FluidPredicate) predicate).drain(stack.copy()));
						continue loop;
					}
			}
			return null;
		}

		return recipe.size() - copy.size() >= needed ? remaining : null;
	}

}
