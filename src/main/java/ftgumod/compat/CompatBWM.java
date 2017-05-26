package ftgumod.compat;

import betterwithmods.common.registry.steelanvil.SteelCraftingManager;
import ftgumod.technology.TechnologyUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class CompatBWM implements ICompat {

	@Override
	public boolean run(Object... arg) {
		if (arg[0] instanceof ItemStack) {
			ItemStack stack = (ItemStack) arg[0];
			for (IRecipe r : SteelCraftingManager.getInstance().getRecipeList())
				if (r != null && !r.getRecipeOutput().isEmpty() && TechnologyUtil.isEqual(stack, r.getRecipeOutput()))
					return true;
		}
		return false;
	}

}
