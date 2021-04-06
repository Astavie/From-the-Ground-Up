package ftgumod.api.technology.puzzle.gui;

import java.util.Collections;
import java.util.List;

import ftgumod.api.technology.puzzle.ResearchConnect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiUtils;

public class PuzzleGuiConnect implements IPuzzleGui {

	private final ResearchConnect research;

	public PuzzleGuiConnect(ResearchConnect research) {
		this.research = research;
	}

	@Override
	public void drawForeground(GuiContainer gui, int mouseX, int mouseY) {
		mouseX -= gui.getGuiLeft();
		mouseY -= gui.getGuiTop();
		if (research.getTechnology().canResearch(gui.mc.player)) {
			if (mouseX >= 25 && mouseX < 43 && mouseY >= 34 && mouseY < 52)
				gui.drawHoveringText(gui.getItemToolTip(research.left.getDisplayStack()), mouseX, mouseY);
			if (mouseX >= 97 && mouseX < 115 && mouseY >= 34 && mouseY < 52)
				gui.drawHoveringText(gui.getItemToolTip(research.right.getDisplayStack()), mouseX, mouseY);
		} else if (mouseX >= 97 && mouseX < 119 && mouseY >= 35 && mouseY < 50) {
			List<String> text = Collections.singletonList(I18n.format(research.getTechnology().isResearched(gui.mc.player) ? "technology.complete.already" : "technology.complete.understand", research.getTechnology().getDisplayInfo().getTitle().getFormattedText()));
			GuiUtils.drawHoveringText(text, mouseX, mouseY, gui.width, gui.height, gui.width - mouseX - gui.getGuiLeft() - 16, Minecraft.getMinecraft().fontRenderer);
		}
	}

	@Override
	public void drawBackground(GuiContainer gui, int mouseX, int mouseY) {
		// Grid
		gui.drawTexturedModalRect(43 + gui.getGuiLeft(), 34 + gui.getGuiTop(), 0, 166, 54, 18);

		// Items
		if (research.getTechnology().canResearch(gui.mc.player)) {
			RenderHelper.enableGUIStandardItemLighting();
			gui.mc.getRenderItem().zLevel = 100.0F;

			GlStateManager.enableDepth();
			gui.mc.getRenderItem().renderItemAndEffectIntoGUI(gui.mc.player, research.left.getDisplayStack(), 26 + gui.getGuiLeft(), 35 + gui.getGuiTop());
			gui.mc.getRenderItem().renderItemOverlayIntoGUI(gui.mc.fontRenderer, research.left.getDisplayStack(), 26 + gui.getGuiLeft(), 35 + gui.getGuiTop(), null);

			gui.mc.getRenderItem().renderItemAndEffectIntoGUI(gui.mc.player, research.right.getDisplayStack(), 98 + gui.getGuiLeft(), 35 + gui.getGuiTop());
			gui.mc.getRenderItem().renderItemOverlayIntoGUI(gui.mc.fontRenderer, research.right.getDisplayStack(), 98 + gui.getGuiLeft(), 35 + gui.getGuiTop(), null);

			gui.mc.getRenderItem().zLevel = 0.0F;
		} else
			gui.drawTexturedModalRect(97 + gui.getGuiLeft(), 35 + gui.getGuiTop(), 54, 181, 22, 15);
	}

}
