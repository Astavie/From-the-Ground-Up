package ftgumod.minetweaker.util;

import java.util.Collection;

public class BaseCollection<T> implements IBaseInterface<T> {

	private final Collection<T> list;

	public BaseCollection(Collection<T> list) {
		this.list = list;
	}

	@Override
	public boolean add(T recipe) {
		return list.add(recipe);
	}

	@Override
	public boolean remove(T recipe) {
		return list.remove(recipe);
	}

}
