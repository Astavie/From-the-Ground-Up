package ftgumod;

import java.util.List;
import net.minecraft.item.ItemStack;
import scala.actors.threadpool.Arrays;

public class Decipher {

	public final DecipherGroup[] unlock = new DecipherGroup[9];
	public final List<DecipherGroup> list;

	public Decipher(DecipherGroup... unlock) {
		for (DecipherGroup d : unlock) {
			for (int i : d.slots) {
				this.unlock[i] = d;
			}
		}
		list = Arrays.asList(unlock);
	}

	public static class DecipherGroup {

		public final List<ItemStack> unlock;
		public final List<Integer> slots;

		public DecipherGroup(Object unlock, Integer... slots) {
			this.unlock = TechnologyUtil.toItems(TechnologyUtil.toItem(unlock));
			this.slots = Arrays.asList(slots);
		}

	}

}
