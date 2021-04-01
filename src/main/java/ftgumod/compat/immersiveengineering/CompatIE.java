package ftgumod.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class CompatIE {

	static final Map<MultiblockHandler.IMultiblock, ResourceLocation> UNLOCK = new HashMap<>();

	@SubscribeEvent
	public void onMultiblockForm(MultiblockHandler.MultiblockFormEvent.Pre evt) {
		if (UNLOCK.containsKey(evt.getMultiblock())) {
			Technology technology = TechnologyManager.INSTANCE.getTechnology(UNLOCK.get(evt.getMultiblock()));
			if (technology != null && !technology.isResearched(evt.getEntityPlayer()))
				evt.setCanceled(true); // TODO: Send message?
		}
	}

}
