package ftgumod.client.gui.book.element;

import ftgumod.client.gui.book.GuiBook;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class BookElementText implements IBookElement {

	private final GuiBook book;
	private final List<ITextComponent> text;
	private final Alignment alignment;
	private final float size;
	private final boolean shadow;

	private final float width;

	public BookElementText(GuiBook book, ITextComponent text, Alignment alignment, float size, boolean shadow) {
		this.book = book;
		width = book.getBook().getPageWidth() / size;
		this.text = GuiUtilRenderComponents.splitText(text, (int) width, book.getFontRenderer(), true, false);
		this.alignment = alignment;
		this.size = size;
		this.shadow = shadow;
	}

	@Override
	public int getHeight() {
		return text.size() * getMargin();
	}

	@Override
	public int getMargin() {
		return (int) (book.getFontRenderer().FONT_HEIGHT * size);
	}

	@Override
	public int getPageWidth() {
		return 1;
	}

	@Override
	public void drawElement(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.scale(size, size, size);
		FontRenderer fontRenderer = book.getFontRenderer();
		float x = alignment.getPosition(width);
		float y = 0;

		for (ITextComponent text : text) {
			String s = text.getFormattedText();
			fontRenderer.drawString(s, x - alignment.getPosition(fontRenderer.getStringWidth(s)), y, 0, shadow);
			y += fontRenderer.FONT_HEIGHT;
		}
	}

	@Override
	public void drawForeground(int mouseX, int mouseY, float partialTicks) {
		book.handleComponentHover(getComponentAt(mouseX, mouseY), mouseX, mouseY);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0)
			book.handleComponentClick(getComponentAt(mouseX, mouseY));
	}

	private ITextComponent getComponentAt(int mouseX, int mouseY) {
		FontRenderer fontRenderer = book.getFontRenderer();
		float x = alignment.getPosition(width);
		float y = 0;

		float mx = mouseX / size;
		float my = mouseY / size;

		for (ITextComponent text : text) {
			int width = fontRenderer.getStringWidth(text.getFormattedText());
			float i = x - alignment.getPosition(width);

			float j = y + fontRenderer.FONT_HEIGHT;
			if (my >= y && my < j && mx >= i && mx < i + width)
				for (ITextComponent child : text)
					if (child instanceof TextComponentString) {
						i += fontRenderer.getStringWidth(((TextComponentString) child).getText());
						if (i > mx)
							return child;
					}
			y = j;
		}
		return null;
	}

}
