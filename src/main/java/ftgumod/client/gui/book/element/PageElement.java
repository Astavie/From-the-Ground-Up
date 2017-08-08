package ftgumod.client.gui.book.element;

import ftgumod.client.gui.book.IBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class PageElement {

	private final IBook book;
	private final IBookElement element;
	private final int y;

	private final int elementWidth;
	private final boolean left;
	private final boolean right;

	public PageElement(IBook book, IBookElement element, int elementPage, int y) {
		this.book = book;
		this.element = element;
		this.y = y;

		elementWidth = book.getPageWidth() * elementPage;
		left = elementPage == 0;
		right = elementPage == element.getPageWidth() - 1;
	}

	public void draw(int pageX, int pageY, int mouseX, int mouseY, float partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		int factor = new ScaledResolution(mc).getScaleFactor();

		int x = left ? 0 : pageX * factor;

		int width = right ? mc.displayWidth - x : book.getPageWidth() * factor;
		int height = mc.displayHeight;

		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(x, 0, width, height);

		GlStateManager.translate(pageX - elementWidth, pageY + y, 0);

		GlStateManager.pushMatrix();
		element.drawElement(mouseX + elementWidth, mouseY - y, partialTicks);
		GlStateManager.popMatrix();

		GL11.glDisable(GL11.GL_SCISSOR_TEST);

		if (mouseX >= 0 && mouseX < book.getPageWidth() && mouseY >= 0 && mouseY < book.getPageHeight()) {
			GlStateManager.pushMatrix();
			element.drawForeground(mouseX + elementWidth, mouseY - y, partialTicks);
			GlStateManager.popMatrix();
		}
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		element.mouseClicked(mouseX + elementWidth, mouseY - y, mouseButton);
	}

}
