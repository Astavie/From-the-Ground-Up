package ftgumod;

import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import org.lwjgl.input.Keyboard;
import ftgumod.CapabilityTechnology.ITechnology;
import ftgumod.TechnologyHandler.GUI;
import ftgumod.item.ItemParchmentResearch;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.workbench.ContainerWorkbenchTech;

public class EventHandler {

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent evt) {
		Item item = evt.getItemStack().getItem();
		if (item == FTGUAPI.i_lookingGlass) {
			NBTTagList blocks = TechnologyUtil.getItemData(evt.getItemStack()).getTagList("FTGU", NBT.TAG_STRING);
			if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				for (int i = 0; i < blocks.tagCount(); i++)
					evt.getToolTip().add("§5§o" + I18n.translateToLocal(blocks.getStringTagAt(i) + ".name"));
				if (blocks.tagCount() > 0)
					evt.getToolTip().add("");
			} else if (blocks.tagCount() > 0) {
				evt.getToolTip().add(I18n.translateToLocal("item.looking_glass.shift"));
				evt.getToolTip().add("");
			}

			evt.getToolTip().add("§4" + I18n.translateToLocal("technology.decipher.tooltip"));
		} else if (item == FTGUAPI.i_parchmentIdea) {
			Technology tech = TechnologyHandler.getTechnology(TechnologyUtil.getItemData(evt.getItemStack()).getString("FTGU"));
			String k = tech.canResearch(evt.getEntityPlayer()) ? "" : "§k";
			evt.getToolTip().add("§6" + tech.getLocalisedName() + " " + I18n.translateToLocal("technology.idea"));
			evt.getToolTip().add("§5§o" + k + tech.getDescription());
		} else if (item == FTGUAPI.i_parchmentResearch) {
			Technology tech = TechnologyHandler.getTechnology(TechnologyUtil.getItemData(evt.getItemStack()).getString("FTGU"));
			String k = tech.canResearch(evt.getEntityPlayer()) ? "" : "§k";
			evt.getToolTip().add("§6" + tech.getLocalisedName() + " " + I18n.translateToLocal("technology.research"));
			evt.getToolTip().add("§5§o" + k + tech.getDescription());
			evt.getToolTip().add("");
			evt.getToolTip().add("§4" + I18n.translateToLocal("item.parchment_research.complete"));
		}
	}

	@SubscribeEvent
	public void onItemCraft(ItemCraftedEvent evt) {
		if (evt.crafting.getItem() == FTGUAPI.i_researchBook) {
			for (int i = 0; i < evt.craftMatrix.getSizeInventory(); i++) {
				ItemStack item = evt.craftMatrix.getStackInSlot(i);
				if (item != null && item.getItem() == FTGUAPI.i_parchmentResearch) {
					((ItemParchmentResearch) item.getItem()).research(item, evt.player, false);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerJoin(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent evt) {
		List<String> headstart = Arrays.asList(TechnologyHandler.STONECRAFT.getUnlocalisedName(), TechnologyHandler.STONEWORKING.getUnlocalisedName(), TechnologyHandler.CARPENTRY.getUnlocalisedName(), TechnologyHandler.REFINEMENT.getUnlocalisedName(), TechnologyHandler.BIBLIOGRAPHY.getUnlocalisedName(), TechnologyHandler.ADVANCED_COMBAT.getUnlocalisedName(), TechnologyHandler.BUILDING_BLOCKS.getUnlocalisedName(), TechnologyHandler.COOKING.getUnlocalisedName());
		ITechnology cap = evt.player.getCapability(CapabilityTechnology.TECH_CAP, null);
		if (cap.isNew()) {
			evt.player.inventory.addItemStackToInventory(new ItemStack(FTGUAPI.i_researchBook));

			if (FTGU.moddedOnly) {
				cap.setResearched(TechnologyHandler.vanilla);
			} else if (FTGU.headstart) {
				cap.setResearched(headstart);
			}

			cap.setOld();
		} else
			PacketDispatcher.sendTo(new TechnologyMessage(evt.player), (EntityPlayerMP) evt.player);
	}

	@SubscribeEvent
	public void onPlayerOpenContainer(PlayerOpenContainerEvent evt) {
		Container work = evt.getEntityPlayer().openContainer;
		if (work instanceof ContainerWorkbench && !(work instanceof ContainerWorkbenchTech)) {
			evt.getEntityPlayer().openGui(FTGU.instance, GUI.CRAFTINGTABLETECH.ordinal(), evt.getEntity().worldObj, 0, 0, 0);
		}
	}

	@SubscribeEvent
	public void onEntityConstruct(AttachCapabilitiesEvent evt) {
		if (evt.getObject() instanceof EntityPlayer) {
			evt.addCapability(new ResourceLocation(FTGU.MODID, "ITechnology"), new ICapabilitySerializable<NBTTagList>() {

				ITechnology inst = CapabilityTechnology.TECH_CAP.getDefaultInstance();

				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
					return capability == CapabilityTechnology.TECH_CAP;
				}

				@Override
				public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
					return capability == CapabilityTechnology.TECH_CAP ? CapabilityTechnology.TECH_CAP.<T> cast(inst) : null;
				}

				@Override
				public NBTTagList serializeNBT() {
					return (NBTTagList) CapabilityTechnology.TECH_CAP.getStorage().writeNBT(CapabilityTechnology.TECH_CAP, inst, null);
				}

				@Override
				public void deserializeNBT(NBTTagList nbt) {
					CapabilityTechnology.TECH_CAP.getStorage().readNBT(CapabilityTechnology.TECH_CAP, inst, null, nbt);
				}
			});
		}
	}

}
