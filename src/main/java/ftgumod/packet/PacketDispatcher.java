package ftgumod.packet;

import ftgumod.packet.client.HintMessage;
import ftgumod.packet.client.HintMessage.HintMessageHandler;
import ftgumod.packet.client.TechnologyInfoMessage;
import ftgumod.packet.client.TechnologyInfoMessage.TechnologyInfoMessageHandler;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.packet.client.TechnologyMessage.TechnologyMessageHandler;
import ftgumod.packet.server.CopyTechMessage;
import ftgumod.packet.server.CopyTechMessage.CopyTechMessageHandler;
import ftgumod.packet.server.RequestMessage;
import ftgumod.packet.server.RequestMessage.RequestMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketDispatcher {

	private static byte packetId = 0;

	private static SimpleNetworkWrapper dispatcher;

	public static void registerPackets() {
		dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel("ftgu");

		PacketDispatcher.registerMessage(RequestMessageHandler.class, RequestMessage.class, Side.SERVER);
		PacketDispatcher.registerMessage(CopyTechMessageHandler.class, CopyTechMessage.class, Side.SERVER);

		PacketDispatcher.registerMessage(TechnologyMessageHandler.class, TechnologyMessage.class, Side.CLIENT);
		PacketDispatcher.registerMessage(TechnologyInfoMessageHandler.class, TechnologyInfoMessage.class, Side.CLIENT);
		PacketDispatcher.registerMessage(HintMessageHandler.class, HintMessage.class, Side.CLIENT);
	}

	@SuppressWarnings({"unchecked"})
	private static void registerMessage(Class handlerClass, Class messageClass, Side side) {
		PacketDispatcher.dispatcher.registerMessage(handlerClass, messageClass, packetId++, side);
	}

	public static void sendTo(IMessage message, EntityPlayerMP player) {
		PacketDispatcher.dispatcher.sendTo(message, player);
	}

	public static void sendToAll(IMessage message) {
		PacketDispatcher.dispatcher.sendToAll(message);
	}

	public static void sendToServer(IMessage message) {
		PacketDispatcher.dispatcher.sendToServer(message);
	}

}
