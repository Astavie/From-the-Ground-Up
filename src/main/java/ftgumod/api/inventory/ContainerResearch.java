package ftgumod.api.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public abstract class ContainerResearch extends Container {

	@Override
	public Slot addSlotToContainer(Slot slotIn) {
		return super.addSlotToContainer(slotIn);
	}

	public void removeSlots(int size) {
		for (int i = 0; i < size; i++) {
			inventorySlots.remove(inventorySlots.size() - 1);
			inventoryItemStacks.remove(inventoryItemStacks.size() - 1);
		}
	}

	public abstract boolean isClient();

	public abstract EntityPlayer getPlayer();

	public abstract void refreshHints(List<ITextComponent> hints);

}
