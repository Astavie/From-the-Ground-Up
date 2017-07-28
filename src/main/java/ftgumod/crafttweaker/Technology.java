package ftgumod.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import ftgumod.ItemList;
import ftgumod.crafttweaker.util.Action.ActionAdd;
import ftgumod.crafttweaker.util.Action.ActionRemove;
import ftgumod.crafttweaker.util.ActionClear;
import ftgumod.crafttweaker.util.CollectionsBuilder;
import ftgumod.crafttweaker.util.IRecipeBuilder;
import ftgumod.crafttweaker.util.InputHelper;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.TechnologyHandler.PAGE;
import ftgumod.technology.TechnologyUtil;
import ftgumod.technology.recipe.IdeaRecipe;
import ftgumod.technology.recipe.ResearchRecipe;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ZenClass("mods.ftgu.Technology")
public class Technology {

	@ZenMethod
	public static void addTechnology(String page, String prev, IItemStack icon, int x, int y, String name, IIngredient[] item) {
		addTechnology(page, prev, null, icon, false, x, y, name, item);
	}

	@ZenMethod
	public static void addTechnology(String page, String prev, IItemStack icon, boolean hide, int x, int y, String name, IIngredient[] item) {
		addTechnology(page, prev, null, icon, hide, x, y, name, item);
	}

	@ZenMethod
	public static void addTechnology(String page, String prev, String[] secret, IItemStack icon, int x, int y, String name, IIngredient[] item) {
		addTechnology(page, prev, secret, icon, false, x, y, name, item);
	}

	@ZenMethod
	public static void addTechnology(String page, String prev, String[] secret, IItemStack icon, boolean hide, int x, int y, String name, IIngredient[] item) {
		ftgumod.technology.Technology p = null;
		if (prev != null) {
			p = TechnologyHandler.getTechnology(prev);
			if (p == null) {
				CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] Technology " + prev + " does not exist. Command ignored!");
				return;
			}
		}

		if (secret != null) {
			List<ftgumod.technology.Technology> ls1 = new ArrayList<>();
			for (String s : secret) {
				ftgumod.technology.Technology t = TechnologyHandler.getTechnology(s);
				if (t != null)
					ls1.add(t);
				else
					CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] Technology " + s + " does not exist. Secret unlock ignored!");
			}

			ftgumod.technology.Technology[] ls2 = new ftgumod.technology.Technology[ls1.size()];

			for (int i = 0; i < ls1.size(); i++) {
				ls2[i] = ls1.get(i);
			}

			CraftTweakerAPI.apply(new AddTech(new ftgumod.technology.Technology(PAGE.get(page), p, ls2, InputHelper.getStack(icon), hide, x, y, name, InputHelper.toObjects(item))));
		} else {
			CraftTweakerAPI.apply(new AddTech(new ftgumod.technology.Technology(PAGE.get(page), p, null, InputHelper.getStack(icon), hide, x, y, name, InputHelper.toObjects(item))));
		}
	}

	@ZenMethod
	public static void removeTechnology(String tech) {
		ftgumod.technology.Technology p = TechnologyHandler.getTechnology(tech);
		if (p == null) {
			CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] Technology " + tech + " does not exist. Command ignored!");
			return;
		}

		CraftTweakerAPI.apply(new RemoveTech(p));
	}

	@ZenMethod
	public static void addItems(String tech, IIngredient[] item) {
		ftgumod.technology.Technology p = TechnologyHandler.getTechnology(tech);
		if (p == null) {
			CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] Technology " + tech + " does not exist. Command ignored!");
			return;
		}

		List<ItemList> list = new ArrayList<>();
		for (Object o : InputHelper.toObjects(item))
			list.add(new ItemList(TechnologyUtil.toItem(o)));

		CraftTweakerAPI.apply(new AddItems(list, p));
	}

	@ZenMethod
	public static void clearItems(String tech) {
		ftgumod.technology.Technology p = TechnologyHandler.getTechnology(tech);
		if (p == null) {
			CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] Technology " + tech + " does not exist. Command ignored!");
			return;
		}

		CraftTweakerAPI.apply(new ActionClear<>(p.getUnlocalizedName() + " unlocks", p.getUnlock()));
	}

	private static class AddTech extends ActionAdd<ftgumod.technology.Technology> {

		protected AddTech(ftgumod.technology.Technology tech) {
			super(tech, new TechnologyBuilder());
		}

		@Override
		public String describe() {
			return "[" + FTGUTweaker.name + "] Adding new technology " + recipe.getUnlocalizedName();
		}
	}

	private static class RemoveTech extends ActionRemove<ftgumod.technology.Technology> {

		protected RemoveTech(ftgumod.technology.Technology tech) {
			super(tech, new TechnologyBuilder());
		}

		@Override
		public String describe() {
			return "[" + FTGUTweaker.name + "] Removing technology " + recipe.getUnlocalizedName();
		}

	}

	private static class TechnologyBuilder implements IRecipeBuilder<ftgumod.technology.Technology> {

		@Override
		public void add(ftgumod.technology.Technology recipe) {
			TechnologyHandler.registerTechnology(recipe);
		}

		@Override
		public void remove(ftgumod.technology.Technology recipe) {
			if (!TechnologyHandler.technologies.get(recipe.getPage()).remove(recipe))
				return;

			IdeaRecipe idea = TechnologyHandler.getIdea(recipe);
			ResearchRecipe research = TechnologyHandler.getResearch(recipe);

			if (idea != null)
				TechnologyHandler.ideas.remove(idea);
			if (research != null)
				TechnologyHandler.researches.remove(research);
		}

	}

	private static class AddItems extends ActionAdd<Collection<ItemList>> {

		private final ftgumod.technology.Technology tech;

		protected AddItems(Collection<ItemList> recipes, ftgumod.technology.Technology tech) {
			super(recipes, new CollectionsBuilder<>(tech.getUnlock()));
			this.tech = tech;
		}

		@Override
		public String describe() {
			return "[" + FTGUTweaker.name + "] Adding item unlocks to " + tech.getUnlocalizedName();
		}

	}

}
