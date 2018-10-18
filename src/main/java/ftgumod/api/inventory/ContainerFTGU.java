package ftgumod.api.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public abstract class ContainerFTGU extends Container {

	@Override
	public Slot addSlot(Slot slotIn) {
		return super.addSlot(slotIn);
	}

	public void removeSlots(int size) {
		for (int i = 0; i < size; i++) {
			inventorySlots.remove(inventorySlots.size() - 1);
			inventoryItemStacks.remove(inventoryItemStacks.size() - 1);
		}
	}

	public abstract boolean isRemote();

	public abstract InventoryPlayer getInventoryPlayer();

	public abstract void refreshHints(List<ITextComponent> hints);

}
