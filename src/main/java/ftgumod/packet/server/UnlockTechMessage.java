package ftgumod.packet.server;

import ftgumod.packet.client.TechnologyMessage;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.CapabilityTechnology.ITechnology;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UnlockTechMessage implements IMessage {

	public int tech;

	public UnlockTechMessage() {
	}

	public UnlockTechMessage(int tech) {
		this.tech = tech;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		tech = buffer.readInt();
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(tech);
	}

	public static class UnlockTechMessageHandler extends ServerMessageHandler<UnlockTechMessage> {

		@Override
		public IMessage handleServerMessage(EntityPlayer player, UnlockTechMessage message, MessageContext ctx) {
			if (player != null && player.capabilities.isCreativeMode) {
				ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
				Technology t = TechnologyHandler.getTechnology(message.tech);

				if (t.isResearched(player)) {
					cap.removeResearched(TechnologyHandler.getTechnology(message.tech).getUnlocalisedName());
					cap.removeResearched(
							TechnologyHandler.getTechnology(message.tech).getUnlocalisedName() + ".unlock");
				} else {
					cap.setResearched(TechnologyHandler.getTechnology(message.tech).getUnlocalisedName());
				}
			}
			return new TechnologyMessage(player);
		}

	}

}
