package ftgumod.inventory;

import ftgumod.tileentity.TileEntityInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotResearchTable extends Slot {

	@SuppressWarnings({"unused", "FieldCanBeLocal"})
	private EntityPlayer player;

	public SlotResearchTable(EntityPlayer player, IInventory inventory, int index, int x, int y, TileEntityInventory tileEntity) {
		super(inventory, index, x, y);
		this.player = player;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack onTake(EntityPlayer player, ItemStack stack) {
		onCrafting(stack);
		return super.onTake(player, stack);
	}

	@Override
	protected void onCrafting(ItemStack stack) {
		// TODO: Advancement
	}
}
