package ftgumod.packet.server;

import ftgumod.packet.MessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class ServerMessageHandler<T extends IMessage> extends MessageHandler<T> {

	@Override
	public IMessage handleClientMessage(EntityPlayer player, T message, MessageContext ctx) {
		return null;
	}

}
