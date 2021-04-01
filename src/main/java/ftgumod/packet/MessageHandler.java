package ftgumod.packet;

import ftgumod.FTGU;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public abstract class MessageHandler<T extends IMessage>
    implements IMessageHandler<T, IMessage> {

  public abstract IMessage handleMessage(EntityPlayer player, T message);

  @Override
  public IMessage onMessage(T message, MessageContext ctx) {

    final IThreadListener target = ctx.side == Side.CLIENT ? Minecraft.getMinecraft() : FMLCommonHandler.instance()
        .getMinecraftServerInstance();

    if (target != null) {
      target.addScheduledTask(new Runner(message, this, ctx));
    }

    return null;
  }

  private final class Runner
      implements Runnable {

    private final T message;
    private final MessageContext ctx;
    private final MessageHandler<T> handler;

    public Runner(final T message, MessageHandler<T> handler, final MessageContext ctx) {

      this.message = message;
      this.handler = handler;
      this.ctx = ctx;
    }

    @Override
    public void run() {

      final IMessage reply = this.handler.handleMessage(FTGU.PROXY.getPlayerEntity(this.ctx), this.message);

      if (reply != null) {

        if (this.ctx.side == Side.CLIENT) {
          PacketDispatcher.sendToServer(reply);

        } else {
          final EntityPlayerMP player = this.ctx.getServerHandler().player;

          if (player != null) {
            PacketDispatcher.sendTo(reply, player);
          }
        }
      }
    }
  }
}
