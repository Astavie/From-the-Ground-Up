package ftgumod.api.util.predicate;

import com.google.gson.JsonObject;
import ftgumod.api.util.JsonContextPublic;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class ItemLambda extends ItemPredicate implements ItemPredicate.Factory<ItemLambda> {

	public static final ItemPredicate EMPTY = new ItemLambda(ItemStack::isEmpty);

	private final Predicate<ItemStack> predicate;

	public ItemLambda(Predicate<ItemStack> predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean apply(ItemStack stack) {
		return predicate.test(stack);
	}

	@Override
	public ItemLambda deserialize(JsonObject object, JsonContextPublic context) {
		return this;
	}

}
