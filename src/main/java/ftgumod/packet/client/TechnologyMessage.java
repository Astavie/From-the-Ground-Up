package ftgumod.packet.client;

import ftgumod.FTGU;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.CapabilityTechnology.ITechnology;
import ftgumod.technology.TechnologyHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Collection;
import java.util.HashSet;

public class TechnologyMessage implements IMessage {

	public Collection<Integer> tech;
	public boolean force;

	public TechnologyMessage() {
	}

	public TechnologyMessage(Collection<Integer> tech, boolean force) {
		this.tech = tech;
		this.force = force;
	}

	public TechnologyMessage(EntityPlayer player, boolean force) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		if (cap != null) {
			tech = new HashSet<>();

			for (String s : cap.getResearched()) {
				if (s.endsWith(".unlock")) {
					//noinspection ConstantConditions
					tech.add(-TechnologyHandler.getTechnology(s.replace(".unlock", "")).getID());
				} else {
					//noinspection ConstantConditions
					tech.add(TechnologyHandler.getTechnology(s).getID());
				}
			}

			this.force = force;
		} else
			throw new IllegalArgumentException();
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		force = buffer.readBoolean();

		this.tech = new HashSet<>();
		int size = buffer.readInt();
		for (int i = 0; i < size; i++) {
			tech.add(buffer.readInt());
		}
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeBoolean(force);

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
			if (player == null)
				return null;

			ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
			if (cap != null) {
				if (!message.force && cap.getResearched().size() == message.tech.size())
					return null;

				cap.clear();
				for (Integer i : message.tech) {
					if (i < 0)
						//noinspection ConstantConditions
						cap.setResearched(TechnologyHandler.getTechnology(-i).getUnlocalizedName() + ".unlock");
					else
						//noinspection ConstantConditions
						cap.setResearched(TechnologyHandler.getTechnology(i).getUnlocalizedName());
				}
				FTGU.INSTANCE.runCompat("jei", message.tech);
			}

			return null;
		}

	}

}
