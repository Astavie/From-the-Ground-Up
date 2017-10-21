package ftgumod.api.recipe;

import ftgumod.api.util.BlockPredicate;
import ftgumod.api.util.BlockSerializable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface IResearchRecipe {

	@Nullable
	ITextComponent getHint(int index);

	default boolean hasHint(int index) {
		return getHint(index) != null && !getHint(index).getUnformattedText().isEmpty();
	}

	Set<BlockPredicate> getDecipher(int index);

	default boolean testDecipher(int index, List<BlockSerializable> inspected) {
		if (getDecipher(index).size() == 0)
			return true;

		for (BlockPredicate predicate : getDecipher(index))
			for (BlockSerializable block : inspected)
				if (block.test(predicate))
					return true;

		return false;
	}

	boolean test(NonNullList<ItemStack> inventory);

}
