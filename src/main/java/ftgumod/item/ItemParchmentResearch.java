package ftgumod.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import ftgumod.FTGUAPI;
import ftgumod.Technology;
import ftgumod.TechnologyHandler;
import ftgumod.TechnologyUtil;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;

public class ItemParchmentResearch extends Item {

	public ItemParchmentResearch(String name) {
		setUnlocalizedName(name);
		setMaxStackSize(1);
		setContainerItem(FTGUAPI.i_parchmentEmpty);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, research(item, player, true));
	}

	public ItemStack research(ItemStack item, EntityPlayer player, boolean already) {
		if (!player.world.isRemote) {
			Technology t = TechnologyHandler.getTechnology(TechnologyUtil.getItemData(item).getString("FTGU"));
			if (t != null) {
				if (t.isResearched(player)) {
					if (already)
						player.sendMessage(new TextComponentString(I18n.translateToLocalFormatted("technology.complete.already", t.getLocalisedName())));
				} else {
					if (t.canResearch(player)) {
						t.setResearched(player);
						player.sendMessage(new TextComponentString(I18n.translateToLocalFormatted("technology.complete.flawless", t.getLocalisedName())));
						player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
						PacketDispatcher.sendTo(new TechnologyMessage(player), (EntityPlayerMP) player);
						return new ItemStack(FTGUAPI.i_parchmentEmpty);
					} else {
						player.sendMessage(new TextComponentString(I18n.translateToLocal("technology.complete.understand")));
					}
				}
				PacketDispatcher.sendTo(new TechnologyMessage(player), (EntityPlayerMP) player);
			}
		}
		return item;
	}

}
