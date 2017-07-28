package ftgumod;

import com.google.common.primitives.Ints;
import ftgumod.technology.TechnologyUtil;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class Decipher {

	public final Set<DecipherGroup> list;
	public DecipherGroup[] unlock = new DecipherGroup[9];

	public Decipher(DecipherGroup... unlock) {
		list = new LinkedHashSet<>(Arrays.asList(unlock));
		recalculateSlots();
	}

	public void recalculateSlots() {
		unlock = new DecipherGroup[9];
		for (DecipherGroup d : list)
			for (int i : d.slots)
				this.unlock[i] = d;
	}

	public static class DecipherGroup {

		public final ItemList unlock;
		public final Set<Integer> slots;

		public DecipherGroup(Object unlock, int... slots) {
			this.unlock = new ItemList(TechnologyUtil.toItem(unlock));
			this.slots = new LinkedHashSet<>(Ints.asList(slots));
		}

	}

}
