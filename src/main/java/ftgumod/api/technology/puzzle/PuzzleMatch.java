package ftgumod.api.technology.puzzle;

import ftgumod.api.FTGUAPI;
import ftgumod.api.inventory.ContainerResearch;
import ftgumod.api.inventory.InventoryCraftingPersistent;
import ftgumod.api.inventory.SlotCrafting;
import ftgumod.api.technology.recipe.IPuzzle;
import ftgumod.api.util.BlockSerializable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.client.config.GuiUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class PuzzleMatch implements IPuzzle {

	private final IInventory inventory = new InventoryBasic(null, false, 9);
	private final List<ContainerResearch> registry = new LinkedList<>();
	private final ResearchMatch research;
	private List<ITextComponent> hints;

	public PuzzleMatch(ResearchMatch research) {
		this.research = research;
	}

	@Override
	public NBTBase write() {
		NBTTagList items = new NBTTagList();
		for (int i = 0; i < inventory.getSizeInventory(); ++i) {
			if (!inventory.getStackInSlot(i).isEmpty()) {
				NBTTagCompound compound = new NBTTagCompound();
				compound.setByte("Slot", (byte) i);
				inventory.getStackInSlot(i).writeToNBT(compound);
				items.appendTag(compound);
			}
		}
		return items;
	}

	@Override
	public void read(NBTBase tag) {
		NBTTagList items = (NBTTagList) tag;
		for (int i = 0; i < items.tagCount(); ++i) {
			NBTTagCompound compound = items.getCompoundTagAt(i);
			byte slot = compound.getByte("Slot");
			if (slot >= 0 && slot < inventory.getSizeInventory())
				inventory.setInventorySlotContents(slot, new ItemStack(compound));
		}
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
	public void onStart(ContainerResearch container) {
		registry.add(container);
		InventoryCrafting crafting = new InventoryCraftingPersistent(inventory, 0, 3, 3);
		for (int sloty = 0; sloty < 3; sloty++)
			for (int slotx = 0; slotx < 3; slotx++)
				container.addSlotToContainer(new SlotCrafting(container, crafting, slotx + sloty * 3, 30 + slotx * 18, 17 + sloty * 18, 1, (Predicate<ItemStack>) null));
	}

	@Override
	public void onInventoryChange(ContainerResearch container) {
		if (!container.isClient()) {
			hints = new ArrayList<>();
			List<BlockSerializable> inspected = Collections.emptyList();
			if (container.inventorySlots.get(2).getHasStack())
				inspected = FTGUAPI.stackUtils.getInspected(container.inventorySlots.get(2).getStack());
			for (int i = 0; i < 9; i++)
				if (research.hasHint(i))
					hints.add(research.getHint(i).getHint(inspected));
				else
					hints.add(null);
			container.refreshHints(hints);
			container.markDirty();
		}
	}

	@Override
	public void onFinish() {
		NonNullList<ItemStack> remaining = ForgeHooks.defaultRecipeGetRemainingItems(new InventoryCraftingPersistent(inventory, 0, 3, 3));
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
	public void onRemove(@Nullable EntityPlayer player, World world, BlockPos pos) {
		if (player != null) {
			for (int i = 0; i < 9; i++) {
				ItemStack stack = inventory.getStackInSlot(i);
				if (!stack.isEmpty() && !player.addItemStackToInventory(stack))
					player.dropItem(stack, false);
			}
		} else InventoryHelper.dropInventoryItems(world, pos, inventory);

		for (ContainerResearch container : registry)
			container.removeSlots(9);
		registry.clear();
	}

	@Override
	public void setHints(List<ITextComponent> hints) {
		this.hints = hints;
	}

	@Override
	public void drawForeground(GuiContainer gui, int mouseX, int mouseY) {
		mouseX -= gui.getGuiLeft();
		mouseY -= gui.getGuiTop();

		boolean b = !research.getTechnology().canResearch(gui.mc.player);

		Slot slot = gui.getSlotUnderMouse();
		if (slot != null && !slot.getHasStack()) {
			int index = slot.getSlotIndex();
			if (slot.inventory instanceof InventoryCraftingPersistent && index >= 0 && index < 9 && research.hasHint(index)) {
				ITextComponent hint = (hints == null || b) ? research.getHint(index).getObfuscatedHint() : hints.get(index);
				if (!hint.getUnformattedText().isEmpty())
					gui.drawHoveringText(Arrays.asList(hint.getFormattedText().split("\n")), mouseX, mouseY);
			}
		} else if (b && mouseX >= 90 && mouseX < 112 && mouseY >= 35 && mouseY < 50) {
			List<String> text = Collections.singletonList(I18n.format(research.getTechnology().isResearched(gui.mc.player) ? "technology.complete.already" : "technology.complete.understand", research.getTechnology().getDisplayInfo().getTitle().getFormattedText()));
			GuiUtils.drawHoveringText(text, mouseX, mouseY, gui.width, gui.height, gui.width - mouseX - gui.getGuiLeft() - 16, Minecraft.getMinecraft().fontRenderer);
		}
	}

	@Override
	public void drawBackground(GuiContainer gui, int mouseX, int mouseY) {
		// Grid
		gui.drawTexturedModalRect(29 + gui.getGuiLeft(), 16 + gui.getGuiTop(), 0, 166, 54, 54);

		// Arrow
		if (research.getTechnology().canResearch(gui.mc.player))
			gui.drawTexturedModalRect(90 + gui.getGuiLeft(), 35 + gui.getGuiTop(), 54, 166, 22, 15);
		else
			gui.drawTexturedModalRect(90 + gui.getGuiLeft(), 35 + gui.getGuiTop(), 54, 181, 22, 15);

		// Hints
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				if (research.hasHint(x + y * 3) && inventory.getStackInSlot(x + y * 3).isEmpty())
					gui.drawTexturedModalRect(30 + x * 18 + gui.getGuiLeft(), 17 + y * 18 + gui.getGuiTop(), 176, 0, 16, 16);
	}

}
