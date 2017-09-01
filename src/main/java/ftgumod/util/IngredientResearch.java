package ftgumod.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.FTGU;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nullable;

public class IngredientResearch extends Ingredient {

	public static IngredientResearch EMPTY = new IngredientResearch(Ingredient.EMPTY, null, null);

	private final Ingredient ingredient;
	private final ITextComponent hint;
	private final BlockPredicate unlock;

	public IngredientResearch(Ingredient ingredient, @Nullable ITextComponent hint, @Nullable BlockPredicate unlock) {
		this.ingredient = ingredient;
		this.hint = hint;
		this.unlock = unlock;
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
			hint = FTGU.GSON.fromJson(element.getAsJsonObject().get("hint"), ITextComponent.class);

		BlockPredicate decipher = null;
		if (object.has("decipher")) {
			JsonElement i = object.get("decipher");
			if (i.isJsonObject())
				decipher = BlockPredicate.deserialize(i.getAsJsonObject());
			else throw new JsonSyntaxException("Expected decipher to be an object");
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

	public boolean hasHint() {
		return hint != null;
	}

	public ITextComponent getHint() {
		return hint;
	}

	public BlockPredicate getDecipher() {
		return unlock;
	}

	public boolean hasDecipher() {
		return unlock != null;
	}

}
