package ftgumod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;

public class CapabilityTechnology {

	@CapabilityInject(ITechnology.class)
	public static final Capability<ITechnology> TECH_CAP = null;

	@SubscribeEvent
	public void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone evt) {
		if (evt.isWasDeath()) {
			ITechnology cap1 = evt.getOriginal().getCapability(TECH_CAP, null);
			ITechnology cap2 = evt.getEntityPlayer().getCapability(TECH_CAP, null);
			cap2.setResearched(cap1.getResearched());

			if (!cap1.isNew())
				cap2.setOld();
		}
	}

	public interface ITechnology {

		public boolean isResearched(String tech);

		public boolean isNew();

		public void setOld();

		public void setResearched(String tech);

		public void setResearched(List<String> tech);

		public List<String> getResearched();

		public void clear();

		public void removeResearched(String tech);

	}

	public static class Storage implements IStorage<ITechnology> {

		@Override
		public NBTBase writeNBT(Capability<ITechnology> capability, ITechnology instance, EnumFacing side) {
			NBTTagList list = new NBTTagList();
			list.appendTag(new NBTTagString(new Boolean(instance.isNew()).toString()));
			for (String s : instance.getResearched()) {
				list.appendTag(new NBTTagString(s));
			}
			return list;
		}

		@Override
		public void readNBT(Capability<ITechnology> capability, ITechnology instance, EnumFacing side, NBTBase nbt) {
			NBTTagList list = (NBTTagList) nbt;
			if (list.getStringTagAt(0).equalsIgnoreCase("false"))
				instance.setOld();
			for (int i = 1; i < list.tagCount(); i++) {
				instance.setResearched(list.getStringTagAt(i));
			}
		}

	}

	public static class DefaultImpl implements ITechnology {

		private List<String> tech = new ArrayList<String>();
		private boolean isNew = true;

		@Override
		public boolean isResearched(String tech) {
			for (String s : this.tech) {
				if (s.equalsIgnoreCase(tech)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void setResearched(String tech) {
			if (!this.tech.contains(tech))
				this.tech.add(tech);
		}

		@Override
		public List<String> getResearched() {
			return tech;
		}

		@Override
		public void clear() {
			tech.clear();
		}

		@Override
		public void setResearched(List<String> tech) {
			this.tech = tech;
		}

		@Override
		public boolean isNew() {
			return isNew;
		}

		@Override
		public void setOld() {
			isNew = false;
		}

		@Override
		public void removeResearched(String tech) {
			this.tech.remove(tech);
		}

	}

}
