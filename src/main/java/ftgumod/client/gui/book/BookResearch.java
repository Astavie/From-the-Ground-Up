package ftgumod.client.gui.book;

import ftgumod.FTGU;
import ftgumod.client.gui.book.content.IPageContent;
import ftgumod.client.gui.book.element.PageElementText;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BookResearch implements IBook {

	@Override
	public List<IPageContent> getContent() {
		return Collections.singletonList((book, elements) -> {
			TextComponentString title = new TextComponentString("Insert Title Here");
			title.getStyle().setBold(true);

			elements.add(new PageElementText(book, title, PageElementText.Alignment.LEFT, 1.2F, false));
			elements.add(new PageElementText(book, new TextComponentString("Paragraph 1\nIncluding new lines!"), PageElementText.Alignment.LEFT, 1.0F, false));
			elements.add(new PageElementText(book, new TextComponentString("Paragraph 2"), PageElementText.Alignment.LEFT, 1.0F, false));
		});
	}

	@Override
	public ResourceLocation getTexture() {
		return new ResourceLocation(FTGU.MODID, "textures/gui/book/research_book.png");
	}

	@Override
	public int getWidthLeft() {
		return 206;
	}

	@Override
	public int getHeight() {
		return 200;
	}

	@Override
	public int getPageXLeft() {
		return 16;
	}

	@Override
	public int getPageY() {
		return 16;
	}

	@Override
	public int getWidthRight() {
		return getWidthLeft();
	}

	@Override
	public int getPageXRight() {
		return getWidthLeft() + 1;
	}

	@Override
	public int getPageWidth() {
		return getWidthLeft() - getPageXLeft() - 1;
	}

	@Override
	public int getPageHeight() {
		return getHeight() - (2 * getPageY());
	}

}
