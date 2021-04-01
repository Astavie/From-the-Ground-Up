package ftgumod.api.util.predicate;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ItemCompound extends ItemPredicate {

	private final Collection<ItemPredicate> predicates;
	private ItemStack[] matching;

	public ItemCompound(Collection<ItemPredicate> predicates) {
		this.predicates = predicates;
	}

	@Override
	public ItemStack[] getMatchingStacks() {
		if (matching == null) {
			List<ItemStack> tmp = new ArrayList<>();
			for (ItemPredicate child : predicates)
				Collections.addAll(tmp, child.getMatchingStacks());
			matching = tmp.toArray(new ItemStack[tmp.size()]);
		}
		return matching;
	}

	@Override
	public boolean apply(ItemStack itemStack) {
		return predicates.stream().anyMatch(i -> i.test(itemStack));
	}

}
