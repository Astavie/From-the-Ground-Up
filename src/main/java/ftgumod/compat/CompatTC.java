package ftgumod.compat;

import ftgumod.workbench.FTGUCraftResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import slimeknights.tconstruct.tools.common.inventory.ContainerCraftingStation;

public class CompatTC implements ICompat {

	@Override
	public boolean run(Object... arg) {
		if (arg[0] instanceof ContainerCraftingStation && arg[1] instanceof EntityPlayer) {
			ContainerCraftingStation inv = (ContainerCraftingStation) arg[0];
			EntityPlayer player = (EntityPlayer) arg[1];

			Slot slot = inv.inventorySlots.get(0);
			inv.craftResult = new FTGUCraftResult(player);
			inv.inventorySlots.set(0, new SlotCrafting(player, inv.craftMatrix, inv.craftResult, 0, slot.xPos, slot.yPos));
			
			return true;
		}
		return false;
	}

}
