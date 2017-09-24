package ftgumod.inventory;

import ftgumod.FTGUAPI;
import ftgumod.item.ItemLookingGlass;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.DecipherMessage;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.tileentity.TileEntityInventory;
import ftgumod.util.BlockPredicate;
import ftgumod.util.BlockSerializable;
import ftgumod.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContainerResearchTable extends Container {

	public final TileEntityInventory invInput;
	private final IInventory invResult = new InventoryCraftResult();
	private final InventoryPlayer invPlayer;

	private final int sizeInventory;

	public Technology recipe = null;
	public int combine;
	public int output;
	public int glass;
	public Set<Integer> deciphered;
	private int feather;
	private int parchment;

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

		onCraftMatrixChanged(tileEntity);
	}

	private int addSlots(TileEntityInventory tileEntity) {
		int c = 0;

		addSlotToContainer(new SlotSpecial(tileEntity, c, 8, 46, 1, new ItemStack(Items.FEATHER)));
		feather = c;
		c++;

		addSlotToContainer(new SlotSpecial(tileEntity, c, 8, 24, 1, new ItemStack(FTGUAPI.i_parchmentIdea)));
		parchment = c;
		c++;

		combine = c;
		for (int sloty = 0; sloty < 3; sloty++) {
			for (int slotx = 0; slotx < 3; slotx++) {
				addSlotToContainer(new SlotSpecial(tileEntity, c, 30 + slotx * 18, 17 + sloty * 18, 1));
				c++;
			}
		}

		addSlotToContainer(new SlotSpecial(tileEntity, c, 150, 35, 1, new ItemStack(FTGUAPI.i_lookingGlass)));
		glass = c;
		c++;

		addSlotToContainer(new Slot(invResult, c, 124, 35));
		output = c;
		c++;

		return c;
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		if (inv == invInput) {
			if (inventorySlots.get(parchment).getHasStack()) {
				NBTTagCompound tag = StackUtils.getItemData(inventorySlots.get(parchment).getStack());
				String s = tag.getString("FTGU");
				Technology tech = TechnologyHandler.technologies.get(new ResourceLocation(s));

				if (tech != null && tech.hasResearchRecipe() && tech.canResearch(invPlayer.player)) {
					recipe = tech;
//					if (!invPlayer.player.world.isRemote && recipe.getResearchRecipe().hasDecipher() && !TechnologyHandler.UNDECIPHERED_RESEARCH.isUnlocked(invPlayer.player))
//						EventHandler.unlock(TechnologyHandler.UNDECIPHERED_RESEARCH, (EntityPlayerMP) invPlayer.player, SoundEvents.ENTITY_PLAYER_LEVELUP);
				}
			} else
				recipe = null;

			if (recipe != null) {
				if (invPlayer.player.world.isRemote) {
					if (deciphered == null || deciphered.size() < 9) {
						inventorySlots.get(output).putStack(ItemStack.EMPTY);
						return;
					}
				} else {
					List<BlockSerializable> blocks = ItemLookingGlass.getInspected(inventorySlots.get(glass).getStack());
					deciphered = new HashSet<>();

					boolean allow = true;
					for (int i = 0; i < 9; i++) {
						if (!recipe.getResearchRecipe().isEmpty(i) && recipe.getResearchRecipe().get(i).hasDecipher()) {
							Set<BlockPredicate> set = recipe.getResearchRecipe().get(i).getDecipher();
							boolean perms = false;
							for (BlockSerializable block : blocks)
								for (BlockPredicate predicate : set)
									if (block.test(predicate, invPlayer.player.getServer())) {
										perms = true;
										break;
									}
							if (!perms)
								allow = false;
							else
								deciphered.add(i);
						} else
							deciphered.add(i);
					}
					PacketDispatcher.sendTo(new DecipherMessage(deciphered), (EntityPlayerMP) invPlayer.player);
					if (!allow) {
						inventorySlots.get(output).putStack(ItemStack.EMPTY);
						return;
					}
				}

				if (inventorySlots.get(feather).getHasStack()) {
					NonNullList<ItemStack> inventory = NonNullList.create();
					for (int i = 0; i < 9; i++)
						inventory.add(inventorySlots.get(combine + i).getStack());

					if (recipe.getResearchRecipe().test(inventory)) {
						ItemStack result = new ItemStack(FTGUAPI.i_parchmentResearch);

						StackUtils.getItemData(result).setString("FTGU", recipe.getRegistryName().toString());

						inventorySlots.get(output).putStack(result);
						return;
					}
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

			for (int i = 0; i < 9; i++) {
				if (!inventorySlots.get(combine + i).getStack().isEmpty()) {
					Item t = inventorySlots.get(combine + i).getStack().getItem();
					if (t.getContainerItem() != null)
						inventorySlots.get(combine + i).putStack(new ItemStack(t.getContainerItem()));
					else
						inventorySlots.get(combine + i).putStack(ItemStack.EMPTY);
				}
			}
			recipe = null;
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
