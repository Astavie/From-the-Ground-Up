package ftgumod.compat.crafttweaker.util;

import crafttweaker.IAction;

public abstract class Action<T> implements IAction {

	protected final IRecipeBuilder<T> builder;
	protected final T recipe;

	protected Action(T recipe, IRecipeBuilder<T> builder) {
		this.recipe = recipe;
		this.builder = builder;
	}

	public static abstract class ActionAdd<T> extends Action<T> {

		protected ActionAdd(T recipe, IRecipeBuilder<T> base) {
			super(recipe, base);
		}

		@Override
		public void apply() {
			builder.add(recipe);
		}

	}

	public static abstract class ActionRemove<T> extends Action<T> {

		protected ActionRemove(T recipe, IRecipeBuilder<T> base) {
			super(recipe, base);
		}

		@Override
		public void apply() {
			builder.remove(recipe);
		}

	}

}
