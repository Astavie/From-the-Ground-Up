package ftgumod.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemResearchBook extends Item {

	public ItemResearchBook(String name) {
		setUnlocalizedName(name);
		setCreativeTab(CreativeTabs.MISC);
		setMaxStackSize(1);
	}

}
