package ftgumod.api.technology.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IIdeaRecipe {

	/**
	 * @param inventory The inventory to compare to
	 * @return The remaining items, or {@code null} if there is no match
	 */
	NonNullList<ItemStack> test(InventoryCrafting inventory);

}
