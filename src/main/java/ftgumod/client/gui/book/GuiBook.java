package ftgumod.client.gui.book;

import ftgumod.client.gui.book.content.IPageContent;
import ftgumod.client.gui.book.element.IPageElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiBook extends GuiScreen {

	private final IBook book;
	private final List<IPageElement> elements = new ArrayList<>();

	public GuiBook(IBook book) {
		this.book = book;
		for (IPageContent content : book.getContent())
			content.build(this, elements);
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

		int y = 0;
		for (IPageElement element : elements) {
			element.drawElement(mouseX, mouseY - y, partialTicks);
			int height = element.getHeight();
			GlStateManager.translate(0F, height, 0F);
			y += height;
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		int y = 0;
		for (IPageElement element : elements) {
			int i = y + element.getHeight();
			if (i > mouseY) {
				element.mouseClicked(mouseX, mouseY - y, mouseButton);
				return;
			}
			y = i;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

}
