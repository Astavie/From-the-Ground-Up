package ftgumod.gui.ideatable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import ftgumod.FTGUAPI;
import ftgumod.gui.TileEntityInventory;

public class TileEntityIdeaTable extends TileEntityInventory {

	public TileEntityIdeaTable() {
		super(5, FTGUAPI.n_ideaTable);
	}

	@Override
	public Container createContainer(InventoryPlayer arg0, EntityPlayer arg1) {
		return new ContainerIdeaTable(this, arg0);
	}

}
