package ftgumod;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.item.ItemStack;

public class Decipher {

	public DecipherGroup[] unlock = new DecipherGroup[9];
	public final List<DecipherGroup> list;

	public Decipher(DecipherGroup... unlock) {
		list = new LinkedList<DecipherGroup>(Arrays.asList(unlock));
	}

	public static class DecipherGroup {

		public final List<ItemStack> unlock;
		public final List<Integer> slots;

		public DecipherGroup(Object unlock, Integer... slots) {
			this.unlock = TechnologyUtil.toItems(TechnologyUtil.toItem(unlock));
			this.slots = Arrays.asList(slots);
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
