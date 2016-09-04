package ftgumod.packet.client;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ftgumod.CapabilityTechnology;
import ftgumod.TechnologyHandler;
import ftgumod.CapabilityTechnology.ITechnology;

public class TechnologyMessage implements IMessage {

	public List<Integer> tech;

	public TechnologyMessage() {
	}

	public TechnologyMessage(List<Integer> tech) {
		this.tech = tech;
	}

	public TechnologyMessage(EntityPlayer player) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		List<Integer> ints = new ArrayList<Integer>();

		for (String s : cap.getResearched()) {
			if (s.endsWith(".unlock")) {
				String s2 = s.replace(".unlock", "");
				ints.add(-TechnologyHandler.getTechnology(s2).getID());
			} else {
				ints.add(TechnologyHandler.getTechnology(s).getID());
			}
		}

		tech = ints;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.tech = new ArrayList<Integer>();
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
			if (player != null) {
				ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
				cap.clear();
				for (Integer i : message.tech) {
					if (i < 0)
						cap.setResearched(TechnologyHandler.getTechnology(-i).getUnlocalisedName() + ".unlock");
					else
						cap.setResearched(TechnologyHandler.getTechnology(i).getUnlocalisedName());
				}
			}
			return null;
		}

	}

}
