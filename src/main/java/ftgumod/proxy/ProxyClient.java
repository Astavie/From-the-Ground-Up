package ftgumod.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ProxyClient extends ProxyCommon {

	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return Minecraft.getMinecraft().thePlayer;
	}

}
