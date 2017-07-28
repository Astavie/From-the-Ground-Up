package ftgumod.crafttweaker.util;

import java.util.Collection;

public class CollectionsBuilder<T> implements IRecipeBuilder<Collection<T>> {

	private final Collection<T> list;

	public CollectionsBuilder(Collection<T> list) {
		this.list = list;
	}

	@Override
	public void add(Collection<T> recipe) {
		list.addAll(recipe);
	}

	@Override
	public void remove(Collection<T> recipe) {
		list.removeAll(recipe);
	}

}
