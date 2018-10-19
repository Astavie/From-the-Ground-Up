package ftgumod.api.inventory;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class SlotCrafting extends SlotSpecial {

	private final Container eventHandler;

	public SlotCrafting(Container eventHandler, IInventory inventory, int index, int x, int y, int limit, Predicate<ItemStack> special) {
		super(inventory, index, x, y, limit, special);
		this.eventHandler = eventHandler;
	}

	public SlotCrafting(Container eventHandler, IInventory inventory, int index, int x, int y, int limit, Iterable<ItemStack> special) {
		super(inventory, index, x, y, limit, special);
		this.eventHandler = eventHandler;
	}

	public SlotCrafting(Container eventHandler, IInventory inventory, int index, int x, int y, int limit, ItemStack special) {
		super(inventory, index, x, y, limit, special);
		this.eventHandler = eventHandler;
	}

	@Override
	public void onSlotChanged() {
		super.onSlotChanged();
		eventHandler.onCraftMatrixChanged(inventory);
	}

}
