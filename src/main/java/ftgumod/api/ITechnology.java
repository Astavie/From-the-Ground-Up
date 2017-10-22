package ftgumod.api;

import ftgumod.api.recipe.IIdeaRecipe;
import ftgumod.api.recipe.IResearchRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Map;
import java.util.Set;

public interface ITechnology<T extends ITechnology> extends IForgeRegistryEntry<T> { // Weird generics are due to how extending IForgeRegistry works

	/**
	 * @return If you can copy this {@code Technology}
	 */
	boolean canCopy();

	/**
	 * @return If this {@code Technology} is researched at the start
	 */
	boolean researchedAtStart();

	/**
	 * @return All children of this {@code Technology}
	 */
	Set<T> getChildren();

	/**
	 * @return If this {@code Technology} has an {@code IdeaRecipe}
	 */
	boolean hasIdeaRecipe();

	/**
	 * @return The {@code IdeaRecipe} of this {@code Technology}
	 */
	IIdeaRecipe getIdeaRecipe();

	/**
	 * @return If this {@code Technology} has a {@code ResearchRecipe}
	 */
	boolean hasResearchRecipe();

	/**
	 * @return The {@code ResearchRecipe} of this {@code Technology}
	 */
	IResearchRecipe getResearchRecipe();

	/**
	 * @return If this {@code Technology} is the root of a tree
	 */
	boolean isRoot();

	/**
	 * @return If this {@code Technology} is displayed in the Research Book
	 */
	boolean displayed();

	/**
	 * @return If this {@code Technology} has a parent
	 */
	boolean hasParent();

	/**
	 * @return This {@code Technology}'s parent
	 */
	T getParent();

	/**
	 * @return All {@code Ingredients} that are unlocked when researching this {@code Technology}
	 */
	NonNullList<Ingredient> getUnlock();

	/**
	 * @return If this {@code Technology} needs to be unlocked
	 */
	boolean hasCustomUnlock();

	/**
	 * @return The list of criteria that can be granted
	 */
	Map<String, Criterion> getCriteria();

	/**
	 * @return The criteria arrays required for this {@code Technology} to be unlocked
	 */
	String[][] getRequirements();

	/**
	 * Grants a player one of this {@code Technology}'s criteria.
	 *
	 * @param player    The {@code EntityPlayer} to grant the criterion to
	 * @param criterion The criterion to grant
	 * @return If the criterion has been granted successfully
	 */
	boolean grantCriterion(EntityPlayer player, String criterion);

	/**
	 * Revokes a player one of this {@code Technology}'s criteria.
	 *
	 * @param player    The {@code EntityPlayer} to revoke the criterion of
	 * @param criterion The criterion to revoke
	 * @return If the criterion has been revoked successfully
	 */
	boolean revokeCriterion(EntityPlayer player, String criterion);

	/**
	 * Gives this {@code Technology} to the specified player.
	 * <p>This method does <i>not</i> notify the player.
	 * If you want to notify the player, call {@link #announceResearched(EntityPlayer)} after this method.
	 *
	 * @param player The {@code EntityPlayer} to give this {@code Technology} to
	 */
	void setResearched(EntityPlayer player);

	/**
	 * Notifies the specified player (and the rest of the server, if announceAdvancements is enabled) that this {@code Technology} has been researched.
	 * <p>Should only be called if the player has actually researched this {@code Technology}.
	 * You can use {@link #setResearched(EntityPlayer)} for that.</p>
	 * <p><strong>{@link ITechnologyManager#sync(EntityPlayerMP, ITechnology[])} should always be invoked after calling this method!</strong></p>
	 *
	 * @param player The {@code EntityPlayer} to notify
	 */
	void announceResearched(EntityPlayer player);

	/**
	 * Removes this {@code Technology} from the specified player.
	 *
	 * @param player The {@code EntityPlayer} to remove this {@code Technology} from
	 */
	void removeResearched(EntityPlayer player);

	/**
	 * @return The {@code Type} of this {@code Technology}
	 */
	Type getType();

	/**
	 * @return The {@code DisplayInfo} of this {@code Technology}
	 */
	DisplayInfo getDisplayInfo();

	/**
	 * @return The display text of this {@code Technology}
	 */
	ITextComponent getDisplayText();

	/**
	 * @param player The {@code EntityPlayer} to check
	 * @return If the specified player has researched this {@code Technology}
	 */
	boolean isResearched(EntityPlayer player);

	/**
	 * @param player The {@code EntityPlayer} to check
	 * @return If the specified player has unlocked this {@code Technology}
	 */
	boolean isUnlocked(EntityPlayer player);

	/**
	 * @param player The {@code EntityPlayer} to check
	 * @return If the specified player can research this {@code Technology}
	 */
	boolean canResearch(EntityPlayer player);

	/**
	 * {@code TechnologyBuilders} created from {@code Technologies} can modify the original using the {@link ITechnologyBuilder#save()} method.
	 *
	 * @return A new {@code TechnologyBuilder}
	 */
	ITechnologyBuilder toBuilder();

	enum Type {
		TECHNOLOGY, THEORY
	}

}
