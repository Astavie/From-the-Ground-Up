package ftgumod.packet.client;

import ftgumod.FTGU;
import ftgumod.packet.server.RequestMessage;
import ftgumod.technology.TechnologyManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class TechnologyInfoMessage implements IMessage {

	private boolean copy;
	private boolean custom;
	private Map<String, Pair<String, Map<ResourceLocation, String>>> json;

	public TechnologyInfoMessage() {
	}

	public TechnologyInfoMessage(boolean copy, boolean custom, Map<String, Pair<String, Map<ResourceLocation, String>>> json) {
		this.copy = copy;
		this.custom = custom;
		this.json = json;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		copy = buf.readBoolean();
		custom = buf.readBoolean();

		json = new HashMap<>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			String domain = ByteBufUtils.readUTF8String(buf);
			String context = ByteBufUtils.readUTF8String(buf);
			int length = buf.readInt();

			Map<ResourceLocation, String> map = new HashMap<>();
			for (int j = 0; j < length; j++)
				map.put(new ResourceLocation(domain, ByteBufUtils.readUTF8String(buf)), ByteBufUtils.readUTF8String(buf));
			json.put(domain, Pair.of(context, map));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(copy);
		buf.writeBoolean(custom);

		buf.writeInt(json.size());
		for (Map.Entry<String, Pair<String, Map<ResourceLocation, String>>> domain : json.entrySet()) {
			ByteBufUtils.writeUTF8String(buf, domain.getKey());
			ByteBufUtils.writeUTF8String(buf, domain.getValue().getLeft());
			buf.writeInt(domain.getValue().getRight().size());
			for (Map.Entry<ResourceLocation, String> entry : domain.getValue().getRight().entrySet()) {
				ByteBufUtils.writeUTF8String(buf, entry.getKey().getResourcePath());
				ByteBufUtils.writeUTF8String(buf, entry.getValue());
			}
		}
	}

	public static class TechnologyInfoMessageHandler implements IMessageHandler<TechnologyInfoMessage, IMessage> {

		@Override
		public IMessage onMessage(TechnologyInfoMessage message, MessageContext ctx) {
			FTGU.copy = message.copy;
			FTGU.custom = message.custom;

			TechnologyManager.INSTANCE.clear();

			TechnologyManager.INSTANCE.cache = message.json;
			TechnologyManager.INSTANCE.load();

			FTGU.PROXY.clearToasts(); // Removes unnecessary recipe toasts
			return new RequestMessage();
		}

	}

}
