package ftgumod.gui.researchtable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import ftgumod.FTGU;
import ftgumod.FTGUAPI;
import ftgumod.TechnologyUtil;
import ftgumod.gui.TileEntityInventory;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.server.RequestTechMessage;

public class GuiResearchTable extends GuiContainer {

	private final ResourceLocation texture;
	private final InventoryPlayer player;
	private final IInventory tileentity;

	public GuiResearchTable(InventoryPlayer player, TileEntityInventory tileentity) {
		super(tileentity.createContainer(player, player.player));
		this.player = player;
		this.tileentity = tileentity;

		texture = new ResourceLocation(FTGU.MODID + ":textures/gui/container/" + tileentity.getName() + ".png");

		PacketDispatcher.sendToServer(new RequestTechMessage());
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = FTGUAPI.b_researchTable.getLocalizedName();
		fontRendererObj.drawString(s, xSize / 2 - fontRendererObj.getStringWidth(s) / 2, 6, 4210752);
		fontRendererObj.drawString(player.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);

		if (player.getItemStack() == null) {
			Slot slot = getSlotUnderMouse();
			if (slot != null && !slot.getHasStack()) {
				ContainerResearchTable table = (ContainerResearchTable) inventorySlots;
				if (slot.inventory == tileentity && table.recipe != null && slot.getSlotIndex() >= table.combine && slot.getSlotIndex() < table.combine + 9 && table.recipe.recipe[slot.getSlotIndex() - table.combine] != null) {
					List<String> text = new ArrayList<String>();
					text.add(I18n.translateToLocal("research." + table.recipe.output.getUnlocalisedName() + "." + TechnologyUtil.toString(table.recipe.recipe[slot.getSlotIndex() - table.combine])));
					drawHoveringText(text, mouseX - guiLeft, mouseY - guiTop, fontRendererObj);
				}
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float arg0, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(texture);
		int marginHorizontal = (width - xSize) / 2;
		int marginVertical = (height - ySize) / 2;
		drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);

		ContainerResearchTable table = (ContainerResearchTable) inventorySlots;
		if (table.recipe != null) {
			for (int i = table.combine; i < table.combine + 9; i++) {
				Slot slot = inventorySlots.inventorySlots.get(i);
				if (!slot.getHasStack() && table.recipe.recipe[i - table.combine] != null) {
					this.drawTexturedModalRect(slot.xDisplayPosition + guiLeft, slot.yDisplayPosition + guiTop, 176, 0, 16, 16);
				}
			}
		}
	}

}
