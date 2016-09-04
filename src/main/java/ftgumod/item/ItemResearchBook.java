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
import ftgumod.gui.researchbook.GuiResearchBook;


public class ItemResearchBook extends Item {
	
	public ItemResearchBook(String name) {
		setUnlocalizedName(name);
		setCreativeTab(CreativeTabs.MISC);
		setMaxStackSize(1);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
		if (world.isRemote) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiResearchBook(player));
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
	}

}
