package ftgumod;

import com.google.common.primitives.Ints;
import ftgumod.util.BlockPredicate;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class Decipher {

	public final Set<DecipherGroup> list;
	public final DecipherGroup[] unlock = new DecipherGroup[9];

	public Decipher(DecipherGroup... unlock) {
		list = new LinkedHashSet<>(Arrays.asList(unlock));
		for (DecipherGroup d : list)
			for (int i : d.slots)
				this.unlock[i] = d;
	}

	public static class DecipherGroup {

		public final BlockPredicate unlock;
		public final Set<Integer> slots;

		public DecipherGroup(BlockPredicate unlock, int... slots) {
			this.unlock = unlock;
			this.slots = new LinkedHashSet<>(Ints.asList(slots));
		}

	}

}
