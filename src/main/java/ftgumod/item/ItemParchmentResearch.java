package ftgumod.item;

import ftgumod.Content;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.technology.Technology;
import ftgumod.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemParchmentResearch extends Item {

	public ItemParchmentResearch(String name) {
		func_77655_b(name);
		func_77625_d(1);
		func_77642_a(Content.i_parchmentEmpty);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		return new ActionResult<>(EnumActionResult.SUCCESS, research(hand == EnumHand.MAIN_HAND ? player.getHeldItemMainhand() : player.getHeldItemOffhand(), player, true));
	}

	public ItemStack research(ItemStack item, EntityPlayer player, boolean already) {
		if (!player.world.isRemote) {
			Technology t = StackUtils.INSTANCE.getTechnology(item);
			if (t != null) {
				if (t.isResearched(player)) {
					if (already)
						player.sendMessage(new TextComponentTranslation("technology.complete.already", t.getDisplayText()));
				} else {
					if (t.canResearchIgnoreCustomUnlock(player)) {
						t.setResearched(player, true);

						PacketDispatcher.sendTo(new TechnologyMessage(player, true, t), (EntityPlayerMP) player);
						return new ItemStack(Content.i_parchmentEmpty);
					} else
						player.sendMessage(new TextComponentTranslation("technology.complete.understand"));
				}
			}
		}
		return item;
	}

}
