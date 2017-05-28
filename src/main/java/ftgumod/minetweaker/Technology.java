package ftgumod.minetweaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ftgumod.ItemList;
import ftgumod.minetweaker.util.BaseCollection;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceAdd;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceRemove;
import ftgumod.minetweaker.util.IBaseInterface;
import ftgumod.minetweaker.util.InputHelper;
import ftgumod.minetweaker.util.ClearCollection;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.TechnologyHandler.PAGE;
import ftgumod.technology.TechnologyUtil;
import ftgumod.technology.recipe.IdeaRecipe;
import ftgumod.technology.recipe.ResearchRecipe;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.ftgu.Technology")
public class Technology {

	protected static final String name = FTGUTweaker.name + " Technology";

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
				MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + Technology.name + " found for " + prev + ". Command ignored!");
				return;
			}
		}

		if (secret != null) {
			List<ftgumod.technology.Technology> ls1 = new ArrayList<ftgumod.technology.Technology>();
			for (String s : secret) {
				ftgumod.technology.Technology t = TechnologyHandler.getTechnology(s);
				if (t != null)
					ls1.add(t);
				else
					MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + Technology.name + " found for " + s + " in " + Technology.name + " " + p.getLocalizedName(false) + ". Secret unlock ignored!");
			}

			ftgumod.technology.Technology[] ls2 = new ftgumod.technology.Technology[ls1.size()];

			for (int i = 0; i < ls1.size(); i++) {
				ls2[i] = ls1.get(i);
			}

			MineTweakerAPI.apply(new AddTech(new ftgumod.technology.Technology(PAGE.get(page), p, ls2, InputHelper.getStack(icon), hide, x, y, name, InputHelper.toObjects(item))));
		} else {
			MineTweakerAPI.apply(new AddTech(new ftgumod.technology.Technology(PAGE.get(page), p, null, InputHelper.getStack(icon), hide, x, y, name, InputHelper.toObjects(item))));
		}
	}

	private static class AddTech extends BaseInterfaceAdd<ftgumod.technology.Technology> {

		protected AddTech(ftgumod.technology.Technology tech) {
			super(name, tech, new BaseTechnology());
		}

		@Override
		protected String getRecipeInfo(ftgumod.technology.Technology recipe) {
			return "<tech:" + recipe.getUnlocalizedName() + ">";
		}

	}

	@ZenMethod
	public static void removeTechnology(String tech) {
		ftgumod.technology.Technology p = TechnologyHandler.getTechnology(tech);
		if (p == null) {
			MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + name + " found for " + tech + ". Command ignored!");
			return;
		}

		MineTweakerAPI.apply(new RemoveTech(p));
	}

	private static class RemoveTech extends BaseInterfaceRemove<ftgumod.technology.Technology> {

		protected RemoveTech(ftgumod.technology.Technology tech) {
			super(name, tech, new BaseTechnology());
		}

		@Override
		protected String getRecipeInfo(ftgumod.technology.Technology recipe) {
			return "<tech:" + recipe.getUnlocalizedName() + ">";
		}

	}

	private static class BaseTechnology implements IBaseInterface<ftgumod.technology.Technology> {

		private IdeaRecipe idea;
		private ResearchRecipe research;

		@Override
		public boolean add(ftgumod.technology.Technology recipe) {
			if (!TechnologyHandler.registerTechnology(recipe))
				return false;

			if (idea != null)
				TechnologyHandler.registerIdea(idea);
			if (research != null)
				TechnologyHandler.registerResearch(research);

			return true;
		}

		@Override
		public boolean remove(ftgumod.technology.Technology recipe) {
			if (!TechnologyHandler.technologies.remove(recipe))
				return false;

			idea = TechnologyHandler.getIdea(recipe);
			research = TechnologyHandler.getResearch(recipe);

			TechnologyHandler.ideas.remove(idea);
			TechnologyHandler.researches.remove(research);

			return true;
		}

	}

	@ZenMethod
	public static void addItems(String tech, IIngredient[] item) {
		ftgumod.technology.Technology p = TechnologyHandler.getTechnology(tech);
		if (p == null) {
			MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + name + " found for " + tech + ". Command ignored!");
			return;
		}

		List<ItemList> list = new ArrayList<ItemList>();
		for (Object o : InputHelper.toObjects(item))
			list.add(new ItemList(TechnologyUtil.toItem(o)));

		MineTweakerAPI.apply(new AddItems(list, p));
	}

	private static class AddItems extends BaseInterfaceAdd<ItemList> {

		protected AddItems(Collection<ItemList> recipes, ftgumod.technology.Technology tech) {
			super(name + " Unlock", recipes, new BaseCollection<ItemList>(tech.item));
		}

		@Override
		protected String getRecipeInfo(ItemList recipe) {
			return "<item:" + recipe.toString().split(".")[1] + ">";
		}

	}

	@ZenMethod
	public static void clearItems(String tech) {
		ftgumod.technology.Technology p = TechnologyHandler.getTechnology(tech);
		if (p == null) {
			MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + name + " found for " + tech + ". Command ignored!");
			return;
		}

		MineTweakerAPI.apply(new ClearCollection(name + " Unlock", p.item));
	}

}
