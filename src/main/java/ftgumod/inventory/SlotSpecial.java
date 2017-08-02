package ftgumod.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotSpecial extends Slot {

	private final ItemStack[] special;
	private final int limit;

	public SlotSpecial(IInventory inventory, int index, int x, int y, int limit, ItemStack... special) {
		super(inventory, index, x, y);
		this.special = special;
		this.limit = limit;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if (special.length == 0) {
			return true;
		}

		for (ItemStack s : special)
			if (stack.getItem() == s.getItem() && stack.getMetadata() == s.getMetadata())
				return true;
		return false;
	}

	@Override
	public int getSlotStackLimit() {
		return limit;
	}

}
