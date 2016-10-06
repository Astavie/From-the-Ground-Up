package ftgumod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import org.lwjgl.input.Keyboard;
import ftgumod.CapabilityTechnology.ITechnology;
import ftgumod.TechnologyHandler.GUI;
import ftgumod.event.PlayerInspectEvent;
import ftgumod.item.ItemParchmentResearch;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.workbench.ContainerWorkbenchTech;

public class EventHandler {

	@SubscribeEvent
	public void onPlayerInspect(PlayerInspectEvent evt) {
		if (!evt.getWorld().isRemote && evt.getBlock().getItem() == Item.getItemFromBlock(Blocks.SOUL_SAND) && ticks.get(evt.getEntityPlayer().getUniqueID()) > t) {
			ITechnology cap = evt.getEntityPlayer().getCapability(CapabilityTechnology.TECH_CAP, null);
			if (cap.isResearched(TechnologyHandler.ENCHANTING.getUnlocalisedName()) && !cap.isResearched(TechnologyHandler.GLOWING_EYES.getUnlocalisedName() + ".unlock")) {
				evt.setUseful(true);
				cap.setResearched(TechnologyHandler.GLOWING_EYES.getUnlocalisedName() + ".unlock");

				evt.getEntityPlayer().addChatMessage(new TextComponentString(TextFormatting.DARK_GRAY + I18n.translateToLocal("technology.noise.whisper2")));
				evt.getEntityPlayer().addChatMessage(new TextComponentString(I18n.translateToLocal("technology.complete.unlock") + " \"" + TechnologyHandler.GLOWING_EYES.getLocalisedName() + "\"!"));
				evt.getEntityPlayer().worldObj.playSound(null, evt.getEntityPlayer().getPosition(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 1.0F, 1.0F);

				PacketDispatcher.sendTo(new TechnologyMessage(evt.getEntityPlayer()), (EntityPlayerMP) evt.getEntityPlayer());
			}
		}
	}

	private static final Map<UUID, Integer> ticks = new HashMap<UUID, Integer>();

	public int s = 5;
	public int t = s * 20;

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent evt) {
		if (!evt.getEntity().worldObj.isRemote && evt.getEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) evt.getEntity();
			if (player.worldObj.getBlockState(player.getPosition().offset(EnumFacing.DOWN, 1)).getBlock() == Blocks.SOUL_SAND) {
				UUID uuid = player.getUniqueID();
				if (!ticks.containsKey(uuid)) {
					ticks.put(uuid, 0);
				} else {
					int tick = ticks.get(uuid);
					if (tick == t) {
						player.addChatMessage(new TextComponentString(TextFormatting.DARK_GRAY + I18n.translateToLocal("technology.noise.whisper1")));
						player.worldObj.playSound(null, player.getPosition(), SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
					}
					if (!(tick > t)) {
						ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
						if (cap.isResearched(TechnologyHandler.ENCHANTING.getUnlocalisedName()) && !cap.isResearched(TechnologyHandler.GLOWING_EYES.getUnlocalisedName() + ".unlock")) {
							ticks.remove(uuid);
							ticks.put(uuid, tick + 1);
						}
					}
				}
			}

			if (!TechnologyHandler.ENCHANTING.isUnlocked(player) && TechnologyHandler.ENCHANTING.canResearchIgnoreCustomUnlock(player)) {
				for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
					ItemStack stack = player.inventory.getStackInSlot(i);
					if (stack != null && stack.getItem() == Items.ENCHANTED_BOOK) {
						ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
						cap.setResearched(TechnologyHandler.ENCHANTING.getUnlocalisedName() + ".unlock");

						player.addChatMessage(new TextComponentString(I18n.translateToLocal("technology.complete.unlock") + " \"" + TechnologyHandler.ENCHANTING.getLocalisedName() + "\"!"));
						player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

						PacketDispatcher.sendTo(new TechnologyMessage(player), (EntityPlayerMP) player);
						break;
					}
				}
			}

			if (!TechnologyHandler.ENDER_KNOWLEDGE.isUnlocked(player) && TechnologyHandler.GLOWING_EYES.isResearched(player) && hasBlock(player.getPosition(), Blocks.DRAGON_EGG, 5, player.worldObj)) {
				ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
				cap.setResearched(TechnologyHandler.ENDER_KNOWLEDGE.getUnlocalisedName() + ".unlock");

				player.addChatMessage(new TextComponentString(I18n.translateToLocal("technology.complete.unlock") + " \"" + TechnologyHandler.ENDER_KNOWLEDGE.getLocalisedName() + "\"!"));
				player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

				PacketDispatcher.sendTo(new TechnologyMessage(player), (EntityPlayerMP) player);
			}
		}
	}

	private static boolean hasBlock(BlockPos pos, Block block, int radius, World world) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		x -= radius / 2;
		y -= 1;
		z -= radius / 2;
		for (int y1 = y; y1 < y + 2; y1++) {
			for (int x1 = x; x1 < x + radius; x1++) {
				for (int z1 = z; z1 < z + radius; z1++) {
					Block b = world.getBlockState(new BlockPos(x1, y1, z1)).getBlock();
					if (b == block)
						return true;
				}
			}
		}
		return false;
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone evt) {
		if (!evt.getOriginal().worldObj.isRemote)
			ticks.remove(evt.getOriginal().getUniqueID());
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent evt) {
		Item item = evt.getItemStack().getItem();
		if (item == FTGUAPI.i_lookingGlass) {
			NBTTagList blocks = TechnologyUtil.getItemData(evt.getItemStack()).getTagList("FTGU", NBT.TAG_STRING);
			if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				for (int i = 0; i < blocks.tagCount(); i++)
					evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + I18n.translateToLocal(blocks.getStringTagAt(i) + ".name"));
				if (blocks.tagCount() > 0)
					evt.getToolTip().add("");
			} else if (blocks.tagCount() > 0) {
				evt.getToolTip().add(I18n.translateToLocal("item.looking_glass.shift"));
				evt.getToolTip().add("");
			}

			evt.getToolTip().add(TextFormatting.DARK_RED + I18n.translateToLocal("technology.decipher.tooltip"));
		} else if (item == FTGUAPI.i_parchmentIdea) {
			Technology tech = TechnologyHandler.getTechnology(TechnologyUtil.getItemData(evt.getItemStack()).getString("FTGU"));
			
			if (tech != null) {
				String k = tech.canResearchIgnoreResearched(evt.getEntityPlayer()) ? "" : "" + TextFormatting.OBFUSCATED;
				evt.getToolTip().add(TextFormatting.GOLD + tech.getLocalisedName() + " " + I18n.translateToLocal("technology.idea"));
				evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + k + tech.getDescription());
			}
		} else if (item == FTGUAPI.i_parchmentResearch) {
			Technology tech = TechnologyHandler.getTechnology(TechnologyUtil.getItemData(evt.getItemStack()).getString("FTGU"));
			
			if (tech != null) {
				String k = tech.canResearchIgnoreResearched(evt.getEntityPlayer()) ? "" : "" + TextFormatting.OBFUSCATED;
				evt.getToolTip().add(TextFormatting.GOLD + tech.getLocalisedName() + " " + I18n.translateToLocal("technology.research"));
				evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + k + tech.getDescription());
				evt.getToolTip().add("");
				evt.getToolTip().add(TextFormatting.DARK_RED + I18n.translateToLocal("item.parchment_research.complete"));
			}
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
		if (!evt.player.worldObj.isRemote)
			ticks.remove(evt.player.getUniqueID());

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
	public void onPlayerOpenContainer(PlayerContainerEvent.Open evt) {
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
