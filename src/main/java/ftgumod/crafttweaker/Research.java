package ftgumod.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import ftgumod.crafttweaker.util.Action.ActionAdd;
import ftgumod.crafttweaker.util.Action.ActionRemove;
import ftgumod.crafttweaker.util.CollectionBuilder;
import ftgumod.crafttweaker.util.InputHelper;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.recipe.ResearchRecipe;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.ftgu.Research")
public class Research {

	@ZenMethod
	public static void addResearch(String tech, IIngredient[][] recipe) {
		Technology t = TechnologyHandler.getTechnology(tech);
		if (t == null) {
			CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] Technology " + tech + " does not exist. Command ignored!");
			return;
		}

		CraftTweakerAPI.apply(new Add(new ResearchRecipe(t, InputHelper.toShapedObjects(recipe))));
	}

	@ZenMethod
	public static void removeResearch(String tech) {
		ResearchRecipe i = TechnologyHandler.getResearch(tech);
		if (i == null) {
			CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No research recipe found for " + tech + ". Command ignored!");
			return;
		}

		CraftTweakerAPI.apply(new Remove(i));
	}

	private static class Add extends ActionAdd<ResearchRecipe> {

		private Add(ResearchRecipe tech) {
			super(tech, new CollectionBuilder<>(TechnologyHandler.researches));
		}

		@Override
		public String describe() {
			return "[" + FTGUTweaker.name + "] Adding research recipe for " + recipe.output.getUnlocalizedName();
		}

	}

	private static class Remove extends ActionRemove<ResearchRecipe> {

		private Remove(ResearchRecipe tech) {
			super(tech, new CollectionBuilder<>(TechnologyHandler.researches));
		}

		@Override
		public String describe() {
			return "[" + FTGUTweaker.name + "] Removing research recipe for " + recipe.output.getUnlocalizedName();
		}

	}

}
