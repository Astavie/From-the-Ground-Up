package ftgumod.tileentity;

import ftgumod.Content;
import ftgumod.inventory.ContainerResearchTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public class TileEntityResearchTable extends TileEntityInventory {

	public TileEntityResearchTable() {
		super(12, Content.n_researchTable);
	}

	@Override
	public Container createContainer(InventoryPlayer arg0, EntityPlayer arg1) {
		return new ContainerResearchTable(this, arg0);
	}

}
