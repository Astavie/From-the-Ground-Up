package ftgumod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import ftgumod.event.PlayerInspectEvent;
import ftgumod.event.PlayerLockEvent;
import ftgumod.item.ItemParchmentResearch;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.CapabilityTechnology.ITechnology;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.TechnologyUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
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
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.common.MinecraftForge;
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
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventHandler {

	@SubscribeEvent
	public void onPlayerInspect(PlayerInspectEvent evt) {
		if (!evt.getWorld().isRemote && evt.getBlock().getItem() == Item.getItemFromBlock(Blocks.SOUL_SAND) && ticks.get(evt.getEntityPlayer().getUniqueID()) > t) {
			ITechnology cap = evt.getEntityPlayer().getCapability(CapabilityTechnology.TECH_CAP, null);
			if (cap.isResearched(TechnologyHandler.ENCHANTING.getUnlocalisedName()) && !cap.isResearched(TechnologyHandler.GLOWING_EYES.getUnlocalisedName() + ".unlock")) {
				evt.setUseful(true);
				cap.setResearched(TechnologyHandler.GLOWING_EYES.getUnlocalisedName() + ".unlock");

				evt.getEntityPlayer().sendMessage(new TextComponentString(TextFormatting.DARK_GRAY + I18n.translateToLocal("technology.noise.whisper2")));
				evt.getEntityPlayer().sendMessage(new TextComponentString(I18n.translateToLocal("technology.complete.unlock") + " \"" + TechnologyHandler.GLOWING_EYES.getLocalisedName() + "\"!"));
				evt.getEntityPlayer().world.playSound(null, evt.getEntityPlayer().getPosition(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 1.0F, 1.0F);

				PacketDispatcher.sendTo(new TechnologyMessage(evt.getEntityPlayer(), true), (EntityPlayerMP) evt.getEntityPlayer());
			}
		}
	}

	private final Map<UUID, Integer> ticks = new HashMap<UUID, Integer>();

	public int s = 5;
	public int t = s * 20;

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent evt) {
		if (!evt.getEntity().world.isRemote && evt.getEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) evt.getEntity();
			if (player.world.getBlockState(player.getPosition().offset(EnumFacing.DOWN, 1)).getBlock() == Blocks.SOUL_SAND) {
				UUID uuid = player.getUniqueID();
				if (!ticks.containsKey(uuid)) {
					ticks.put(uuid, 0);
				} else {
					int tick = ticks.get(uuid);
					if (tick == t) {
						player.sendMessage(new TextComponentString(TextFormatting.DARK_GRAY + I18n.translateToLocal("technology.noise.whisper1")));
						player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
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
					if (stack != ItemStack.EMPTY && stack.getItem() == Items.ENCHANTED_BOOK) {
						ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
						cap.setResearched(TechnologyHandler.ENCHANTING.getUnlocalisedName() + ".unlock");

						player.sendMessage(new TextComponentString(I18n.translateToLocal("technology.complete.unlock") + " \"" + TechnologyHandler.ENCHANTING.getLocalisedName() + "\"!"));
						player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

						PacketDispatcher.sendTo(new TechnologyMessage(player, true), (EntityPlayerMP) player);
						break;
					}
				}
			}

			if (!TechnologyHandler.ENDER_KNOWLEDGE.isUnlocked(player) && TechnologyHandler.GLOWING_EYES.isResearched(player) && hasBlock(player.getPosition(), Blocks.DRAGON_EGG, 5, player.world)) {
				ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
				cap.setResearched(TechnologyHandler.ENDER_KNOWLEDGE.getUnlocalisedName() + ".unlock");

				player.sendMessage(new TextComponentString(I18n.translateToLocal("technology.complete.unlock") + " \"" + TechnologyHandler.ENDER_KNOWLEDGE.getLocalisedName() + "\"!"));
				player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

				PacketDispatcher.sendTo(new TechnologyMessage(player, true), (EntityPlayerMP) player);
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
				evt.getToolTip().add(TextFormatting.GOLD + I18n.translateToLocalFormatted("technology.idea", tech.getLocalisedName()));
				evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + k + tech.getDescription());
			}
		} else if (item == FTGUAPI.i_parchmentResearch) {
			Technology tech = TechnologyHandler.getTechnology(TechnologyUtil.getItemData(evt.getItemStack()).getString("FTGU"));

			if (tech != null) {
				String k = tech.canResearchIgnoreResearched(evt.getEntityPlayer()) ? "" : "" + TextFormatting.OBFUSCATED;
				evt.getToolTip().add(TextFormatting.GOLD + I18n.translateToLocalFormatted("technology.research", tech.getLocalisedName()));
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
				if (item != ItemStack.EMPTY && item.getItem() == FTGUAPI.i_parchmentResearch) {
					((ItemParchmentResearch) item.getItem()).research(item, evt.player, false);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		if (!evt.player.world.isRemote) {
			ContainerPlayer inv = (ContainerPlayer) evt.player.openContainer;
			inv.addListener(new CraftingListener(evt.player));

			ticks.remove(evt.player.getUniqueID());
		}

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
			PacketDispatcher.sendTo(new TechnologyMessage(evt.player, false), (EntityPlayerMP) evt.player);
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone evt) {
		if (!evt.getEntity().world.isRemote) {
			ContainerPlayer inv = (ContainerPlayer) evt.getEntityPlayer().openContainer;
			inv.addListener(new CraftingListener(evt.getEntityPlayer()));

			ticks.remove(evt.getOriginal().getUniqueID());
		}
	}

	@SubscribeEvent
	public void onPlayerOpenContainer(PlayerContainerEvent.Open evt) {
		Container work = evt.getEntityPlayer().openContainer;
		work.addListener(new CraftingListener(evt.getEntityPlayer()));
	}

	@SubscribeEvent
	public void onPlayerCloseContainer(PlayerContainerEvent.Close evt) {
		Container inv = evt.getEntityPlayer().openContainer;
		inv.addListener(new CraftingListener(evt.getEntityPlayer()));
	}

	private ItemStack stack = ItemStack.EMPTY;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPlayerInGui(DrawScreenEvent.Pre evt) {
		Gui work = evt.getGui();
		if (work instanceof GuiContainer) {
			Container inv = ((GuiContainer) work).inventorySlots;
			for (Slot s : inv.inventorySlots) {
				if (s.inventory instanceof InventoryCraftResult) {
					ItemStack stack = s.inventory.getStackInSlot(0);
					if (stack != this.stack) {
						PlayerLockEvent event = new PlayerLockEvent(Minecraft.getMinecraft().player, stack);
						if (stack != ItemStack.EMPTY)
							MinecraftForge.EVENT_BUS.post(event);

						this.stack = event.willLock() ? ItemStack.EMPTY : stack;
						s.inventory.setInventorySlotContents(0, this.stack);
					}
					return;
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
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
