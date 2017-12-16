package ftgumod.api.technology;

import ftgumod.api.technology.recipe.IIdeaRecipe;
import ftgumod.api.technology.recipe.IResearchRecipe;
import ftgumod.api.technology.unlock.IUnlock;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
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
	 * @see #getIdeaRecipe()
	 * @see #hasResearchRecipe()
	 * @see #getResearchRecipe()
	 */
	boolean hasIdeaRecipe();

	/**
	 * @return The {@code IdeaRecipe} of this {@code Technology}
	 * @see #hasIdeaRecipe()
	 * @see #hasResearchRecipe()
	 * @see #getResearchRecipe()
	 */
	IIdeaRecipe getIdeaRecipe();

	/**
	 * @return If this {@code Technology} has a {@code ResearchRecipe}
	 * @see #getResearchRecipe()
	 * @see #hasIdeaRecipe()
	 * @see #getIdeaRecipe()
	 */
	boolean hasResearchRecipe();

	/**
	 * @return The {@code ResearchRecipe} of this {@code Technology}
	 * @see #hasResearchRecipe()
	 * @see #hasIdeaRecipe()
	 * @see #getIdeaRecipe()
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
	 * @see #getParent()
	 */
	boolean hasParent();

	/**
	 * @return This {@code Technology}'s parent
	 * @see #hasParent()
	 */
	T getParent();

	/**
	 * @return All {@code IUnlocks} that are unlocked when researching this {@code Technology}
	 */
	NonNullList<IUnlock> getUnlock();

	/**
	 * @return If this {@code Technology} needs to be unlocked
	 * @see #isUnlocked(EntityPlayer)
	 */
	boolean hasCustomUnlock();

	/**
	 * @return The list of criteria that can be granted
	 * @see #getRequirements()
	 */
	Map<String, Criterion> getCriteria();

	/**
	 * @return The criteria arrays required for this {@code Technology} to be unlocked
	 * @see #getCriteria()
	 */
	String[][] getRequirements();

	/**
	 * Grants a player one of this {@code Technology}'s criteria.
	 *
	 * @param player    The {@code EntityPlayer} to grant the criterion to
	 * @param criterion The criterion to grant
	 * @return If the criterion has been granted successfully
	 * @see #revokeCriterion(EntityPlayer, String)
	 */
	boolean grantCriterion(EntityPlayer player, String criterion);

	/**
	 * Revokes a player one of this {@code Technology}'s criteria.
	 *
	 * @param player    The {@code EntityPlayer} to revoke the criterion of
	 * @param criterion The criterion to revoke
	 * @return If the criterion has been revoked successfully
	 * @see #grantCriterion(EntityPlayer, String)
	 */
	boolean revokeCriterion(EntityPlayer player, String criterion);

	/**
	 * Gives this {@code Technology} to the specified player.
	 * <p>This method does <i>not</i> notify the player.
	 * If you want to notify the player, call {@link #announceResearched(EntityPlayer)} after this method.</p>
	 * <p><strong>{@link ITechnologyManager#sync(EntityPlayerMP, ITechnology[])} should always be invoked after calling this method!</strong></p>
	 *
	 * @param player The {@code EntityPlayer} to give this {@code Technology} to
	 * @see #announceResearched(EntityPlayer)
	 * @see #removeResearched(EntityPlayer)
	 */
	void setResearched(EntityPlayer player);

	/**
	 * Notifies the specified player (and the rest of the server, if announceAdvancements is enabled) that this {@code Technology} has been researched.
	 * <p>Should only be called if the player has actually researched this {@code Technology}.
	 * You can use {@link #setResearched(EntityPlayer)} for that.</p>
	 *
	 * @param player The {@code EntityPlayer} to notify
	 * @see #setResearched(EntityPlayer)
	 */
	void announceResearched(EntityPlayer player);

	/**
	 * Removes this {@code Technology} from the specified player.
	 *
	 * @param player The {@code EntityPlayer} to remove this {@code Technology} from
	 * @see #setResearched(EntityPlayer)
	 */
	void removeResearched(EntityPlayer player);

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
	 * @see #canResearch(EntityPlayer)
	 */
	boolean isResearched(EntityPlayer player);

	/**
	 * @param player The {@code EntityPlayer} to check
	 * @return If the specified player has unlocked this {@code Technology}
	 * @see #hasCustomUnlock()
	 */
	boolean isUnlocked(EntityPlayer player);

	/**
	 * @param player The {@code EntityPlayer} to check
	 * @return If the specified player can research this {@code Technology}
	 * @see #isResearched(EntityPlayer)
	 */
	boolean canResearch(EntityPlayer player);

	/**
	 * {@code TechnologyBuilders} created from {@code Technologies} can modify the original using the {@link ITechnologyBuilder#save()} method.
	 *
	 * @return A new {@code TechnologyBuilder}
	 * @see ITechnologyManager#createBuilder(ResourceLocation)
	 */
	ITechnologyBuilder toBuilder();

	/**
	 * Game Stages is a mod that adds... Game Stages
	 *
	 * @return The Game Stage needed to research this technology, or {@code null} if none are required
	 */
	String getGameStage();

}
