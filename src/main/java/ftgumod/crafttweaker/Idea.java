package ftgumod.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import ftgumod.ItemList;
import ftgumod.crafttweaker.util.Action.ActionAdd;
import ftgumod.crafttweaker.util.Action.ActionRemove;
import ftgumod.crafttweaker.util.CollectionBuilder;
import ftgumod.crafttweaker.util.InputHelper;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.recipe.IdeaRecipe;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.ftgu.Idea")
public class Idea {

	@ZenMethod
	public static void addIdea(String tech, IIngredient[] recipe) {
		Technology t = TechnologyHandler.getTechnology(tech);
		if (t == null) {
			CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] Technology " + tech + " does not exist. Command ignored!");
			return;
		}

		List<ItemList> list = new ArrayList<>();
		for (Object o : InputHelper.toObjects(recipe))
			if (o != null)
				list.add(new ItemList(o));

		CraftTweakerAPI.apply(new Add(new IdeaRecipe(list, t)));
	}

	@ZenMethod
	public static void removeIdea(String tech) {
		IdeaRecipe i = TechnologyHandler.getIdea(tech);
		if (i == null) {
			CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No idea recipe found for " + tech + ". Command ignored!");
			return;
		}

		CraftTweakerAPI.apply(new Remove(i));
	}

	private static class Add extends ActionAdd<IdeaRecipe> {

		private Add(IdeaRecipe tech) {
			super(tech, new CollectionBuilder<>(TechnologyHandler.ideas));
		}

		@Override
		public String describe() {
			return "[" + FTGUTweaker.name + "] Adding idea recipe for " + recipe.output.getUnlocalizedName();
		}

	}

	private static class Remove extends ActionRemove<IdeaRecipe> {

		private Remove(IdeaRecipe tech) {
			super(tech, new CollectionBuilder<>(TechnologyHandler.ideas));
		}

		@Override
		public String describe() {
			return "[" + FTGUTweaker.name + "] Removing idea recipe for " + recipe.output.getUnlocalizedName();
		}

	}

}
