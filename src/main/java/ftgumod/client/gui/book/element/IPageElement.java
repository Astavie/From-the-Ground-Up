package ftgumod.client.gui.book.element;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IPageElement {

	int getHeight();

	@Override
	int hashCode();

	void drawElement(int mouseX, int mouseY, float partialTicks);

}
