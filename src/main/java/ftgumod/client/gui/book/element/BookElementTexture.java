package ftgumod.client.gui.book.element;

import ftgumod.client.gui.book.GuiBook;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class BookElementTexture implements IBookElement {

	private final GuiBook book;
	private final ResourceLocation texture;
	private final int x, width, height;

	private final ITextComponent hover = new TextComponentString("");
	private final ITextComponent click = new TextComponentString("");

	public BookElementTexture(GuiBook book, ResourceLocation texture, int width, int height, Alignment alignment) {
		this(book, texture, width, height, alignment, null, null, null);
	}

	public BookElementTexture(GuiBook book, ResourceLocation texture, int width, int height, Alignment alignment, @Nullable HoverEvent hover, @Nullable ClickEvent click, @Nullable String insertion) {
		this.book = book;
		this.texture = texture;

		this.x = (int) (alignment.getPosition(book.getBook().getPageWidth()) - alignment.getPosition(width));
		this.width = width;
		this.height = height;

		if (hover != null)
			this.hover.getStyle().setHoverEvent(hover);

		Style style = this.click.getStyle();
		if (click != null)
			style.setClickEvent(click);
		if (insertion != null)
			style.setInsertion(insertion);
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getMargin() {
		return book.getFontRenderer().FONT_HEIGHT;
	}

	@Override
	public int getPageWidth() {
		return 1;
	}

	@Override
	public void drawElement(int mouseX, int mouseY, float partialTicks) {
		book.mc.getTextureManager().bindTexture(texture);
		Gui.drawModalRectWithCustomSizedTexture(x, 0, 0, 0, width, height, width, height);
	}

	@Override
	public void drawForeground(int mouseX, int mouseY, float partialTicks) {
		if (mouseX >= x && mouseX < x + width && mouseY >= 0 && mouseY < height)
			book.handleComponentHover(hover, mouseX, mouseY);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseX >= x && mouseX < x + width && mouseY >= 0 && mouseY < height)
			book.handleComponentClick(click);
	}

}
