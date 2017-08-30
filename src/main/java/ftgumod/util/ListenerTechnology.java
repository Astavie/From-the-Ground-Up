package ftgumod.util;

import ftgumod.technology.Technology;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayer;

public final class ListenerTechnology<T extends ICriterionInstance> extends ICriterionTrigger.Listener<T> {

	private final EntityPlayer player;
	private final Technology technology;
	private final String name;

	public ListenerTechnology(EntityPlayer player, T instance, Technology technology, String name) {
		super(instance, null, null);
		this.player = player;
		this.technology = technology;
		this.name = name;
	}

	@Override
	public void grantCriterion(PlayerAdvancements playerAdvancements) {
		technology.grantCriterion(player, name);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof ListenerTechnology && hashCode() == obj.hashCode();
	}

	@Override
	public int hashCode() {
		int i = getCriterionInstance().hashCode();
		i = 31 * i + technology.hashCode();
		i = 31 * i + name.hashCode();
		return i;
	}

}
