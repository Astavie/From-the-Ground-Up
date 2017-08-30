package ftgumod.technology;

import ftgumod.server.RecipeBookServerImpl;
import ftgumod.technology.CapabilityTechnology.ITechnology;
import ftgumod.technology.recipe.IdeaRecipe;
import ftgumod.technology.recipe.ResearchRecipe;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class Technology {

	private static final Logger LOGGER = LogManager.getLogger();
	private final int level;
	private final boolean root;
	private final ITextComponent displayText;
	private final DisplayInfo display;
	private final Type type;
	private final NonNullList<Ingredient> unlock;
	private final Technology prev;
	private final ResourceLocation id;
	private final NonNullList<IdeaRecipe> ideas;
	private ResearchRecipe research;
	private boolean customUnlock = false;

	public Technology(ResourceLocation id, @Nullable Technology prev, boolean root, DisplayInfo display, Type type, @Nullable NonNullList<Ingredient> unlock, @Nullable NonNullList<IdeaRecipe> ideas, @Nullable ResearchRecipe research) {
		this.id = id;
		this.prev = prev;
		this.root = root;
		this.display = display;
		this.type = type;
		this.unlock = unlock == null ? NonNullList.create() : unlock;
		this.ideas = ideas == null ? NonNullList.create() : ideas;
		this.research = research;

		if (prev == null)
			level = 1;
		else
			level = prev.level + 1;

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

	public ResearchRecipe getResearchRecipe() {
		return research;
	}

	public void setResearchRecipe(ResearchRecipe recipe) {
		research = recipe;
	}

	public boolean hasResearchRecipe() {
		return research != null;
	}

	public NonNullList<IdeaRecipe> getIdeaRecipes() {
		return ideas;
	}

	public void addIdeaRecipe(IdeaRecipe recipe) {
		ideas.add(recipe);
	}

	public boolean isRoot() {
		return root;
	}

	public DisplayInfo getDisplay() {
		return display;
	}

	public Technology getPrevious() {
		return prev;
	}

	public NonNullList<Ingredient> getUnlock() {
		return unlock;
	}

	public void setCustomUnlock(boolean b) {
		customUnlock = b;
	}

	public boolean hasCustomUnlock() {
		return customUnlock;
	}

	public void setUnlocked(EntityPlayer player) {
		if (customUnlock) {
			ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
			if (cap != null)
				cap.setResearched(id.toString() + ".unlock");
		}
	}

	public void setResearched(EntityPlayer player) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		if (cap != null) {
			cap.setResearched(id.toString());

			if (!player.world.isRemote) {
				RecipeBookServer book = ((EntityPlayerMP) player).getRecipeBook();
				if (book instanceof RecipeBookServerImpl)
					((RecipeBookServerImpl) book).addItems(unlock, (EntityPlayerMP) player);
				else
					LOGGER.error("RecipeBookServer of " + player.getDisplayNameString() + " wasn't an instance of RecipeBookServerImpl: no recipes granted!");
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
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		return !customUnlock || cap != null && cap.isResearched(id.toString() + ".unlock");
	}

	public boolean canResearch(EntityPlayer player) {
		return !isResearched(player) && (!customUnlock || isUnlocked(player) && (prev == null || prev.isResearched(player)));
	}

	public boolean canResearchIgnoreCustomUnlock(EntityPlayer player) {
		return !isResearched(player) && (prev == null || prev.isResearched(player));
	}

	public boolean canResearchIgnoreResearched(EntityPlayer player) {
		return isResearched(player) || !customUnlock || isUnlocked(player) && (prev == null || prev.isResearched(player));
	}

	public int requirementsUntilAvailable(EntityPlayer player) {
		if (isResearched(player))
			return 0;
		if (canResearch(player))
			return 1;
		if (prev != null)
			return prev.requirementsUntilAvailable(player) + 1;
		return 2;
	}

	public enum Type {
		TECHNOLOGY, THEORY
	}

}
