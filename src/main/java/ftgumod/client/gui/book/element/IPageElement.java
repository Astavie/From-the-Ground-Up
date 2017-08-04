package ftgumod.client.gui.book.element;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IPageElement {

	int getHeight();

	int getMargin();

	int getPageWidth();

	void drawElement(int mouseX, int mouseY, float partialTicks);

	void drawForeground(int mouseX, int mouseY, float partialTicks);

	void mouseClicked(int mouseX, int mouseY, int mouseButton);

}
