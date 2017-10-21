package ftgumod.technology.recipe;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.api.recipe.IResearchRecipe;
import ftgumod.api.util.BlockPredicate;
import ftgumod.util.IngredientResearch;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.Map;
import java.util.Set;

public class ResearchRecipe implements IResearchRecipe {

	private final NonNullList<IngredientResearch> ingredients;

	public ResearchRecipe(NonNullList<IngredientResearch> ingredients) {
		this.ingredients = ingredients;
	}

	public static ResearchRecipe deserialize(JsonObject object, JsonContext context) {
		Map<Character, IngredientResearch> ingMap = Maps.newHashMap();
		for (Map.Entry<String, JsonElement> entry : JsonUtils.getJsonObject(object, "key").entrySet()) {
			if (entry.getKey().length() != 1)
				throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
			if (" ".equals(entry.getKey()))
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

			ingMap.put(entry.getKey().toCharArray()[0], IngredientResearch.deserialize(entry.getValue(), context));
		}

		ingMap.put(' ', IngredientResearch.EMPTY);

		JsonArray patternJ = JsonUtils.getJsonArray(object, "pattern");

		if (patternJ.size() != 3)
			throw new JsonSyntaxException("Invalid pattern: must be 3x3");

		String[] pattern = new String[patternJ.size()];
		for (int x = 0; x < pattern.length; ++x) {
			String line = JsonUtils.getString(patternJ.get(x), "pattern[" + x + "]");
			if (line.length() != 3)
				throw new JsonSyntaxException("Invalid pattern: must be 3x3");
			pattern[x] = line;
		}

		NonNullList<IngredientResearch> input = NonNullList.withSize(9, IngredientResearch.EMPTY);

		Set<Character> keys = Sets.newHashSet(ingMap.keySet());
		keys.remove(' ');

		int x = 0;
		for (String line : pattern) {
			for (char chr : line.toCharArray()) {
				IngredientResearch ing = ingMap.get(chr);
				if (ing == null)
					throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
				input.set(x++, ing);
				keys.remove(chr);
			}
		}

		if (!keys.isEmpty())
			throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);

		return new ResearchRecipe(input);
	}

	@Override
	public ITextComponent getHint(int index) {
		return get(index).getHint();
	}

	@Override
	public Set<BlockPredicate> getDecipher(int index) {
		return get(index).getDecipher();
	}

	@Override
	public boolean test(NonNullList<ItemStack> inventory) {
		for (int i = 0; i < 9; i++)
			if (!ingredients.get(i).test(inventory.get(i)))
				return false;
		return true;
	}

	public IngredientResearch get(int index) {
		return ingredients.get(index);
	}

}
