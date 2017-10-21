package ftgumod.util;

import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;

public final class ListenerTechnology<T extends ICriterionInstance> extends ICriterionTrigger.Listener<T> {

	private final Technology technology;
	private final String name;

	public ListenerTechnology(T instance, Technology technology, String name) {
		super(instance, null, null);
		this.technology = technology;
		this.name = name;
	}

	@Override
	public void grantCriterion(PlayerAdvancements playerAdvancements) {
		EntityPlayerMP player = playerAdvancements.player;

		if (TechnologyManager.INSTANCE.containsValue(technology))
			technology.grantCriterion(player, name);
		PacketDispatcher.sendTo(new TechnologyMessage(player, true), player);
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
