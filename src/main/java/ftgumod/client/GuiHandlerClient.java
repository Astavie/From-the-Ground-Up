package ftgumod.client;

import ftgumod.GuiHandler;
import ftgumod.client.gui.GuiIdeaTable;
import ftgumod.client.gui.GuiResearchTable;
import ftgumod.technology.TechnologyManager;
import ftgumod.tileentity.TileEntityInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiHandlerClient extends GuiHandler {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

		if (tileEntity != null) {
			if (ID == TechnologyManager.GUI.IDEATABLE.ordinal()) {
				return new GuiIdeaTable(player.inventory, (TileEntityInventory) tileEntity);
			} else if (ID == TechnologyManager.GUI.RESEARCHTABLE.ordinal()) {
				return new GuiResearchTable(player.inventory, (TileEntityInventory) tileEntity);
			}
		}

		return null;
	}

}
