package ftgumod.client.gui.book.element;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IBookElement {

	int getHeight();

	int getMargin();

	int getPageWidth();

	void drawElement(int mouseX, int mouseY, float partialTicks);

	void drawForeground(int mouseX, int mouseY, float partialTicks);

	void mouseClicked(int mouseX, int mouseY, int mouseButton);

	enum Alignment {

		LEFT(0.0F), CENTER(0.5F), RIGHT(1.0F);

		private final float modifier;

		Alignment(float modifier) {
			this.modifier = modifier;
		}

		public float getPosition(float width) {
			return width * modifier;
		}

	}

}
