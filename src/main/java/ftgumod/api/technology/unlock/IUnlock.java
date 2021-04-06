package ftgumod.api.technology.unlock;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import ftgumod.api.util.JsonContextPublic;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

public interface IUnlock {

	/**
	 * @return If this unlock is displayed in the Research Book
	 * @see #getIcon()
	 */
	boolean isDisplayed();

	/**
	 * @return The icon in the Research Book, or {@code null} if {@link #isDisplayed()} is false
	 * @see #isDisplayed()
	 */
	@Nullable
	Ingredient getIcon();

	/**
	 * @param stack The {@code ItemStack} to check
	 * @return If this {@code IUnlock} unlocks the recipe for the specified {@code ItemStack}
	 */
	boolean unlocks(ItemStack stack);

	void unlock(EntityPlayerMP player);

	void lock(EntityPlayerMP player);

	interface Factory<T extends IUnlock> {

		T deserialize(JsonObject object, JsonContextPublic context, ResourceLocation technology);

	}

}
