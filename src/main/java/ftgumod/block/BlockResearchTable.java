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
		func_149711_c(3.5F);
		func_149672_a(SoundType.STONE);
		func_149663_c(name);
		func_149647_a(CreativeTabs.DECORATIONS);
		field_149758_A = true;
	}

	@Override
	public boolean func_180639_a(World parWorld, BlockPos parBlockPos, IBlockState parIBlockState, EntityPlayer parPlayer, EnumHand hand, EnumFacing parSide, float hitX, float hitY, float hitZ) {
		if (!parWorld.isRemote)
			parPlayer.openGui(FTGU.INSTANCE, GUI.RESEARCHTABLE.ordinal(), parWorld, parBlockPos.getX(), parBlockPos.getY(), parBlockPos.getZ());
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase player, ItemStack stack) {
		world.setBlockState(blockPos, blockState.func_177226_a(FACING, player.getHorizontalFacing().getOpposite()), 2);
	}

	@Override
	public void func_180663_b(World world, BlockPos pos, IBlockState state) {
		TileEntity tileentity = world.getTileEntity(pos);

		if (tileentity instanceof TileEntityResearchTable) {
			InventoryHelper.dropInventoryItems(world, pos, (TileEntityResearchTable) tileentity);
			IPuzzle puzzle = ((TileEntityResearchTable) tileentity).puzzle;
			if (puzzle != null)
				puzzle.onRemove(null, world, pos);

			world.updateComparatorOutputLevel(pos, this);
		}

		super.func_180663_b(world, pos, state);
	}

	@Override
	public TileEntity func_149915_a(World world, int meta) {
		return new TileEntityResearchTable();
	}

	@Override
	public IBlockState func_176203_a(int meta) {
		EnumFacing enumfacing = EnumFacing.byHorizontalIndex(meta);
		if (enumfacing.getAxis() == EnumFacing.Axis.Y)
			enumfacing = EnumFacing.NORTH;

		return getDefaultState().func_177226_a(FACING, enumfacing);
	}

	@Override
	public int func_176201_c(IBlockState state) {
		return state.get(FACING).getIndex();
	}

	@Override
	protected BlockStateContainer func_180661_e() {
		return new BlockStateContainer(this, FACING);
	}

}
