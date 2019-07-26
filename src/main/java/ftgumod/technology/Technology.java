package ftgumod.technology;

import com.google.gson.*;
import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.api.technology.ITechnology;
import ftgumod.api.technology.recipe.IIdeaRecipe;
import ftgumod.api.technology.recipe.IResearchRecipe;
import ftgumod.api.technology.recipe.IdeaRecipe;
import ftgumod.api.technology.unlock.IUnlock;
import ftgumod.api.util.JsonContextPublic;
import ftgumod.event.TechnologyEvent;
import ftgumod.util.ListenerTechnology;
import net.minecraft.advancements.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class Technology implements ITechnology {

	// Field added by SpongeForge
	private static final Field BYPASS_EVENT;
	private static final Logger LOGGER = LogManager.getLogger();

	static {
		Field f = null;
		try {
			f = Class.forName("org.spongepowered.common.advancement.SpongeScoreCriterion").getField("BYPASS_EVENT");
		} catch (ClassNotFoundException | NoSuchFieldException ignore) {
		}
		BYPASS_EVENT = f;
	}

	private final Set<Technology> children = new HashSet<>();
	private final ResourceLocation id;
	private final int level; // TODO: Use this variable for something

	ITextComponent displayText;
	DisplayInfo display;
	NonNullList<IUnlock> unlock;
	Technology parent;

	AdvancementRewards rewards;
	Map<String, Criterion> criteria;
	String[][] requirements;

	IIdeaRecipe idea;
	IResearchRecipe research;

	String stage;

	boolean start;
	boolean copy;

	Technology(ResourceLocation id, @Nullable Technology parent, DisplayInfo display, AdvancementRewards rewards, Map<String, Criterion> criteria, String[][] requirements, boolean start, boolean copy, @Nullable NonNullList<IUnlock> unlock, @Nullable IIdeaRecipe idea, @Nullable IResearchRecipe research, String stage) {
		this.id = id;
		this.parent = parent;
		this.display = display;

		this.start = start;
		this.copy = copy;

		this.rewards = rewards;
		this.criteria = criteria;
		this.requirements = requirements;

		this.unlock = unlock == null ? NonNullList.create() : unlock;
		this.idea = idea;
		this.research = research;

		this.stage = stage;

		if (parent == null)
			level = 1;
		else
			level = parent.level + 1;

		updateDisplayText();
	}

	public static Logger getLogger() {
		return LOGGER;
	}

	static void bypassEvent(boolean set) {
		if (BYPASS_EVENT != null) {
			if (set)
				LOGGER.debug("Avoiding crash: bypassing SpongeForge criteria events");

			try {
				BYPASS_EVENT.set(null, set);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	void updateDisplayText() {
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

	@Override
	public boolean canCopy() {
		return copy;
	}

	@Override
	public boolean researchedAtStart() {
		return start;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<ITechnology> getChildren() {
		return (Set) children;
	}

	public void getChildren(Collection<Technology> collection, boolean tree) {
		collection.add(this);
		children.forEach(tech -> {
			if (!tree || !tech.isRoot())
				tech.getChildren(collection, tree);
		});
	}

	@Override
	public IResearchRecipe getResearchRecipe() {
		return research;
	}

	@Override
	public boolean hasResearchRecipe() {
		return research != null;
	}

	@Override
	public IIdeaRecipe getIdeaRecipe() {
		return idea;
	}

	@Override
	public boolean hasIdeaRecipe() {
		return idea != null;
	}

	@Override
	public boolean isRoot() {
		return !hasParent() || !getRegistryName().getPath().substring(0, getRegistryName().getPath().indexOf('/')).equals(parent.getRegistryName().getPath().substring(0, parent.getRegistryName().getPath().indexOf('/')));
	}

	@Override
	public DisplayInfo getDisplayInfo() {
		return display;
	}

	@Override
	public Technology getParent() {
		return parent;
	}

	@Override
	public boolean hasParent() {
		return parent != null;
	}

	@Override
	public NonNullList<IUnlock> getUnlock() {
		return unlock;
	}

	@Override
	public boolean hasCustomUnlock() {
		return requirements.length > 0;
	}

	@Override
	public void setResearched(EntityPlayer player, boolean announce) {
		CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		if (cap != null) {
			cap.setResearched(getRegistryName().toString());

			if (player instanceof EntityPlayerMP) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;
				addRecipes(playerMP);

				if (rewards != null)
					rewards.apply(playerMP);

				for (Technology child : children)
					if (child.hasCustomUnlock())
						child.registerListeners(playerMP);

				Content.c_technologyResearched.trigger((EntityPlayerMP) player, this);
				MinecraftForge.EVENT_BUS.post(new TechnologyEvent.Research(player, this));
			}
			if (announce) {
				player.getServer().getPlayerList().sendMessage(new TextComponentTranslation("chat.type.technology", player.getDisplayName(), displayText));
				for (Technology child : children)
					if (child.isRoot() && child.isUnlocked(player))
						player.sendMessage(new TextComponentTranslation("technology.complete.unlock.root", child.displayText));
				player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
		}
	}

	public void addRecipes(EntityPlayerMP player) {
		unlock.forEach(unlock -> unlock.unlock(player));
	}

	@Override
	public void removeResearched(EntityPlayer player) {
		CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		if (cap != null) {
			if (isResearched(player)) {
				cap.removeResearched(getRegistryName().toString());

				if (player instanceof EntityPlayerMP) {
					EntityPlayerMP playerMP = (EntityPlayerMP) player;

					unlock.forEach(unlock -> unlock.lock(playerMP));
					for (Technology child : children)
						if (child.hasCustomUnlock())
							child.unregisterListeners(playerMP);

					MinecraftForge.EVENT_BUS.post(new TechnologyEvent.Revoke(player, this));
				}
			}
			if (hasCustomUnlock()) {
				AdvancementProgress progress = TechnologyManager.INSTANCE.getProgress(player, this);

				bypassEvent(true);

				for (String criterion : progress.getCompletedCriteria())
					if (progress.revokeCriterion(criterion))
						cap.removeResearched(getRegistryName() + "#" + criterion);

				bypassEvent(false);

				if (player instanceof EntityPlayerMP)
					registerListeners((EntityPlayerMP) player);
			}
		}
	}

	@Override
	public Map<String, Criterion> getCriteria() {
		return criteria;
	}

	@Override
	public String[][] getRequirements() {
		return requirements;
	}

	@Override
	public boolean grantCriterion(EntityPlayer player, String name) {
		AdvancementProgress progress = TechnologyManager.INSTANCE.getProgress(player, this);
		boolean done = progress.isDone();

		bypassEvent(true);

		if (progress.grantCriterion(name)) {
			player.getCapability(CapabilityTechnology.TECH_CAP, null).setResearched(getRegistryName() + "#" + name);
			if (player instanceof EntityPlayerMP) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;

				unregisterListeners(playerMP);
				if (!done && progress.isDone() && unlockedStage(player))
					unlock(playerMP);
			}

			bypassEvent(false);
			return true;
		}

		bypassEvent(false);
		return false;
	}

	public void unlock(EntityPlayerMP player) {
		MinecraftForge.EVENT_BUS.post(new TechnologyEvent.Unlock(player, this));

		player.sendMessage(new TextComponentTranslation(isRoot() ? "technology.complete.unlock.root" : "technology.complete.unlock", displayText));
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

		Content.c_technologyUnlocked.trigger(player, this);
	}

	@Override
	public boolean revokeCriterion(EntityPlayer player, String name) {
		AdvancementProgress progress = TechnologyManager.INSTANCE.getProgress(player, this);
		boolean done = progress.isDone();

		bypassEvent(true);

		if (progress.revokeCriterion(name)) {
			player.getCapability(CapabilityTechnology.TECH_CAP, null).removeResearched(getRegistryName() + "#" + name);
			if (player instanceof EntityPlayerMP) {
				registerListeners((EntityPlayerMP) player);
				if (done && !progress.isDone())
					MinecraftForge.EVENT_BUS.post(new TechnologyEvent.Revoke(player, this));
			}

			bypassEvent(false);
			return true;
		}

		bypassEvent(false);
		return false;
	}

	public void registerListeners(EntityPlayerMP player) {
		AdvancementProgress progress = TechnologyManager.INSTANCE.getProgress(player, this);
		if (!progress.isDone()) {
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
	}

	public void unregisterListeners(EntityPlayerMP player) {
		boolean parent = this.parent != null && !this.parent.isResearched(player);
		AdvancementProgress progress = TechnologyManager.INSTANCE.getProgress(player, this);

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

	@Override
	public ITextComponent getDisplayText() {
		return displayText;
	}

	public boolean hasProgress(EntityPlayer player) {
		return isResearched(player) || (hasCustomUnlock() && TechnologyManager.INSTANCE.getProgress(player, this).hasProgress());
	}

	@Override
	public boolean isResearched(EntityPlayer player) {
		CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		return cap != null && cap.isResearched(getRegistryName().toString());
	}

	public boolean isUnlockedIgnoreStage(EntityPlayer player) {
		return !hasCustomUnlock() || TechnologyManager.INSTANCE.getProgress(player, this).isDone();
	}

	@Override
	public boolean isUnlocked(EntityPlayer player) {
		return unlockedStage(player) && isUnlockedIgnoreStage(player);
	}

	@Override
	public boolean canResearch(EntityPlayer player) {
		return !isResearched(player) && isUnlocked(player) && (parent == null || parent.isResearched(player));
	}

	@Override
	public TechnologyBuilder toBuilder() {
		return new TechnologyBuilder(this);
	}

	@Override
	public String getGameStage() {
		return stage;
	}

	private boolean unlockedStage(EntityPlayer player) {
		return stage == null || !FTGU.INSTANCE.runCompat("gamestages", player, stage);
	}

	public boolean canResearchIgnoreCustomUnlock(EntityPlayer player) {
		return !isResearched(player) && (parent == null || parent.isResearched(player));
	}

	public boolean canResearchIgnoreResearched(EntityPlayer player) {
		return isResearched(player) || isUnlocked(player) && (parent == null || parent.isResearched(player));
	}

	@Override
	public ResourceLocation getRegistryName() {
		return id;
	}

	public static class Builder {

		private final ResourceLocation parentId;
		private final DisplayInfo display;
		private final AdvancementRewards rewards;
		private final Map<String, Criterion> criteria;
		private final String[][] requirements;

		private final JsonArray unlock;
		private final JsonObject idea;
		private final JsonObject research;

		private final String stage;

		private final boolean start;
		private final boolean copy;

		private Technology parent;

		private Builder(@Nullable ResourceLocation parent, DisplayInfo display, AdvancementRewards rewards, Map<String, Criterion> criteria, String[][] requirements, boolean start, boolean copy, @Nullable JsonArray unlock, @Nullable JsonObject idea, @Nullable JsonObject research, String stage) {
			this.parentId = parent;
			this.display = display;
			this.rewards = rewards;
			this.criteria = criteria;
			this.requirements = requirements;
			this.start = start;
			this.copy = copy;
			this.unlock = unlock;
			this.idea = idea;
			this.research = research;
			this.stage = stage;
		}

		public boolean resolveParent(Map<ResourceLocation, Technology> map) {
			if (parentId == null)
				return true;
			parent = map.get(parentId);
			return parent != null;
		}

		public Technology build(ResourceLocation location, JsonContextPublic context) {
			NonNullList<IUnlock> unlock = NonNullList.create();
			if (this.unlock != null)
				for (JsonElement element : this.unlock)
					unlock.add(TechnologyManager.INSTANCE.getUnlock(element, context, location));

			IIdeaRecipe idea = this.idea == null ? null : IdeaRecipe.deserialize(this.idea, context);
			IResearchRecipe research = this.research == null ? null : TechnologyManager.INSTANCE.getPuzzle(this.research, context, location);

			Technology r = new Technology(location, parent, display, rewards, criteria, requirements, start, copy, unlock, idea, research, stage);
			if (research != null)
				research.setTechnology(r);
			return r;
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

			String stage = JsonUtils.getString(json, "gamestage", null);

			boolean start = JsonUtils.getBoolean(json, "start", false);
			boolean copy = JsonUtils.getBoolean(json, "copy", true);

			return new Builder(parent, display, rewards, criteria, requirements, start, copy, unlock, idea, research, stage);
		}

	}

}
