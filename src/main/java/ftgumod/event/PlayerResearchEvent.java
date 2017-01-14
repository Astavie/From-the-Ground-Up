package ftgumod.event;

import ftgumod.Technology;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerResearchEvent extends PlayerEvent {

	private final Technology tech;
	private boolean canResearch;

	public PlayerResearchEvent(EntityPlayer player, Technology tech) {
		super(player);
		this.tech = tech;
		this.canResearch = tech.canResearch(player);
	}

	public Technology getTechnology() {
		return tech;
	}

	public boolean canResearch() {
		return canResearch;
	}

	public void setCanResearch(boolean canResearch) {
		this.canResearch = canResearch;
	}

}
