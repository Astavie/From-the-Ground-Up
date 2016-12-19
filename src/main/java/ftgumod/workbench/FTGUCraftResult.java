package ftgumod.workbench;

import javax.annotation.Nullable;
import ftgumod.Technology;
import ftgumod.TechnologyHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.item.ItemStack;

public class FTGUCraftResult extends InventoryCraftResult {

	private final EntityPlayer player;

	public FTGUCraftResult(EntityPlayer player) {
		this.player = player;
	}

	public void setInventorySlotContents(int slot, @Nullable ItemStack stack) {
		Technology tech = TechnologyHandler.getLocked(stack);
		if (tech != null)
			if (tech.isResearched(player))
				super.setInventorySlotContents(slot, stack);
			else
				super.setInventorySlotContents(slot, null);
		else
			super.setInventorySlotContents(slot, stack);
	}

}
