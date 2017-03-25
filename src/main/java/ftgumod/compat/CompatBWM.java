package ftgumod.compat;

import java.lang.reflect.Field;
import betterwithmods.client.container.anvil.ContainerSteelAnvil;
import betterwithmods.client.container.anvil.SlotSteelAnvilCrafting;
import betterwithmods.common.registry.steelanvil.SteelCraftingManager;
import ftgumod.technology.TechnologyUtil;
import ftgumod.workbench.FTGUCraftResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class CompatBWM implements ICompat {

	@Override
	public boolean run(Object... arg) {
		if (arg[0] instanceof ItemStack) {
			ItemStack stack = (ItemStack) arg[0];
			for (IRecipe r : SteelCraftingManager.getInstance().getRecipeList())
				if (r != null && r.getRecipeOutput() != ItemStack.EMPTY && TechnologyUtil.isEqual(stack, r.getRecipeOutput()))
					return true;
		} else if (arg[0] instanceof ContainerSteelAnvil && arg[1] instanceof EntityPlayer) {
			ContainerSteelAnvil inv = (ContainerSteelAnvil) arg[0];
			EntityPlayer player = (EntityPlayer) arg[1];

			inv.craftResult = new FTGUCraftResult(player);
			
			Slot slot = inv.inventorySlots.get(0);
			inv.inventorySlots.set(0, new SlotSteelAnvilCrafting(player, inv.craftMatrix, inv.craftResult, 0, slot.xPos, slot.yPos));

			return true;
		}
		return false;
	}

}
