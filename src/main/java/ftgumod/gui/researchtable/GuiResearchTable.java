package ftgumod.gui.researchtable;

import ftgumod.Decipher;
import ftgumod.Decipher.DecipherGroup;
import ftgumod.FTGU;
import ftgumod.FTGUAPI;
import ftgumod.gui.TileEntityInventory;
import ftgumod.item.ItemLookingGlass;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.server.RequestTechMessage;
import ftgumod.technology.TechnologyHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

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
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = FTGUAPI.b_researchTable.getLocalizedName();
		fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 6, 4210752);
		fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);

		Slot slot = getSlotUnderMouse();
		if (slot != null && !slot.getHasStack()) {
			ContainerResearchTable table = (ContainerResearchTable) inventorySlots;
			int index = slot.getSlotIndex() - table.combine;
			if (slot.inventory == tileentity && table.recipe != null && index >= 0 && index < 9 && !table.recipe.recipe.get(index).isEmpty()) {
				List<String> text = new ArrayList<>();

				String hint = I18n.format("research." + table.recipe.output.getUnlocalizedName() + "." + table.recipe.recipe.get(index).toString());
				if (TechnologyHandler.hasDecipher(table.recipe)) {
					Decipher d = TechnologyHandler.unlock.get(table.recipe);
					DecipherGroup g = d.unlock[index];
					if (g != null) {
						if (!table.inventorySlots.get(table.glass).getHasStack()) {
							hint = TextFormatting.OBFUSCATED + hint;
						} else {
							List<String> items = ItemLookingGlass.getItems(table.inventorySlots.get(table.glass).getStack());
							boolean perms = false;
							for (ItemStack stack : g.unlock)
								for (String t : items)
									if ((stack.isEmpty() && t.equals("tile.null")) || (!stack.isEmpty() && stack.getItem().getUnlocalizedName(stack).equals(t)))
										perms = true;
							if (!perms)
								hint = TextFormatting.OBFUSCATED + hint;
						}
					}
				}
				text.add(hint);

				drawHoveringText(text, mouseX - guiLeft, mouseY - guiTop, fontRenderer);
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
			for (int i = 0; i < 9; i++) {
				if (!table.recipe.recipe.get(i).isEmpty()) {
					Slot slot = inventorySlots.inventorySlots.get(i + table.combine);
					if (!slot.getHasStack())
						this.drawTexturedModalRect(slot.xPos + guiLeft, slot.yPos + guiTop, 176, 0, 16, 16);
				}
			}
		}
	}

}
