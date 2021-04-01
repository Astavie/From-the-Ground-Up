package ftgumod.api.technology.puzzle.gui;

import ftgumod.api.inventory.InventoryCraftingPersistent;
import ftgumod.api.technology.puzzle.PuzzleMatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PuzzleGuiMatch implements IPuzzleGui {

	private final PuzzleMatch puzzle;
	private final IInventory inventory;

	public PuzzleGuiMatch(PuzzleMatch puzzle, IInventory inventory) {
		this.puzzle = puzzle;
		this.inventory = inventory;
	}

	@Override
	public void drawForeground(GuiContainer gui, int mouseX, int mouseY) {
		mouseX -= gui.getGuiLeft();
		mouseY -= gui.getGuiTop();

		boolean b = !puzzle.getRecipe().getTechnology().canResearch(gui.mc.player);

		Slot slot = gui.getSlotUnderMouse();
		if (slot != null && !slot.getHasStack()) {
			int index = slot.getSlotIndex();
			if (slot.inventory instanceof InventoryCraftingPersistent && index >= 0 && index < 9 && puzzle.getRecipe().hasHint(index)) {
				ITextComponent hint = (puzzle.getHints() == null || b) ? puzzle.getRecipe().getHint(index).getObfuscatedHint() : puzzle.getHints().get(index);
				if (hint != null && !hint.getUnformattedText().isEmpty())
					gui.drawHoveringText(Arrays.asList(hint.getFormattedText().split("\n")), mouseX, mouseY);
			}
		} else if (b && mouseX >= 90 && mouseX < 112 && mouseY >= 35 && mouseY < 50) {
			List<String> text = Collections.singletonList(I18n.format(puzzle.getRecipe().getTechnology().isResearched(gui.mc.player) ? "technology.complete.already" : "technology.complete.understand", puzzle.getRecipe().getTechnology().getDisplayInfo().getTitle().getFormattedText()));
			GuiUtils.drawHoveringText(text, mouseX, mouseY, gui.width, gui.height, gui.width - mouseX - gui.getGuiLeft() - 16, Minecraft.getMinecraft().fontRenderer);
		}
	}

	@Override
	public void drawBackground(GuiContainer gui, int mouseX, int mouseY) {
		// Grid
		gui.drawTexturedModalRect(29 + gui.getGuiLeft(), 16 + gui.getGuiTop(), 0, 166, 54, 54);

		// Arrow
		if (puzzle.getRecipe().getTechnology().canResearch(gui.mc.player))
			gui.drawTexturedModalRect(90 + gui.getGuiLeft(), 35 + gui.getGuiTop(), 54, 166, 22, 15);
		else
			gui.drawTexturedModalRect(90 + gui.getGuiLeft(), 35 + gui.getGuiTop(), 54, 181, 22, 15);

		// Hints
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				if (puzzle.getRecipe().hasHint(x + y * 3) && inventory.getStackInSlot(x + y * 3).isEmpty())
					gui.drawTexturedModalRect(30 + x * 18 + gui.getGuiLeft(), 17 + y * 18 + gui.getGuiTop(), 176, 0, 16, 16);
	}

}
