package ftgumod.inventory;

import ftgumod.util.StackUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.Collections;

public class SlotSpecial extends Slot {

	private final Iterable<ItemStack> special;
	private final int limit;

	public SlotSpecial(IInventory inventory, int index, int x, int y, int limit, Iterable<ItemStack> special) {
		super(inventory, index, x, y);
		this.special = special;
		this.limit = limit;
	}

	public SlotSpecial(IInventory inventory, int index, int x, int y, int limit, ItemStack special) {
		this(inventory, index, x, y, limit, Collections.singleton(special));
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if (special == null)
			return true;

		for (ItemStack s : special)
			if (StackUtils.isStackOf(s, stack))
				return true;
		return false;
	}

	@Override
	public int getSlotStackLimit() {
		return limit;
	}

}
