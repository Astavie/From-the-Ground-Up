package ftgumod.api.util.predicate;

import com.google.gson.JsonObject;
import ftgumod.api.util.JsonContextPublic;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public class ItemPredicate extends Ingredient {

	public ItemPredicate(ItemStack... stacks) {
		super(stacks);
	}

	public ItemStack getDisplayStack() {
		ItemStack[] matching = getMatchingStacks();
		if (matching.length > 0)
			return matching[0];
		return ItemStack.EMPTY;
	}

	public interface Factory<T extends ItemPredicate> {

		T deserialize(JsonObject object, JsonContextPublic context);

	}

}
