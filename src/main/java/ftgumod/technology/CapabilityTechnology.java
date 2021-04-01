package ftgumod.technology;

import ftgumod.FTGU;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.HashSet;

public class CapabilityTechnology {

	@CapabilityInject(ITechnology.class)
	public static Capability<ITechnology> TECH_CAP;

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone evt) {
		ITechnology cap1 = evt.getOriginal().getCapability(TECH_CAP, null);
		ITechnology cap2 = evt.getEntityPlayer().getCapability(TECH_CAP, null);

		if (cap1 != null && cap2 != null) {
			cap2.setResearched(cap1.getResearched());
			if (!cap1.isNew())
				cap2.setOld();
		}
	}

	@SuppressWarnings("rawtypes")
	@SubscribeEvent
	public void onEntityConstruct(AttachCapabilitiesEvent evt) {
		if (evt.getObject() instanceof EntityPlayer) {
			evt.addCapability(new ResourceLocation(FTGU.MODID, "ITechnology"), new ICapabilitySerializable<NBTTagCompound>() {

				private final ITechnology inst = CapabilityTechnology.TECH_CAP.getDefaultInstance();

				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
					return capability == CapabilityTechnology.TECH_CAP;
				}

				@Override
				public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
					return capability == CapabilityTechnology.TECH_CAP ? CapabilityTechnology.TECH_CAP.<T>cast(inst) : null;
				}

				@Override
				public NBTTagCompound serializeNBT() {
					return (NBTTagCompound) CapabilityTechnology.TECH_CAP.getStorage().writeNBT(CapabilityTechnology.TECH_CAP, inst, null);
				}

				@Override
				public void deserializeNBT(NBTTagCompound nbt) {
					CapabilityTechnology.TECH_CAP.getStorage().readNBT(CapabilityTechnology.TECH_CAP, inst, null, nbt);
				}

			});
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
			NBTTagCompound compound = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			for (String s : instance.getResearched())
				list.appendTag(new NBTTagString(s));
			compound.setBoolean("new", instance.isNew());
			compound.setTag("researched", list);
			return compound;
		}

		@Override
		public void readNBT(Capability<ITechnology> capability, ITechnology instance, EnumFacing side, NBTBase nbt) {
			NBTTagCompound compound = (NBTTagCompound) nbt;
			if (!compound.getBoolean("new"))
				instance.setOld();
			NBTTagList list = compound.getTagList("researched", Constants.NBT.TAG_STRING);
			for (int i = 0; i < list.tagCount(); i++)
				instance.setResearched(list.getStringTagAt(i));
		}

	}

	public static class DefaultImpl implements ITechnology {

		private final Collection<String> tech = new HashSet<>();
		private boolean isNew = true;

		@Override
		public boolean isResearched(String tech) {
			return this.tech.contains(tech);
		}

		@Override
		public void setResearched(String tech) {
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
			this.tech.removeIf(string -> string.startsWith(tech + "#"));
		}

	}

}
