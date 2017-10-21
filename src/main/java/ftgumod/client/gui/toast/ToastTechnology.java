package ftgumod.client.gui.toast;

import ftgumod.technology.Technology;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ToastTechnology implements IToast {

	private final Technology tech;

	public ToastTechnology(Technology tech) {
		this.tech = tech;
	}

	@Override
	public Visibility draw(GuiToast gui, long delta) {
		gui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		gui.drawTexturedModalRect(0, 0, 0, 0, 160, 32);

		String display = I18n.format("technology.toast." + (tech.getType() == Technology.Type.THEORY ? "theory" : "technology"));
		String title = tech.getDisplayInfo().getTitle().getUnformattedText();

		List<String> list = gui.getMinecraft().fontRenderer.listFormattedStringToWidth(title, 125);

		if (list.size() == 1) {
			gui.getMinecraft().fontRenderer.drawString(display, 30, 7, 0xFFFF00);
			gui.getMinecraft().fontRenderer.drawString(title, 30, 18, -1);
		} else {
			if (delta < 1500L) {
				int k = MathHelper.floor(MathHelper.clamp((float) (1500L - delta) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 0x400000;
				gui.getMinecraft().fontRenderer.drawString(display, 30, 11, 0xFFFF00 | k);
			} else {
				int i1 = MathHelper.floor(MathHelper.clamp((float) (delta - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 0x400000;
				int l = 16 - list.size() * gui.getMinecraft().fontRenderer.FONT_HEIGHT / 2;

				for (String s : list) {
					gui.getMinecraft().fontRenderer.drawString(s, 30, l, 0xFFFFFF | i1);
					l += gui.getMinecraft().fontRenderer.FONT_HEIGHT;
				}
			}
		}

		RenderHelper.enableGUIStandardItemLighting();
		gui.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(null, tech.getDisplayInfo().getIcon(), 8, 8);

		return delta >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
	}

}
