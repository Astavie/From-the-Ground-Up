package ftgumod;

import ftgumod.technology.TechnologyHandler.ITEM_GROUP;
import ftgumod.technology.TechnologyUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Iterator;
import java.util.List;

public class ItemList implements Iterable<ItemStack> {

	protected final NonNullList<ItemStack> list = NonNullList.create();
	protected String name;

	public ItemList() {
		name = "null";
	}

	public ItemList(Object obj) {
		name = TechnologyUtil.toString(obj);

		if (obj instanceof ItemStack)
			list.add((ItemStack) obj);
		else if (obj instanceof String)
			list.addAll(OreDictionary.getOres((String) obj));
		else if (obj instanceof Item)
			list.add(new ItemStack((Item) obj, 1, OreDictionary.WILDCARD_VALUE));
		else if (obj instanceof Block)
			list.add(new ItemStack((Block) obj, 1, OreDictionary.WILDCARD_VALUE));
		else if (obj instanceof ITEM_GROUP)
			for (ItemList l : ((ITEM_GROUP) obj).item)
				list.addAll(l.list);
		else if (obj instanceof ItemList)
			list.addAll(((ItemList) obj).list);
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean contains(ItemStack item) {
		if (isEmpty() && item.isEmpty())
			return true;
		for (ItemStack s : list)
			if (OreDictionary.itemMatches(s, item, false) && (!s.hasTagCompound() || ItemStack.areItemStackTagsEqual(s, item)))
				return true;
		return false;
	}

	@Override
	public Iterator<ItemStack> iterator() {
		return list.iterator();
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * @return a list of item stacks
	 * @deprecated Use {@link ItemList#iterator()} or for-each loops instead.
	 */
	@Deprecated
	public List<ItemStack> getRaw() {
		return list;
	}

}
