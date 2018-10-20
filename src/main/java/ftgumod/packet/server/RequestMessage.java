package ftgumod.packet.server;

import ftgumod.packet.MessageHandler;
import ftgumod.packet.client.TechnologyMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RequestMessage implements IMessage {

	private int id;

	public RequestMessage() {
	}

	public RequestMessage(int id) {
		this.id = id;
	}

	@Override
	public void fromBytes(ByteBuf arg0) {
		id = arg0.readInt();
	}

	@Override
	public void toBytes(ByteBuf arg0) {
		arg0.writeInt(id);
	}

	public static class RequestMessageHandler extends MessageHandler<RequestMessage> {

		@Override
		public IMessage handleMessage(EntityPlayer player, RequestMessage message) {
			if (message.id == 0)
				return new TechnologyMessage(player, false);
			return null;
		}

	}

}
