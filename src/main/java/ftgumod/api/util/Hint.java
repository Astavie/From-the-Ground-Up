package ftgumod.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import ftgumod.api.util.predicate.BlockPredicate;
import ftgumod.api.util.predicate.BlockPredicateCompound;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
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

	public static Hint deserialize(JsonElement eHint, JsonElement decipher) {
		ITextComponent hint = GSON.fromJson(eHint, ITextComponent.class);
		if (decipher != null) {
			if (decipher.isJsonArray()) {
				List<Pair<BlockPredicate, ITextComponent>> hints = new ArrayList<>();
				for (JsonElement e : decipher.getAsJsonArray()) {
					if (!e.isJsonObject())
						throw new JsonSyntaxException("Expected decipher to be an object or an array of objects");
					BlockPredicate predicate = BlockPredicateCompound.deserialize(e.getAsJsonObject().get("decipher"));
					ITextComponent newHint = GSON.fromJson(e.getAsJsonObject().get("hint"), ITextComponent.class);
					hints.add(Pair.of(predicate, newHint));
				}
				return new Hint(hint, hints);
			}
			if (!decipher.isJsonObject())
				throw new JsonSyntaxException("Expected decipher to be an object or an array of objects");
			if (decipher.getAsJsonObject().has("decipher")) {
				BlockPredicate predicate = BlockPredicateCompound.deserialize(decipher.getAsJsonObject().get("decipher"));
				ITextComponent newHint = GSON.fromJson(decipher.getAsJsonObject().get("hint"), ITextComponent.class);
				List<Pair<BlockPredicate, ITextComponent>> hints = Collections.singletonList(Pair.of(predicate, newHint));
				return new Hint(hint, hints);
			}
		}
		return new Hint(hint, Collections.emptyList());
	}

	public ITextComponent getHint(List<BlockSerializable> inspected) {
		ITextComponent text = this.hint.createCopy();
		for (Pair<BlockPredicate, ITextComponent> hint : hints) {
			for (BlockSerializable block : inspected) {
				if (block.test(hint.getLeft())) {
					ITextComponent sibling = hint.getRight().createCopy();
					sibling.getStyle().setColor(TextFormatting.GOLD);
					text.appendText("\n - ");
					text.appendSibling(sibling);
				}
			}
		}
		return text;
	}

	public ITextComponent getObfuscatedHint() {
		ITextComponent hint = this.hint.createCopy();
		hint.getStyle().setObfuscated(true);
		return hint;
	}

	public boolean inspect(BlockSerializable block, List<BlockSerializable> inspected) {
		a:
		for (Pair<BlockPredicate, ITextComponent> hint : hints) {
			if (block.test(hint.getLeft()))
				for (BlockSerializable b : inspected) {
					if (b.test(hint.getLeft()))
						continue a;
					return true;
				}
		}
		return false;
	}

}
