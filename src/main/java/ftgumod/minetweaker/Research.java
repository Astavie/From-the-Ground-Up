package ftgumod.minetweaker;

import ftgumod.minetweaker.util.BaseCollection;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceAdd;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceRemove;
import ftgumod.minetweaker.util.InputHelper;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.recipe.ResearchRecipe;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.ftgu.Research")
public class Research {

	static final String name = FTGUTweaker.name + " Research";

	@ZenMethod
	public static void addResearch(String tech, IIngredient[][] recipe) {
		Technology t = TechnologyHandler.getTechnology(tech);
		if (t == null) {
			MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + ftgumod.minetweaker.Technology.name + " found for " + tech + ". Command ignored!");
			return;
		}

		MineTweakerAPI.apply(new Add(new ResearchRecipe(t, InputHelper.toShapedObjects(recipe))));
	}

	@ZenMethod
	public static void removeResearch(String tech) {
		ResearchRecipe i = TechnologyHandler.getResearch(tech);
		if (i == null) {
			MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + name + " found for " + tech + ". Command ignored!");
			return;
		}

		MineTweakerAPI.apply(new Remove(i));
	}

	private static class Add extends BaseInterfaceAdd<ResearchRecipe> {

		private Add(ResearchRecipe tech) {
			super(name, tech, new BaseCollection<>(TechnologyHandler.researches));
		}

		@Override
		protected String getRecipeInfo(ResearchRecipe recipe) {
			return "<tech:" + recipe.output.getUnlocalizedName() + ">";
		}

	}

	private static class Remove extends BaseInterfaceRemove<ResearchRecipe> {

		private Remove(ResearchRecipe tech) {
			super(name, tech, new BaseCollection<>(TechnologyHandler.researches));
		}

		@Override
		protected String getRecipeInfo(ResearchRecipe recipe) {
			return "<tech:" + recipe.output.getUnlocalizedName() + ">";
		}

	}

}
