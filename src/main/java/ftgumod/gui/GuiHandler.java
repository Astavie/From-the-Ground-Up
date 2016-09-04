package ftgumod.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import ftgumod.TechnologyHandler.GUI;
import ftgumod.gui.ideatable.ContainerIdeaTable;
import ftgumod.gui.ideatable.GuiIdeaTable;
import ftgumod.gui.researchtable.ContainerResearchTable;
import ftgumod.gui.researchtable.GuiResearchTable;
import ftgumod.workbench.ContainerWorkbenchTech;
import ftgumod.workbench.GuiWorkbenchTech;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

		if (tileEntity != null) {
			if (ID == GUI.IDEATABLE.ordinal()) {
				return new ContainerIdeaTable((TileEntityInventory) tileEntity, player.inventory);
			} else if (ID == GUI.RESEARCHTABLE.ordinal()) {
				return new ContainerResearchTable((TileEntityInventory) tileEntity, player.inventory);
			}
		} else if (ID == GUI.CRAFTINGTABLETECH.ordinal()) {
			return new ContainerWorkbenchTech(player.inventory, world, new BlockPos(x, y, z));
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

		if (tileEntity != null) {
			if (ID == GUI.IDEATABLE.ordinal()) {
				return new GuiIdeaTable(player.inventory, (TileEntityInventory) tileEntity);
			} else if (ID == GUI.RESEARCHTABLE.ordinal()) {
				return new GuiResearchTable(player.inventory, (TileEntityInventory) tileEntity);
			}
		} else if (ID == GUI.CRAFTINGTABLETECH.ordinal()) {
			return new GuiWorkbenchTech(player.inventory, world, new BlockPos(x, y, z));
		}

		return null;
	}

}
