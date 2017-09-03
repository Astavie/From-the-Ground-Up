package ftgumod.packet.client;

import ftgumod.packet.server.RequestMessage;
import ftgumod.technology.TechnologyHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class TechnologyInfoMessage implements IMessage {

	private Map<ResourceLocation, String> json;

	public TechnologyInfoMessage() {
	}

	public TechnologyInfoMessage(Map<ResourceLocation, String> json) {
		this.json = json;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		json = new HashMap<>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++)
			json.put(new ResourceLocation(ByteBufUtils.readUTF8String(buf)), ByteBufUtils.readUTF8String(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(json.size());
		for (Map.Entry<ResourceLocation, String> entry : json.entrySet()) {
			ByteBufUtils.writeUTF8String(buf, entry.getKey().toString());
			ByteBufUtils.writeUTF8String(buf, entry.getValue());
		}
	}

	public static class TechnologyInfoMessageHandler implements IMessageHandler<TechnologyInfoMessage, IMessage> {

		@Override
		public IMessage onMessage(TechnologyInfoMessage message, MessageContext ctx) {
			TechnologyHandler.clear();
			TechnologyHandler.loadBuiltin().forEach(message.json::putIfAbsent);
			TechnologyHandler.deserialize(message.json);
			return new RequestMessage();
		}

	}

}
