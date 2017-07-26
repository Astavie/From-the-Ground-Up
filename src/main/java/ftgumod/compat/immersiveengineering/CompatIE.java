package ftgumod.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import ftgumod.compat.ICompat;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class CompatIE implements ICompat {

	private final Map<MultiblockHandler.IMultiblock, Technology> unlock = new HashMap<>();

	@Override
	public boolean run(Object... arg) {
		boolean first = true;
		Technology tech = null;

		for (Object o : arg) {
			if (first) {
				tech = TechnologyHandler.getTechnology(o.toString());
				if (tech == null)
					return false;
				first = false;
			} else {
				for (MultiblockHandler.IMultiblock multiblock : MultiblockHandler.getMultiblocks())
					if (o.toString().equals(multiblock.getUniqueName())) {
						unlock.put(multiblock, tech);
						break;
					}
			}
		}

		return true;
	}

	@SubscribeEvent
	public void onMultiblockForm(MultiblockHandler.MultiblockFormEvent evt) {
		if (unlock.containsKey(evt.getMultiblock()) && !unlock.get(evt.getMultiblock()).isResearched(evt.getEntityPlayer()))
			evt.setCanceled(true); // TODO: Send message?
	}

}
