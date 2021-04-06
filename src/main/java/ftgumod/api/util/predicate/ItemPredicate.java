package ftgumod.api.util.predicate;

import java.util.function.Function;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class ItemPredicate extends Ingredient {

	public ItemPredicate(ItemStack... stacks) {
		super(stacks);
	}

	public ItemStack getDisplayStack() {
		ItemStack[] matching = getMatchingStacks();
		if (matching.length > 0)
			return matching[0];
		return ItemStack.EMPTY;
	}

	public static ItemPredicate getAsPredicate(Ingredient ingredient) {
		if (ingredient instanceof ItemPredicate)
			return (ItemPredicate) ingredient;
		return new ItemIngredient(ingredient);
	}

	public interface Factory extends Function<JsonObject, ItemPredicate>, IIngredientFactory {

		@Nonnull
		@Override
		default Ingredient parse(JsonContext context, JsonObject json) {
			return apply(json);
		}

		default Function<JsonObject, net.minecraft.advancements.critereon.ItemPredicate> andThen() {
			return andThen(i -> new net.minecraft.advancements.critereon.ItemPredicate() {
				@Override
				public boolean test(ItemStack item) {
					return i.test(item);
				}
			});
		}

	}

}
