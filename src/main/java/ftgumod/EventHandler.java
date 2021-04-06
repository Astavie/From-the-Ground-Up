package ftgumod;

import java.util.List;

import ftgumod.api.util.BlockSerializable;
import ftgumod.compat.jei.CompatJEI;
import ftgumod.event.PlayerLockEvent;
import ftgumod.item.ItemMagnifyingGlass;
import ftgumod.item.ItemParchmentResearch;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyInfoMessage;
import ftgumod.packet.server.RequestMessage;
import ftgumod.proxy.ProxyClient;
import ftgumod.server.RecipeBookServerImpl;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import ftgumod.util.LootUtils;
import ftgumod.util.StackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class EventHandler {

	private ItemStack stack = ItemStack.EMPTY;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onItemTooltip(ItemTooltipEvent evt) {
		Item item = evt.getItemStack().getItem();
		if (item == Content.i_magnifyingGlass) {
			List<BlockSerializable> blocks = ItemMagnifyingGlass.getInspected(evt.getItemStack());
			if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				for (BlockSerializable block : blocks)
					evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + block.getLocalizedName());
				if (blocks.size() > 0)
					evt.getToolTip().add("");
			} else if (blocks.size() > 0) {
				evt.getToolTip().add(I18n.format(Content.i_magnifyingGlass.getTranslationKey() + ".shift"));
				evt.getToolTip().add("");
			}

			evt.getToolTip().add(TextFormatting.DARK_RED + I18n.format("technology.decipher.tooltip"));
		} else if (item == Content.i_parchmentIdea) {
			Technology tech = StackUtils.INSTANCE.getTechnology(evt.getItemStack());
			if (tech != null) {
				String k = tech.isResearched(evt.getEntityPlayer()) || tech.canResearchIgnoreCustomUnlock(evt.getEntityPlayer()) ? "" : "" + TextFormatting.OBFUSCATED;
				evt.getToolTip().add(TextFormatting.GOLD + I18n.format("technology.idea", tech.getDisplayInfo().getTitle().getUnformattedText()));
				evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + k + tech.getDisplayInfo().getDescription().getUnformattedText());
			}
		} else if (item == Content.i_parchmentResearch) {
			Technology tech = StackUtils.INSTANCE.getTechnology(evt.getItemStack());
			if (tech != null) {
				boolean can = tech.isResearched(evt.getEntityPlayer()) || tech.canResearchIgnoreCustomUnlock(evt.getEntityPlayer());
				String k = can ? "" : "" + TextFormatting.OBFUSCATED;

				evt.getToolTip().add(TextFormatting.GOLD + tech.getDisplayInfo().getTitle().getUnformattedText());
				evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + k + tech.getDisplayInfo().getDescription().getUnformattedText());

				if (can && !tech.isResearched(evt.getEntityPlayer())) {
					evt.getToolTip().add("");
					evt.getToolTip().add(TextFormatting.DARK_RED + I18n.format("item.parchment_research.complete"));
				}
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKey(InputEvent.KeyInputEvent event) {
		if (ProxyClient.key.isPressed()) {
			FTGU.PROXY.openResearchBook(Minecraft.getMinecraft().player);
			PacketDispatcher.sendToServer(new RequestMessage());
		}
	}

	@SubscribeEvent
	public void onItemCraft(ItemCraftedEvent evt) {
		if (evt.crafting.getItem() == Content.i_researchBook)
			for (int i = 0; i < evt.craftMatrix.getSizeInventory(); i++) {
				ItemStack item = evt.craftMatrix.getStackInSlot(i);
				if (!item.isEmpty() && item.getItem() == Content.i_parchmentResearch)
					((ItemParchmentResearch) item.getItem()).research(item, evt.player, false);
			}
	}

	private void replaceRecipeBook(EntityPlayerMP player) {
		RecipeBookServerImpl book = new RecipeBookServerImpl(player);
		book.read(player.recipeBook.write());
		player.recipeBook = book;
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		if (!evt.player.world.isRemote) {
			replaceRecipeBook((EntityPlayerMP) evt.player);

			Container inv = evt.player.openContainer;
			inv.addListener(new CraftingListener(evt.player));

			CapabilityTechnology.ITechnology cap = evt.player.getCapability(CapabilityTechnology.TECH_CAP, null);
			if (cap != null) {
				for (Technology tech : TechnologyManager.INSTANCE.getStart()) {
					if (!cap.isResearched(tech.getRegistryName().toString())) {
						cap.setResearched(tech.getRegistryName().toString());
						tech.addRecipes((EntityPlayerMP) evt.player);
					}
				}
				if (cap.isNew()) {
					if (FTGUConfig.giveResearchBook) {
						evt.player.inventory.addItemStackToInventory(new ItemStack(Content.i_researchBook));
					}
					cap.setOld();
				}
			}

			for (Technology tech : TechnologyManager.INSTANCE)
				if (tech.hasCustomUnlock() && tech.canResearchIgnoreCustomUnlock(evt.player))
					tech.registerListeners((EntityPlayerMP) evt.player);

			PacketDispatcher.sendTo(new TechnologyInfoMessage(TechnologyManager.INSTANCE.cache), (EntityPlayerMP) evt.player);
		}
	}

	@SubscribeEvent
	public void onPlayerLeave(PlayerLoggedOutEvent evt) {
		TechnologyManager.INSTANCE.unloadProgress(evt.player);
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone evt) {
		if (!evt.getEntity().world.isRemote) {
			replaceRecipeBook((EntityPlayerMP) evt.getEntityPlayer());

			ContainerPlayer inv = (ContainerPlayer) evt.getEntityPlayer().openContainer;
			inv.addListener(new CraftingListener(evt.getEntityPlayer()));
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
					if (stack.isEmpty())
						this.stack = stack;
					else if (stack != this.stack) {
						PlayerLockEvent event = new PlayerLockEvent(Minecraft.getMinecraft().player, stack, ((InventoryCraftResult) s.inventory).getRecipeUsed());
						MinecraftForge.EVENT_BUS.post(event);

						if (!event.isCanceled())
							s.inventory.setInventorySlotContents(0, ItemStack.EMPTY);
						this.stack = s.inventory.getStackInSlot(0);
					}
					return;
				}
			}
		}
	}

	@SubscribeEvent
	public void onLootTableLoad(LootTableLoadEvent event) {
		if (event.getName().toString().equals("minecraft:chests/village_blacksmith"))
			LootUtils.addLootPools(event.getLootTableManager(), event.getTable(), new ResourceLocation(FTGU.MODID, "inject/blacksmith"));
		if (event.getName().toString().equals("minecraft:chests/desert_pyramid"))
			LootUtils.addLootPools(event.getLootTableManager(), event.getTable(), new ResourceLocation(FTGU.MODID, "inject/pyramid"));
		if (event.getName().toString().equals("minecraft:chests/stronghold_library"))
			LootUtils.addLootPools(event.getLootTableManager(), event.getTable(), new ResourceLocation(FTGU.MODID, "inject/library"));
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote && event.getEntity() == Minecraft.getMinecraft().player)
			PacketDispatcher.sendToServer(new RequestMessage());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START && FTGU.JEI_LOADED)
			CompatJEI.refreshHiddenItems(true);
	}

}
