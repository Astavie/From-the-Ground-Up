package ftgumod.packet.client;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ftgumod.FTGU;
import ftgumod.client.gui.GuiResearchBook;
import ftgumod.packet.server.RequestMessage;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.Pair;

public class TechnologyInfoMessage implements IMessage {

	private boolean copy;
	private boolean custom;
	private byte hide;
	private Map<String, Pair<String, Map<ResourceLocation, String>>> json;

	public TechnologyInfoMessage() {
	}

	public TechnologyInfoMessage(Map<String, Pair<String, Map<ResourceLocation, String>>> json) {
		this.copy = FTGU.copy;
		this.custom = FTGU.custom;
		this.hide = FTGU.hide;
		this.json = json;
	}

	private String readLongString(ByteBuf buf) {
		int size = buf.readInt();
		String str = buf.toString(buf.readerIndex(), size, StandardCharsets.UTF_8);
		buf.readerIndex(buf.readerIndex() + size);
		return str;
	}

	private void writeLongString(ByteBuf buf, String string) {
		byte[] utf8Bytes = string.getBytes(StandardCharsets.UTF_8);
		buf.writeInt(utf8Bytes.length);
		buf.writeBytes(utf8Bytes);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		copy = buf.readBoolean();
		custom = buf.readBoolean();
		hide = buf.readByte();

		json = new HashMap<>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			String domain = ByteBufUtils.readUTF8String(buf);
			String context = readLongString(buf);
			int length = buf.readInt();

			Map<ResourceLocation, String> map = new HashMap<>();
			for (int j = 0; j < length; j++)
				map.put(new ResourceLocation(domain, ByteBufUtils.readUTF8String(buf)), readLongString(buf));
			json.put(domain, Pair.of(context, map));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(copy);
		buf.writeBoolean(custom);
		buf.writeByte(hide);

		buf.writeInt(json.size());
		for (Map.Entry<String, Pair<String, Map<ResourceLocation, String>>> domain : json.entrySet()) {
			ByteBufUtils.writeUTF8String(buf, domain.getKey());
			writeLongString(buf, domain.getValue().getLeft());
			buf.writeInt(domain.getValue().getRight().size());
			for (Map.Entry<ResourceLocation, String> entry : domain.getValue().getRight().entrySet()) {
				ByteBufUtils.writeUTF8String(buf, entry.getKey().getPath());
				writeLongString(buf, entry.getValue());
			}
		}
	}

	public static class TechnologyInfoMessageHandler implements IMessageHandler<TechnologyInfoMessage, IMessage> {

		@Override
		public IMessage onMessage(TechnologyInfoMessage message, MessageContext ctx) {
			if (FMLClientHandler.instance().getServer() == null) {
				FTGU.copy = message.copy;
				FTGU.custom = message.custom;
				if (message.hide > FTGU.hide)
					FTGU.hide = message.hide;

				TechnologyManager.INSTANCE.clear();

				TechnologyManager.INSTANCE.cache = message.json;
				TechnologyManager.INSTANCE.load();
			}

			Supplier<Stream<Technology>> stream = TechnologyManager.INSTANCE.getRoots()::stream;
			GuiResearchBook.zoom = stream.get().collect(Collectors.toMap(Technology::getRegistryName, tech -> 1.0F));
			GuiResearchBook.xScrollO = stream.get()
					.collect(Collectors.toMap(Technology::getRegistryName, tech -> -82.0));
			GuiResearchBook.yScrollO = stream.get()
					.collect(Collectors.toMap(Technology::getRegistryName, tech -> -82.0));

			FTGU.PROXY.clearToasts(); // Removes unnecessary recipe toasts
			return new RequestMessage();
		}

	}

}
