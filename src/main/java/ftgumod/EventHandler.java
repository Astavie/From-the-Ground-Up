package ftgumod;

import ftgumod.event.PlayerLockEvent;
import ftgumod.item.ItemLookingGlass;
import ftgumod.item.ItemParchmentResearch;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.server.RecipeBookServerImpl;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.CapabilityTechnology.ITechnology;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.util.BlockSerializable;
import ftgumod.util.StackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandReload;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.CommandEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHandler {

	private static final Field BOOK = ReflectionHelper.findField(EntityPlayerMP.class, "recipeBook", "field_192041_cq");

	private final Map<EntityPlayer, Integer> ticks = new HashMap<>();
	private final int s = 5; // 5 seconds
	private final int t = s * 20; // 5 * 20 ticks
	private ItemStack stack = ItemStack.EMPTY;

	@SubscribeEvent
	public void onCommand(CommandEvent evt) {
		if (evt.getCommand() instanceof CommandReload) {
			// TODO: Reload stuff
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onItemTooltip(ItemTooltipEvent evt) {
		Item item = evt.getItemStack().getItem();
		if (item == FTGUAPI.i_lookingGlass) {
			List<BlockSerializable> blocks = ItemLookingGlass.getInspected(evt.getItemStack());
			if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				for (BlockSerializable block : blocks)
					evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + block.getLocalizedName());
				if (blocks.size() > 0)
					evt.getToolTip().add("");
			} else if (blocks.size() > 0) {
				evt.getToolTip().add(I18n.format("item.looking_glass.shift"));
				evt.getToolTip().add("");
			}

			evt.getToolTip().add(TextFormatting.DARK_RED + I18n.format("technology.decipher.tooltip"));
		} else if (item == FTGUAPI.i_parchmentIdea) {
			Technology tech = TechnologyHandler.getTechnology(new ResourceLocation(StackUtils.getItemData(evt.getItemStack()).getString("FTGU")));

			if (tech != null) {
				String k = tech.canResearchIgnoreResearched(evt.getEntityPlayer()) ? "" : "" + TextFormatting.OBFUSCATED;
				evt.getToolTip().add(TextFormatting.GOLD + I18n.format("technology.idea", tech.getDisplay().getTitle().getUnformattedText()));
				evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + k + tech.getDisplay().getDescription().getUnformattedText());
			}
		} else if (item == FTGUAPI.i_parchmentResearch) {
			Technology tech = TechnologyHandler.getTechnology(new ResourceLocation(StackUtils.getItemData(evt.getItemStack()).getString("FTGU")));

			if (tech != null) {
				boolean can = tech.canResearchIgnoreResearched(evt.getEntityPlayer());
				String k = can ? "" : "" + TextFormatting.OBFUSCATED;

				evt.getToolTip().add(TextFormatting.GOLD + tech.getDisplay().getTitle().getUnformattedText());
				evt.getToolTip().add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + k + tech.getDisplay().getDescription().getUnformattedText());

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
			RecipeBookServerImpl book = new RecipeBookServerImpl(player);
			book.read(player.getRecipeBook().write());
			BOOK.set(player, book);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		if (!evt.player.world.isRemote) {
			ContainerPlayer inv = (ContainerPlayer) evt.player.openContainer;
			inv.addListener(new CraftingListener((EntityPlayerMP) evt.player));

			ITechnology cap = evt.player.getCapability(CapabilityTechnology.TECH_CAP, null);
			if (cap != null && cap.isNew()) {
				evt.player.inventory.addItemStackToInventory(new ItemStack(FTGUAPI.i_researchBook));

				cap.setResearched(TechnologyHandler.start);
				if (FTGU.headStart)
					cap.setResearched(TechnologyHandler.headStart);
				if (FTGU.moddedOnly)
					cap.setResearched(TechnologyHandler.vanilla);

				cap.setOld();
			}
			replaceRecipeBook((EntityPlayerMP) evt.player);
			PacketDispatcher.sendTo(new TechnologyMessage(evt.player, false), (EntityPlayerMP) evt.player);
		}
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone evt) {
		if (!evt.getEntity().world.isRemote) {
			replaceRecipeBook((EntityPlayerMP) evt.getEntityPlayer());

			ContainerPlayer inv = (ContainerPlayer) evt.getEntityPlayer().openContainer;
			inv.addListener(new CraftingListener((EntityPlayerMP) evt.getEntityPlayer()));

			ticks.remove(evt.getOriginal());
		}
	}

	@SubscribeEvent
	public void onPlayerOpenContainer(PlayerContainerEvent.Open evt) {
		if (!evt.getEntity().world.isRemote) {
			Container inv = evt.getEntityPlayer().openContainer;
			inv.addListener(new CraftingListener((EntityPlayerMP) evt.getEntityPlayer()));
		}
	}

	@SubscribeEvent
	public void onPlayerCloseContainer(PlayerContainerEvent.Close evt) {
		if (!evt.getEntity().world.isRemote) {
			Container inv = evt.getEntityPlayer().openContainer;
			inv.addListener(new CraftingListener((EntityPlayerMP) evt.getEntityPlayer()));
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

	@SuppressWarnings("rawtypes")
	@SubscribeEvent
	public void onEntityConstruct(AttachCapabilitiesEvent evt) {
		if (evt.getObject() instanceof EntityPlayer) {
			evt.addCapability(new ResourceLocation(FTGU.MODID, "ITechnology"), new ICapabilitySerializable<NBTTagList>() {

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
