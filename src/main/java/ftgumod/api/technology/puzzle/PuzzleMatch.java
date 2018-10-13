package ftgumod.api.technology.puzzle;

import ftgumod.api.FTGUAPI;
import ftgumod.api.inventory.ContainerFTGU;
import ftgumod.api.inventory.InventoryCraftingPersistent;
import ftgumod.api.inventory.SlotCrafting;
import ftgumod.api.technology.recipe.IPuzzle;
import ftgumod.api.util.BlockSerializable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeHooks;

import java.util.*;

public class PuzzleMatch implements IPuzzle {

	public final IInventory inventory = new InventoryBasic(null, false, 9);
	private final List<ContainerFTGU> registry = new LinkedList<>();
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
	public void onStart(ContainerFTGU container) {
		registry.add(container);
		InventoryCrafting crafting = new InventoryCraftingPersistent(inventory, 0, 3, 3);
		for (int sloty = 0; sloty < 3; sloty++)
			for (int slotx = 0; slotx < 3; slotx++)
				container.addSlotToContainer(new SlotCrafting(container, crafting, slotx + sloty * 3, 30 + slotx * 18, 17 + sloty * 18, 1, (Iterable<ItemStack>) null));
	}

	@Override
	public void onInventoryChange(ContainerFTGU container) {
		if (!container.isRemote() && research.getTechnology().canResearch(container.getInventoryPlayer().player)) {
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
	public void onRemove(EntityPlayer player) {
		for (int i = 0; i < 9; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty() && !player.addItemStackToInventory(stack))
				player.dropItem(stack, false);
		}
		for (ContainerFTGU container : registry)
			container.removeSlots(9);
		registry.clear();
	}

	@Override
	public void drawForeground(GuiContainer gui, int mouseX, int mouseY) {
		Slot slot = gui.getSlotUnderMouse();
		boolean b = !research.getTechnology().canResearch(gui.mc.player);
		if (slot != null && !slot.getHasStack()) {
			int index = slot.getSlotIndex();
			if (slot.inventory instanceof InventoryCraftingPersistent && index >= 0 && index < 9 && (b || research.hasHint(index))) {
				ITextComponent hint = hints == null ? research.getHint(index).getObfuscatedHint() : hints.get(index);
				if (!hint.getUnformattedText().isEmpty())
					gui.drawHoveringText(Arrays.asList(hint.getFormattedText().split("\n")), mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
			}
		} else if (b && mouseX >= 90 && mouseX < 112 && mouseY >= 35 && mouseY < 50)
			gui.drawHoveringText("No can do", mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
	}

	@Override
	public void drawBackground(GuiContainer gui, int mouseX, int mouseY) {
		// Grid
		gui.drawTexturedModalRect(29 + gui.getGuiLeft(), 16 + gui.getGuiTop(), 0, 166, 54, 54);

		// Arrow
		boolean b = research.getTechnology().canResearch(gui.mc.player);
		if (b)
			gui.drawTexturedModalRect(90 + gui.getGuiLeft(), 35 + gui.getGuiTop(), 54, 166, 22, 15);
		else
			gui.drawTexturedModalRect(90 + gui.getGuiLeft(), 35 + gui.getGuiTop(), 54, 181, 22, 15);

		// Hints
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				if ((!b || research.hasHint(x + y * 3)) && inventory.getStackInSlot(x + y * 3).isEmpty())
					gui.drawTexturedModalRect(30 + x * 18 + gui.getGuiLeft(), 17 + y * 18 + gui.getGuiTop(), 176, 0, 16, 16);
	}

}
