package ftgumod.packet;

import ftgumod.FTGU;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class MessageHandler<T extends IMessage> implements IMessageHandler<T, IMessage> {

	public abstract IMessage handleMessage(EntityPlayer player, T message);

	@Override
	public IMessage onMessage(T message, MessageContext ctx) {
		return handleMessage(FTGU.PROXY.getPlayerEntity(ctx), message);
	}
}
