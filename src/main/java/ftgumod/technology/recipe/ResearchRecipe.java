package ftgumod.technology.recipe;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.FTGU;
import ftgumod.api.technology.recipe.IResearchRecipe;
import ftgumod.api.util.BlockPredicate;
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
	private final Set<BlockPredicate>[] deciphers;
	private final ITextComponent[] hints;
	private final Boolean[] consume;

	public ResearchRecipe(Set<ItemPredicate>[] ingredients, Set<BlockPredicate>[] deciphers, ITextComponent[] hints, Boolean[] consume) {
		this.ingredients = ingredients;
		this.deciphers = deciphers;
		this.hints = hints;
		this.consume = consume;
	}

	@SuppressWarnings("unchecked")
	public static ResearchRecipe deserialize(JsonObject object, JsonContext context) {
		Map<Character, Set<ItemPredicate>> ingMap = Maps.newHashMap();
		Map<Character, Set<BlockPredicate>> decipherMap = Maps.newHashMap();
		Map<Character, ITextComponent> hintMap = Maps.newHashMap();
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

			Set<BlockPredicate> decipher = new HashSet<>();
			if (first.has("decipher")) {
				JsonElement i = first.get("decipher");
				if (i.isJsonArray())
					for (JsonElement j : i.getAsJsonArray())
						if (j.isJsonObject())
							decipher.add(BlockPredicate.deserialize(j.getAsJsonObject()));
						else
							throw new JsonSyntaxException("Expected decipher to be an object or array of objects");
				else if (i.isJsonObject())
					decipher.add(BlockPredicate.deserialize(i.getAsJsonObject()));
				else
					throw new JsonSyntaxException("Expected decipher to be an object or array of objects");
			}

			decipherMap.put(c, decipher);

			ITextComponent hint = null;
			if (first.has("hint"))
				hint = FTGU.GSON.fromJson(first.get("hint"), ITextComponent.class);

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
		decipherMap.put(' ', Collections.emptySet());

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
		Set<BlockPredicate>[] deciphers = (Set<BlockPredicate>[]) new Set[9];
		ITextComponent[] hints = new ITextComponent[9];
		Boolean[] consume = new Boolean[9];

		Arrays.fill(predicates, ingMap.get(' '));
		Arrays.fill(deciphers, decipherMap.get(' '));

		Set<Character> keys = Sets.newHashSet(ingMap.keySet());
		keys.remove(' ');

		int x = 0;
		for (String line : pattern) {
			for (char chr : line.toCharArray()) {
				Set<ItemPredicate> ing = ingMap.get(chr);
				if (ing == null)
					throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
				predicates[x] = ing;
				deciphers[x] = decipherMap.get(chr);
				hints[x] = hintMap.get(chr);
				consume[x] = useMap.get(chr);
				x++;
				keys.remove(chr);
			}
		}

		if (!keys.isEmpty())
			throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);

		return new ResearchRecipe(predicates, deciphers, hints, consume);
	}

	@Nullable
	@Override
	public ITextComponent getHint(int index) {
		return hints[index];
	}

	@Override
	public Set<BlockPredicate> getDecipher(int index) {
		return deciphers[index];
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
