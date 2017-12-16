package ftgumod.compat.gamestages;

import ftgumod.compat.ICompat;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import net.darkhax.gamestages.capabilities.PlayerDataHandler;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CompatGameStages implements ICompat {

	@Override
	public boolean run(Object... arg) {
		return !PlayerDataHandler.getStageData((EntityPlayer) arg[0]).hasUnlockedStage((String) arg[1]);
	}

	@SubscribeEvent
	public void onGameStage(GameStageEvent.Added event) {
		if (!event.getPlayer().world.isRemote)
			for (Technology tech : TechnologyManager.INSTANCE)
				if (event.getStageName().equals(tech.getGameStage()) && tech.canResearch(event.getPlayer()))
					tech.unlock((EntityPlayerMP) event.getPlayer());
	}

}
