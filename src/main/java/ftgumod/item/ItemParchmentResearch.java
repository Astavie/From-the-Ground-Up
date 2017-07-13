package ftgumod.item;

import ftgumod.FTGUAPI;
import ftgumod.event.PlayerResearchEvent;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.TechnologyUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ItemParchmentResearch extends Item {

	public ItemParchmentResearch(String name) {
		setUnlocalizedName(name);
		setMaxStackSize(1);
		setContainerItem(FTGUAPI.i_parchmentEmpty);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, research(hand == EnumHand.MAIN_HAND ? player.getHeldItemMainhand() : player.getHeldItemOffhand(), player, true));
	}

	public ItemStack research(ItemStack item, EntityPlayer player, boolean already) {
		if (!player.world.isRemote) {
			Technology t = TechnologyHandler.getTechnology(TechnologyUtil.getItemData(item).getString("FTGU"));
			if (t != null) {
				if (t.isResearched(player)) {
					if (already)
						player.sendMessage(new TextComponentTranslation("technology.complete.already", t.getLocalizedName(true)));
				} else {
					PlayerResearchEvent event = new PlayerResearchEvent(player, t);
					MinecraftForge.EVENT_BUS.post(event);

					if (event.canResearch()) {
						t.setResearched(player);
						player.sendMessage(new TextComponentTranslation("technology.complete.flawless", t.getLocalizedName(true)));
						player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
						PacketDispatcher.sendTo(new TechnologyMessage(player, true), (EntityPlayerMP) player);
						return new ItemStack(FTGUAPI.i_parchmentEmpty);
					} else
						player.sendMessage(new TextComponentTranslation("technology.complete.understand"));
				}
			}
		}
		return item;
	}

}
