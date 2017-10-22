package ftgumod.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.FTGU;
import ftgumod.api.util.BlockPredicate;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IngredientResearch extends Ingredient {

	public static final IngredientResearch EMPTY = new IngredientResearch(Ingredient.EMPTY, null, null);

	private final Ingredient ingredient;
	private final ITextComponent hint;
	private final Set<BlockPredicate> unlock;

	public IngredientResearch(Ingredient ingredient, @Nullable ITextComponent hint, @Nullable Set<BlockPredicate> unlock) {
		this.ingredient = ingredient;
		this.hint = hint;
		this.unlock = unlock != null ? unlock : Collections.emptySet();
	}

	public static IngredientResearch deserialize(JsonElement element, JsonContext context) {
		Ingredient ingredient = CraftingHelper.getIngredient(element, context);
		if (ingredient == null)
			return null;
		while (element.isJsonArray())
			element = element.getAsJsonArray().get(0);

		JsonObject object = element.getAsJsonObject();

		ITextComponent hint = null;
		if (object.has("hint"))
			hint = FTGU.GSON.fromJson(object.get("hint"), ITextComponent.class);

		Set<BlockPredicate> decipher = new HashSet<>();
		if (object.has("decipher")) {
			JsonElement i = object.get("decipher");
			if (i.isJsonArray())
				for (JsonElement j : i.getAsJsonArray())
					if (j.isJsonObject())
						decipher.add(BlockPredicate.deserialize(j.getAsJsonObject()));
					else
						throw new JsonSyntaxException("Expected decipher inside an array to be an object");
			else if (i.isJsonObject())
				decipher.add(BlockPredicate.deserialize(i.getAsJsonObject()));
			else
				throw new JsonSyntaxException("Expected decipher to be a object or array of objects");
		}

		return new IngredientResearch(ingredient, hint, decipher);
	}

	@Override
	public ItemStack[] getMatchingStacks() {
		return ingredient.getMatchingStacks();
	}

	@Override
	public boolean apply(@Nullable ItemStack p_apply_1_) {
		return ingredient.apply(p_apply_1_);
	}

	@Override
	public IntList getValidItemStacksPacked() {
		return ingredient.getValidItemStacksPacked();
	}

	public ITextComponent getHint() {
		return hint;
	}

	public Set<BlockPredicate> getDecipher() {
		return unlock;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

}
