package ftgumod.compat.crafttweaker.util;

import crafttweaker.IAction;
import ftgumod.compat.crafttweaker.FTGUTweaker;

import java.util.Collection;

public class ActionClear<T> implements IAction {

	private final String name;
	private final Collection<T> collection;

	public ActionClear(String name, Collection<T> collection) {
		this.name = name;
		this.collection = collection;
	}

	@Override
	public void apply() {
		collection.clear();
	}

	@Override
	public String describe() {
		return "[" + FTGUTweaker.name + "] Clearing all " + name;
	}

}
