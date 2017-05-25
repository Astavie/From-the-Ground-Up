package ftgumod.minetweaker.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ftgumod.minetweaker.FTGUTweaker;
import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;

public abstract class BaseInterface<T> implements IUndoableAction {

	private final String name;
	private Collection<T> recipes;
	private final IBaseInterface<T> base;

	protected BaseInterface(String name, Collection<T> recipes, IBaseInterface<T> base) {
		this.name = name;
		this.recipes = recipes;
		this.base = base;
	}

	public BaseInterface(String name, T tech, IBaseInterface<T> base) {
		this.name = name;
		this.recipes = new ArrayList<T>();
		recipes.add(tech);
		this.base = base;
	}

	protected void add() {
		if (recipes.isEmpty())
			return;

		Set<T> successful = new HashSet<T>();

		for (T recipe : recipes) {
			if (recipe != null) {
				if (base.add(recipe)) {
					successful.add(recipe);
				} else {
					MineTweakerAPI.logError(
							"[" + FTGUTweaker.name + "] Error adding " + name + " Recipe for " + getRecipeInfo(recipe));
				}
			} else {
				MineTweakerAPI.logError("[" + FTGUTweaker.name + "] Error adding " + name + " Recipe: null object");
			}
		}

		recipes = successful;
	}

	protected void remove() {
		if (recipes.isEmpty())
			return;

		Set<T> successful = new HashSet<T>();

		for (T recipe : recipes) {
			if (recipe != null) {
				if (base.remove(recipe)) {
					successful.add(recipe);
				} else {
					MineTweakerAPI.logError("[" + FTGUTweaker.name + "] Error removing " + name + " Recipe for "
							+ getRecipeInfo(recipe));
				}
			} else {
				MineTweakerAPI.logError("[" + FTGUTweaker.name + "] Error removing " + name + " Recipe: null object");
			}
		}

		recipes = successful;
	}

	protected String describeAdd() {
		return "[" + FTGUTweaker.name + "] Adding " + recipes.size() + " " + name + " Recipe(s) for " + getRecipeInfo();
	}

	protected String describeRemove() {
		return "[" + FTGUTweaker.name + "] Removing " + recipes.size() + " " + name + " Recipe(s) for "
				+ getRecipeInfo();
	}

	protected String getRecipeInfo() {
		if (!recipes.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (T recipe : recipes)
				if (recipe != null)
					sb.append(getRecipeInfo(recipe)).append(", ");

			if (sb.length() > 0)
				sb.setLength(sb.length() - 2);

			return sb.toString();
		}
		return "Unknown item";
	}

	protected abstract String getRecipeInfo(T recipe);

	@Override
	public boolean canUndo() {
		return !recipes.isEmpty();
	}

	@Override
	public Object getOverrideKey() {
		return null;
	}

	protected boolean equals(T recipe1, T recipe2) {
		if (recipe1 == recipe2) {
			return true;
		}

		if (!recipe1.equals(recipe2)) {
			return false;
		}

		return true;
	}

	public static abstract class BaseInterfaceAdd<T> extends BaseInterface<T> {

		protected BaseInterfaceAdd(String name, Collection<T> recipes, IBaseInterface<T> base) {
			super(name, recipes, base);
		}

		protected BaseInterfaceAdd(String name, T tech, IBaseInterface<T> base) {
			super(name, tech, base);
		}

		@Override
		public void apply() {
			add();
		}

		@Override
		public String describe() {
			return describeAdd();
		}

		@Override
		public String describeUndo() {
			return describeRemove();
		}

		@Override
		public void undo() {
			remove();
		}

	}

	public static abstract class BaseInterfaceRemove<T> extends BaseInterface<T> {

		protected BaseInterfaceRemove(String name, Collection<T> recipes, IBaseInterface<T> base) {
			super(name, recipes, base);
		}

		protected BaseInterfaceRemove(String name, T tech, IBaseInterface<T> base) {
			super(name, tech, base);
		}

		@Override
		public void apply() {
			remove();
		}

		@Override
		public String describe() {
			return describeRemove();
		}

		@Override
		public String describeUndo() {
			return describeAdd();
		}

		@Override
		public void undo() {
			add();
		}

	}

}
