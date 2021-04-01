package ftgumod.api.technology.puzzle.gui;

import net.minecraft.client.gui.inventory.GuiContainer;

public interface IPuzzleGui {

	void drawForeground(GuiContainer gui, int mouseX, int mouseY);

	void drawBackground(GuiContainer gui, int mouseX, int mouseY);

}
