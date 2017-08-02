package ftgumod.client.gui.book;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IBook {

	int getPageWidth();

	int getPageHeight();

}
