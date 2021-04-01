package ftgumod.block;

import ftgumod.FTGU;
import ftgumod.api.technology.recipe.IPuzzle;
import ftgumod.technology.TechnologyManager.GUI;
import ftgumod.tileentity.TileEntityResearchTable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
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

public class BlockResearchTable extends Block implements ITileEntityProvider {

	private static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockResearchTable(String name) {
		super(Material.ROCK);
		setHardness(3.5F);
		setSoundType(SoundType.STONE);
		setTranslationKey(name);
		setCreativeTab(CreativeTabs.DECORATIONS);
		hasTileEntity = true;
	}

	@Override
	public boolean onBlockActivated(World parWorld, BlockPos parBlockPos, IBlockState parIBlockState, EntityPlayer parPlayer, EnumHand hand, EnumFacing parSide, float hitX, float hitY, float hitZ) {
		if (!parWorld.isRemote)
			parPlayer.openGui(FTGU.INSTANCE, GUI.RESEARCHTABLE.ordinal(), parWorld, parBlockPos.getX(), parBlockPos.getY(), parBlockPos.getZ());
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase player, ItemStack stack) {
		world.setBlockState(blockPos, blockState.withProperty(FACING, player.getHorizontalFacing().getOpposite()), 2);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity tileentity = world.getTileEntity(pos);

		if (tileentity instanceof TileEntityResearchTable) {
			InventoryHelper.dropInventoryItems(world, pos, (TileEntityResearchTable) tileentity);
			IPuzzle puzzle = ((TileEntityResearchTable) tileentity).puzzle;
			if (puzzle != null)
				puzzle.onRemove(null, world, pos);

			world.updateComparatorOutputLevel(pos, this);
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityResearchTable();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byHorizontalIndex(meta);
		if (enumfacing.getAxis() == EnumFacing.Axis.Y)
			enumfacing = EnumFacing.NORTH;
		return getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

}
