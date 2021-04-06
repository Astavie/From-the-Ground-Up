package ftgumod.api.util;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.api.FTGUAPI;
import ftgumod.api.util.predicate.ItemPredicate;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

public class JsonContextPublic extends JsonContext {

	private final Map<String, ItemPredicate> constants = new HashMap<>();

	public JsonContextPublic(String modId) {
		super(modId);
	}

	@Nullable
	@Override
	public ItemPredicate getConstant(String name) {
		return constants.get(name);
	}

	public void loadConstants(JsonObject[] jsons) {
		for (JsonObject json : jsons) {
			if (json.has("conditions") && !CraftingHelper.processConditions(json.getAsJsonArray("conditions"), this))
				continue;
			if (!json.has("ingredient"))
				throw new JsonSyntaxException("Constant entry must contain 'ingredient' value");
			constants.put(JsonUtils.getString(json, "name"),
					FTGUAPI.stackUtils.getItemPredicate(json.get("ingredient"), this));
		}

	}

}
