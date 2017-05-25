package ftgumod.gui.ideatable;

import ftgumod.gui.TileEntityInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotIdeaTable extends Slot {

	@SuppressWarnings("unused")
	private EntityPlayer player;

	public SlotIdeaTable(EntityPlayer player, IInventory inventory, int index, int x, int y, TileEntityInventory tileEntity) {
		super(inventory, index, x, y);
		this.player = player;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	@Override
	public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
		super.onPickupFromSlot(player, stack);
		onCrafting(stack);
	}

	@Override
	protected void onCrafting(ItemStack stack) {
		// TODO: Achievement
	}
}
