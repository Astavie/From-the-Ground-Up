package ftgumod;

import ftgumod.event.PlayerLockEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;

public class CraftingListener implements IContainerListener {

	private final EntityPlayer player;

	CraftingListener(EntityPlayer player) {
		this.player = player;
	}

	@Override
	public void sendSlotContents(Container container, int index, ItemStack stack) {
		if (stack.isEmpty())
			return;

		Slot slot = container.getSlot(index);
		if (slot.inventory instanceof InventoryCraftResult) {
			IRecipe recipe = ((InventoryCraftResult) slot.inventory).getRecipeUsed();

			PlayerLockEvent event = new PlayerLockEvent(player, stack, recipe);
			MinecraftForge.EVENT_BUS.post(event);

			if (!event.isCanceled()) {
				slot.inventory.setInventorySlotContents(0, ItemStack.EMPTY);
				if (player instanceof EntityPlayerMP)
					Content.c_itemLocked.trigger((EntityPlayerMP) player, recipe, stack);
			}
		}
	}

	@Override
	public void sendAllWindowProperties(Container arg0, IInventory arg1) {
	}

	@Override
	public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
	}

	@Override
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
	}

}
