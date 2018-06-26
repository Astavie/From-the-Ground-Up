package ftgumod.client.gui;

import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.inventory.ContainerResearchTable;
import ftgumod.inventory.InventoryCraftingPersistent;
import ftgumod.technology.recipe.PuzzleMatch;
import ftgumod.tileentity.TileEntityResearchTable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.Arrays;

public class GuiResearchTable extends GuiContainer {

	private final ResourceLocation texture;
	private final InventoryPlayer player;
	private final TileEntityResearchTable tile;

	public GuiResearchTable(InventoryPlayer player, TileEntityResearchTable tileentity) {
		super(tileentity.createContainer(player, player.player));
		this.player = player;
		this.tile = tileentity;

		texture = new ResourceLocation(FTGU.MODID + ":textures/gui/container/" + tileentity.getName() + ".png");
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = Content.b_researchTable.getLocalizedName();
		fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 6, 4210752);
		fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);

		Slot slot = getSlotUnderMouse();
		if (slot != null && !slot.getHasStack()) {
			ContainerResearchTable table = (ContainerResearchTable) inventorySlots;
			if (table.recipe != null && tile.puzzle != null) {
				PuzzleMatch puzzle = (PuzzleMatch) tile.puzzle;
				// if (puzzle.hints == null)
				//     PacketDispatcher.sendToServer(new RequestMessage(1));

				int index = slot.getSlotIndex();
				if ((slot.inventory instanceof InventoryCraftingPersistent) && index >= 0 && index < 9 && puzzle.getRecipe().hasHint(index)) {
					ITextComponent hint = puzzle.hints == null ? puzzle.getRecipe().getHint(index).getObfuscatedHint() : puzzle.hints.get(index);
					if (!hint.getUnformattedText().isEmpty())
						drawHoveringText(Arrays.asList(hint.getFormattedText().split("\n")), mouseX - guiLeft, mouseY - guiTop, fontRenderer);
				}
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float arg0, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		ContainerResearchTable table = (ContainerResearchTable) inventorySlots;
		if (table.recipe != null)
			for (int i = 0; i < 9; i++)
				if (inventorySlots.inventorySlots.size() > table.puzzle && ((PuzzleMatch) tile.puzzle).getRecipe().hasHint(i)) {
					Slot slot = inventorySlots.inventorySlots.get(i + table.puzzle);
					if (!slot.getHasStack())
						this.drawTexturedModalRect(slot.xPos + guiLeft, slot.yPos + guiTop, 176, 0, 16, 16);
				}
	}

}
