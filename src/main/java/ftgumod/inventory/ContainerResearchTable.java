package ftgumod.inventory;

import ftgumod.Content;
import ftgumod.api.util.BlockSerializable;
import ftgumod.api.util.IStackUtils;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.HintMessage;
import ftgumod.technology.Technology;
import ftgumod.tileentity.TileEntityInventory;
import ftgumod.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContainerResearchTable extends Container {

	public final TileEntityInventory invInput;

	private final InventoryCrafting craftMatrix;
	private final InventoryPlayer invPlayer;

	private final int sizeInventory;

	public int combine;
	public int output;
	public int glass;
	public Technology recipe;
	private int feather;
	private int parchment;
	private NonNullList<ItemStack> remaining;

	public List<ITextComponent> hints;

	public ContainerResearchTable(TileEntityInventory tileEntity, InventoryPlayer invPlayer) {
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

		craftMatrix = new InventoryCraftingPersistent(this, tileEntity, combine, 3, 3);
		onCraftMatrixChanged(tileEntity);
	}

	private int addSlots(TileEntityInventory tileEntity) {
		int c = 0;

		addSlotToContainer(new SlotSpecial(tileEntity, c, 8, 46, 1, OreDictionary.getOres("feather")));
		feather = c;
		c++;

		addSlotToContainer(new SlotSpecial(tileEntity, c, 8, 24, 1, new ItemStack(Content.i_parchmentIdea)));
		parchment = c;
		c++;

		combine = c;
		for (int sloty = 0; sloty < 3; sloty++) {
			for (int slotx = 0; slotx < 3; slotx++) {
				addSlotToContainer(new SlotSpecial(tileEntity, c, 30 + slotx * 18, 17 + sloty * 18, 1, (Iterable<ItemStack>) null));
				c++;
			}
		}

		addSlotToContainer(new SlotSpecial(tileEntity, c, 150, 35, 1, new ItemStack(Content.i_magnifyingGlass)));
		glass = c;
		c++;

		addSlotToContainer(new Slot(new InventoryCraftResult(), c, 124, 35));
		output = c;
		c++;

		return c;
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		if (inv == invInput) {
			if (!invPlayer.player.world.isRemote)
				hints = new ArrayList<>();

			if (inventorySlots.get(parchment).getHasStack()) {
				Technology tech = StackUtils.INSTANCE.getTechnology(inventorySlots.get(parchment).getStack());
				if (tech != null && tech.hasResearchRecipe() && tech.canResearch(invPlayer.player))
					recipe = tech;
			} else
				recipe = null;

			if (recipe != null) {
				List<BlockSerializable> inspected = Collections.emptyList();
				if (inventorySlots.get(glass).getHasStack())
					inspected = StackUtils.INSTANCE.getInspected(inventorySlots.get(glass).getStack());
				if (!invPlayer.player.world.isRemote)
					for (int i = 0; i < 9; i++)
						if (recipe.getResearchRecipe().hasHint(i))
							hints.add(recipe.getResearchRecipe().getHint(i).getHint(inspected));
						else
							hints.add(null);

				if (inventorySlots.get(feather).getHasStack()) {
					remaining = recipe.getResearchRecipe().test(craftMatrix);
					if (remaining != null) {
						inventorySlots.get(output).putStack(StackUtils.INSTANCE.getParchment(recipe, IStackUtils.Parchment.RESEARCH));
						return;
					}
				}
			}

			if (!invPlayer.player.world.isRemote)
				PacketDispatcher.sendTo(new HintMessage(hints), (EntityPlayerMP) invPlayer.player);
			inventorySlots.get(output).putStack(ItemStack.EMPTY);
		}
	}

	@Override
	public ItemStack slotClick(int index, int mouse, ClickType mode, EntityPlayer player) {
		if (index == output && inventorySlots.get(output).getHasStack()) {
			inventorySlots.get(parchment).decrStackSize(1);
			for (int i = 0; i < 9; i++)
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

}
