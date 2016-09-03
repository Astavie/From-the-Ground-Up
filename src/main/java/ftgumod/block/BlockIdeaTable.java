package ftgumod.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ftgumod.FTGU;
import ftgumod.TechnologyHandler.GUI;
import ftgumod.gui.TileEntityInventory;
import ftgumod.gui.ideatable.TileEntityIdeaTable;

public class BlockIdeaTable extends Block implements ITileEntityProvider {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	
	private String name;

	public BlockIdeaTable(String name) {
		super(Material.WOOD);
		setHardness(2.5F);
		setSoundType(SoundType.WOOD);
		setUnlocalizedName(name);
		setCreativeTab(CreativeTabs.DECORATIONS);
		isBlockContainer = true;
		this.name = name;
	}

	@Override
	public boolean onBlockActivated(World parWorld, BlockPos parBlockPos, IBlockState parIBlockState, EntityPlayer parPlayer, EnumHand hand, ItemStack item, EnumFacing parSide, float hitX, float hitY, float hitZ) {
		if (!parWorld.isRemote) {
			parPlayer.openGui(FTGU.instance, GUI.IDEATABLE.ordinal(), parWorld, parBlockPos.getX(), parBlockPos.getY(), parBlockPos.getZ());
		}
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase player, ItemStack stack) {
		if (!world.isRemote) {
			world.setBlockState(blockPos, blockState.withProperty(FACING, player.getHorizontalFacing().getOpposite()), 2);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity tileentity = world.getTileEntity(pos);

		if (tileentity instanceof TileEntityInventory) {
			InventoryHelper.dropInventoryItems(world, pos, (TileEntityInventory) tileentity);
			world.updateComparatorOutputLevel(pos, this);
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	public TileEntity createNewTileEntity(World arg0, int arg1) {
		return new TileEntityIdeaTable();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing) state.getValue(FACING)).getIndex();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	@SideOnly(Side.CLIENT)
	static final class SwitchEnumFacing {

		static final int[] enumFacingArray = new int[EnumFacing.values().length];

		static {
			try {
				enumFacingArray[EnumFacing.WEST.ordinal()] = 1;
			} catch (NoSuchFieldError var4) {
				;
			}

			try {
				enumFacingArray[EnumFacing.EAST.ordinal()] = 2;
			} catch (NoSuchFieldError var3) {
				;
			}

			try {
				enumFacingArray[EnumFacing.NORTH.ordinal()] = 3;
			} catch (NoSuchFieldError var2) {
				;
			}

			try {
				enumFacingArray[EnumFacing.SOUTH.ordinal()] = 4;
			} catch (NoSuchFieldError var1) {
				;
			}
		}
	}
}
