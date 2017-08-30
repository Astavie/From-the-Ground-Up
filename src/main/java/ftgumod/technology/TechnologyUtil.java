package ftgumod.technology;

import ftgumod.FTGU;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

public class TechnologyUtil {

	public static String toString(Object obj) {
		if (obj == null)
			return "";

		if (obj instanceof ItemStack)
			return ((ItemStack) obj).getUnlocalizedName();
		else if (obj instanceof String)
			return "ore." + obj;
		else if (obj instanceof Item)
			return ((Item) obj).getUnlocalizedName();
		else if (obj instanceof Block)
			return ((Block) obj).getUnlocalizedName();

		return obj.toString();
	}

	public static NBTTagCompound getItemData(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}
		return nbt;
	}

	public static boolean hasRecipe(ItemStack stack) {
		for (IRecipe r : ForgeRegistries.RECIPES) {
			if (r != null && OreDictionary.itemMatches(r.getRecipeOutput(), stack, false) && (!r.getRecipeOutput().hasTagCompound() || ItemStack.areItemStackTagsEqual(r.getRecipeOutput(), stack)))
				return true;
		}
		return FTGU.INSTANCE.runCompat("betterwithmods", stack); // TODO: BetterWithMods support
	}

}
