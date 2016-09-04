package ftgumod.gui;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotSpecial extends Slot {

	private ItemStack[] special;
	private int limit;
	public Container container;

	public SlotSpecial(Container container, IInventory inventory, int index, int x, int y, int limit, ItemStack... special) {
		super(inventory, index, x, y);
		this.special = special;
		this.limit = limit;
		this.container = container;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if (special.length == 0) {
			return true;
		}

		for (ItemStack s : special) {
			if (stack.getItem() == s.getItem() && stack.getMetadata() == s.getMetadata()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getSlotStackLimit() {
		return limit;
	}
}
