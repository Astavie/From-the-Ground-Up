package ftgumod.packet.client;

import ftgumod.FTGU;
import ftgumod.packet.MessageHandler;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.CapabilityTechnology.ITechnology;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;

public class TechnologyMessage implements IMessage {

	private Collection<String> tech;
	private boolean force;
	private Technology toast;

	public TechnologyMessage() {
	}

	public TechnologyMessage(EntityPlayer player, boolean force) {
		this(player, force, null);
	}

	public TechnologyMessage(EntityPlayer player, boolean force, @Nullable Technology toast) {
		ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		if (cap != null) {
			this.tech = cap.getResearched();
			this.force = force;
			this.toast = toast;
		} else
			throw new IllegalArgumentException();
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		force = buffer.readBoolean();

		this.tech = new HashSet<>();
		int size = buffer.readInt();
		for (int i = 0; i < size; i++)
			tech.add(ByteBufUtils.readUTF8String(buffer));

		if (buffer.readBoolean())
			toast = TechnologyHandler.getTechnology(new ResourceLocation(ByteBufUtils.readUTF8String(buffer)));
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeBoolean(force);

		if (tech != null) {
			buffer.writeInt(tech.size());
			for (String s : tech)
				ByteBufUtils.writeUTF8String(buffer, s);
		} else
			buffer.writeInt(0);

		if (toast != null) {
			buffer.writeBoolean(true);
			ByteBufUtils.writeUTF8String(buffer, toast.getRegistryName().toString());
		} else
			buffer.writeBoolean(false);
	}

	public static class TechnologyMessageHandler extends MessageHandler<TechnologyMessage> {

		@Override
		public IMessage handleMessage(EntityPlayer player, TechnologyMessage message, MessageContext ctx) {
			if (player == null)
				return null;

			ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
			if (cap != null) {
				if (!message.force && cap.getResearched().size() == message.tech.size())
					return null;

				cap.clear();
				cap.setResearched(message.tech);

				if (message.toast != null)
					FTGU.PROXY.showTechnologyToast(message.toast);

				FTGU.INSTANCE.runCompat("jei", message.tech);
			}

			return null;
		}

	}

}
