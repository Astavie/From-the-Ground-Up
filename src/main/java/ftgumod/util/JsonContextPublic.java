package ftgumod.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.HashMap;
import java.util.Map;

public class JsonContextPublic extends JsonContext {

	private Map<String, Ingredient> constants = new HashMap<>();

	public JsonContextPublic(String modId) {
		super(modId);
	}

	@Override
	public Ingredient getConstant(String name) {
		return constants.get(name);
	}

	public void loadConstants(JsonObject[] jsons) {
		for (JsonObject json : jsons) {
			if (json.has("conditions") && !CraftingHelper.processConditions(json.getAsJsonArray("conditions"), this))
				continue;
			if (!json.has("ingredient"))
				throw new JsonSyntaxException("Constant entry must contain 'ingredient' value");
			constants.put(JsonUtils.getString(json, "name"), CraftingHelper.getIngredient(json.get("ingredient"), this));
		}

	}

}
