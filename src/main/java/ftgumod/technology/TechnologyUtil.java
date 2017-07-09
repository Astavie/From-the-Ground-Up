package ftgumod.technology;

import ftgumod.FTGU;
import ftgumod.technology.TechnologyHandler.ITEM_GROUP;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public class TechnologyUtil {

	public static Object toItem(Object obj) {
		if (obj instanceof String && ((String) obj).contains(":")) {
			String[] itemSplit = ((String) obj).split(":");
			Item t = Item.REGISTRY.getObject(new ResourceLocation(itemSplit[0] + ":" + itemSplit[1]));
			Block b = Block.REGISTRY.getObject(new ResourceLocation(itemSplit[0] + ":" + itemSplit[1]));
			if (b != null) {
				if (itemSplit.length > 2) {
					int meta = Integer.parseInt(itemSplit[2]);
					return new ItemStack(b, 1, meta);
				} else {
					return b;
				}
			} else if (t != null) {
				if (itemSplit.length > 2) {
					int meta = Integer.parseInt(itemSplit[2]);
					return new ItemStack(t, 1, meta);
				} else {
					return t;
				}
			} else {
				return null;
			}
		}
		return obj;
	}

	public static String toString(Object obj) {
		if (obj == null)
			return "";

		if (obj instanceof ItemStack)
			return ((ItemStack) obj).getUnlocalizedName();
		else if (obj instanceof String)
			return "ore." + (String) obj;
		else if (obj instanceof ITEM_GROUP)
			return "group." + ((ITEM_GROUP) obj).getName();
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
		for (ResourceLocation l : CraftingManager.REGISTRY.getKeys()) {
			IRecipe r = CraftingManager.REGISTRY.getObject(l);
			if (OreDictionary.itemMatches(r.getRecipeOutput(), stack, false) && (!r.getRecipeOutput().hasTagCompound() || ItemStack.areItemStackTagsEqual(r.getRecipeOutput(), stack)))
				return true;
		}
		return FTGU.INSTANCE.runCompat("betterwithmods", stack);
	}

}
