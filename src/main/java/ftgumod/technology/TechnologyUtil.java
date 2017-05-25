package ftgumod.technology;

import java.util.ArrayList;
import java.util.List;

import ftgumod.FTGU;
import ftgumod.technology.TechnologyHandler.ITEM_GROUP;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class TechnologyUtil {

	public static boolean isEqual(Object obj, ItemStack stack1) {
		if ((obj == null || obj == ItemStack.EMPTY) && stack1 == ItemStack.EMPTY) {
			return true;
		} else if (stack1 == ItemStack.EMPTY || (obj == null || obj == ItemStack.EMPTY)) {
			return false;
		}

		ItemStack stack = stack1.copy();

		stack.setCount(1);
		if (obj instanceof ItemStack) {
			return (((ItemStack) obj).getMetadata() == OreDictionary.WILDCARD_VALUE
					&& ((ItemStack) obj).getItem() == stack.getItem()
					|| ItemStack.areItemStacksEqual((ItemStack) obj, stack));
		} else if (obj instanceof String) {
			List<ItemStack> item = OreDictionary.getOres((String) obj);
			for (ItemStack s : item)
				if ((s.getMetadata() == OreDictionary.WILDCARD_VALUE && s.getItem() == stack.getItem())
						|| ItemStack.areItemStacksEqual(stack, s))
					return true;
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

	public static NBTTagCompound getItemData(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}
		return nbt;
	}

	public static String toString(Object obj) {
		if (obj == null || obj == ItemStack.EMPTY)
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

	public static List<ItemStack> toItems(Object obj) {
		List<ItemStack> item = new ArrayList<ItemStack>();
		if (obj == null || obj == ItemStack.EMPTY)
			return item;

		if (obj instanceof ItemStack)
			item.add((ItemStack) obj);
		else if (obj instanceof String) {
			List<ItemStack> ore = OreDictionary.getOres((String) obj);
			for (ItemStack s : ore)
				item.add(s);
		} else if (obj instanceof Item)
			item.add(new ItemStack((Item) obj, 1, OreDictionary.WILDCARD_VALUE));
		else if (obj instanceof Block)
			item.add(new ItemStack((Block) obj, 1, OreDictionary.WILDCARD_VALUE));
		else if (obj instanceof ITEM_GROUP)
			item.addAll(((ITEM_GROUP) obj).toItems());

		return item;
	}

	@SideOnly(Side.CLIENT)
	public static boolean hasRecipe(ItemStack stack) {
		for (IRecipe r : CraftingManager.getInstance().getRecipeList())
			if (r != null && r.getRecipeOutput() != ItemStack.EMPTY
					&& TechnologyUtil.isEqual(stack, r.getRecipeOutput()))
				return true;
		return FTGU.INSTANCE.runCompat("betterwithmods", stack);
	}

}
