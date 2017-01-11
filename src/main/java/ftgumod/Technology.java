package ftgumod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import ftgumod.CapabilityTechnology.ITechnology;
import ftgumod.TechnologyHandler.PAGE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public class Technology {

	private final int ID;

	public final boolean hide;

	public final int x;
	public final int y;

	public ItemStack icon;
	public List<ItemStack> item;
	public Technology prev;
	public Technology[] secret;
	public PAGE page;

	public final int level;

	private boolean customUnlock = false;

	public boolean researched = false;

	private String name;

	public Technology(PAGE page, @Nullable Technology prev, ItemStack icon, int x, int y, String name, Object... item) {
		this(page, prev, null, icon, false, x, y, name, item);
	}

	public Technology(PAGE page, @Nullable Technology prev, ItemStack icon, boolean hide, int x, int y, String name, Object... item) {
		this(page, prev, null, icon, hide, x, y, name, item);
	}

	public Technology(PAGE page, @Nullable Technology prev, Technology[] secret, ItemStack icon, int x, int y, String name, Object... item) {
		this(page, prev, secret, icon, false, x, y, name, item);
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
			cap.setResearched(name + ".unlock");
		}
	}

	public void setResearched(EntityPlayer player) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		cap.setResearched(name);
	}

	public Technology(PAGE page, @Nullable Technology prev, Technology[] secret, ItemStack icon, boolean hide, int x, int y, String name, Object... item) {
		ID = TechnologyHandler.getID();

		this.x = x;
		this.y = y;
		this.name = name;
		this.prev = prev;
		this.page = page;
		this.icon = icon;
		this.secret = secret;
		this.hide = hide;

		if (prev == null)
			level = 1;
		else
			level = prev.level + 1;

		this.item = new ArrayList<ItemStack>();
		for (Object o: item) {
			this.item.addAll(TechnologyUtil.toItems(TechnologyUtil.toItem(o)));
		}
	}

	public String getUnlocalisedName() {
		return name;
	}

	public String getLocalisedName() {
		return I18n.translateToLocal("technology." + name + ".name");
	}

	public String getDescription() {
		return I18n.translateToLocal("technology." + name + ".desc");
	}

	public List<ItemStack> getItems() {
		return item;
	}

	public int getID() {
		return ID;
	}

	public boolean isResearched(EntityPlayer player) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		return cap.isResearched(name) || researched;
	}

	public boolean isUnlocked(EntityPlayer player) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		return customUnlock ? cap.isResearched(name + ".unlock") : true;
	}

	public boolean canResearch(EntityPlayer player) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);

		if (isResearched(player))
			return false;
		if (customUnlock && !cap.isResearched(name + ".unlock"))
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
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);

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

	public int requirementsUntilAvailible(EntityPlayer player) {
		if (isResearched(player))
			return 0;
		if (canResearch(player))
			return 1;
		int r = 2;
		if (prev != null)
			r = Math.max(prev.requirementsUntilAvailible(player) + 1, r);
		return r;
	}

}
