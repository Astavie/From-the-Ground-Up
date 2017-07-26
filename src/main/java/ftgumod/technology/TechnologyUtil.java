package ftgumod.technology;

import ftgumod.FTGU;
import ftgumod.technology.TechnologyHandler.ITEM_GROUP;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

public class TechnologyUtil {

	public static Object toItem(Object obj) {
		if (obj instanceof String && ((String) obj).contains(":")) {
			String[] itemSplit = ((String) obj).split(":");
			Item t = Item.REGISTRY.getObject(new ResourceLocation(itemSplit[0] + ":" + itemSplit[1]));
			Block b = Block.REGISTRY.getObject(new ResourceLocation(itemSplit[0] + ":" + itemSplit[1]));
			if (b != Blocks.AIR) {
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
			return "ore." + obj;
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

	public static TextComponentBase getDisplayName(ItemStack stack) {
		if (stack.hasDisplayName())
			return new TextComponentString(stack.getDisplayName());
		else return new TextComponentTranslation(stack.getUnlocalizedName() + ".name");
	}

	public static boolean hasRecipe(ItemStack stack) {
		for (IRecipe r : ForgeRegistries.RECIPES) {
			if (r != null && OreDictionary.itemMatches(r.getRecipeOutput(), stack, false) && (!r.getRecipeOutput().hasTagCompound() || ItemStack.areItemStackTagsEqual(r.getRecipeOutput(), stack)))
				return true;
		}
		return FTGU.INSTANCE.runCompat("betterwithmods", stack); // TODO: BetterWithMods support
	}

}
