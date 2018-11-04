package ftgumod.packet.server;

import ftgumod.inventory.ContainerResearchTable;
import ftgumod.packet.MessageHandler;
import ftgumod.packet.client.HintMessage;
import ftgumod.packet.client.TechnologyMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RequestMessage implements IMessage {

	private boolean b;

	public RequestMessage() {
	}

	public RequestMessage(boolean b) {
		this.b = b;
	}

	@Override
	public void fromBytes(ByteBuf arg0) {
		this.b = arg0.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf arg0) {
		arg0.writeBoolean(this.b);
	}

	public static class RequestMessageHandler extends MessageHandler<RequestMessage> {

		@Override
		public IMessage handleMessage(EntityPlayer player, RequestMessage message) {
			if (player != null) {
				if (message.b) {
					if (player.openContainer instanceof ContainerResearchTable && ((ContainerResearchTable) player.openContainer).invInput.puzzle != null)
						return new HintMessage(((ContainerResearchTable) player.openContainer).invInput.puzzle.getHints());
					return null;
				}
				return new TechnologyMessage(player, false);
			}
			return null;
		}

	}

}
