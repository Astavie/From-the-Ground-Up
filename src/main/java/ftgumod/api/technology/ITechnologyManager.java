package ftgumod.api.technology;

import ftgumod.api.technology.recipe.IResearchRecipe;
import ftgumod.api.technology.unlock.IUnlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ITechnologyManager {

	/**
	 * Looks through all registered {@code Technologies}' unlocks and returns the one unlocking the specified {@code ItemStack}.
	 *
	 * @param stack The {@code ItemStack} to check
	 * @return The {@code Technology} that unlocks the specified {@code ItemStack}, or {@code null} if there isn't one
	 */
	@Nullable
	ITechnology getLocked(ItemStack stack);

	/**
	 * When a new {@code Technology} is about to be added, the specified {@code Predicate} will be tested.
	 * If this returns {@code true}, then the {@code Technology} will be discarded.
	 *
	 * @param predicate The {@code Predicate} to check
	 * @see #addCallback(Consumer)
	 * @see #createCallback(Runnable)
	 */
	void removeCallback(Predicate<? super ITechnology> predicate);

	/**
	 * When a new {@code Technology} has just been added, the specified {@code Consumer} will look at it.
	 * This can be used to edit {@code Technologies}.
	 *
	 * @param action The {@code Consumer} which will accept all new {@code Technologies}
	 * @see #removeCallback(Predicate)
	 * @see #createCallback(Runnable)
	 */
	void addCallback(Consumer<? super ITechnology> action);

	/**
	 * Before {@code Technologies} are (re)loaded, the specified {@code Runnable} will run.
	 * This can be used to register {@code Technologies} that do not depend on others.
	 *
	 * @param action The {@code Runnable} to run
	 * @see #removeCallback(Predicate)
	 * @see #addCallback(Consumer)
	 */
	void createCallback(Runnable action);

	/**
	 * Registers a new {@code Technology}.
	 * Before this happens, callbacks registered through {@link #removeCallback(Predicate)} and {@link #addCallback(Consumer)} get called.
	 *
	 * @param value The {@code Technology} to be registered
	 * @throws IllegalArgumentException If the specified {@code Technology} is of an unexpected class
	 * @throws NullPointerException     If the specified {@code Technology} is {@code null} or has a {@code null} registry name
	 */
	void register(ITechnology value);

	void registerAll(ITechnology... values);

	boolean contains(ResourceLocation key);

	default boolean contains(ITechnology value) {
		return contains(value.getRegistryName());
	}

	ITechnology getTechnology(ResourceLocation key);

	Collection<ITechnology> getTechnologies();

	Set<ResourceLocation> getRegistryNames();

	/**
	 * Used to create {@code Technologies} at runtime.
	 * <p><strong>Only call this method if using JSON files is impossible!</strong>
	 *
	 * @param id The registry name of the new {@code Technology}
	 * @return A new {@code TechnologyBuilder}
	 * @see ITechnology#toBuilder()
	 */
	ITechnologyBuilder createBuilder(ResourceLocation id);

	/**
	 * Sends a message to the client to sync the researched {@code Technologies}.
	 * <p><strong>Should always be invoked after calling {@link ITechnology#setResearched(EntityPlayer, boolean)}!</strong></p>
	 *
	 * @param player The {@code EntityPlayer} to synchronize
	 * @param toasts The {@code Technologies} to show toasts of
	 */
	void sync(EntityPlayerMP player, ITechnology... toasts);

	/**
	 * Bypasses FTGU's regulation of recipe unlocking and adds all specified recipes to the Recipe Book
	 */
	void addRecipes(List<IRecipe> recipes, EntityPlayerMP player);

	void registerUnlock(ResourceLocation name, IUnlock.Factory<?> factory);

	void registerPuzzle(ResourceLocation name, IResearchRecipe.Factory<?> factory);

}
