package ftgumod.api.technology.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IIdeaRecipe {

	boolean test(NonNullList<ItemStack> inventory);

}
