package ftgumod.api.technology;

import ftgumod.api.technology.recipe.IIdeaRecipe;
import ftgumod.api.technology.recipe.IResearchRecipe;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;

public interface ITechnologyBuilder {

	void setParent(@Nullable ResourceLocation parent);

	void setDisplayInfo(DisplayInfo display);

	void setType(ITechnology.Type type);

	void setRewards(AdvancementRewards rewards);

	void setCriteria(Map<String, Criterion> criteria, String[][] requirements);

	void setResearchedAtStart(boolean start);

	void setCanCopy(boolean copy);

	void addUnlock(Ingredient... ingredients);

	void setIdeaRecipe(IIdeaRecipe idea);

	void setResearchRecipe(IResearchRecipe research);

	/**
	 * If this {@code TechnologyBuilder} was built from an existing {@code Technology}, that {@code Technology} will be modified.
	 *
	 * @throws NullPointerException If this {@code TechnologyBuilder} is not a copy or if the parent does not exist
	 * @see #build()
	 */
	void save();

	/**
	 * Builds a new {@code Technology}.
	 * All saves afterwards will then change it.
	 *
	 * @return A new {@code Technology}
	 * @throws NullPointerException If the parent does not exist
	 * @see #save()
	 */
	ITechnology build();

}
