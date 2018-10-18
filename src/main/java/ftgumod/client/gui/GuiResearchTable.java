package ftgumod.client.gui;

import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.tileentity.TileEntityResearchTable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

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
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.render(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = Content.b_researchTable.func_149732_F();
		fontRenderer.drawStringWithShadow(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 6, 4210752);
		fontRenderer.drawStringWithShadow(player.getDisplayName().func_150260_c(), 8, ySize - 96 + 2, 4210752);
		if (tile.puzzle != null)
			tile.puzzle.drawForeground(this, mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float arg0, int mouseX, int mouseY) {
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if (tile.puzzle != null)
			tile.puzzle.drawBackground(this, mouseX, mouseY);
	}

}
