package ftgumod.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.compat.ICompat;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class CompatIE implements ICompat {

	private final Map<MultiblockHandler.IMultiblock, ResourceLocation> unlock = new HashMap<>();

	@Override
	public boolean run(Object... arg) { // ResourceLocation, JsonObject
		if (arg[0] instanceof ResourceLocation && arg[1] instanceof JsonObject) {
			ResourceLocation tech = (ResourceLocation) arg[0];
			JsonObject object = (JsonObject) arg[1];
			if (object.has("type") && JsonUtils.getString(object, "type").equals("immersiveengineering:multiblock")) {
				String id = JsonUtils.getString(object, "multiblock");
				MultiblockHandler.IMultiblock multiblock = null;

				for (MultiblockHandler.IMultiblock m : MultiblockHandler.getMultiblocks())
					if (m.getUniqueName().equals(id)) {
						multiblock = m;
						break;
					}

				if (multiblock == null)
					throw new JsonSyntaxException("Unknown multiblock " + id);

				unlock.put(multiblock, tech);

				object.remove("type");
				object.remove("multiblock");
				return true;
			}
		}
		return false;
	}

	@SubscribeEvent
	public void onMultiblockForm(MultiblockHandler.MultiblockFormEvent evt) {
		if (unlock.containsKey(evt.getMultiblock())) {
			Technology technology = TechnologyHandler.getTechnology(unlock.get(evt.getMultiblock()));
			if (technology != null && !technology.isResearched(evt.getEntityPlayer()))
				evt.setCanceled(true); // TODO: Send message?
		}
	}

}
