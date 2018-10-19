package ftgumod.item;

import ftgumod.client.gui.GuiResearchBook;
import ftgumod.technology.TechnologyManager;
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

public class ItemResearchBook extends Item {

	public ItemResearchBook(String name) {
		setTranslationKey(name);
		setCreativeTab(CreativeTabs.MISC);
		setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (world.isRemote && TechnologyManager.INSTANCE.getRoots().stream().anyMatch(tech -> tech.canResearchIgnoreResearched(player)))
			Minecraft.getMinecraft().displayGuiScreen(new GuiResearchBook(player));
		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

}
