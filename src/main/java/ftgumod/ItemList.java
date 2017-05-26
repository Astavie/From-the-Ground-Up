package ftgumod;

import java.util.Iterator;
import java.util.List;

import ftgumod.technology.TechnologyHandler.ITEM_GROUP;
import ftgumod.technology.TechnologyUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class ItemList implements Iterable<ItemStack> {

	protected final NonNullList<ItemStack> list = NonNullList.create();
	private final String name;

	public ItemList(Object obj) {
		name = TechnologyUtil.toString(obj);

		if (obj instanceof ItemStack)
			list.add((ItemStack) obj);
		else if (obj instanceof String) {
			List<ItemStack> ore = OreDictionary.getOres((String) obj);
			for (ItemStack s : ore)
				list.add(s);
		} else if (obj instanceof Item)
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
		for (ItemStack s : list)
			if (OreDictionary.itemMatches(s, item, false) && (!s.hasTagCompound() || ItemStack.areItemStackTagsEqual(s, item)))
				return true;
		return false;
	}

	@Override
	public Iterator<ItemStack> iterator() {
		return list.iterator();
	}

}
