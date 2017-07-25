package ftgumod.minetweaker;

import ftgumod.minetweaker.util.BaseCollection;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceAdd;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceRemove;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.TechnologyHandler.PAGE;
import minetweaker.MineTweakerAPI;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Set;

@ZenClass("mods.ftgu.Page")
public class Page {

	static final String name = FTGUTweaker.name + " Page";

	@ZenMethod
	public static void addPage(String name) {
		MineTweakerAPI.apply(new Add(new PAGE(name)));
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

	private static class Add extends BaseInterfaceAdd<PAGE> {

		private final PAGE page;

		private Add(PAGE page) {
			super(name, page, new BaseCollection<>(PAGE.pages));
			this.page = page;
		}

		@Override
		protected String getRecipeInfo(PAGE page) {
			return "<page:" + page.name + ">";
		}

		@Override
		public void undo() {
			super.undo();
			TechnologyHandler.technologies.remove(page);
		}

	}

	private static class Remove extends BaseInterfaceRemove<PAGE> {

		private final PAGE page;
		private Set<Technology> undo;

		private Remove(PAGE page) {
			super(name, page, new BaseCollection<>(PAGE.pages));
			this.page = page;
		}

		@Override
		protected String getRecipeInfo(PAGE page) {
			return "<page:" + page.name + ">";
		}

		@Override
		public void apply() {
			super.apply();
			undo = TechnologyHandler.technologies.remove(page);
		}

		@Override
		public void undo() {
			super.undo();
			if (undo != null)
				TechnologyHandler.technologies.put(page, undo);
		}

	}

}
