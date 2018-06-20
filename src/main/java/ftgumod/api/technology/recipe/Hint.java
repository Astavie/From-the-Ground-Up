package ftgumod.api.technology.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import ftgumod.api.util.BlockPredicate;
import ftgumod.api.util.BlockPredicateCompound;
import ftgumod.api.util.BlockSerializable;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hint {

	private static final Gson GSON;

	static {
		GsonBuilder gsonbuilder = new GsonBuilder();
		gsonbuilder.registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer());
		gsonbuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
		gsonbuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
		GSON = gsonbuilder.create();
	}

	private final ITextComponent hint;
	private final List<Pair<BlockPredicate, ITextComponent>> hints;

	public Hint(ITextComponent hint, List<Pair<BlockPredicate, ITextComponent>> hints) {
		this.hint = hint;
		this.hints = hints;
	}

	public static Hint deserialize(JsonElement element) {
		ITextComponent hint = GSON.fromJson(element, ITextComponent.class);
		if (element.isJsonObject() && element.getAsJsonObject().has("decipher")) {
			JsonElement array = element.getAsJsonObject().get("decipher");
			if (!array.isJsonArray())
				throw new JsonSyntaxException("Expected decipher to be an array of objects");

			List<Pair<BlockPredicate, ITextComponent>> hints = new ArrayList<>();
			for (JsonElement decipher : array.getAsJsonArray()) {
				if (!decipher.isJsonObject())
					throw new JsonSyntaxException("Expected decipher to be an array of objects");
				BlockPredicate predicate = BlockPredicateCompound.deserialize(decipher.getAsJsonObject().get("decipher"));
				ITextComponent newHint = GSON.fromJson(decipher.getAsJsonObject().get("hint"), ITextComponent.class);
				hints.add(Pair.of(predicate, newHint));
			}
			return new Hint(hint, hints);
		} else return new Hint(hint, Collections.emptyList());
	}

	public ITextComponent getHint(List<BlockSerializable> inspected) {
		return hint;
	}

}
