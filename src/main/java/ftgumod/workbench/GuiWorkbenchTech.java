package ftgumod.workbench;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiWorkbenchTech extends GuiContainer {

	public GuiWorkbenchTech(InventoryPlayer p_i45504_1_, World p_i45504_2_) {
		this(p_i45504_1_, p_i45504_2_, BlockPos.ORIGIN);
	}

	public GuiWorkbenchTech(InventoryPlayer p_i45505_1_, World p_i45505_2_, BlockPos p_i45505_3_) {
		super(new ContainerWorkbenchTech(p_i45505_1_, p_i45505_2_, p_i45505_3_));
	}

	protected void drawGuiContainerForegroundLayer(int p_drawGuiContainerForegroundLayer_1_, int p_drawGuiContainerForegroundLayer_2_) {
		fontRendererObj.drawString(I18n.format("container.crafting", new Object[0]), 28, 6, 0x404040);
		fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, (ySize - 96) + 2, 0x404040);
	}

	protected void drawGuiContainerBackgroundLayer(float p_drawGuiContainerBackgroundLayer_1_, int p_drawGuiContainerBackgroundLayer_2_, int p_drawGuiContainerBackgroundLayer_3_) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES);
		int lvt_4_1_ = (width - xSize) / 2;
		int lvt_5_1_ = (height - ySize) / 2;
		drawTexturedModalRect(lvt_4_1_, lvt_5_1_, 0, 0, xSize, ySize);
	}

	private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/crafting_table.png");

}
