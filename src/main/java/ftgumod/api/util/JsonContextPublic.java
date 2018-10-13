package ftgumod.api.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.api.FTGUAPI;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class JsonContextPublic extends JsonContext {

	private final Map<String, ItemPredicate> constants = new HashMap<>();

	public JsonContextPublic(String modId) {
		super(modId);
	}

	public ItemPredicate getPredicate(String name) {
		return constants.get(name);
	}

	@Nullable
	@Override
	public Ingredient getConstant(String name) {
		ItemPredicate predicate = getPredicate(name);
		if (predicate == null)
			return null;
		return new Ingredient() {

			@Override
			public boolean apply(@Nullable ItemStack stack) {
				return predicate.test(stack);
			}

		};
	}

	public void loadConstants(JsonObject[] jsons) {
		for (JsonObject json : jsons) {
			if (json.has("conditions") && !CraftingHelper.processConditions(json.getAsJsonArray("conditions"), this))
				continue;
			if (!json.has("ingredient"))
				throw new JsonSyntaxException("Constant entry must contain 'ingredient' value");
			constants.put(JsonUtils.getString(json, "name"), FTGUAPI.stackUtils.getItemPredicate(json.get("ingredient"), this));
		}

	}

}
