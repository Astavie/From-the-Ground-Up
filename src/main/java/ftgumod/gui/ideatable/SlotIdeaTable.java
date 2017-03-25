package ftgumod.gui.ideatable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import ftgumod.gui.TileEntityInventory;

public class SlotIdeaTable extends Slot {

	private EntityPlayer player;
	private TileEntityInventory tileEntity;

	public SlotIdeaTable(EntityPlayer player, IInventory inventory, int index, int x, int y, TileEntityInventory tileEntity) {
		super(inventory, index, x, y);
		this.player = player;
		this.tileEntity = tileEntity;
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
		// TODO: Achievement
	}
}
