package ftgumod.compat;

import betterwithmods.common.registry.steelanvil.SteelCraftingManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;

public class CompatBWM implements ICompat {

	@Override
	public boolean run(Object... arg) {
		if (arg[0] instanceof ItemStack) {
			ItemStack stack = (ItemStack) arg[0];
			for (IRecipe r : SteelCraftingManager.getInstance().getRecipeList())
				if (OreDictionary.itemMatches(r.getRecipeOutput(), stack, false) && (!r.getRecipeOutput().hasTagCompound() || ItemStack.areItemStackTagsEqual(r.getRecipeOutput(), stack)))
					return true;
		}
		return false;
	}

}
