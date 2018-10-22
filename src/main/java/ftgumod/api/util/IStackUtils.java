package ftgumod.api.util;

import com.google.gson.JsonElement;
import ftgumod.api.technology.ITechnology;
import ftgumod.api.util.predicate.ItemPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public interface IStackUtils {

	boolean isStackOf(ItemStack ingredient, ItemStack stack);

	boolean isEqual(ItemStack s1, ItemStack s2);

	ItemPredicate getItemPredicate(JsonElement element, JsonContextPublic context);

	void registerItemPredicate(ResourceLocation location, ItemPredicate.Factory factory);

	/**
	 * @param tech The {@code Technology}
	 * @param type The type of the parchment
	 * @return A parchment of the specified type containing the specified {@code Technology}
	 * @see #getTechnology(ItemStack)
	 */
	ItemStack getParchment(ITechnology tech, Parchment type);

	/**
	 * @param parchment A parchment item
	 * @return The {@code Technology} contained in the parchment
	 * @see #getParchment(ITechnology, Parchment)
	 */
	@Nullable
	ITechnology getTechnology(ItemStack parchment);

	/**
	 * @param inspector A magnifying glass item
	 * @return All blocks inspected by the specified magnifying glass
	 */
	List<BlockSerializable> getInspected(ItemStack inspector);

	enum Parchment {
		IDEA, RESEARCH
	}

}
