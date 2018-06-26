package ftgumod.inventory;

import ftgumod.Content;
import ftgumod.api.util.IStackUtils;
import ftgumod.technology.Technology;
import ftgumod.tileentity.TileEntityInventory;
import ftgumod.tileentity.TileEntityResearchTable;
import ftgumod.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class ContainerResearchTable extends Container {

	public final TileEntityResearchTable invInput;

	public final InventoryPlayer invPlayer;

	private final int sizeInventory;
	private final IInventory result;

	public int puzzle;
	public int output;
	public int glass;
	public Technology recipe;
	private int feather;
	private int parchment;

	public ContainerResearchTable(TileEntityResearchTable tileEntity, InventoryPlayer invPlayer) {
		this.invInput = tileEntity;
		this.invPlayer = invPlayer;

		result = new InventoryCraftResult();
		sizeInventory = addSlots(tileEntity);

		for (int sloty = 0; sloty < 3; sloty++) {
			for (int slotx = 0; slotx < 9; slotx++) {
				addSlotToContainer(new Slot(invPlayer, slotx + sloty * 9 + 9, 8 + slotx * 18, 84 + sloty * 18));
			}
		}

		for (int slot = 0; slot < 9; slot++) {
			addSlotToContainer(new Slot(invPlayer, slot, 8 + slot * 18, 142));
		}

		puzzle = sizeInventory + 36;
		if (invInput.puzzle != null) {
			invInput.puzzle.onStart(this);
			invInput.puzzle.onInventoryChange(this);
		}

		onCraftMatrixChanged(null);
	}

	@Override
	public void setAll(List<ItemStack> stacks) {
		for (int i = 0; i < inventorySlots.size(); ++i)
			getSlot(i).putStack(stacks.get(i));
		onCraftMatrixChanged(null);
	}

	private int addSlots(TileEntityInventory tileEntity) {
		int c = 0;

		addSlotToContainer(new SlotSpecial(tileEntity, c, 8, 46, 1, OreDictionary.getOres("feather")));
		feather = c;
		c++;

		addSlotToContainer(new SlotSpecial(tileEntity, c, 8, 24, 1, new ItemStack(Content.i_parchmentIdea)));
		parchment = c;
		c++;

		addSlotToContainer(new SlotSpecial(tileEntity, c, 150, 35, 1, new ItemStack(Content.i_magnifyingGlass)));
		glass = c;
		c++;

		addSlotToContainer(new Slot(result, c, 124, 35));
		output = c;
		c++;

		return c;
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		if (inv != invPlayer) {
			if (inventorySlots.get(parchment).getHasStack()) {
				Technology tech = StackUtils.INSTANCE.getTechnology(inventorySlots.get(parchment).getStack());
				if (tech != null && tech.hasResearchRecipe() && tech.canResearch(invPlayer.player))
					recipe = tech;
			} else
				recipe = null;

			if (recipe != null) {
				if (invInput.puzzle == null || invInput.puzzle.getRecipe() != recipe.getResearchRecipe()) {
					if (invInput.puzzle != null)
						invInput.puzzle.onRemove(this);
					invInput.puzzle = recipe.getResearchRecipe().createInstance();
					invInput.puzzle.onStart(this);
					invInput.puzzle.onInventoryChange(this);
				}
				if (inv != result && inv != invInput)
					invInput.puzzle.onInventoryChange(this);
				if (inventorySlots.get(feather).getHasStack() && invInput.puzzle.test()) {
					inventorySlots.get(output).putStack(StackUtils.INSTANCE.getParchment(recipe, IStackUtils.Parchment.RESEARCH));
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
			invInput.puzzle.onFinish(this);
		}

		ItemStack clickItemStack = super.slotClick(index, mouse, mode, player);
		if (index >= 0)
			onCraftMatrixChanged(inventorySlots.get(index).inventory);
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
			} else if (slotIndex > output && slotIndex < puzzle) {
				if (itemStack2.getItem() == Content.i_parchmentIdea) {
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

	@Override
	public Slot addSlotToContainer(Slot slot) {
		return super.addSlotToContainer(slot);
	}

	public void removeSlots(int size) {
		for (int i = 0; i < size; i++) {
			inventorySlots.remove(inventorySlots.size() - 1);
			inventoryItemStacks.remove(inventoryItemStacks.size() - 1);
		}
	}

}
