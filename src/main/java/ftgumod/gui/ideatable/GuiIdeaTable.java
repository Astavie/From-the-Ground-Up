package ftgumod.gui.ideatable;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import ftgumod.FTGU;
import ftgumod.FTGUAPI;
import ftgumod.gui.TileEntityInventory;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.server.RequestTechMessage;
import ftgumod.technology.TechnologyHandler;

public class GuiIdeaTable extends GuiContainer {

	private final ResourceLocation texture;
	private final InventoryPlayer player;
	private final IInventory tileentity;

	public GuiIdeaTable(InventoryPlayer player, TileEntityInventory tileentity) {
		super(tileentity.createContainer(player, player.player));
		this.player = player;
		this.tileentity = tileentity;

		texture = new ResourceLocation(FTGU.MODID + ":textures/gui/container/" + tileentity.getName() + ".png");

		PacketDispatcher.sendToServer(new RequestTechMessage());
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = FTGUAPI.b_ideaTable.getLocalizedName();
		fontRendererObj.drawString(s, xSize / 2 - fontRendererObj.getStringWidth(s) / 2, 6, 4210752);
		fontRendererObj.drawString(player.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float arg0, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(texture);
		int marginHorizontal = (width - xSize) / 2;
		int marginVertical = (height - ySize) / 2;
		drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);
	}

}
