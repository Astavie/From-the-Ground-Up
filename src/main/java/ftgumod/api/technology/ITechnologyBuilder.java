package ftgumod.api.technology;

import java.util.Map;

import javax.annotation.Nullable;

import ftgumod.api.technology.recipe.IIdeaRecipe;
import ftgumod.api.technology.recipe.IResearchRecipe;
import ftgumod.api.technology.unlock.IUnlock;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.util.ResourceLocation;

public interface ITechnologyBuilder {

	ITechnologyBuilder setParent(@Nullable ResourceLocation parent);

	ITechnologyBuilder setDisplayInfo(DisplayInfo display);

	ITechnologyBuilder setRewards(AdvancementRewards rewards);

	ITechnologyBuilder setCriteria(Map<String, Criterion> criteria, String[][] requirements);

	ITechnologyBuilder setResearchedAtStart(boolean start);

	ITechnologyBuilder setCanCopy(boolean copy);

	ITechnologyBuilder addUnlock(IUnlock... ingredients);

	ITechnologyBuilder setIdeaRecipe(IIdeaRecipe idea);

	ITechnologyBuilder setResearchRecipe(IResearchRecipe research);

	ITechnologyBuilder setGameStage(String stage);

	/**
	 * If this {@code TechnologyBuilder} was built from an existing
	 * {@code Technology}, that {@code Technology} will be modified.
	 *
	 * @throws NullPointerException If this {@code TechnologyBuilder} is not a copy
	 *                              or if the parent does not exist
	 * @see #build()
	 */
	void save();

	/**
	 * Builds a new {@code Technology}. All saves afterwards will then change it.
	 *
	 * @return A new {@code Technology}
	 * @throws NullPointerException If the parent does not exist
	 * @see #save()
	 */
	ITechnology build();

}
