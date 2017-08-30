package ftgumod.proxy;

import ftgumod.GuiHandler;
import ftgumod.technology.Technology;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ProxyCommon {

	public void showTechnologyToast(Technology technology) {
	}

	public IGuiHandler getGuiHandler() {
		return new GuiHandler();
	}

	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return ctx.getServerHandler().player;
	}

	public void preInit() {
	}

	public void init() {
	}

	public void postInit() {
	}

}
