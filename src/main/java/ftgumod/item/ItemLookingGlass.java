package ftgumod.item;

import ftgumod.event.PlayerInspectEvent;
import ftgumod.technology.TechnologyUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class ItemLookingGlass extends Item {

	public ItemLookingGlass(String name) {
		setUnlocalizedName(name);
		setCreativeTab(CreativeTabs.TOOLS);
		setMaxStackSize(1);
	}

	public static List<ItemStack> getInspected(ItemStack item) {
		List<ItemStack> list = new ArrayList<>();
		NBTTagList blocks = TechnologyUtil.getItemData(item).getTagList("FTGU", NBT.TAG_COMPOUND);
		for (int i = 0; i < blocks.tagCount(); i++) {
			list.add(new ItemStack(blocks.getCompoundTagAt(i)));
		}
		return list;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float f1, float f2, float f3) {
		if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			ItemStack item = hand == EnumHand.MAIN_HAND ? player.getHeldItemMainhand() : player.getHeldItemOffhand();

			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			ItemStack stack = block.getPickBlock(state, null, world, pos, player);

			List<ItemStack> items = getInspected(item);
			for (ItemStack blockstack : items)
				if (ItemStack.areItemStacksEqual(blockstack, item)) {
					if (!world.isRemote) {
						player.sendMessage(new TextComponentTranslation("technology.decipher.already", TechnologyUtil.getDisplayName(item)));
						world.playSound(null, player.getPosition(), SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
					}
					return EnumActionResult.SUCCESS;
				}

			PlayerInspectEvent event = new PlayerInspectEvent(player, hand, items, pos, face, stack);
			MinecraftForge.EVENT_BUS.post(event);

			if (event.isCanceled()) {
				if (!world.isRemote) {
					player.sendMessage(new TextComponentTranslation("technology.decipher.understand"));
					world.playSound(null, player.getPosition(), SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
				}
				return EnumActionResult.SUCCESS;
			}

			if (!world.isRemote) {
				player.sendMessage(new TextComponentTranslation("technology.decipher.flawless"));
				world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
			TechnologyUtil.getItemData(item).getTagList("FTGU", NBT.TAG_COMPOUND).appendTag(stack.serializeNBT());

			return EnumActionResult.SUCCESS;
		} else
			return EnumActionResult.PASS;
	}

}
