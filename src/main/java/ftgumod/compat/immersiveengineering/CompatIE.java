package ftgumod.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class CompatIE {

	static final Map<MultiblockHandler.IMultiblock, ResourceLocation> UNLOCK = new HashMap<>();

	public void run() {
		MinecraftForge.EVENT_BUS.register(this);
		TechnologyManager.INSTANCE.registerUnlock(new ResourceLocation("immersiveengineering", "multiblock"), new UnlockMultiblockFactory());
	}

	@SubscribeEvent
	public void onMultiblockForm(MultiblockHandler.MultiblockFormEvent evt) {
		if (UNLOCK.containsKey(evt.getMultiblock())) {
			Technology technology = TechnologyManager.INSTANCE.technologies.get(UNLOCK.get(evt.getMultiblock()));
			if (technology != null && !technology.isResearched(evt.getEntityPlayer()))
				evt.setCanceled(true); // TODO: Send message?
		}
	}

}
