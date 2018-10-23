package ftgumod.proxy;

import ftgumod.GuiHandler;
import ftgumod.api.technology.ITechnology;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.Technology;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ProxyCommon {

	public void displayToastTechnology(ITechnology technology) {
	}

	public void clearToasts() {
	}

	public void openResearchBook(EntityPlayer player) {
	}

	public IGuiHandler getGuiHandler() {
		return new GuiHandler();
	}

	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return ctx.getServerHandler().player;
	}

	public void autoResearch(Technology tech) {
		FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers().forEach(player -> {
			CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
			cap.setResearched(tech.getRegistryName().toString());
		});
	}

	public void init(FMLInitializationEvent event) {
	}

}
