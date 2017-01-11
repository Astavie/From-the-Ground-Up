package ftgumod;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.item.ItemStack;

public class Decipher {

	public DecipherGroup[] unlock = new DecipherGroup[9];
	public final Set<DecipherGroup> list;

	public Decipher(DecipherGroup... unlock) {
		list = new LinkedHashSet<DecipherGroup>(Arrays.asList(unlock));
	}

	public static class DecipherGroup {

		public final List<ItemStack> unlock;
		public final Set<Integer> slots;

		public DecipherGroup(Object unlock, Integer... slots) {
			this.unlock = TechnologyUtil.toItems(TechnologyUtil.toItem(unlock));
			this.slots = new LinkedHashSet<Integer>(Arrays.asList(slots));
		}

	}

	public void recalculateSlots() {
		unlock = new DecipherGroup[9];
		for (DecipherGroup d : list) {
			for (int i : d.slots) {
				this.unlock[i] = d;
			}
		}
	}

}
