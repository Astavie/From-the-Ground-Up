package ftgumod.technology.recipe;

import ftgumod.api.technology.recipe.IPuzzle;
import ftgumod.api.util.BlockSerializable;
import ftgumod.inventory.ContainerResearchTable;
import ftgumod.inventory.InventoryCraftingPersistent;
import ftgumod.inventory.SlotSpecial;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.HintMessage;
import ftgumod.util.StackUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeHooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PuzzleMatch implements IPuzzle {

	public final IInventory inventory = new InventoryBasic(null, false, 9);
	private final ResearchMatch research;
	public List<ITextComponent> hints;

	public PuzzleMatch(ResearchMatch research) {
		this.research = research;
	}

	@Override
	public ResearchMatch getRecipe() {
		return research;
	}

	@Override
	public boolean test() {
		for (int i = 0; i < 9; i++) {
			if (research.ingredients[i].test(inventory.getStackInSlot(i)))
				continue;
			return false;
		}
		return true;
	}

	@Override
	public void onStart(ContainerResearchTable container) {
		InventoryCrafting matrix = new InventoryCraftingPersistent(container, inventory, 0, 3, 3);
		for (int sloty = 0; sloty < 3; sloty++)
			for (int slotx = 0; slotx < 3; slotx++)
				container.addSlotToContainer(new SlotSpecial(matrix, slotx + sloty * 3, 30 + slotx * 18, 17 + sloty * 18, 1, (Iterable<ItemStack>) null));
	}

	@Override
	public void onInventoryChange(ContainerResearchTable container) {
		if (!container.invInput.getWorld().isRemote) {
			hints = new ArrayList<>();
			List<BlockSerializable> inspected = Collections.emptyList();
			if (container.inventorySlots.get(container.glass).getHasStack())
				inspected = StackUtils.INSTANCE.getInspected(container.inventorySlots.get(container.glass).getStack());
			for (int i = 0; i < 9; i++)
				if (research.hasHint(i))
					hints.add(research.getHint(i).getHint(inspected));
				else
					hints.add(null);
			PacketDispatcher.sendTo(new HintMessage(hints), (EntityPlayerMP) container.invPlayer.player);
		}
	}

	@Override
	public void onFinish(ContainerResearchTable container) {
		NonNullList<ItemStack> remaining = ForgeHooks.defaultRecipeGetRemainingItems(new InventoryCraftingPersistent(container, inventory, 0, 3, 3));
		for (int i = 0; i < 9; i++) {
			if (research.consume[i] != null)
				if (research.consume[i])
					inventory.setInventorySlotContents(i, ItemStack.EMPTY);
				else
					inventory.setInventorySlotContents(i, inventory.getStackInSlot(i).copy());
			else
				inventory.setInventorySlotContents(i, remaining.get(i));
		}
	}

	@Override
	public void onRemove(ContainerResearchTable container) {
		container.removeSlots(9);
	}

}
