package ftgumod.client.gui.book;

import ftgumod.client.gui.book.content.IBookContent;
import ftgumod.client.gui.book.element.IBookElement;
import ftgumod.client.gui.book.element.PageElement;
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
	private final List<List<PageElement>> pages = new ArrayList<>();
	private final int bookWidth;

	private int page = 0;

	public GuiBook(IBook book) {
		this.book = book;
		bookWidth = book.getWidthLeft() + book.getWidthRight();

		List<IBookElement> list = new ArrayList<>();
		for (IBookContent content : book.getContent())
			content.build(this, list);

		int page = 0;
		int y = 0;
		IBookElement last = null;
		for (IBookElement element : list) {
			int margin = last == null ? 0 : last.getMargin();
			if (y != 0 && y + margin + element.getHeight() > book.getPageHeight()) {
				y = 0;
				page++;
			} else
				y += margin;

			for (int i = page; i < page + element.getPageWidth(); i++)
				getPage(i).add(new PageElement(book, element, i - page, y));

			page += element.getPageWidth() - 1;
			y += element.getHeight();
			last = element;
		}
	}

	private List<PageElement> getPage(int index) {
		while (pages.size() <= index)
			pages.add(new ArrayList<>());
		return pages.get(index);
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

		int marginTop = (this.height - book.getHeight()) / 2;
		int marginLeft = (this.width - bookWidth) / 2;

		mc.getTextureManager().bindTexture(book.getTexture());

		boolean left = page == 0;
		boolean right = 2 * page - 2 >= pages.size();

		int x = left ? book.getWidthLeft() : 0;
		int y = left || right ? book.getHeight() : 0;
		int width = right ? book.getWidthLeft() : left ? book.getWidthRight() : bookWidth;

		if (left)
			marginLeft += book.getWidthLeft();

		drawModalRectWithCustomSizedTexture(marginLeft, marginTop, x, y, width, book.getHeight(), 512, 512);

		if (!left && !right) {
			int xaLeft = marginLeft + book.getPageXLeft() - 1;
			int xaRight = marginLeft + book.getPageXRight() - 1;
			int ya = marginTop + book.getPageY();

			for (PageElement element : getPage(2 * page - 2)) {
				GlStateManager.pushMatrix();
				element.draw(xaLeft, ya, mouseX - xaLeft, mouseY - ya, partialTicks);
				GlStateManager.popMatrix();
			}

			for (PageElement element : getPage(2 * page - 1)) {
				GlStateManager.pushMatrix();
				element.draw(xaRight, ya, mouseX - xaRight, mouseY - ya, partialTicks);
				GlStateManager.popMatrix();
			}
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 0)
			page++;
		if (mouseButton == 1)
			page--;
		if (page < 0)
			page = 0;

		if (page != 0 && 2 * page - 2 < pages.size()) {
			int marginTop = (this.height - book.getHeight()) / 2;
			int marginLeft = (this.width - bookWidth) / 2;

			int xaLeft = marginLeft + book.getPageXLeft() - 1;
			int xaRight = marginLeft + book.getPageXRight() - 1;
			int ya = marginTop + book.getPageY();

			if (mouseY > ya && mouseY < ya + book.getPageHeight())
				if (mouseX >= xaLeft && mouseX < xaLeft + book.getPageWidth())
					for (PageElement element : getPage(2 * page - 2))
						element.mouseClicked(mouseX - xaLeft, mouseY - ya, mouseButton);
				else if (mouseX >= xaRight && mouseX < xaRight + book.getPageWidth())
					for (PageElement element : getPage(2 * page - 1))
						element.mouseClicked(mouseX - xaRight, mouseY - ya, mouseButton);
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

}
