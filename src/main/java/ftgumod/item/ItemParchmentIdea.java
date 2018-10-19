package ftgumod.item;

import ftgumod.Content;
import net.minecraft.item.Item;

public class ItemParchmentIdea extends Item {

	public ItemParchmentIdea(String name) {
		setTranslationKey(name);
		setMaxStackSize(1);
		setContainerItem(Content.i_parchmentEmpty);
	}

}
