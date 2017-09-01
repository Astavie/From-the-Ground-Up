package ftgumod.util;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class BlockSerializable {

	private final DimensionType dimension;
	private final BlockPos pos;
	private final Block block;
	private final int data;
	private final Map<IProperty<?>, Object> properties;

	public BlockSerializable(World world, BlockPos pos, IBlockState state) {
		this.dimension = world.provider.getDimensionType();
		this.pos = pos;
		this.block = state.getBlock();
		this.data = state.getBlock().getMetaFromState(state);
		this.properties = new HashMap<>();

		for (IProperty<?> property : state.getPropertyKeys())
			properties.put(property, state.getValue(property));
	}

	public BlockSerializable(NBTTagCompound compound) {
		this.dimension = DimensionType.byName(compound.getString("dimension"));
		this.pos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
		this.block = Block.REGISTRY.getObject(new ResourceLocation(compound.getString("block")));
		this.data = compound.getInteger("data");
		this.properties = new HashMap<>();

		NBTTagCompound state = compound.getCompoundTag("state");
		BlockStateContainer container = block.getBlockState();

		for (String name : state.getKeySet()) {
			IProperty<?> property = container.getProperty(name);
			properties.put(property, property.parseValue(state.getString(name)).get());
		}
	}

	public NBTTagCompound serialize() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("dimension", dimension.getName());
		compound.setInteger("x", pos.getX());
		compound.setInteger("y", pos.getY());
		compound.setInteger("z", pos.getZ());
		compound.setString("block", block.getRegistryName().toString());
		compound.setInteger("data", data);

		NBTTagCompound state = new NBTTagCompound();
		for (Map.Entry<IProperty<?>, Object> entry : properties.entrySet())
			state.setString(entry.getKey().getName(), getPropertyName(entry.getKey(), entry.getValue()));

		compound.setTag("state", state);

		return compound;
	}

	@SuppressWarnings("unchecked")
	private <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Object object) {
		return property.getName((T) object);
	}

	public String getLocalizedName() {
		return new ItemStack(block, 1, data).getDisplayName();
	}

	public boolean test(BlockPredicate predicate, MinecraftServer server) {
		return predicate.test(server.getWorld(dimension.getId()), pos, block, properties);
	}

}
