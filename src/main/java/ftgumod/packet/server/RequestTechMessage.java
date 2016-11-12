package ftgumod.packet.server;

import ftgumod.packet.client.TechnologyMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RequestTechMessage implements IMessage {

	@Override
	public void fromBytes(ByteBuf arg0) {
	}

	@Override
	public void toBytes(ByteBuf arg0) {
	}

	public static class RequestTechMessageHandler extends ServerMessageHandler<RequestTechMessage> {

		@Override
		public IMessage handleServerMessage(EntityPlayer player, RequestTechMessage message, MessageContext ctx) {
			player = ctx.getServerHandler().playerEntity;
			return new TechnologyMessage(player);
		}

	}

}
