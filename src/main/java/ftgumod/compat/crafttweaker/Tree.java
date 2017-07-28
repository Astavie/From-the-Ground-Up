package ftgumod.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import ftgumod.compat.crafttweaker.util.Action.ActionAdd;
import ftgumod.compat.crafttweaker.util.Action.ActionRemove;
import ftgumod.compat.crafttweaker.util.CollectionBuilder;
import ftgumod.technology.TechnologyHandler;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.ftgu.Tree")
public class Tree {

	@ZenMethod
	public static void addTree(String name) {
		CraftTweakerAPI.apply(new Add(new TechnologyHandler.Tree(name)));
	}

	@ZenMethod
	public static void removeTree(String name) {
		TechnologyHandler.Tree tree = TechnologyHandler.Tree.get(name);
		if (tree == null) {
			CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] Tree " + name + " does not exist. Command ignored!");
			return;
		}
		CraftTweakerAPI.apply(new Remove(tree));
	}

	private static class Add extends ActionAdd<TechnologyHandler.Tree> {

		private Add(TechnologyHandler.Tree tree) {
			super(tree, new CollectionBuilder<>(TechnologyHandler.Tree.TREES));
		}

		@Override
		public String describe() {
			return "[" + FTGUTweaker.name + "] Adding new page " + recipe.name;
		}
	}

	private static class Remove extends ActionRemove<TechnologyHandler.Tree> {

		private Remove(TechnologyHandler.Tree tree) {
			super(tree, new CollectionBuilder<>(TechnologyHandler.Tree.TREES));
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
