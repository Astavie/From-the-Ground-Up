package ftgumod.event;

import ftgumod.technology.Technology;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

public abstract class TechnologyEvent extends PlayerEvent {

	private final Technology tech;

	public TechnologyEvent(EntityPlayer player, Technology tech) {
		super(player);
		this.tech = tech;
		setCanceled(!tech.canResearch(player));
	}

	public Technology getTechnology() {
		return tech;
	}

	@Cancelable
	public static class Research extends TechnologyEvent {

		public Research(EntityPlayer player, Technology tech) {
			super(player, tech);
		}

	}

	public static class Unlock extends TechnologyEvent {

		public Unlock(EntityPlayer player, Technology tech) {
			super(player, tech);
		}

	}

}
