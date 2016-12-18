package ftgumod.minetweaker.util;

import java.util.List;

public class BaseList<T> implements IBaseInterface<T> {

	protected final List<T> list;

	public BaseList(List<T> list) {
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
