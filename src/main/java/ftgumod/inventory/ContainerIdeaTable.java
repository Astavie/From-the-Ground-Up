package ftgumod.inventory;

import ftgumod.Content;
import ftgumod.api.util.IStackUtils;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import ftgumod.tileentity.TileEntityInventory;
import ftgumod.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

public class ContainerIdeaTable extends Container {

	private final TileEntityInventory invInput;

	private final InventoryCrafting craftMatrix;
	private final InventoryPlayer invPlayer;

	private final int sizeInventory;

	private int feather;
	private int parchment;
	private int combine;
	private int output;

	private NonNullList<ItemStack> remaining;

	public ContainerIdeaTable(TileEntityInventory tileEntity, InventoryPlayer invPlayer) {
		this.invInput = tileEntity;
		this.invPlayer = invPlayer;

		sizeInventory = addSlots(tileEntity);

		for (int slotx = 0; slotx < 3; slotx++) {
			for (int sloty = 0; sloty < 9; sloty++) {
				addSlotToContainer(new Slot(invPlayer, sloty + slotx * 9 + 9, 8 + sloty * 18, 84 + slotx * 18));
			}
		}

		for (int slot = 0; slot < 9; slot++) {
			addSlotToContainer(new Slot(invPlayer, slot, 8 + slot * 18, 142));
		}

		craftMatrix = new InventoryCraftingPersistent(this, tileEntity, combine, 3, 1);
		onCraftMatrixChanged(tileEntity);
	}

	private int addSlots(TileEntityInventory tileEntity) {
		int c = 0;

		addSlotToContainer(new SlotSpecial(tileEntity, c, 37, 23, 1, OreDictionary.getOres("feather")));
		feather = c;
		c++;

		addSlotToContainer(new SlotSpecial(tileEntity, c, 59, 23, 64, new ItemStack(Content.i_parchmentEmpty)));
		parchment = c;
		c++;

		combine = c;
		for (int slot = 0; slot < 3; slot++) {
			addSlotToContainer(new SlotSpecial(tileEntity, c, 30 + slot * 18, 45, 1, (Iterable<ItemStack>) null));
			c++;
		}

		addSlotToContainer(new Slot(new InventoryCraftResult(), c, 124, 35));
		output = c;
		c++;

		return c;
	}

	private Technology hasRecipe() {
		for (Technology tech : TechnologyManager.INSTANCE.technologies.values()) {
			if (tech.hasIdeaRecipe() && tech.canResearch(invPlayer.player)) {
				remaining = tech.getIdeaRecipe().test(craftMatrix);
				if (remaining != null)
					return tech;
			}
		}
		return null;
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		if (inv == invInput) {
			if (inventorySlots.get(feather).getHasStack() && inventorySlots.get(parchment).getHasStack()) {
				Technology tech = hasRecipe();

				if (tech != null) {
					inventorySlots.get(output).putStack(StackUtils.INSTANCE.getParchment(tech.getRegistryName(), tech.hasResearchRecipe() ? IStackUtils.Parchment.IDEA : IStackUtils.Parchment.RESEARCH));
					return;
				}
			}
			inventorySlots.get(output).putStack(ItemStack.EMPTY);
		}
	}

	@Override
	public ItemStack slotClick(int index, int mouse, ClickType mode, EntityPlayer player) {
		if (index == output && inventorySlots.get(output).getHasStack()) {
			inventorySlots.get(parchment).decrStackSize(1);
			for (int i = 0; i < craftMatrix.getSizeInventory(); i++)
				craftMatrix.setInventorySlotContents(i, remaining.get(i));
		}

		ItemStack clickItemStack = super.slotClick(index, mouse, mode, player);
		onCraftMatrixChanged(invInput);
		return clickItemStack;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex) {
		ItemStack itemStack1 = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(slotIndex);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemStack2 = slot.getStack();
			itemStack1 = itemStack2.copy();

			if (slotIndex == output) {
				if (!mergeItemStack(itemStack2, sizeInventory, sizeInventory + 36, true))
					return ItemStack.EMPTY;
			} else if (slotIndex > output) {
				if (itemStack2.getItem() == Content.i_parchmentEmpty) {
					if (!mergeItemStack(itemStack2, parchment, parchment + 1, false))
						return ItemStack.EMPTY;
				} else if (ArrayUtils.contains(OreDictionary.getOreIDs(itemStack2), OreDictionary.getOreID("feather")))
					if (!mergeItemStack(itemStack2, feather, feather + 1, false))
						return ItemStack.EMPTY;
				return ItemStack.EMPTY;
			} else if (!mergeItemStack(itemStack2, sizeInventory, sizeInventory + 36, false))
				return ItemStack.EMPTY;

			if (itemStack2.getCount() != 0)
				slot.onSlotChanged();

			if (itemStack2.getCount() == itemStack1.getCount())
				return ItemStack.EMPTY;

			slot.onTake(playerIn, itemStack2);
		}

		return itemStack1;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

}
