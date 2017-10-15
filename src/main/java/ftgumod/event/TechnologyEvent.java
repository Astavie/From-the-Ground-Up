package ftgumod.event;

import ftgumod.technology.Technology;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public abstract class TechnologyEvent extends PlayerEvent {

	private final Technology tech;

	public TechnologyEvent(EntityPlayer player, Technology tech) {
		super(player);
		this.tech = tech;
	}

	public Technology getTechnology() {
		return tech;
	}

	/**
	 * Fires after a Technology is researched, using commands or with the research table
	 */
	public static class Research extends TechnologyEvent {

		public Research(EntityPlayer player, Technology tech) {
			super(player, tech);
		}

	}

	/**
	 * Fires after a Technology is unlocked
	 */
	public static class Unlock extends TechnologyEvent {

		public Unlock(EntityPlayer player, Technology tech) {
			super(player, tech);
		}

	}

	/**
	 * Fires after a Technology (or unlock) has been revoked
	 */
	public static class Revoke extends TechnologyEvent {

		public Revoke(EntityPlayer player, Technology tech) {
			super(player, tech);
		}

	}

}
