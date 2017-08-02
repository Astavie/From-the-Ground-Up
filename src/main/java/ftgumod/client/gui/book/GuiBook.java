package ftgumod.client.gui.book;

import ftgumod.client.gui.book.element.IPageElement;
import ftgumod.client.gui.book.element.PageElementText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class GuiBook extends GuiScreen {

	private final IBook book;
	private final IPageElement element;

	public GuiBook(IBook book) {
		this.book = book;
		this.element = new PageElementText(this, new TextComponentString("Small text...\nThis is not fun!"), PageElementText.Alignment.LEFT, 5F, true);
	}

	public IBook getBook() {
		return book;
	}

	public FontRenderer getFontRenderer() {
		return Minecraft.getMinecraft().fontRenderer;
	}

	@Override
	public void handleComponentHover(ITextComponent component, int x, int y) {
		super.handleComponentHover(component, x, y);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		element.drawElement(mouseX, mouseY, partialTicks);
	}

}
