package ftgumod.item;

import ftgumod.FTGUAPI;
import ftgumod.event.TechnologyEvent;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
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
		return new ActionResult<>(EnumActionResult.SUCCESS, research(hand == EnumHand.MAIN_HAND ? player.getHeldItemMainhand() : player.getHeldItemOffhand(), player, true));
	}

	public ItemStack research(ItemStack item, EntityPlayer player, boolean already) {
		if (!player.world.isRemote) {
			Technology t = TechnologyHandler.technologies.get(new ResourceLocation(StackUtils.getItemData(item).getString("FTGU")));
			if (t != null) {
				if (t.isResearched(player)) {
					if (already)
						player.sendMessage(new TextComponentTranslation("technology.complete.already", t.getDisplayText()));
				} else {
					TechnologyEvent event = new TechnologyEvent.Research(player, t);
					MinecraftForge.EVENT_BUS.post(event);
					if (!event.isCanceled()) {
						t.setResearched(player);
						t.displayResearched(player);

						PacketDispatcher.sendTo(new TechnologyMessage(player, true, t), (EntityPlayerMP) player);
						return new ItemStack(FTGUAPI.i_parchmentEmpty);
					} else
						player.sendMessage(new TextComponentTranslation("technology.complete.understand"));
				}
			}
		}
		return item;
	}

}
