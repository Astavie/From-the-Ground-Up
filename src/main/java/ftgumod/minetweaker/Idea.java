package ftgumod.minetweaker;

import ftgumod.ItemList;
import ftgumod.minetweaker.util.BaseCollection;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceAdd;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceRemove;
import ftgumod.minetweaker.util.InputHelper;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.recipe.IdeaRecipe;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.ftgu.Idea")
public class Idea {

	static final String name = FTGUTweaker.name + " Idea";

	@ZenMethod
	public static void addIdea(String tech, IIngredient[] recipe) {
		Technology t = TechnologyHandler.getTechnology(tech);
		if (t == null) {
			MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + ftgumod.minetweaker.Technology.name + " found for " + tech + ". Command ignored!");
			return;
		}

		List<ItemList> list = new ArrayList<>();
		for (Object o : InputHelper.toObjects(recipe))
			if (o != null)
				list.add(new ItemList(o));

		MineTweakerAPI.apply(new Add(new IdeaRecipe(list, t)));
	}

	@ZenMethod
	public static void removeIdea(String tech) {
		IdeaRecipe i = TechnologyHandler.getIdea(tech);
		if (i == null) {
			MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + name + " found for " + tech + ". Command ignored!");
			return;
		}

		MineTweakerAPI.apply(new Remove(i));
	}

	private static class Add extends BaseInterfaceAdd<IdeaRecipe> {

		private Add(IdeaRecipe tech) {
			super(name, tech, new BaseCollection<>(TechnologyHandler.ideas));
		}

		@Override
		protected String getRecipeInfo(IdeaRecipe recipe) {
			return "<tech:" + recipe.output.getUnlocalizedName() + ">";
		}

	}

	private static class Remove extends BaseInterfaceRemove<IdeaRecipe> {

		private Remove(IdeaRecipe tech) {
			super(name, tech, new BaseCollection<>(TechnologyHandler.ideas));
		}

		@Override
		protected String getRecipeInfo(IdeaRecipe recipe) {
			return "<tech:" + recipe.output.getUnlocalizedName() + ">";
		}

	}

}
