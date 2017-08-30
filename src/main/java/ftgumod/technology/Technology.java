package ftgumod.technology;

import ftgumod.FTGUAPI;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Technology {

	private static final Logger LOGGER = LogManager.getLogger();

	private final Set<Technology> children = new HashSet<>();

	private final int level;
	private final boolean root;
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

	public Technology(ResourceLocation id, @Nullable Technology parent, boolean root, DisplayInfo display, Type type, AdvancementRewards rewards, Map<String, Criterion> criteria, String[][] requirements, @Nullable NonNullList<Ingredient> unlock, @Nullable IdeaRecipe idea, @Nullable ResearchRecipe research) {
		this.id = id;
		this.parent = parent;
		this.root = root;
		this.display = display;
		this.type = type;

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

	public Set<Technology> getChildren() {
		return children;
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
		return root;
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
					child.registerListeners(playerMP);
			}
		}
	}

	public Map<String, Criterion> getCriteria() {
		return criteria;
	}

	public String[][] getRequirements() {
		return requirements;
	}

	public void grantCriterion(EntityPlayer player, String name) {
		AdvancementProgress progress = TechnologyHandler.getProgress(player, this);
		boolean done = progress.isDone();

		if (progress.grantCriterion(name)) {
			player.getCapability(CapabilityTechnology.TECH_CAP, null).setResearched(id.toString() + "." + name);
			if (player instanceof EntityPlayerMP) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;

				unregisterListeners(playerMP);
				if (!done && progress.isDone()) {
					playerMP.sendMessage(new TextComponentTranslation("technology.complete.unlock", displayText));
					playerMP.world.playSound(null, playerMP.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

					FTGUAPI.c_technologyUnlocked.trigger(playerMP, this);
				}

				PacketDispatcher.sendTo(new TechnologyMessage(playerMP, true), playerMP);
			}
		}
	}

	public void revokeCriterion(EntityPlayer player, String name) {
		if (TechnologyHandler.getProgress(player, this).revokeCriterion(name)) {
			player.getCapability(CapabilityTechnology.TECH_CAP, null).removeResearched(id.toString() + "." + name);
			if (player instanceof EntityPlayerMP) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;

				registerListeners(playerMP);
				PacketDispatcher.sendTo(new TechnologyMessage(player, true), playerMP);
			}
		}
	}

	private void registerListeners(EntityPlayerMP player) {
		AdvancementProgress progress = TechnologyHandler.getProgress(player, this);
		if (!progress.isDone())
			for (Map.Entry<String, Criterion> entry : criteria.entrySet()) {
				CriterionProgress criterionProgress = progress.getCriterionProgress(entry.getKey());
				if (criterionProgress != null && !criterionProgress.isObtained()) {
					ICriterionInstance instance = entry.getValue().getCriterionInstance();
					if (instance != null) {
						ICriterionTrigger<ICriterionInstance> trigger = CriteriaTriggers.get(instance.getId());
						if (trigger != null)
							trigger.addListener(player.getAdvancements(), new ListenerTechnology<>(player, instance, this, entry.getKey()));
					}
				}
			}
	}

	private void unregisterListeners(EntityPlayerMP player) {
		AdvancementProgress progress = TechnologyHandler.getProgress(player, this);
		for (Map.Entry<String, Criterion> entry : criteria.entrySet()) {
			CriterionProgress criterionProgress = progress.getCriterionProgress(entry.getKey());
			if (criterionProgress != null && (criterionProgress.isObtained() || progress.isDone())) {
				ICriterionInstance instance = entry.getValue().getCriterionInstance();
				if (instance != null) {
					ICriterionTrigger<ICriterionInstance> trigger = CriteriaTriggers.get(instance.getId());
					if (trigger != null)
						trigger.removeListener(player.getAdvancements(), new ListenerTechnology<>(player, instance, this, entry.getKey()));
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

}
