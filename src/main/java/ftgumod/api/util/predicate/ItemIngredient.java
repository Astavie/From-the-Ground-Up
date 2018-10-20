package ftgumod.api.util.predicate;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public class ItemIngredient extends ItemPredicate {

	private final Ingredient ingredient;

	public ItemIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	@Override
	public ItemStack[] getMatchingStacks() {
		return ingredient.getMatchingStacks();
	}

	@Override
	public boolean apply(ItemStack stack) {
		return ingredient.test(stack);
	}

}
