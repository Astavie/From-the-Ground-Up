package ftgumod.packet.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import ftgumod.FTGU;
import ftgumod.api.event.FTGUClientSyncEvent;
import ftgumod.api.technology.ITechnology;
import ftgumod.compat.jei.CompatJEI;
import ftgumod.packet.MessageHandler;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class TechnologyMessage implements IMessage {

	private Collection<String> tech;
	private boolean force;
	private ITechnology[] toasts;

	public TechnologyMessage() {
	}

	public TechnologyMessage(EntityPlayer player, boolean force, ITechnology... toasts) {
		CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
		if (cap != null) {
			this.tech = cap.getResearched();
			this.force = force;
			this.toasts = toasts;
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

		toasts = new ITechnology[buffer.readInt()];
		for (int i = 0; i < toasts.length; i++)
			toasts[i] = TechnologyManager.INSTANCE
					.getTechnology(new ResourceLocation(ByteBufUtils.readUTF8String(buffer)));
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

		buffer.writeInt(toasts.length);
		for (ITechnology toast : toasts)
			ByteBufUtils.writeUTF8String(buffer, toast.getRegistryName().toString());
	}

	public static class TechnologyMessageHandler extends MessageHandler<TechnologyMessage> {

		@Override
		public IMessage handleMessage(EntityPlayer player, TechnologyMessage message) {
			if (player == null)
				return null;

			CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
			if (cap != null) {
				if (!message.force && cap.getResearched().size() == message.tech.size())
					return null;

				// Defensive copy to prevent concurrent modification exception
				Collection<String> researched = new ArrayList<>(cap.getResearched());
				for (String name : researched)
					if (!message.tech.contains(name)) {
						cap.removeResearched(name);

						String[] split = name.split("#");
						if (split.length == 2) {
							Technology tech = TechnologyManager.INSTANCE.getTechnology(new ResourceLocation(split[0]));
							if (tech != null) {
								TechnologyManager.INSTANCE.getProgress(player, tech).revokeCriterion(split[1]);
							}
						}
					}

				for (String name : message.tech)
					if (!cap.isResearched(name)) {
						cap.setResearched(name);

						String[] split = name.split("#");
						if (split.length == 2) {
							Technology tech = TechnologyManager.INSTANCE.getTechnology(new ResourceLocation(split[0]));
							if (tech != null) {
								TechnologyManager.INSTANCE.getProgress(player, tech).grantCriterion(split[1]);
							}
						}
					}

				for (ITechnology toast : message.toasts)
					FTGU.PROXY.displayToastTechnology(toast);

				if(FTGU.JEI_LOADED)
					CompatJEI.refreshHiddenItems(false);
				MinecraftForge.EVENT_BUS.post(new FTGUClientSyncEvent.Post());
			}

			return null;
		}

	}

}
