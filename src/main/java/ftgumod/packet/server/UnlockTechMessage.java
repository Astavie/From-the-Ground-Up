package ftgumod.packet.server;

import ftgumod.packet.MessageHandler;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UnlockTechMessage implements IMessage {

	private String tech;

	public UnlockTechMessage() {
	}

	public UnlockTechMessage(Technology tech) {
		this.tech = tech.getRegistryName().toString();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tech = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, tech);
	}

	public static class UnlockTechMessageHandler extends MessageHandler<UnlockTechMessage> {

		@Override
		public IMessage handleMessage(EntityPlayer player, UnlockTechMessage message, MessageContext ctx) {
			if (player != null && player.capabilities.isCreativeMode) {
				Technology t = TechnologyHandler.technologies.get(new ResourceLocation(message.tech));

				if (t != null) {
					if (t.isResearched(player)) {
						t.removeResearched(player);
					} else {
						t.setResearched(player);
					}

					return new TechnologyMessage(player, true);
				}
			}
			return null;
		}

	}

}
