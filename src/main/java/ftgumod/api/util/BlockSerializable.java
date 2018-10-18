package ftgumod.api.util;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BlockSerializable {

	private final DimensionType dimension;
	private final BlockPos pos;
	private final Block block;
	private final Map<IProperty<?>, Object> properties;

	private final ItemStack display;

	public BlockSerializable(World world, BlockPos pos, IBlockState state, @Nullable ItemStack display) {
		this.dimension = world.dimension.getType();
		this.pos = pos;
		this.block = state.getBlock();
		this.properties = new HashMap<>();

		for (IProperty<?> property : state.func_177227_a())
			properties.put(property, state.get(property));

		if (display == null || display.isEmpty())
			this.display = new ItemStack(block, 1, block.func_176201_c(state));
		else
			this.display = display;
	}

	public BlockSerializable(NBTTagCompound compound) {
		this.dimension = DimensionType.byName(compound.getString("dimension"));
		this.pos = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
		this.block = Block.field_149771_c.getOrDefault(new ResourceLocation(compound.getString("block")));
		this.properties = new HashMap<>();

		NBTTagCompound state = compound.getCompound("state");
		BlockStateContainer container = block.getStateContainer();

		for (String name : state.keySet()) {
			IProperty<?> property = container.getProperty(name);
			properties.put(property, property.parseValue(state.getString(name)).get());
		}

		display = new ItemStack(compound.getCompound("display"));
	}

	public NBTTagCompound serialize() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.putString("dimension", dimension.name());
		compound.putInt("x", pos.getX());
		compound.putInt("y", pos.getY());
		compound.putInt("z", pos.getZ());
		compound.putString("block", block.getRegistryName().toString());

		NBTTagCompound state = new NBTTagCompound();
		for (Map.Entry<IProperty<?>, Object> entry : properties.entrySet())
			state.putString(entry.getKey().getName(), getPropertyName(entry.getKey(), entry.getValue()));

		compound.put("state", state);
		compound.put("display", display.serializeNBT());

		return compound;
	}

	@SuppressWarnings("unchecked")
	private <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Object object) {
		return property.getName((T) object);
	}

	public String getLocalizedName() {
		return display.func_82833_r();
	}

	public boolean test(BlockPredicate predicate) {
		return predicate.test(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension.getId()), pos, block, properties);
	}

}
