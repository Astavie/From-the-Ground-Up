package ftgumod.api.util;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.Iterator;
import java.util.Map;

public class BlockPredicate {

	private final Block block;
	private final Map<IProperty<?>, Object> properties;
	private final LocationPredicate location;

	public BlockPredicate(Block block, Map<IProperty<?>, Object> properties, LocationPredicate location) {
		this.block = block;
		this.properties = properties;
		this.location = location;
	}

	public static BlockPredicate deserialize(JsonObject object) {
		Block block = null;
		if (object.has("block")) {
			ResourceLocation location = new ResourceLocation(JsonUtils.getString(object, "block"));
			if (!Block.REGISTRY.containsKey(location))
				throw new JsonSyntaxException("Unknown block type '" + location + "'");

			block = Block.REGISTRY.getObject(location);
		}

		Map<IProperty<?>, Object> properties = null;
		if (object.has("state")) {
			if (block == null)
				throw new JsonSyntaxException("Can't define block state without a specific block type");

			BlockStateContainer blockState = block.getBlockState();
			IProperty property;

			//noinspection Guava
			Optional optional;
			for (Iterator<Map.Entry<String, JsonElement>> var6 = JsonUtils.getJsonObject(object, "state").entrySet().iterator(); var6.hasNext(); properties.put(property, optional.get())) {
				Map.Entry<String, JsonElement> entry = var6.next();
				property = blockState.getProperty(entry.getKey());
				if (property == null)
					throw new JsonSyntaxException("Unknown block state property '" + entry.getKey() + "' for block '" + Block.REGISTRY.getNameForObject(block) + "'");

				String name = JsonUtils.getString(entry.getValue(), entry.getKey());
				optional = property.parseValue(name);
				if (!optional.isPresent())
					throw new JsonSyntaxException("Invalid block state value '" + name + "' for property '" + entry.getKey() + "' on block '" + Block.REGISTRY.getNameForObject(block) + "'");

				if (properties == null)
					properties = Maps.newHashMap();
			}
		}

		LocationPredicate location = LocationPredicate.deserialize(object.get("location"));
		return new BlockPredicate(block, properties, location);
	}

	public boolean test(WorldServer world, BlockPos pos, IBlockState state) {
		if (this.block != null && state.getBlock() != this.block)
			return false;
		if (this.properties != null)
			for (Map.Entry<IProperty<?>, Object> entry : this.properties.entrySet())
				if (state.getValue(entry.getKey()) != entry.getValue())
					return false;
		return location.test(world, pos.getX(), pos.getY(), pos.getZ());
	}

	public boolean test(WorldServer world, BlockPos pos, Block block, Map<IProperty<?>, Object> properties) {
		if (this.block != null && block != this.block)
			return false;
		if (this.properties != null)
			for (Map.Entry<IProperty<?>, Object> entry : this.properties.entrySet())
				if (properties.get(entry.getKey()) != entry.getValue())
					return false;
		return location.test(world, pos.getX(), pos.getY(), pos.getZ());
	}

}
