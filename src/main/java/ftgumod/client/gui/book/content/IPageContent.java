package ftgumod.client.gui.book.content;

import ftgumod.client.gui.book.GuiBook;
import ftgumod.client.gui.book.element.IPageElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public interface IPageContent {

	void build(GuiBook book, List<IPageElement> elements);

}
