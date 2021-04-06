package ftgumod.api.technology.puzzle;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.api.FTGUAPI;
import ftgumod.api.technology.ITechnology;
import ftgumod.api.technology.recipe.IPuzzle;
import ftgumod.api.technology.recipe.IResearchRecipe;
import ftgumod.api.util.BlockSerializable;
import ftgumod.api.util.Hint;
import ftgumod.api.util.JsonContextPublic;
import ftgumod.api.util.predicate.ItemLambda;
import ftgumod.api.util.predicate.ItemPredicate;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class ResearchMatch implements IResearchRecipe {

	final ItemPredicate[] ingredients;
	final Hint[] hints;
	final Boolean[] consume;
	private ITechnology tech;

	public ResearchMatch(ItemPredicate[] ingredients, Hint[] hints, Boolean[] consume) {
		this.ingredients = ingredients;
		this.hints = hints;
		this.consume = consume;
	}

	@Nullable
	public Hint getHint(int index) {
		return hints[index];
	}

	public boolean hasHint(int index) {
		return hints[index] != null;
	}

	@Override
	public boolean inspect(BlockSerializable block, List<BlockSerializable> inspected) {
		for (Hint hint : hints)
			if (hint != null && hint.inspect(block, inspected))
				return true;
		return false;
	}

	@Override
	public IPuzzle createInstance() {
		return new PuzzleMatch(this);
	}

	@Override
	public ITechnology getTechnology() {
		return tech;
	}

	@Override
	public void setTechnology(ITechnology tech) {
		this.tech = tech;
	}

	public static class Factory implements IResearchRecipe.Factory<ResearchMatch> {

		@Override
		public ResearchMatch deserialize(JsonObject object, JsonContextPublic context, ResourceLocation technology) {
			Map<Character, ItemPredicate> ingMap = Maps.newHashMap();
			Map<Character, Hint> hintMap = Maps.newHashMap();
			Map<Character, Boolean> useMap = Maps.newHashMap();

			for (Map.Entry<String, JsonElement> entry : JsonUtils.getJsonObject(object, "key").entrySet()) {
				if (entry.getKey().length() != 1)
					throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey()
							+ "' is an invalid symbol (must be 1 character only).");
				if (" ".equals(entry.getKey()))
					throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

				JsonElement element = entry.getValue();
				char c = entry.getKey().toCharArray()[0];

				if (!element.isJsonObject())
					throw new JsonSyntaxException("Expected predicate to be an object");

				JsonObject first = element.getAsJsonObject();
				ingMap.put(c, FTGUAPI.stackUtils.getItemPredicate(first.get("item"), context));

				Hint hint = null;
				if (first.has("hint"))
					hint = Hint.deserialize(first.get("hint"), first.get("decipher"));

				Boolean use = null;
				if (first.has("consume"))
					use = JsonUtils.getBoolean(first, "consume");

				hintMap.put(c, hint);
				useMap.put(c, use);
			}

			ingMap.put(' ', ItemLambda.EMPTY);

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

			ItemPredicate[] predicates = new ItemPredicate[9];
			Hint[] hints = new Hint[9];
			Boolean[] consume = new Boolean[9];

			Arrays.fill(predicates, ingMap.get(' '));

			Set<Character> keys = Sets.newHashSet(ingMap.keySet());
			keys.remove(' ');

			int x = 0;
			for (String line : pattern) {
				for (char chr : line.toCharArray()) {
					ItemPredicate ing = ingMap.get(chr);
					if (ing == null)
						throw new JsonSyntaxException(
								"Pattern references symbol '" + chr + "' but it's not defined in the key");
					predicates[x] = ing;
					hints[x] = hintMap.get(chr);
					consume[x] = useMap.get(chr);
					x++;
					keys.remove(chr);
				}
			}

			if (!keys.isEmpty())
				throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);

			return new ResearchMatch(predicates, hints, consume);
		}

	}

}
