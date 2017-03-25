package ftgumod.item;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ftgumod.FTGUAPI;
import ftgumod.gui.researchbook.GuiResearchBook;

public class ItemResearchBook extends Item {

	public ItemResearchBook(String name) {
		setUnlocalizedName(name);
		setCreativeTab(CreativeTabs.MISC);
		setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (world.isRemote) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiResearchBook(player));
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, hand == EnumHand.MAIN_HAND ? player.getHeldItemMainhand() : player.getHeldItemOffhand());
	}

}
