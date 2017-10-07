package ftgumod.technology;

import com.google.gson.*;
import ftgumod.FTGU;
import ftgumod.FTGUAPI;
import ftgumod.server.RecipeBookServerImpl;
import ftgumod.technology.CapabilityTechnology.ITechnology;
import ftgumod.technology.recipe.IdeaRecipe;
import ftgumod.technology.recipe.ResearchRecipe;
import ftgumod.util.ListenerTechnology;
import net.minecraft.advancements.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

public class Technology {

	private static final Logger LOGGER = LogManager.getLogger();

	public final boolean start;
	public final boolean headStart;

	private final Set<Technology> children = new HashSet<>();

	private final int level;
	private final ITextComponent displayText;
	private final DisplayInfo display;
	private final Type type;
	private final NonNullList<Ingredient> unlock;
	private final Technology parent;
	private final ResourceLocation id;

	private final AdvancementRewards rewards;
	private final Map<String, Criterion> criteria;
	private final String[][] requirements;

	private final IdeaRecipe idea;
	private final ResearchRecipe research;

	private final boolean copy;

	private Technology(ResourceLocation id, @Nullable Technology parent, DisplayInfo display, Type type, AdvancementRewards rewards, Map<String, Criterion> criteria, String[][] requirements, boolean start, boolean headStart, boolean copy, @Nullable NonNullList<Ingredient> unlock, @Nullable IdeaRecipe idea, @Nullable ResearchRecipe research) {
		this.id = id;
		this.parent = parent;
		this.display = display;
		this.type = type;

		this.start = start;
		this.headStart = headStart;
		this.copy = copy;

		this.rewards = rewards;
		this.criteria = criteria;
		this.requirements = requirements;

		this.unlock = unlock == null ? NonNullList.create() : unlock;
		this.idea = idea;
		this.research = research;

		if (parent == null)
			level = 1;
		else
			level = parent.level + 1;

		this.displayText = new TextComponentString("[");
		this.displayText.getStyle().setColor(display.getFrame().getFormat());
		ITextComponent itextcomponent = display.getTitle().createCopy();
		ITextComponent itextcomponent1 = new TextComponentString("");
		ITextComponent itextcomponent2 = itextcomponent.createCopy();
		itextcomponent2.getStyle().setColor(display.getFrame().getFormat());
		itextcomponent1.appendSibling(itextcomponent2);
		itextcomponent1.appendText("\n");
		itextcomponent1.appendSibling(display.getDescription());
		itextcomponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itextcomponent1));
		this.displayText.appendSibling(itextcomponent);
		this.displayText.appendText("]");
	}

	public static Logger getLogger() {
		return LOGGER;
	}

	public boolean canCopy() {
		return copy;
	}

	public Set<Technology> getChildren() {
		return children;
	}

	public void getChildren(Collection<Technology> collection, boolean tree) {
		collection.add(this);
		getChildren().forEach(tech -> {
			if (!tree || !tech.isRoot())
				tech.getChildren(collection, tree);
		});
	}

	public ResearchRecipe getResearchRecipe() {
		return research;
	}

	public boolean hasResearchRecipe() {
		return research != null;
	}

	public IdeaRecipe getIdeaRecipe() {
		return idea;
	}

	public boolean hasIdeaRecipe() {
		return idea != null;
	}

	public boolean isRoot() {
		return !hasParent() || !id.getResourcePath().substring(0, id.getResourcePath().indexOf('/')).equals(parent.id.getResourcePath().substring(0, parent.id.getResourcePath().indexOf('/')));
	}

	public DisplayInfo getDisplay() {
		return display;
	}

	public Technology getParent() {
		return parent;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public NonNullList<Ingredient> getUnlock() {
		return unlock;
	}

	public boolean hasCustomUnlock() {
		return requirements.length > 0;
	}

	public void setResearched(EntityPlayer player) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		if (cap != null) {
			cap.setResearched(id.toString());

			if (player instanceof EntityPlayerMP) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;

				RecipeBookServer book = playerMP.getRecipeBook();
				if (book instanceof RecipeBookServerImpl)
					((RecipeBookServerImpl) book).addRecipes(unlock, playerMP);
				else
					LOGGER.error("RecipeBookServer of " + player.getDisplayNameString() + " wasn't an instance of RecipeBookServerImpl: no recipes granted!");

				if (rewards != null)
					rewards.apply(playerMP);

				for (Technology child : children)
					if (child.hasCustomUnlock())
						child.registerListeners(playerMP);
			}
		}
	}

	public void removeResearched(EntityPlayer player) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		if (cap != null) {
			if (isResearched(player)) {
				cap.removeResearched(id.toString());

				if (player instanceof EntityPlayerMP) {
					EntityPlayerMP playerMP = (EntityPlayerMP) player;

					RecipeBookServer book = playerMP.getRecipeBook();
					if (book instanceof RecipeBookServerImpl)
						((RecipeBookServerImpl) book).removeRecipes(unlock, playerMP);
					else
						LOGGER.error("RecipeBookServer of " + player.getDisplayNameString() + " wasn't an instance of RecipeBookServerImpl: no recipes granted!");

					for (Technology child : children)
						if (child.hasCustomUnlock())
							child.unregisterListeners(playerMP);
				}
			}

			if (hasCustomUnlock()) {
				AdvancementProgress progress = TechnologyHandler.getProgress(player, this);
				for (String criterion : progress.getCompletedCriteria())
					if (progress.revokeCriterion(criterion))
						cap.removeResearched(id + "#" + criterion);

				if (player instanceof EntityPlayerMP)
					registerListeners((EntityPlayerMP) player);
			}
		}
	}

	public Map<String, Criterion> getCriteria() {
		return criteria;
	}

	public String[][] getRequirements() {
		return requirements;
	}

	public boolean grantCriterion(EntityPlayer player, String name) {
		AdvancementProgress progress = TechnologyHandler.getProgress(player, this);
		boolean done = progress.isDone();

		if (progress.grantCriterion(name)) {
			player.getCapability(CapabilityTechnology.TECH_CAP, null).setResearched(id.toString() + "#" + name);
			if (player instanceof EntityPlayerMP) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;

				unregisterListeners(playerMP);
				if (!done && progress.isDone()) {
					playerMP.sendMessage(new TextComponentTranslation(isRoot() ? "technology.complete.unlock.root" : "technology.complete.unlock", displayText));
					playerMP.world.playSound(null, playerMP.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

					FTGUAPI.c_technologyUnlocked.trigger(playerMP, this);
				}
			}
			return true;
		}
		return false;
	}

	public boolean revokeCriterion(EntityPlayer player, String name) {
		if (TechnologyHandler.getProgress(player, this).revokeCriterion(name)) {
			player.getCapability(CapabilityTechnology.TECH_CAP, null).removeResearched(id.toString() + "#" + name);
			if (player instanceof EntityPlayerMP)
				registerListeners((EntityPlayerMP) player);
			return true;
		}
		return false;
	}

	public void registerListeners(EntityPlayerMP player) {
		AdvancementProgress progress = TechnologyHandler.getProgress(player, this);
		if (!progress.isDone())
			for (Map.Entry<String, Criterion> entry : criteria.entrySet()) {
				CriterionProgress criterionProgress = progress.getCriterionProgress(entry.getKey());
				if (criterionProgress != null && !criterionProgress.isObtained()) {
					ICriterionInstance instance = entry.getValue().getCriterionInstance();
					if (instance != null) {
						ICriterionTrigger<ICriterionInstance> trigger = CriteriaTriggers.get(instance.getId());
						if (trigger != null)
							trigger.addListener(player.getAdvancements(), new ListenerTechnology<>(instance, this, entry.getKey()));
					}
				}
			}
	}

	public void unregisterListeners(EntityPlayerMP player) {
		boolean parent = this.parent != null && !this.parent.isResearched(player);
		AdvancementProgress progress = TechnologyHandler.getProgress(player, this);

		for (Map.Entry<String, Criterion> entry : criteria.entrySet()) {
			CriterionProgress criterionProgress = progress.getCriterionProgress(entry.getKey());
			if (criterionProgress != null && (parent || criterionProgress.isObtained() || progress.isDone())) {
				ICriterionInstance instance = entry.getValue().getCriterionInstance();
				if (instance != null) {
					ICriterionTrigger<ICriterionInstance> trigger = CriteriaTriggers.get(instance.getId());
					if (trigger != null)
						trigger.removeListener(player.getAdvancements(), new ListenerTechnology<>(instance, this, entry.getKey()));
				}
			}
		}
	}

	public Type getType() {
		return type;
	}

	public ResourceLocation getRegistryName() {
		return id;
	}

	public ITextComponent getDisplayText() {
		return displayText;
	}

	public boolean hasProgress(EntityPlayer player) {
		return isResearched(player) || (hasCustomUnlock() && TechnologyHandler.getProgress(player, this).hasProgress());
	}

	public boolean isResearched(EntityPlayer player) {
		final ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		return cap != null && cap.isResearched(id.toString());
	}

	public boolean isUnlocked(EntityPlayer player) {
		return !hasCustomUnlock() || TechnologyHandler.getProgress(player, this).isDone();
	}

	public boolean canResearch(EntityPlayer player) {
		return !isResearched(player) && isUnlocked(player) && (parent == null || parent.isResearched(player));
	}

	public boolean canResearchIgnoreCustomUnlock(EntityPlayer player) {
		return !isResearched(player) && (parent == null || parent.isResearched(player));
	}

	public boolean canResearchIgnoreResearched(EntityPlayer player) {
		return isResearched(player) || isUnlocked(player) && (parent == null || parent.isResearched(player));
	}

	public int requirementsUntilAvailable(EntityPlayer player) {
		if (isResearched(player))
			return 0;
		if (canResearch(player))
			return 1;
		if (parent != null)
			return parent.requirementsUntilAvailable(player) + 1;
		return 2;
	}

	public enum Type {
		TECHNOLOGY, THEORY
	}

	public static class Builder {

		private final ResourceLocation parentId;
		private final DisplayInfo display;
		private final Type type;
		private final AdvancementRewards rewards;
		private final Map<String, Criterion> criteria;
		private final String[][] requirements;

		private final JsonArray unlock;
		private final JsonObject idea;
		private final JsonObject research;

		private final boolean start;
		private final boolean headStart;
		private final boolean copy;

		private Technology parent;

		private Builder(@Nullable ResourceLocation parent, DisplayInfo display, Type type, AdvancementRewards rewards, Map<String, Criterion> criteria, String[][] requirements, boolean start, boolean headStart, boolean copy, @Nullable JsonArray unlock, @Nullable JsonObject idea, @Nullable JsonObject research) {
			this.parentId = parent;
			this.display = display;
			this.type = type;
			this.rewards = rewards;
			this.criteria = criteria;
			this.requirements = requirements;
			this.start = start;
			this.headStart = headStart;
			this.copy = copy;
			this.unlock = unlock;
			this.idea = idea;
			this.research = research;
		}

		public boolean resolveParent(Map<ResourceLocation, Technology> map) {
			if (parentId == null)
				return true;
			parent = map.get(parentId);
			return parent != null;
		}

		public Technology build(ResourceLocation location, JsonContext context) {
			NonNullList<Ingredient> unlock = NonNullList.create();
			if (this.unlock != null)
				for (JsonElement element : this.unlock) {
					if (element.isJsonObject())
						FTGU.INSTANCE.runCompat("immersiveengineering", location, element.getAsJsonObject());
					unlock.add(CraftingHelper.getIngredient(element, context));
				}

			IdeaRecipe idea = this.idea == null ? null : IdeaRecipe.deserialize(this.idea, context);
			ResearchRecipe research = this.research == null ? null : ResearchRecipe.deserialize(this.research, context);

			return new Technology(location, parent, display, type, rewards, criteria, requirements, start, headStart, copy, unlock, idea, research);
		}

	}

	public static class Deserializer implements JsonDeserializer<Builder> {

		@Override
		public Builder deserialize(JsonElement element, java.lang.reflect.Type ignore, JsonDeserializationContext context) throws JsonParseException {
			if (!element.isJsonObject())
				throw new JsonSyntaxException("Expected technology to be an object");
			JsonObject json = element.getAsJsonObject();

			ResourceLocation parent = json.has("parent") ? new ResourceLocation(JsonUtils.getString(json, "parent")) : null;

			JsonObject displayObject = JsonUtils.getJsonObject(json, "display");
			DisplayInfo display = DisplayInfo.deserialize(displayObject, context);

			if (displayObject.has("x") || displayObject.has("y"))
				display.setPosition(JsonUtils.getInt(displayObject, "x"), JsonUtils.getInt(displayObject, "y"));

			Type type = displayObject.has("theory") && JsonUtils.getBoolean(displayObject, "theory") ? Type.THEORY : Type.TECHNOLOGY;

			AdvancementRewards rewards = JsonUtils.deserializeClass(json, "rewards", AdvancementRewards.EMPTY, context, AdvancementRewards.class);
			Map<String, Criterion> criteria = json.has("criteria") ? Criterion.criteriaFromJson(JsonUtils.getJsonObject(json, "criteria"), context) : Collections.emptyMap();

			JsonArray array = JsonUtils.getJsonArray(json, "requirements", new JsonArray());
			String[][] requirements = new String[array.size()][];

			for (int i = 0; i < array.size(); ++i) {
				JsonArray subarray = JsonUtils.getJsonArray(array.get(i), "requirements[" + i + "]");
				requirements[i] = new String[subarray.size()];

				for (int j = 0; j < subarray.size(); ++j)
					requirements[i][j] = JsonUtils.getString(subarray.get(j), "requirements[" + i + "][" + j + "]");
			}

			if (requirements.length == 0) {
				requirements = new String[criteria.size()][];
				int k = 0;

				for (String s2 : criteria.keySet())
					requirements[k++] = new String[] {s2};
			}

			for (String[] subarray : requirements) {
				if (subarray.length == 0 && criteria.isEmpty())
					throw new JsonSyntaxException("Requirement entry cannot be empty");

				for (String s : subarray)
					if (!criteria.containsKey(s))
						throw new JsonSyntaxException("Unknown required criterion '" + s + "'");
			}

			for (String s1 : criteria.keySet()) {
				boolean flag = false;

				for (String[] subarray : requirements) {
					if (ArrayUtils.contains(subarray, s1)) {
						flag = true;
						break;
					}
				}

				if (!flag)
					throw new JsonSyntaxException("Criterion '" + s1 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
			}

			JsonArray unlock = json.has("unlock") ? JsonUtils.getJsonArray(json, "unlock") : null;
			JsonObject idea = json.has("idea") ? JsonUtils.getJsonObject(json, "idea") : null;
			JsonObject research = json.has("research") ? JsonUtils.getJsonObject(json, "research") : null;

			boolean start = JsonUtils.getBoolean(json, "start", false);
			boolean headStart = JsonUtils.getBoolean(json, "headstart", false);
			boolean copy = JsonUtils.getBoolean(json, "copy", true);

			return new Builder(parent, display, type, rewards, criteria, requirements, start, headStart, copy, unlock, idea, research);
		}

	}

}
