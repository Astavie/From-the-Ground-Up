package ftgumod.minetweaker.util;

import ftgumod.minetweaker.FTGUTweaker;
import minetweaker.IUndoableAction;

import java.util.Collection;
import java.util.HashSet;

public class ClearCollection<T> implements IUndoableAction {

	private final String name;
	private final Collection<T> collection;
	private Collection<T> removed = new HashSet<>();

	public ClearCollection(String name, Collection<T> collection) {
		this.name = name;
		this.collection = collection;
	}

	@Override
	public void apply() {
		removed = new HashSet<>(collection);
		collection.clear();
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public String describe() {
		return "[" + FTGUTweaker.name + "] Clearing all " + name + " Recipe(s)";
	}

	@Override
	public String describeUndo() {
		return "[" + FTGUTweaker.name + "] Re-adding all " + name + " Recipe(s)";
	}

	@Override
	public Object getOverrideKey() {
		return null;
	}

	@Override
	public void undo() {
		collection.addAll(removed);
		removed = new HashSet<>();
	}

}
