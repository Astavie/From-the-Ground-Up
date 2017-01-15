package ftgumod.minetweaker;

import ftgumod.minetweaker.util.BaseCollection;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceAdd;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceRemove;
import ftgumod.technology.TechnologyHandler.PAGE;
import minetweaker.MineTweakerAPI;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.ftgu.Page")
public class Page {

	protected static final String name = FTGUTweaker.name + " Page";

	@ZenMethod
	public static void addPage(String name) {
		MineTweakerAPI.apply(new Add(new PAGE(name)));
	}

	private static class Add extends BaseInterfaceAdd<PAGE> {

		protected Add(PAGE page) {
			super(name, page, new BaseCollection(PAGE.pages));
		}

		@Override
		protected String getRecipeInfo(PAGE page) {
			return "<page:" + page.name + ">";
		}

	}

	@ZenMethod
	public static void removePage(String name) {
		PAGE page = PAGE.get(name);
		if (page == null) {
			MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + Page.name + " found for " + name + ". Command ignored!");
			return;
		}
		MineTweakerAPI.apply(new Remove(page));
	}

	private static class Remove extends BaseInterfaceRemove<PAGE> {

		protected Remove(PAGE page) {
			super(name, page, new BaseCollection(PAGE.pages));
		}

		@Override
		protected String getRecipeInfo(PAGE page) {
			return "<page:" + page.name + ">";
		}

	}

}
