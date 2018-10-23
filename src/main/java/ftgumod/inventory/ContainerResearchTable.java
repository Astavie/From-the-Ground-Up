package ftgumod.inventory;

import ftgumod.Content;
import ftgumod.api.inventory.ContainerResearch;
import ftgumod.api.inventory.SlotCrafting;
import ftgumod.api.util.IStackUtils;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.HintMessage;
import ftgumod.technology.Technology;
import ftgumod.tileentity.TileEntityInventory;
import ftgumod.tileentity.TileEntityResearchTable;
import ftgumod.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class ContainerResearchTable extends ContainerResearch {

	public final TileEntityResearchTable invInput;

	public final InventoryPlayer invPlayer;

	private final int sizeInventory;
	private final IInventory result;

	public int puzzle;
	public int output;
	public int glass;
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
			// invInput.sync();
		}

		onCraftMatrixChanged(null);
	}

	@Override
	public void setAll(List<ItemStack> stacks) {
		for (int i = 0; i < inventorySlots.size(); ++i)
			getSlot(i).putStack(stacks.get(i));
	}

	private int addSlots(TileEntityInventory tileEntity) {
		int c = 0;

		addSlotToContainer(new SlotCrafting(this, tileEntity, c, 8, 46, 1, OreDictionary.getOres("feather")));
		feather = c;
		c++;

		addSlotToContainer(new SlotCrafting(this, tileEntity, c, 8, 24, 1, new ItemStack(Content.i_parchmentIdea)));
		parchment = c;
		c++;

		addSlotToContainer(new SlotCrafting(this, tileEntity, c, 150, 35, 1, new ItemStack(Content.i_magnifyingGlass)));
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
				if (tech != null && tech.hasResearchRecipe() && (invInput.puzzle == null || invInput.puzzle.getRecipe().getTechnology() != tech)) {
					if (invInput.puzzle != null)
						invInput.puzzle.onRemove(invPlayer.player, invInput.getWorld(), invInput.getPos());
					invInput.puzzle = tech.getResearchRecipe().createInstance();
					invInput.puzzle.onStart(this);
					invInput.puzzle.onInventoryChange(this);
				}
			} else if (invInput.puzzle != null) {
				invInput.puzzle.onRemove(invPlayer.player, invInput.getWorld(), invInput.getPos());
				invInput.puzzle = null;
			}

			if (invInput.puzzle != null) {
				if (inv != result)
					invInput.puzzle.onInventoryChange(this);
				if (inventorySlots.get(feather).getHasStack() && invInput.puzzle.getRecipe().getTechnology().canResearch(invPlayer.player) && invInput.puzzle.test()) {
					inventorySlots.get(output).putStack(StackUtils.INSTANCE.getParchment(invInput.puzzle.getRecipe().getTechnology(), IStackUtils.Parchment.RESEARCH));
					return;
				}
			}

			inventorySlots.get(output).putStack(ItemStack.EMPTY);
		}
	}

	@Override
	public ItemStack slotClick(int index, int mouse, ClickType mode, EntityPlayer player) {
		if (mode != ClickType.CLONE && index == output && inventorySlots.get(output).getHasStack()) {
			inventorySlots.get(parchment).decrStackSize(1);
			invInput.puzzle.onFinish();
			invInput.puzzle.onRemove(player, invInput.getWorld(), invInput.getPos());
			invInput.puzzle = null;
		}
		return super.slotClick(index, mouse, mode, player);
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
	public boolean isClient() {
		return invInput.getWorld().isRemote;
	}

	@Override
	public EntityPlayer getPlayer() {
		return invPlayer.player;
	}

	@Override
	public void refreshHints(List<ITextComponent> hints) {
		PacketDispatcher.sendTo(new HintMessage(hints), (EntityPlayerMP) invPlayer.player);
	}

	@Override
	public void markDirty() {
		invInput.markDirty();
	}

}
