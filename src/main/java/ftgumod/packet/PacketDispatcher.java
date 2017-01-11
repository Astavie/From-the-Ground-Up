package ftgumod.packet;

import ftgumod.packet.client.ClientMessageHandler;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.packet.client.TechnologyMessage.TechnologyMessageHandler;
import ftgumod.packet.server.CopyTechMessage;
import ftgumod.packet.server.CopyTechMessage.CopyTechMessageHandler;
import ftgumod.packet.server.RequestTechMessage;
import ftgumod.packet.server.RequestTechMessage.RequestTechMessageHandler;
import ftgumod.packet.server.UnlockTechMessage;
import ftgumod.packet.server.UnlockTechMessage.UnlockTechMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketDispatcher {

	private static byte packetId = 0;

	private static SimpleNetworkWrapper dispatcher;

	public static final void registerPackets() {
		dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel("ftgu");
		PacketDispatcher.registerMessage(RequestTechMessageHandler.class, RequestTechMessage.class, Side.SERVER);
		PacketDispatcher.registerMessage(UnlockTechMessageHandler.class, UnlockTechMessage.class, Side.SERVER);
		PacketDispatcher.registerMessage(CopyTechMessageHandler.class, CopyTechMessage.class, Side.SERVER);
		PacketDispatcher.registerMessage(TechnologyMessageHandler.class, TechnologyMessage.class, Side.CLIENT);
	}

	private static final void registerMessage(Class handlerClass, Class messageClass, Side side) {
		PacketDispatcher.dispatcher.registerMessage(handlerClass, messageClass, packetId++, side);
	}

	private static final void registerMessage(Class handlerClass, Class messageClass) {
		Side side = ClientMessageHandler.class.isAssignableFrom(handlerClass) ? Side.CLIENT : Side.SERVER;
		PacketDispatcher.dispatcher.registerMessage(handlerClass, messageClass, packetId++, side);
	}

	public static final void sendTo(IMessage message, EntityPlayerMP player) {
		PacketDispatcher.dispatcher.sendTo(message, player);
	}

	public static final void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
		PacketDispatcher.dispatcher.sendToAllAround(message, point);
	}

	public static final void sendToAllAround(IMessage message, int dimension, double x, double y, double z, double range) {
		PacketDispatcher.sendToAllAround(message, new NetworkRegistry.TargetPoint(dimension, x, y, z, range));
	}

	public static final void sendToAllAround(IMessage message, EntityPlayer player, double range) {
		PacketDispatcher.sendToAllAround(message, player.world.provider.getDimension(), player.posX, player.posY, player.posZ, range);
	}

	public static final void sendToDimension(IMessage message, int dimensionId) {
		PacketDispatcher.dispatcher.sendToDimension(message, dimensionId);
	}

	public static final void sendToServer(IMessage message) {
		PacketDispatcher.dispatcher.sendToServer(message);
	}
}
