package ftgumod.client.gui.book;

import ftgumod.client.gui.book.content.IPageContent;
import ftgumod.client.gui.book.element.IPageElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

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

		GlStateManager.pushMatrix();
		{
			int width = book.getWidthLeft() + book.getWidthRight();
			int marginTop = (this.height - book.getHeight()) / 2;
			int marginLeft = (this.width - width) / 2;
			GlStateManager.translate(marginLeft, marginTop, 0);

			GlStateManager.scale(2, 2, 2);
			{
				mc.getTextureManager().bindTexture(book.getTexture());
				drawTexturedModalRect(0, 0, 0, 0, width / 2, book.getHeight() / 2);
			}
			GlStateManager.scale(0.5, 0.5, 0.5);

			GlStateManager.pushMatrix();
			{
				int factor = new ScaledResolution(mc).getScaleFactor();
				GlStateManager.translate(book.getPageXLeft(), book.getPageY(), 0);

				int y = book.getHeight() - (book.getPageHeight() + book.getPageY());
				GL11.glScissor((marginLeft + book.getPageXLeft()) * factor, (marginTop + y) * factor, book.getPageWidth() * factor, book.getPageHeight() * factor);

				y = 0;
				for (IPageElement element : elements) {
					GL11.glEnable(GL11.GL_SCISSOR_TEST);
					element.drawElement(mouseX, mouseY - y, partialTicks);
					GL11.glDisable(GL11.GL_SCISSOR_TEST);

					element.drawForeground(mouseX, mouseY - y, partialTicks);
					int height = element.getHeight() + element.getMargin();
					GlStateManager.translate(0, height, 0);
					y += height;
				}
			}
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();

		super.drawScreen(mouseX, mouseY, partialTicks);
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
