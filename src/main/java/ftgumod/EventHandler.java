package ftgumod;

import ftgumod.event.PlayerInspectEvent;
import ftgumod.event.PlayerLockEvent;
import ftgumod.item.ItemParchmentResearch;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.server.RecipeBookServerImpl;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.CapabilityTechnology.ITechnology;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.TechnologyUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.util.*;

public class EventHandler {

	private static final Field BOOK = ReflectionHelper.findField(EntityPlayerMP.class, "recipeBook", "field_192041_cq");

	private final Map<UUID, Integer> ticks = new HashMap<>();
	private int s = 5; // 5 seconds
	private int t = s * 20; // 5 * 20 ticks
	private ItemStack stack = ItemStack.EMPTY;

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
	public void onPlayerInspect(PlayerInspectEvent evt) {
		if (!evt.getWorld().isRemote && evt.getBlock().getItem() == Item.getItemFromBlock(Blocks.SOUL_SAND) && ticks.get(evt.getEntityPlayer().getUniqueID()) > t) {
			EntityPlayer player = evt.getEntityPlayer();
			if (!TechnologyHandler.GLOWING_EYES.isUnlocked(player) && TechnologyHandler.GLOWING_EYES.canResearchIgnoreCustomUnlock(player)) {
				evt.setUseful(true);

				TechnologyHandler.GLOWING_EYES.setUnlocked(player);

				player.sendMessage(new TextComponentTranslation("technology.noise.whisper2"));
				player.sendMessage(new TextComponentTranslation("technology.complete.unlock", TechnologyHandler.GLOWING_EYES.getLocalizedName(true)));
				player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 1.0F, 1.0F);

				PacketDispatcher.sendTo(new TechnologyMessage(player, true), (EntityPlayerMP) player);
			}
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent evt) {
		if (!evt.getEntity().world.isRemote && evt.getEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) evt.getEntity();
			UUID uuid = player.getUniqueID();

			if (!TechnologyHandler.GLOWING_EYES.isUnlocked(player) && TechnologyHandler.GLOWING_EYES.canResearchIgnoreCustomUnlock(player)) {
				if (player.world.getBlockState(player.getPosition().offset(EnumFacing.DOWN, 1)).getBlock() == Blocks.SOUL_SAND) {
					if (!ticks.containsKey(uuid)) {
						ticks.put(uuid, 0);
					} else {
						int tick = ticks.get(uuid);
						if (tick == t) {
							player.sendMessage(new TextComponentTranslation("technology.noise.whisper1"));
							player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
						}
						if (!(tick > t))
							ticks.put(uuid, tick + 1);
					}
				} else if (ticks.containsKey(uuid) && ticks.get(uuid) < t)
					ticks.remove(uuid);
			} else if (!TechnologyHandler.ENCHANTING.isUnlocked(player) && TechnologyHandler.ENCHANTING.canResearchIgnoreCustomUnlock(player)) {
				for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
					ItemStack stack = player.inventory.getStackInSlot(i);
					if (!stack.isEmpty() && stack.getItem() == Items.ENCHANTED_BOOK) {
						TechnologyHandler.ENCHANTING.setUnlocked(player);

						player.sendMessage(new TextComponentTranslation("technology.complete.unlock", TechnologyHandler.ENCHANTING.getLocalizedName(true)));
						player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

						PacketDispatcher.sendTo(new TechnologyMessage(player, true), (EntityPlayerMP) player);
						break;
					}
				}
			} else if (!TechnologyHandler.ENDER_KNOWLEDGE.isUnlocked(player) && TechnologyHandler.ENDER_KNOWLEDGE.canResearchIgnoreCustomUnlock(player) && hasBlock(player.getPosition(), Blocks.DRAGON_EGG, 5, player.world)) {
				TechnologyHandler.ENDER_KNOWLEDGE.setUnlocked(player);

				player.sendMessage(new TextComponentTranslation("technology.complete.unlock", TechnologyHandler.ENDER_KNOWLEDGE.getLocalizedName(true)));
				player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

				PacketDispatcher.sendTo(new TechnologyMessage(player, true), (EntityPlayerMP) player);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onItemTooltip(ItemTooltipEvent evt) {
		Item item = evt.getItemStack().getItem();
		if (item == FTGUAPI.i_lookingGlass) {
			NBTTagList blocks = TechnologyUtil.getItemData(evt.getItemStack()).getTagList("FTGU", NBT.TAG_STRING);
			if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				for (int i = 0; i < blocks.tagCount(); i++)
					evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + I18n.format(blocks.getStringTagAt(i) + ".name"));
				if (blocks.tagCount() > 0)
					evt.getToolTip().add("");
			} else if (blocks.tagCount() > 0) {
				evt.getToolTip().add(I18n.format("item.looking_glass.shift"));
				evt.getToolTip().add("");
			}

			evt.getToolTip().add(TextFormatting.DARK_RED + I18n.format("technology.decipher.tooltip"));
		} else if (item == FTGUAPI.i_parchmentIdea) {
			Technology tech = TechnologyHandler.getTechnology(TechnologyUtil.getItemData(evt.getItemStack()).getString("FTGU"));

			if (tech != null) {
				String k = tech.canResearchIgnoreResearched(evt.getEntityPlayer()) ? "" : "" + TextFormatting.OBFUSCATED;
				evt.getToolTip().add(TextFormatting.GOLD + I18n.format("technology.idea", tech.getLocalizedName(false).getFormattedText()));
				evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + k + tech.getDescription().getFormattedText());
			}
		} else if (item == FTGUAPI.i_parchmentResearch) {
			Technology tech = TechnologyHandler.getTechnology(TechnologyUtil.getItemData(evt.getItemStack()).getString("FTGU"));

			if (tech != null) {
				boolean can = tech.canResearchIgnoreResearched(evt.getEntityPlayer());
				String k = can ? "" : "" + TextFormatting.OBFUSCATED;

				evt.getToolTip().add(TextFormatting.GOLD + tech.getLocalizedName(true).getFormattedText());
				evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + k + tech.getDescription().getFormattedText());

				if (can && !tech.isResearched(evt.getEntityPlayer())) {
					evt.getToolTip().add("");
					evt.getToolTip().add(TextFormatting.DARK_RED + I18n.format("item.parchment_research.complete"));
				}
			}
		}
	}

	@SubscribeEvent
	public void onItemCraft(ItemCraftedEvent evt) {
		if (evt.crafting.getItem() == FTGUAPI.i_researchBook)
			for (int i = 0; i < evt.craftMatrix.getSizeInventory(); i++) {
				ItemStack item = evt.craftMatrix.getStackInSlot(i);
				if (!item.isEmpty() && item.getItem() == FTGUAPI.i_parchmentResearch)
					((ItemParchmentResearch) item.getItem()).research(item, evt.player, false);
			}
	}

	private void replaceRecipeBook(EntityPlayerMP player) {
		try {
			RecipeBookServerImpl book = new RecipeBookServerImpl();
			book.read(player.getRecipeBook().write());
			BOOK.set(player, book);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		if (!evt.player.world.isRemote) {
			replaceRecipeBook((EntityPlayerMP) evt.player);

			ContainerPlayer inv = (ContainerPlayer) evt.player.openContainer;
			inv.addListener(new CraftingListener(evt.player));

			ticks.remove(evt.player.getUniqueID());

			List<String> headstart = Arrays.asList(TechnologyHandler.STONECRAFT.getUnlocalizedName(), TechnologyHandler.STONEWORKING.getUnlocalizedName(), TechnologyHandler.CARPENTRY.getUnlocalizedName(), TechnologyHandler.REFINEMENT.getUnlocalizedName(), TechnologyHandler.BIBLIOGRAPHY.getUnlocalizedName(), TechnologyHandler.ADVANCED_COMBAT.getUnlocalizedName(), TechnologyHandler.BUILDING_BLOCKS.getUnlocalizedName(), TechnologyHandler.COOKING.getUnlocalizedName());
			ITechnology cap = evt.player.getCapability(CapabilityTechnology.TECH_CAP, null);
			if (cap != null && cap.isNew()) {
				evt.player.inventory.addItemStackToInventory(new ItemStack(FTGUAPI.i_researchBook));

				if (FTGU.moddedOnly) {
					cap.setResearched(TechnologyHandler.vanilla);
				} else if (FTGU.headStart) {
					cap.setResearched(headstart);
				}

				cap.setOld();
			}
			PacketDispatcher.sendTo(new TechnologyMessage(evt.player, false), (EntityPlayerMP) evt.player);
		}
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone evt) {
		if (!evt.getEntity().world.isRemote) {
			replaceRecipeBook((EntityPlayerMP) evt.getEntityPlayer());

			ContainerPlayer inv = (ContainerPlayer) evt.getEntityPlayer().openContainer;
			inv.addListener(new CraftingListener(evt.getEntityPlayer()));

			ticks.remove(evt.getOriginal().getUniqueID());
		}
	}

	@SubscribeEvent
	public void onPlayerOpenContainer(PlayerContainerEvent.Open evt) {
		if (!evt.getEntity().world.isRemote) {
			Container inv = evt.getEntityPlayer().openContainer;
			inv.addListener(new CraftingListener(evt.getEntityPlayer()));
		}
	}

	@SubscribeEvent
	public void onPlayerCloseContainer(PlayerContainerEvent.Close evt) {
		if (!evt.getEntity().world.isRemote) {
			Container inv = evt.getEntityPlayer().openContainer;
			inv.addListener(new CraftingListener(evt.getEntityPlayer()));
		}
	}

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
						PlayerLockEvent event = new PlayerLockEvent(Minecraft.getMinecraft().player, stack, ((InventoryCraftResult) s.inventory).getRecipeUsed());
						if (!stack.isEmpty())
							MinecraftForge.EVENT_BUS.post(event);

						this.stack = event.isLocked() ? ItemStack.EMPTY : stack;
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
					return capability == CapabilityTechnology.TECH_CAP ? CapabilityTechnology.TECH_CAP.<T>cast(inst) : null;
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
