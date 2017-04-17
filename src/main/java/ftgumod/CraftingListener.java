package ftgumod;

import java.util.List;
import ftgumod.event.PlayerLockEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;

public class CraftingListener implements IContainerListener {

	private EntityPlayer player;

	public CraftingListener(EntityPlayer player) {
		this.player = player;
	}

	@Override
	public void sendSlotContents(Container container, int index, ItemStack stack) {
		Slot slot = container.getSlot(index);
		if (slot.inventory instanceof InventoryCraftResult) {
			PlayerLockEvent event = new PlayerLockEvent(player, stack);
			if (stack != ItemStack.EMPTY)
				MinecraftForge.EVENT_BUS.post(event);

			slot.inventory.setInventorySlotContents(0, event.willLock() ? ItemStack.EMPTY : stack);
		}
	}

	@Override
	public void sendAllWindowProperties(Container arg0, IInventory arg1) {
	}

	@Override
	public void sendProgressBarUpdate(Container arg0, int arg1, int arg2) {
	}

	@Override
	public void updateCraftingInventory(Container arg0, NonNullList<ItemStack> arg1) {
	}

}
