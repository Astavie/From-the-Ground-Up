package ftgumod.packet.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ftgumod.inventory.ContainerResearchTable;
import ftgumod.packet.MessageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class HintMessage implements IMessage {

	private final List<ITextComponent> hints;

	public HintMessage() {
		this.hints = new ArrayList<>();
	}

	public HintMessage(List<ITextComponent> hints) {
		this.hints = hints;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		try {
			for (int i = 0; i < 9; i++)
				if (buffer.readBoolean())
					hints.add(buffer.readTextComponent());
				else
					hints.add(null);
		} catch (IOException ignore) {
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		for (int i = 0; i < 9; i++) {
			boolean b = hints.size() > i && hints.get(i) != null;
			buffer.writeBoolean(b);
			if (b)
				buffer.writeTextComponent(hints.get(i));
		}
	}

	public static class HintMessageHandler extends MessageHandler<HintMessage> {

		@Override
		public IMessage handleMessage(EntityPlayer player, HintMessage message) {
			if (player.openContainer instanceof ContainerResearchTable && ((ContainerResearchTable) player.openContainer).invInput.puzzle != null)
				((ContainerResearchTable) player.openContainer).invInput.puzzle.setHints(message.hints);
			return null;
		}

	}

}
