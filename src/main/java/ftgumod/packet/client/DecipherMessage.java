package ftgumod.packet.client;

import ftgumod.inventory.ContainerResearchTable;
import ftgumod.packet.MessageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashSet;
import java.util.Set;

public class DecipherMessage implements IMessage {

	private Set<Integer> deciphered;

	public DecipherMessage() {
	}

	public DecipherMessage(Set<Integer> deciphered) {
		this.deciphered = deciphered;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		deciphered = new HashSet<>();

		int size = buf.readInt();
		for (int i = 0; i < size; i++)
			deciphered.add(buf.readInt());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(deciphered.size());
		for (int i : deciphered)
			buf.writeInt(i);
	}

	public static class DecipherMessageHandler extends MessageHandler<DecipherMessage> {

		@Override
		public IMessage handleMessage(EntityPlayer player, DecipherMessage message) {
			if (player.openContainer instanceof ContainerResearchTable) {
				ContainerResearchTable table = (ContainerResearchTable) player.openContainer;
				table.deciphered = message.deciphered;
				table.onCraftMatrixChanged(table.invInput);
			}
			return null;
		}

	}

}
