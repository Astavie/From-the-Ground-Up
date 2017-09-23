package ftgumod.packet.client;

import ftgumod.packet.server.RequestMessage;
import ftgumod.technology.TechnologyHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

			Map<ResourceLocation, Pair<JsonContext, String>> map = message.json.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, t -> Pair.of(new JsonContext(t.getKey().getResourceDomain()), t.getValue())));
			TechnologyHandler.loadBuiltin().forEach(map::putIfAbsent);
			TechnologyHandler.deserialize(map);
			return new RequestMessage();
		}

	}

}
