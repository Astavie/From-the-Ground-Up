package ftgumod.crafttweaker.util;

import crafttweaker.IAction;

public abstract class Action<T> implements IAction {

	protected final IRecipeBuilder<T> base;
	protected final T recipe;

	protected Action(T recipe, IRecipeBuilder<T> base) {
		this.recipe = recipe;
		this.base = base;
	}

	public static abstract class ActionAdd<T> extends Action<T> {

		protected ActionAdd(T recipe, IRecipeBuilder<T> base) {
			super(recipe, base);
		}

		@Override
		public void apply() {
			base.add(recipe);
		}

	}

	public static abstract class ActionRemove<T> extends Action<T> {

		protected ActionRemove(T recipe, IRecipeBuilder<T> base) {
			super(recipe, base);
		}

		@Override
		public void apply() {
			base.remove(recipe);
		}

	}

}
