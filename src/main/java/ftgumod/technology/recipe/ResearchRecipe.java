package ftgumod.technology.recipe;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.api.technology.recipe.Hint;
import ftgumod.api.technology.recipe.IResearchRecipe;
import ftgumod.api.util.BlockSerializable;
import ftgumod.util.FluidPredicate;
import ftgumod.util.StackUtils;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nullable;
import java.util.*;

public class ResearchRecipe implements IResearchRecipe {

	private final Set<ItemPredicate>[] ingredients;
	private final Hint[] hints;
	private final Boolean[] consume;

	public ResearchRecipe(Set<ItemPredicate>[] ingredients, Hint[] hints, Boolean[] consume) {
		this.ingredients = ingredients;
		this.hints = hints;
		this.consume = consume;
	}

	@SuppressWarnings("unchecked")
	public static ResearchRecipe deserialize(JsonObject object, JsonContext context) {
		Map<Character, Set<ItemPredicate>> ingMap = Maps.newHashMap();
		Map<Character, Hint> hintMap = Maps.newHashMap();
		Map<Character, Boolean> useMap = Maps.newHashMap();

		for (Map.Entry<String, JsonElement> entry : JsonUtils.getJsonObject(object, "key").entrySet()) {
			if (entry.getKey().length() != 1)
				throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
			if (" ".equals(entry.getKey()))
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

			JsonElement element = entry.getValue();
			char c = entry.getKey().toCharArray()[0];

			ingMap.put(c, StackUtils.INSTANCE.getItemPredicate(element, context));

			while (!element.isJsonObject()) {
				if (element.isJsonArray())
					element = element.getAsJsonArray().get(0);
				else throw new JsonSyntaxException("Expected predicate to be an object or array of objects");
			}

			JsonObject first = element.getAsJsonObject();

			Hint hint = null;
			if (first.has("hint"))
				hint = Hint.deserialize(first.get("hint"));

			Boolean use = null;
			if (first.has("consume"))
				use = JsonUtils.getBoolean(first, "consume");

			hintMap.put(c, hint);
			useMap.put(c, use);
		}

		ingMap.put(' ', Collections.singleton(new ItemPredicate() {

			@Override
			public boolean test(ItemStack item) {
				return item.isEmpty();
			}

		}));

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

		Set<ItemPredicate>[] predicates = (Set<ItemPredicate>[]) new Set[9];
		Hint[] hints = new Hint[9];
		Boolean[] consume = new Boolean[9];

		Arrays.fill(predicates, ingMap.get(' '));

		Set<Character> keys = Sets.newHashSet(ingMap.keySet());
		keys.remove(' ');

		int x = 0;
		for (String line : pattern) {
			for (char chr : line.toCharArray()) {
				Set<ItemPredicate> ing = ingMap.get(chr);
				if (ing == null)
					throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
				predicates[x] = ing;
				hints[x] = hintMap.get(chr);
				consume[x] = useMap.get(chr);
				x++;
				keys.remove(chr);
			}
		}

		if (!keys.isEmpty())
			throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);

		return new ResearchRecipe(predicates, hints, consume);
	}

	@Nullable
	@Override
	public ITextComponent getHint(int index, List<BlockSerializable> inspected) {
		return hints[index].getHint(inspected);
	}

	@Override
	public boolean hasHint(int index) {
		return hints[index] != null;
	}

	@Override
	public NonNullList<ItemStack> test(InventoryCrafting inventory) {
		NonNullList<ItemStack> remaining = ForgeHooks.defaultRecipeGetRemainingItems(inventory);

		loop:
		for (int i = 0; i < 9; i++) {
			for (ItemPredicate predicate : ingredients[i])
				if (predicate.test(inventory.getStackInSlot(i))) {
					if (consume[i] != null) {
						if (consume[i])
							remaining.set(i, ItemStack.EMPTY);
						else
							remaining.set(i, inventory.getStackInSlot(i).copy());
					} else if (predicate instanceof FluidPredicate)
						remaining.set(i, ((FluidPredicate) predicate).drain(inventory.getStackInSlot(i).copy()));
					continue loop;
				}
			return null;
		}

		return remaining;
	}

}
