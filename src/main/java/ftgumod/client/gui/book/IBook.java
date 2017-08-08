package ftgumod.client.gui.book;

import ftgumod.client.gui.book.content.IBookContent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public interface IBook {

	List<IBookContent> getContent();

	ResourceLocation getTexture();

	int getWidthLeft();

	int getWidthRight();

	int getHeight();

	int getPageXLeft();

	int getPageXRight();

	int getPageY();

	int getPageWidth();

	int getPageHeight();

}
