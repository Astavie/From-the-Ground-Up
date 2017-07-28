package ftgumod.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import ftgumod.compat.crafttweaker.util.Action.ActionAdd;
import ftgumod.compat.crafttweaker.util.Action.ActionRemove;
import ftgumod.compat.crafttweaker.util.CollectionBuilder;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.TechnologyHandler.PAGE;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.ftgu.Page")
public class Page {

	@ZenMethod
	public static void addPage(String name) {
		CraftTweakerAPI.apply(new Add(new PAGE(name)));
	}

	@ZenMethod
	public static void removePage(String name) {
		PAGE page = PAGE.get(name);
		if (page == null) {
			CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] Page " + name + " does not exist. Command ignored!");
			return;
		}
		CraftTweakerAPI.apply(new Remove(page));
	}

	private static class Add extends ActionAdd<PAGE> {

		private Add(PAGE page) {
			super(page, new CollectionBuilder<>(PAGE.pages));
		}

		@Override
		public String describe() {
			return "[" + FTGUTweaker.name + "] Adding new page " + recipe.name;
		}
	}

	private static class Remove extends ActionRemove<PAGE> {

		private Remove(PAGE page) {
			super(page, new CollectionBuilder<>(PAGE.pages));
		}

		@Override
		public void apply() {
			super.apply();
			TechnologyHandler.technologies.remove(recipe);
		}

		@Override
		public String describe() {
			return "[" + FTGUTweaker.name + "] Removing page " + recipe.name;
		}

	}

}
