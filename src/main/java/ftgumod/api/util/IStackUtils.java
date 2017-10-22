package ftgumod.api.util;

import ftgumod.api.technology.ITechnology;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public interface IStackUtils<T extends ITechnology<T>> {

	/**
	 * @param tech The identifier of a {@code Technology}
	 * @param type The type of the parchment
	 * @return A parchment of the specified type containing the specified {@code Technology}
	 * @see #getTechnology(ItemStack)
	 */
	ItemStack getParchment(ResourceLocation tech, Parchment type);

	/**
	 * @param parchment A parchment item
	 * @return The {@code Technology} contained in the parchment
	 * @see #getParchment(ResourceLocation, Parchment)
	 */
	@Nullable
	T getTechnology(ItemStack parchment);

	/**
	 * @param inspector A magnifying glass item
	 * @return All blocks inspected by the specified magnifying glass
	 */
	List<BlockSerializable> getInspected(ItemStack inspector);

	enum Parchment {
		IDEA, RESEARCH
	}

}
