package ftgumod.client.gui.book;

import ftgumod.client.gui.book.content.IPageContent;
import ftgumod.client.gui.book.element.IPageElement;
import ftgumod.client.shader.FramebufferTransparent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class GuiBook extends GuiScreen {

	private final IBook book;
	private final Map<IPageElement, FramebufferTransparent> elements = new LinkedHashMap<>();
	private final int factor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();

	public GuiBook(IBook book) {
		this.book = book;
		List<IPageElement> list = new ArrayList<>();
		for (IPageContent content : book.getContent())
			content.build(this, list);
		for (IPageElement element : list)
			elements.put(element, new FramebufferTransparent(book.getPageWidth(), book.getPageHeight(), factor, true));
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

		int width = book.getWidthLeft() + book.getWidthRight();
		int marginTop = (this.height - book.getHeight()) / 2;
		int marginLeft = (this.width - width) / 2;

		GlStateManager.pushMatrix();
		GlStateManager.scale(2, 2, 2);
		mc.getTextureManager().bindTexture(book.getTexture());
		drawTexturedModalRect(marginLeft / 2, marginTop / 2, 0, 0, width / 2, book.getHeight() / 2);
		GlStateManager.popMatrix();

		int y = 0;

		int xa = marginLeft + book.getPageXLeft() - 1;
		int ya = marginTop + (book.getHeight() - (book.getPageHeight() + book.getPageY()));

		for (Map.Entry<IPageElement, FramebufferTransparent> entry : elements.entrySet()) {
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			GlStateManager.ortho(0.0D, book.getPageWidth() * factor, book.getPageHeight() * factor, 0.0D, 1000.0D, 3000.0D);
			GlStateManager.matrixMode(5888);
			GlStateManager.loadIdentity();
			GlStateManager.translate(0.0F, 0.0F, -2000.0F);

			int mx = mouseX - marginLeft - book.getPageXLeft();
			int my = mouseY - marginTop - book.getPageY() - y;

			IPageElement element = entry.getKey();
			FramebufferTransparent framebuffer = entry.getValue();

			framebuffer.bindFramebuffer(true);

			GlStateManager.pushMatrix();
			GlStateManager.scale(factor, factor, factor);
			element.drawElement(mx, my, partialTicks);
			GlStateManager.popMatrix();

			mc.getFramebuffer().bindFramebuffer(true);

			GlStateManager.pushMatrix();
			framebuffer.framebufferRender(xa, ya - y, 0, 0, book.getPageWidth(), book.getPageHeight(), true);
			framebuffer.framebufferClear();
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.translate(marginLeft + book.getPageXLeft(), marginTop + book.getPageY() + y, 0);
			element.drawForeground(mx, my, partialTicks);
			GlStateManager.popMatrix();

			y += element.getHeight() + element.getMargin();
		}

		GlStateManager.popMatrix();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		int y = 0;
		for (IPageElement element : elements.keySet()) {
			int i = y + element.getHeight();
			if (i > mouseY) {
				element.mouseClicked(mouseX, mouseY - y, mouseButton);
				return;
			}
			y = i;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void onGuiClosed() {
		for (FramebufferTransparent framebuffer : elements.values())
			framebuffer.deleteFramebuffer();
	}

}
