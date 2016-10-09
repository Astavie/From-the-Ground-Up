package ftgumod.minetweaker;

import java.util.ArrayList;
import java.util.List;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import ftgumod.TechnologyHandler;
import ftgumod.TechnologyHandler.PAGE;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceAdd;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceRemove;
import ftgumod.minetweaker.util.IBaseInterface;
import ftgumod.minetweaker.util.InputHelper;

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
		ftgumod.Technology p = null;
		if (prev != null) {
			p = TechnologyHandler.getTechnology(prev);
			if (p == null) {
				MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + Technology.name + " found for " + prev + ". Command ignored!");
				return;
			}
		}

		if (secret != null) {
			List<ftgumod.Technology> ls1 = new ArrayList<ftgumod.Technology>();
			for (String s : secret) {
				ftgumod.Technology t = TechnologyHandler.getTechnology(s);
				if (t != null)
					ls1.add(t);
				else
					MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + Technology.name + " found for " + s + " in " + Technology.name + " " + p.getLocalisedName() + ". Secret unlock ignored!");
			}

			ftgumod.Technology[] ls2 = new ftgumod.Technology[ls1.size()];

			for (int i = 0; i < ls1.size(); i++) {
				ls2[i] = ls1.get(i);
			}

			MineTweakerAPI.apply(new Add(new ftgumod.Technology(PAGE.get(page), p, ls2, InputHelper.getStack(icon), hide, x, y, name, InputHelper.toObjects(item))));
		} else {
			MineTweakerAPI.apply(new Add(new ftgumod.Technology(PAGE.get(page), p, null, InputHelper.getStack(icon), hide, x, y, name, InputHelper.toObjects(item))));
		}
	}

	private static class Add extends BaseInterfaceAdd<ftgumod.Technology> {

		protected Add(ftgumod.Technology tech) {
			super(name, tech, new BaseTechnology());
		}

		@Override
		protected String getRecipeInfo(ftgumod.Technology recipe) {
			return "<tech:" + recipe.getUnlocalisedName() + ">";
		}

	}

	@ZenMethod
	public static void removeTechnology(String tech) {
		ftgumod.Technology p = TechnologyHandler.getTechnology(tech);
		if (p == null) {
			MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + name + " found for " + tech + ". Command ignored!");
			return;
		}

		MineTweakerAPI.apply(new Remove(p));
	}

	private static class Remove extends BaseInterfaceRemove<ftgumod.Technology> {

		protected Remove(ftgumod.Technology tech) {
			super(name, tech, new BaseTechnology());
		}

		@Override
		protected String getRecipeInfo(ftgumod.Technology recipe) {
			return "<tech:" + recipe.getUnlocalisedName() + ">";
		}

	}

	private static class BaseTechnology implements IBaseInterface<ftgumod.Technology> {

		@Override
		public boolean add(ftgumod.Technology recipe) {
			try {
				TechnologyHandler.registerTechnology(recipe);
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean remove(ftgumod.Technology recipe) {
			TechnologyHandler.ideas.remove(TechnologyHandler.getIdea(recipe));
			TechnologyHandler.researches.remove(TechnologyHandler.getResearch(recipe));

			TechnologyHandler.locked.remove(recipe);
			return TechnologyHandler.technologies.remove(recipe);
		}

	}

}
