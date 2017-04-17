package ftgumod.compat;

import betterwithmods.craft.steelanvil.CraftingManagerSteelAnvil;
import ftgumod.technology.TechnologyUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class CompatBWM implements ICompat {

	@Override
	public boolean run(Object... arg) {
		if (arg[0] instanceof ItemStack) {
			ItemStack stack = (ItemStack) arg[0];
			for (IRecipe r : CraftingManagerSteelAnvil.INSTANCE.getRecipes())
				if (r != null && r.getRecipeOutput() != null && TechnologyUtil.isEqual(stack, r.getRecipeOutput()))
					return true;
		}
		return false;
	}

}
