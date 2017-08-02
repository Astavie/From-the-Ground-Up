package ftgumod.client.gui.book;

import ftgumod.client.gui.book.content.IPageContent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public interface IBook {

	List<IPageContent> getContent();

	int getPageWidth();

	int getPageHeight();

}
