package ftgumod.api.technology.recipe;

import ftgumod.api.util.BlockSerializable;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.util.List;

public interface IResearchRecipe {

	@Nullable
	Hint getHint(int index);

	boolean hasHint(int index);

	/**
	 * @param block     The new block that has been inspected
	 * @param inspected The already inspected block listed on the magnifying glass
	 * @return If the newly inspected block will help with researching this
	 */
	boolean inspect(BlockSerializable block, List<BlockSerializable> inspected);

	/**
	 * @param inventory The inventory to compare to
	 * @return The remaining items, or {@code null} if there is no match
	 */
	NonNullList<ItemStack> test(InventoryCrafting inventory);

}
