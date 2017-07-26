package ftgumod.event;

import ftgumod.technology.Technology;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerResearchEvent extends PlayerEvent {

	private final Technology tech;

	public PlayerResearchEvent(EntityPlayer player, Technology tech) {
		super(player);
		this.tech = tech;
		setCanceled(!tech.canResearch(player));
	}

	public Technology getTechnology() {
		return tech;
	}

}
