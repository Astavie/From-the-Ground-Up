package ftgumod.compat.gamestages;

import ftgumod.compat.ICompat;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CompatGameStages implements ICompat {

	@Override
	public boolean run(Object... arg) {
		return !GameStageHelper.hasStage((EntityPlayer) arg[0], (String) arg[1]);
	}

	@SubscribeEvent
	public void onGameStage(GameStageEvent.Added event) {
		if (!event.getEntityPlayer().world.isRemote)
			for (Technology tech : TechnologyManager.INSTANCE)
				if (event.getStageName().equals(tech.getGameStage()) && tech.canResearch(event.getEntityPlayer()))
					tech.unlock((EntityPlayerMP) event.getEntityPlayer());
	}

}
