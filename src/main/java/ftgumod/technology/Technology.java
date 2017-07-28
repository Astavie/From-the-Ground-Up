package ftgumod.technology;

import ftgumod.ItemList;
import ftgumod.server.RecipeBookServerImpl;
import ftgumod.technology.CapabilityTechnology.ITechnology;
import ftgumod.technology.TechnologyHandler.Tree;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Technology {

	private static final Logger LOGGER = LogManager.getLogger();

	private final boolean hide;

	private final int x;
	private final int y;
	private final int level;

	private final int ID;
	private final TextComponentBase displayText;
	private final ItemStack icon;
	private final List<ItemList> item;
	private final Technology prev;
	private final Technology[] secret;
	private final TechnologyHandler.Tree tree;
	private final String name;
	private int next = 0;
	private boolean customUnlock = false;

	public Technology(Tree tree, @Nullable Technology prev, ItemStack icon, int x, int y, String name, Object... item) {
		this(tree, prev, null, icon, false, x, y, name, item);
	}

	public Technology(Tree tree, @Nullable Technology prev, ItemStack icon, boolean hide, int x, int y, String name, Object... item) {
		this(tree, prev, null, icon, hide, x, y, name, item);
	}

	public Technology(Tree tree, @Nullable Technology prev, Technology[] secret, ItemStack icon, int x, int y, String name, Object... item) {
		this(tree, prev, secret, icon, false, x, y, name, item);
	}

	public Technology(TechnologyHandler.Tree tree, @Nullable Technology prev, Technology[] secret, ItemStack icon, boolean hide, int x, int y, String name, Object... item) {
		ID = TechnologyHandler.getID();

		this.x = x;
		this.y = y;
		this.name = name;
		this.prev = prev;
		this.tree = tree;
		this.icon = icon;
		this.secret = secret;
		this.hide = hide;

		if (prev == null)
			level = 1;
		else {
			level = prev.level + 1;
			prev.next += 1;
		}

		this.item = new ArrayList<>();
		for (Object o : item) {
			this.item.add(new ItemList(TechnologyUtil.toItem(o)));
		}

		displayText = new TextComponentString("[");
		displayText.getStyle().setColor(TextFormatting.GREEN);

		ITextComponent itextcomponent = getLocalizedName(true);
		ITextComponent itextcomponent1 = new TextComponentString("");
		ITextComponent itextcomponent2 = itextcomponent.createCopy();

		itextcomponent2.getStyle().setColor(TextFormatting.GREEN);

		itextcomponent1.appendSibling(itextcomponent2);
		itextcomponent1.appendText("\n");
		itextcomponent1.appendSibling(getDescription());

		itextcomponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itextcomponent1));

		displayText.appendSibling(itextcomponent);
		displayText.appendText("]");
	}

	public static Logger getLogger() {
		return LOGGER;
	}

	public Tree getTree() {
		return tree;
	}

	public ItemStack getIcon() {
		return icon;
	}

	public Technology getPrevious() {
		return prev;
	}

	public List<ItemList> getUnlock() {
		return item;
	}

	public boolean isHidden() {
		return hide;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
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
				cap.setResearched(name + ".unlock");
		}
	}

	public void setResearched(EntityPlayer player) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		if (cap != null) {
			cap.setResearched(name);

			if (!player.world.isRemote) {
				RecipeBookServer book = ((EntityPlayerMP) player).getRecipeBook();
				if (book instanceof RecipeBookServerImpl)
					((RecipeBookServerImpl) book).addItems(item, (EntityPlayerMP) player);
				else
					LOGGER.error("RecipeBookServer of " + player.getDisplayNameString() + " wasn't an instance of RecipeBookServerImpl: no recipes granted!");
			}
		}
	}

	public boolean isTheory() {
		return next > item.size();
	}

	public String getUnlocalizedName() {
		return name;
	}

	public TextComponentBase getLocalizedName(boolean suffix) {
		TextComponentTranslation name = new TextComponentTranslation("technology." + this.name + ".name");
		return suffix ? new TextComponentTranslation(isTheory() ? "technology.theory" : "technology.technology", name) : name;
	}

	public TextComponentBase getDescription() {
		return new TextComponentTranslation("technology." + name + ".desc");
	}

	public TextComponentBase getDisplayText() {
		return displayText;
	}

	public int getID() {
		return ID;
	}

	public boolean isResearched(EntityPlayer player) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		return cap != null && cap.isResearched(name);
	}

	public boolean isUnlocked(EntityPlayer player) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		return !customUnlock || cap != null && cap.isResearched(name + ".unlock");
	}

	public boolean canResearch(EntityPlayer player) {
		if (isResearched(player))
			return false;
		if (customUnlock && !isUnlocked(player))
			return false;
		if (prev != null && !prev.isResearched(player))
			return false;
		if (secret == null)
			return true;
		else
			for (Technology t : secret)
				if (!t.isResearched(player))
					return false;
		return true;
	}

	public boolean canResearchIgnoreCustomUnlock(EntityPlayer player) {
		if (isResearched(player))
			return false;
		if (prev != null && !prev.isResearched(player))
			return false;
		if (secret == null)
			return true;
		else
			for (Technology t : secret)
				if (!t.isResearched(player))
					return false;
		return true;
	}

	public boolean canResearchIgnoreResearched(EntityPlayer player) {
		if (isResearched(player))
			return true;
		if (customUnlock && !isUnlocked(player))
			return false;
		if (prev != null && !prev.isResearched(player))
			return false;
		if (secret == null)
			return true;
		else
			for (Technology t : secret)
				if (!t.isResearched(player))
					return false;
		return true;
	}

	public int requirementsUntilAvailable(EntityPlayer player) {
		if (isResearched(player))
			return 0;
		if (canResearch(player))
			return 1;
		int r = 2;
		if (prev != null)
			r = Math.max(prev.requirementsUntilAvailable(player) + 1, r);
		return r;
	}

}
