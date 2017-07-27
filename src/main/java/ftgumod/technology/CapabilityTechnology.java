package ftgumod.technology;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.HashSet;

public class CapabilityTechnology {

	@CapabilityInject(ITechnology.class)
	public static Capability<ITechnology> TECH_CAP;

	@SubscribeEvent
	public void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone evt) {
		if (evt.isWasDeath()) {
			ITechnology cap1 = evt.getOriginal().getCapability(TECH_CAP, null);
			ITechnology cap2 = evt.getEntityPlayer().getCapability(TECH_CAP, null);

			if (cap1 != null && cap2 != null) {
				cap2.setResearched(cap1.getResearched());

				if (!cap1.isNew())
					cap2.setOld();
			}
		}
	}

	public interface ITechnology {

		boolean isResearched(String tech);

		boolean isNew();

		void setOld();

		void setResearched(String tech);

		Collection<String> getResearched();

		void setResearched(Collection<String> tech);

		void clear();

		void removeResearched(String tech);

	}

	public static class Storage implements IStorage<ITechnology> {

		@Override
		public NBTBase writeNBT(Capability<ITechnology> capability, ITechnology instance, EnumFacing side) {
			NBTTagList list = new NBTTagList();
			list.appendTag(new NBTTagString(Boolean.toString(instance.isNew())));
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

		private Collection<String> tech = new HashSet<>();
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
		public Collection<String> getResearched() {
			return tech;
		}

		@Override
		public void setResearched(Collection<String> tech) {
			this.tech.addAll(tech);
		}

		@Override
		public void clear() {
			tech.clear();
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
