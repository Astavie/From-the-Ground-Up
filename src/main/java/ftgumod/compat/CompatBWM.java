package ftgumod.compat;

import java.lang.reflect.Field;
import betterwithmods.client.container.ContainerSteelAnvil;
import betterwithmods.client.container.inventory.SlotSteelAnvilCrafting;
import betterwithmods.craft.steelanvil.CraftingManagerSteelAnvil;
import ftgumod.technology.TechnologyUtil;
import ftgumod.workbench.FTGUCraftResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

public class CompatBWM implements ICompat {

	private Field result, matrix;

	public CompatBWM() {
		try {
			result = ContainerSteelAnvil.class.getDeclaredField("result");
			result.setAccessible(true);

			matrix = ContainerSteelAnvil.class.getDeclaredField("matrix");
			matrix.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean run(Object... arg) {
		if (arg[0] instanceof ItemStack) {
			ItemStack stack = (ItemStack) arg[0];
			for (IRecipe r : CraftingManagerSteelAnvil.INSTANCE.getRecipes())
				if (r != null && r.getRecipeOutput() != null && TechnologyUtil.isEqual(stack, r.getRecipeOutput()))
					return true;
		} else if (arg[0] instanceof ContainerSteelAnvil && arg[1] instanceof EntityPlayer) {
			ContainerSteelAnvil inv = (ContainerSteelAnvil) arg[0];
			EntityPlayer player = (EntityPlayer) arg[1];

			try {
				result.set(inv, new FTGUCraftResult(player));

				Slot slot = inv.inventorySlots.get(0);
				inv.inventorySlots.set(0, new SlotSteelAnvilCrafting(player, (InventoryCrafting) matrix.get(inv), (IInventory) result.get(inv), 0, slot.xPos, slot.yPos));

				return true;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
