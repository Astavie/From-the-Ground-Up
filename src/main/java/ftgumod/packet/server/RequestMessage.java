package ftgumod.packet.server;

import ftgumod.packet.MessageHandler;
import ftgumod.packet.client.TechnologyMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RequestMessage implements IMessage {

	public RequestMessage() {
	}

	@Override
	public void fromBytes(ByteBuf arg0) {
	}

	@Override
	public void toBytes(ByteBuf arg0) {
	}

	public static class RequestMessageHandler extends MessageHandler<RequestMessage> {

		@Override
		public IMessage handleMessage(EntityPlayer player, RequestMessage message) {
			return new TechnologyMessage(player, false);
		}

	}

}
