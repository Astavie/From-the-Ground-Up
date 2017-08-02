package ftgumod.client.gui.book.element;

import ftgumod.client.gui.book.GuiBook;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;

@SideOnly(Side.CLIENT)
public class PageElementText implements IPageElement {

	private final GuiBook book;
	private final List<ITextComponent> text;
	private final Alignment alignment;
	private final float size;
	private final boolean shadow;

	private final float width;

	public PageElementText(GuiBook book, ITextComponent text, Alignment alignment, float size, boolean shadow) {
		this.book = book;
		width = book.getBook().getPageWidth() / size;
		text.getStyle().setColor(TextFormatting.DARK_PURPLE);
		this.text = GuiUtilRenderComponents.splitText(text, (int) width, book.getFontRenderer(), true, false);
		this.alignment = alignment;
		this.size = size;
		this.shadow = shadow;
	}

	@Override
	public int getHeight() {
		return (int) ((text.size() + 1) * book.getFontRenderer().FONT_HEIGHT * size);
	}

	/**
	 * @deprecated Text elements should not be used as references
	 */
	@Override
	@Deprecated
	public int hashCode() {
		return text.get(0).getUnformattedText().hashCode();
	}

	@Override
	public void drawElement(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.scale(size, size, size);

		FontRenderer fontRenderer = book.getFontRenderer();
		float x = alignment.getPosition(width);
		float y = 0;

		float mx = mouseX / size;
		float my = mouseY / size;

		ITextComponent hover = null;

		for (ITextComponent text : text) {
			String s = text.getFormattedText();
			int width = fontRenderer.getStringWidth(s);
			float i = x - alignment.getPosition(width);

			if (my >= y && my < y + fontRenderer.FONT_HEIGHT && mx >= i && mx < i + width) {
				float j = i;
				for (ITextComponent child : text)
					if (child instanceof TextComponentString) {
						j += fontRenderer.getStringWidth(((TextComponentString) child).getText());
						if (j > mx) {
							hover = child;
							break;
						}
					}
			}

			fontRenderer.drawString(s, i, y, Color.WHITE.getRGB(), shadow);
			y += fontRenderer.FONT_HEIGHT;
		}

		float size = 1 / this.size;
		GlStateManager.scale(size, size, size);

		if (hover != null)
			book.handleComponentHover(hover, mouseX, mouseY);
	}

	public enum Alignment {

		LEFT(0.0F), CENTER(0.5F), RIGHT(1.0F);

		private final float modifier;

		Alignment(float modifier) {
			this.modifier = modifier;
		}

		private float getPosition(float width) {
			return width * modifier;
		}

	}

}
