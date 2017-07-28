package ftgumod.compat.crafttweaker.util;

import java.util.Collection;

public class CollectionBuilder<T> implements IRecipeBuilder<T> {

	private final Collection<T> list;

	public CollectionBuilder(Collection<T> list) {
		this.list = list;
	}

	@Override
	public void add(T recipe) {
		list.add(recipe);
	}

	@Override
	public void remove(T recipe) {
		list.remove(recipe);
	}

}
