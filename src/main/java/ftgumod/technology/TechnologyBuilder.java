package ftgumod.technology;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;

import ftgumod.api.technology.ITechnologyBuilder;
import ftgumod.api.technology.recipe.IIdeaRecipe;
import ftgumod.api.technology.recipe.IResearchRecipe;
import ftgumod.api.technology.unlock.IUnlock;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class TechnologyBuilder implements ITechnologyBuilder {

	private final ResourceLocation id;
	private Technology original;

	private ResourceLocation parent;
	private DisplayInfo display;
	private AdvancementRewards rewards;
	private Map<String, Criterion> criteria;
	private String[][] requirements;
	private boolean start;
	private boolean copy;
	private NonNullList<IUnlock> unlock;
	private IIdeaRecipe idea;
	private IResearchRecipe research;
	private String stage;

	public TechnologyBuilder(ResourceLocation id) {
		this.original = null;
		this.id = id;

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
		rewards = tech.rewards;
		criteria = tech.criteria;
		requirements = tech.requirements;
		start = tech.start;
		copy = tech.copy;
		unlock = tech.unlock;
		idea = tech.idea;
		research = tech.research;
		stage = tech.stage;
	}

	@Override
	public ITechnologyBuilder setParent(@Nullable ResourceLocation parent) {
		this.parent = parent;
		return this;
	}

	@Override
	public ITechnologyBuilder setDisplayInfo(DisplayInfo display) {
		this.display = display;
		return this;
	}

	@Override
	public ITechnologyBuilder setRewards(AdvancementRewards rewards) {
		this.rewards = rewards;
		return this;
	}

	@Override
	public ITechnologyBuilder setCriteria(Map<String, Criterion> criteria, String[][] requirements) {
		this.criteria = criteria;
		this.requirements = requirements;
		return this;
	}

	@Override
	public ITechnologyBuilder setResearchedAtStart(boolean start) {
		this.start = start;
		return this;
	}

	@Override
	public ITechnologyBuilder setCanCopy(boolean copy) {
		this.copy = copy;
		return this;
	}

	@Override
	public ITechnologyBuilder addUnlock(IUnlock... ingredients) {
		this.unlock.addAll(Arrays.asList(ingredients));
		return this;
	}

	@Override
	public ITechnologyBuilder setIdeaRecipe(IIdeaRecipe idea) {
		this.idea = idea;
		return this;
	}

	@Override
	public ITechnologyBuilder setResearchRecipe(IResearchRecipe research) {
		this.research = research;
		return this;
	}

	@Override
	public ITechnologyBuilder setGameStage(String stage) {
		this.stage = stage;
		return this;
	}

	@Override
	public void save() {
		if (original == null)
			throw new NullPointerException("Trying to save to a null technology");

		Technology parent = this.parent == null ? null : TechnologyManager.INSTANCE.getTechnology(this.parent);
		if (this.parent != null && parent == null)
			throw new NullPointerException("Unknown technology '" + this.parent + "'");

		if (parent != original.parent) {
			if (original.parent != null)
				original.parent.getChildren().remove(original);
			if (parent != null)
				parent.getChildren().add(original);
		}

		original.parent = parent;
		original.display = display;
		original.rewards = rewards;
		original.criteria = criteria;
		original.requirements = requirements;
		original.start = start;
		original.copy = copy;
		original.unlock = unlock;
		original.idea = idea;
		original.research = research;
		original.updateDisplayText();
	}

	@Override
	public Technology build() {
		Technology parent = this.parent == null ? null : TechnologyManager.INSTANCE.getTechnology(this.parent);
		if (this.parent != null && parent == null)
			throw new NullPointerException("Unknown technology '" + this.parent + "'");

		original = new Technology(id, parent, display, rewards, criteria, requirements, start, copy, unlock, idea, research, stage);
		return original;
	}

}
