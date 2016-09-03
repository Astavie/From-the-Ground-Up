package ftgumod.gui.researchtable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import ftgumod.gui.TileEntityInventory;

public class SlotResearchTable extends Slot {

	private EntityPlayer player;
	private TileEntityInventory tileEntity;

	public SlotResearchTable(EntityPlayer player, IInventory inventory, int index, int x, int y, TileEntityInventory tileEntity) {
		super(inventory, index, x, y);
		this.player = player;
		this.tileEntity = tileEntity;
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
