package ftgumod.packet.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import ftgumod.FTGU;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.CapabilityTechnology.ITechnology;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TechnologyMessage implements IMessage {

	public Collection<Integer> tech;

	public TechnologyMessage() {
	}

	public TechnologyMessage(Collection<Integer> tech) {
		this.tech = tech;
	}

	public TechnologyMessage(EntityPlayer player) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		tech = new HashSet<Integer>();

		for (String s : cap.getResearched()) {
			if (s.endsWith(".unlock")) {
				String s2 = s.replace(".unlock", "");
				tech.add(-TechnologyHandler.getTechnology(s2).getID());
			} else {
				tech.add(TechnologyHandler.getTechnology(s).getID());
			}
		}
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.tech = new HashSet<Integer>();
		int size = buffer.readInt();
		for (int i = 0; i < size; i++) {
			tech.add(buffer.readInt());
		}
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		if (tech != null) {
			buffer.writeInt(tech.size());
			for (Integer i : tech) {
				buffer.writeInt(i);
			}
		} else {
			buffer.writeInt(0);
		}
	}

	public static class TechnologyMessageHandler extends ClientMessageHandler<TechnologyMessage> {

		@Override
		public IMessage handleClientMessage(EntityPlayer player, TechnologyMessage message, MessageContext ctx) {
			ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
			cap.clear();
			for (Integer i : message.tech) {
				if (i < 0)
					cap.setResearched(TechnologyHandler.getTechnology(-i).getUnlocalisedName() + ".unlock");
				else
					cap.setResearched(TechnologyHandler.getTechnology(i).getUnlocalisedName());
			}
			return null;
		}

	}

}
