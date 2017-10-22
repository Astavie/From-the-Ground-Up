package ftgumod.technology;

import ftgumod.api.technology.ITechnology;
import ftgumod.api.technology.ITechnologyBuilder;
import ftgumod.api.technology.recipe.IIdeaRecipe;
import ftgumod.api.technology.recipe.IResearchRecipe;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class TechnologyBuilder implements ITechnologyBuilder {

	private final ResourceLocation id;
	private Technology original;

	private ResourceLocation parent;
	private DisplayInfo display;
	private ITechnology.Type type;
	private AdvancementRewards rewards;
	private Map<String, Criterion> criteria;
	private String[][] requirements;
	private boolean start;
	private boolean copy;
	private NonNullList<Ingredient> unlock;
	private IIdeaRecipe idea;
	private IResearchRecipe research;

	public TechnologyBuilder(ResourceLocation id) {
		this.original = null;
		this.id = id;

		type = ITechnology.Type.TECHNOLOGY;
		rewards = AdvancementRewards.EMPTY;
		criteria = Collections.emptyMap();
		requirements = new String[0][];
		start = false;
		copy = true;
		unlock = NonNullList.create();
	}

	public TechnologyBuilder(Technology tech) {
		this.original = tech;
		this.id = tech.getRegistryName();

		if (tech.hasParent())
			parent = tech.parent.getRegistryName();
		display = tech.display;
		type = tech.type;
		rewards = tech.rewards;
		criteria = tech.criteria;
		requirements = tech.requirements;
		start = tech.start;
		copy = tech.copy;
		unlock = tech.unlock;
		idea = tech.idea;
		research = tech.research;
	}

	@Override
	public void setParent(@Nullable ResourceLocation parent) {
		this.parent = parent;
	}

	@Override
	public void setDisplayInfo(DisplayInfo display) {
		this.display = display;
	}

	@Override
	public void setType(ITechnology.Type type) {
		this.type = type;
	}

	@Override
	public void setRewards(AdvancementRewards rewards) {
		this.rewards = rewards;
	}

	@Override
	public void setCriteria(Map<String, Criterion> criteria, String[][] requirements) {
		this.criteria = criteria;
		this.requirements = requirements;
	}

	@Override
	public void setResearchedAtStart(boolean start) {
		this.start = start;
	}

	@Override
	public void setCanCopy(boolean copy) {
		this.copy = copy;
	}

	@Override
	public void addUnlock(Ingredient... ingredients) {
		unlock.addAll(Arrays.asList(ingredients));
	}

	@Override
	public void setIdeaRecipe(IIdeaRecipe idea) {
		this.idea = idea;
	}

	@Override
	public void setResearchRecipe(IResearchRecipe research) {
		this.research = research;
	}

	@Override
	public void save() {
		if (original == null)
			throw new NullPointerException("Trying to save to a null technology");

		Technology parent = TechnologyManager.INSTANCE.technologies.get(this.parent);
		if (parent == null)
			throw new NullPointerException("Unknown technology '" + this.parent + "'");

		if (parent != original.parent && TechnologyManager.INSTANCE.containsValue(original)) {
			original.parent.getChildren().remove(original);
			parent.getChildren().add(original);
		}

		original.parent = parent;
		original.display = display;
		original.type = type;
		original.rewards = rewards;
		original.criteria = criteria;
		original.requirements = requirements;
		original.start = start;
		original.copy = copy;
		original.unlock = unlock;
		original.idea = idea;
		original.research = research;
	}

	@Override
	public Technology build() {
		Technology parent = TechnologyManager.INSTANCE.technologies.get(this.parent);
		if (parent == null)
			throw new NullPointerException("Unknown technology '" + this.parent + "'");

		original = new Technology(id, parent, display, type, rewards, criteria, requirements, start, copy, unlock, idea, research);
		return original;
	}

}
