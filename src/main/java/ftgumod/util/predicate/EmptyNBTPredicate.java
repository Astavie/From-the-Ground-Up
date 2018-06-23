package ftgumod.util.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NBTPredicate;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.potion.PotionType;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class EmptyNBTPredicate extends ItemPredicate {

	private static final NBTPredicate EMPTY = new NBTPredicate(null) {

		@Override
		public boolean test(@Nullable NBTBase nbt) {
			return nbt == null || nbt.hasNoTags();
		}

	};

	private EmptyNBTPredicate(@Nullable Item item, @Nullable Integer data, MinMaxBounds count, MinMaxBounds durability, EnchantmentPredicate[] enchantments, @Nullable PotionType potion) {
		super(item, data, count, durability, enchantments, potion, EMPTY);
	}

	public static ItemPredicate factory(@Nullable JsonElement element) {
		if (element != null && !element.isJsonNull()) {
			JsonObject jsonobject = JsonUtils.getJsonObject(element, "item");
			MinMaxBounds minmaxbounds = MinMaxBounds.deserialize(jsonobject.get("count"));
			MinMaxBounds minmaxbounds1 = MinMaxBounds.deserialize(jsonobject.get("durability"));
			Integer integer = jsonobject.has("data") ? JsonUtils.getInt(jsonobject, "data") : null;
			Item item = null;

			if (jsonobject.has("item")) {
				ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(jsonobject, "item"));
				item = Item.REGISTRY.getObject(resourcelocation);

				if (item == null) {
					throw new JsonSyntaxException("Unknown item id '" + resourcelocation + "'");
				}
			}

			EnchantmentPredicate[] aenchantmentpredicate = EnchantmentPredicate.deserializeArray(jsonobject.get("enchantments"));
			PotionType potiontype = null;

			if (jsonobject.has("potion")) {
				ResourceLocation resourcelocation1 = new ResourceLocation(JsonUtils.getString(jsonobject, "potion"));
				if (!PotionType.REGISTRY.containsKey(resourcelocation1)) {
					throw new JsonSyntaxException("Unknown potion '" + resourcelocation1 + "'");
				}
				potiontype = PotionType.REGISTRY.getObject(resourcelocation1);
			}

			return new EmptyNBTPredicate(item, integer, minmaxbounds, minmaxbounds1, aenchantmentpredicate, potiontype);
		} else {
			return ANY;
		}
	}

}
