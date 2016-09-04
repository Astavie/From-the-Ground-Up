package ftgumod.workbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ftgumod.Technology;
import ftgumod.TechnologyHandler;

public class ContainerWorkbenchTech extends ContainerWorkbench {

	private EntityPlayer player;
	private World world;

	public ContainerWorkbenchTech(InventoryPlayer player, World world, BlockPos pos) {
		super(player, world, pos);
		this.player = player.player;
		this.world = world;
	}

	@Override
	public void onCraftMatrixChanged(IInventory inventory) {
		ItemStack result = CraftingManager.getInstance().findMatchingRecipe(craftMatrix, world);
		Technology tech = TechnologyHandler.getLocked(result);
		
		if (tech != null) {
			if (tech.isResearched(player))
				craftResult.setInventorySlotContents(0, result);
			else
				craftResult.setInventorySlotContents(0, null);
		} else {
			craftResult.setInventorySlotContents(0, result);
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

}
