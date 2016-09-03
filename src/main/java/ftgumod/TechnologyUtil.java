package ftgumod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import ftgumod.TechnologyHandler.ITEM_GROUP;

public class TechnologyUtil {

	public static boolean isEqual(Object obj, ItemStack stack) {
		if (obj == null && stack == null) {
			return true;
		} else if (stack == null || obj == null) {
			return false;
		}

		if (obj instanceof ItemStack) {
			return ItemStack.areItemStacksEqual((ItemStack) obj, stack);
		} else if (obj instanceof String) {
			List<ItemStack> item = OreDictionary.getOres((String) obj);
			for (ItemStack s : item) {
				if (ItemStack.areItemStacksEqual(stack, s)) {
					return true;
				}
			}
		} else if (obj instanceof ITEM_GROUP) {
			return ((ITEM_GROUP) obj).contains(stack);
		} else if (obj instanceof Item) {
			return stack.getItem() == (Item) obj;
		} else if (obj instanceof Block) {
			return stack.getItem() == Item.getItemFromBlock((Block) obj);
		}
		return false;
	}

	public static Object toItem(Object obj) {
		if (obj instanceof String && ((String) obj).contains(":")) {
			String[] itemSplit = ((String) obj).split(":");
			Item t = GameRegistry.findItem(itemSplit[0], itemSplit[1]);
			Block b = GameRegistry.findBlock(itemSplit[0], itemSplit[1]);
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

	public static NBTTagCompound getItemData(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}
		return nbt;
	}

	public static String toString(Object obj) {
		if (obj == null)
			return "";

		if (obj instanceof ItemStack)
			return ((ItemStack) obj).getUnlocalizedName();
		else if (obj instanceof String)
			return (String) obj;
		else if (obj instanceof ITEM_GROUP)
			return ((ITEM_GROUP) obj).getName();
		else if (obj instanceof Item)
			return ((Item) obj).getUnlocalizedName();
		else if (obj instanceof Block)
			return ((Block) obj).getUnlocalizedName();

		return "";
	}

	private static List<ItemStack> getItems(Object o) {
		List<ItemStack> item = new ArrayList<ItemStack>();
		if (o instanceof Item) {
			Item i = (Item) o;
			i.getSubItems(i, null, item);
		} else if (o instanceof Block) {
			Block b = (Block) o;
			b.getSubBlocks(Item.getItemFromBlock(b), null, item);
		} else if (o instanceof ItemStack) {
			item.add((ItemStack) o);
		}
		return item;
	}

	public static List<ItemStack> toItems(Object obj) {
		List<ItemStack> item = new ArrayList<ItemStack>();
		if (obj == null)
			return item;

		if (obj instanceof ItemStack)
			item.add((ItemStack) obj);
		else if (obj instanceof String)
			item.addAll(OreDictionary.getOres((String) obj));
		else if (obj instanceof Item)
			item.addAll(getItems(obj));
		else if (obj instanceof Block)
			item.addAll(getItems(obj));
		else if (obj instanceof ITEM_GROUP)
			item.addAll(((ITEM_GROUP) obj).toItems());

		return item;
	}

}
