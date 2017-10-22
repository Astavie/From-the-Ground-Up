package ftgumod.api.technology;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ITechnologyManager<T extends ITechnology<T>> extends IForgeRegistry<T> { // Weird generics are due to how extending IForgeRegistry works

	/**
	 * Looks through all registered {@code Technologies}' unlocks and returns the one unlocking the specified {@code ItemStack}.
	 *
	 * @param stack The {@code ItemStack} to check
	 * @return The {@code Technology} that unlocks the specified {@code ItemStack}, or {@code null} if there isn't one
	 */
	@Nullable
	T getLocked(ItemStack stack);

	/**
	 * When a new {@code Technology} is about to be added, the specified {@code Predicate} will be tested.
	 * If this returns {@code true}, then the {@code Technology} will be discarded.
	 *
	 * @param predicate The {@code Predicate} to check
	 */
	void removeCallback(Predicate<? super T> predicate);

	/**
	 * When a new {@code Technology} has just been added, the specified {@code Consumer} will look at it.
	 * This can be used to edit {@code Technologies} before they're added.
	 *
	 * @param action The {@code Consumer} which will accept all new {@code Technologies}
	 */
	void addCallback(Consumer<? super T> action);

	/**
	 * Before {@code Technologies} are (re)loaded, the specified {@code Runnable} will run.
	 * This can be used to register {@code Technologies} that do no depend on others.
	 *
	 * @param action The {@code Runnable} to run
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
	@SuppressWarnings("unchecked")
	default void registerTechnology(ITechnology value) {
		if (!getRegistrySuperType().isInstance(value))
			throw new IllegalArgumentException("Tried to register a technology with an unexpected class");
		register((T) value);
	}

	@Override
	default boolean containsValue(ITechnology value) {
		return containsKey(value.getRegistryName());
	}

	@Nullable
	@Override
	default ResourceLocation getKey(ITechnology value) {
		return containsValue(value) ? value.getRegistryName() : null;
	}

	/**
	 * Used to create {@code Technologies} at runtime.
	 * <p><strong>Only call this method if using JSON files is impossible!</strong>
	 *
	 * @param id The registry name of the new {@code Technology}
	 * @return A new {@code TechnologyBuilder}
	 */
	ITechnologyBuilder createBuilder(ResourceLocation id);

	/**
	 * Sends a message to the client to sync the researched {@code Technologies}.
	 * <p><strong>Should always be invoked after calling {@link ITechnology#setResearched(EntityPlayer)}!</strong></p>
	 *
	 * @param player The {@code EntityPlayer} to synchronize
	 * @param toasts The {@code Technologies} to show a toast of
	 */
	void sync(EntityPlayerMP player, ITechnology... toasts);

}
