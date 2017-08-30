package ftgumod.inventory;

import ftgumod.FTGUAPI;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.tileentity.TileEntityInventory;
import ftgumod.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class ContainerIdeaTable extends Container {

	private final TileEntityInventory invInput;
	private final IInventory invResult = new InventoryCraftResult();
	private final InventoryPlayer invPlayer;

	private final int sizeInventory;

	private int feather;
	private int parchment;
	private int combine;
	private int output;

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

		onCraftMatrixChanged(tileEntity);
	}

	private int addSlots(TileEntityInventory tileEntity) {
		int c = 0;

		addSlotToContainer(new SlotSpecial(tileEntity, c, 37, 23, 1, new ItemStack(Items.FEATHER)));
		feather = c;
		c++;

		addSlotToContainer(new SlotSpecial(tileEntity, c, 59, 23, 64, new ItemStack(FTGUAPI.i_parchmentEmpty)));
		parchment = c;
		c++;

		combine = c;
		for (int slot = 0; slot < 3; slot++) {
			addSlotToContainer(new SlotSpecial(tileEntity, c, 30 + slot * 18, 45, 1));
			c++;
		}

		addSlotToContainer(new Slot(invResult, c, 124, 35));
		output = c;
		c++;

		return c;
	}

	private Technology hasRecipe() {
		Collection<ItemStack> inventory = new HashSet<>();
		for (int i = 0; i < 3; i++) {
			ItemStack stack = inventoryItemStacks.get(combine + i);
			if (!stack.isEmpty())
				inventory.add(stack);
		}

		for (Technology tech : TechnologyHandler.technologies)
			if (tech.hasIdeaRecipe() && tech.canResearch(invPlayer.player))
				if (tech.getIdeaRecipe().test(inventory))
					return tech;
		return null;
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		if (inv == invInput) {
			if (inventorySlots.get(feather).getHasStack() && inventorySlots.get(parchment).getHasStack()) {
				Technology tech = hasRecipe();

				if (tech != null) {
					ItemStack result = new ItemStack(FTGUAPI.i_parchmentIdea);

					StackUtils.getItemData(result).setString("FTGU", tech.getRegistryName().toString());

					inventorySlots.get(output).putStack(result);
					return;
				}
			}
			inventorySlots.get(output).putStack(ItemStack.EMPTY);
		}
	}

	@Override
	public ItemStack slotClick(int index, int mouse, ClickType mode, EntityPlayer player) {
		ItemStack clickItemStack = super.slotClick(index, mouse, mode, player);

		onCraftMatrixChanged(invInput);
		if (index == output && inventorySlots.get(output).getHasStack()) {
			inventorySlots.get(parchment).decrStackSize(1);
			inventorySlots.get(output).putStack(ItemStack.EMPTY);

			for (int i = 0; i < 3; i++) {
				if (!inventorySlots.get(combine + i).getStack().isEmpty()) {
					Item t = inventorySlots.get(combine + i).getStack().getItem();
					if (t.getContainerItem() != null)
						inventorySlots.get(combine + i).putStack(new ItemStack(t.getContainerItem()));
					else
						inventorySlots.get(combine + i).putStack(ItemStack.EMPTY);
				}
			}
		}

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
				if (!mergeItemStack(itemStack2, sizeInventory, sizeInventory + 36, true)) {
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemStack2, itemStack1);
			} else if (!(slotIndex < output)) {
				return ItemStack.EMPTY;
			} else if (!mergeItemStack(itemStack2, sizeInventory, sizeInventory + 36, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemStack2.getCount() == itemStack1.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemStack2);
		}

		return itemStack1;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

}
