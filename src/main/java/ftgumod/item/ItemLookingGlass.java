package ftgumod.item;

import ftgumod.Decipher;
import ftgumod.FTGUAPI;
import ftgumod.event.PlayerInspectEvent;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.util.BlockSerializable;
import ftgumod.util.StackUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

	public static List<BlockSerializable> getInspected(ItemStack item) {
		List<BlockSerializable> list = new ArrayList<>();
		NBTTagList blocks = StackUtils.getItemData(item).getTagList("FTGU", NBT.TAG_COMPOUND);
		for (int i = 0; i < blocks.tagCount(); i++)
			list.add(new BlockSerializable(blocks.getCompoundTagAt(i)));
		return list;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float f1, float f2, float f3) {
		if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			if (!world.isRemote) {
				ItemStack item = hand == EnumHand.MAIN_HAND ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
				List<BlockSerializable> list = getInspected(item);

				IBlockState state = world.getBlockState(pos);
				BlockSerializable block = new BlockSerializable(world, pos, state);

				PlayerInspectEvent event = new PlayerInspectEvent(player, hand, pos, state, face);
				event.setCanceled(true);

				outer:
				for (Technology tech : TechnologyHandler.technologies)
					if (tech.hasResearchRecipe() && tech.getResearchRecipe().hasDecipher())
						group:for (Decipher.DecipherGroup decipher : tech.getResearchRecipe().getDecipher().list)
							if (block.test(decipher.unlock)) {
								for (BlockSerializable other : list)
									if (other.test(decipher.unlock))
										continue group;
								event.setCanceled(false);
								break outer;
							}

				MinecraftForge.EVENT_BUS.post(event);

				if (event.isCanceled()) {
					player.sendMessage(new TextComponentTranslation("technology.decipher.understand"));
					world.playSound(null, player.getPosition(), SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
					return EnumActionResult.SUCCESS;
				}

				player.sendMessage(new TextComponentTranslation("technology.decipher.flawless"));
				world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

				FTGUAPI.c_inspect.trigger((EntityPlayerMP) player, pos, state);

				NBTTagCompound tag = StackUtils.getItemData(item);
				NBTTagList nbt = tag.getTagList("FTGU", NBT.TAG_COMPOUND);
				nbt.appendTag(block.serialize());
				tag.setTag("FTGU", nbt);
			}
			return EnumActionResult.SUCCESS;
		} else
			return EnumActionResult.PASS;
	}

}
