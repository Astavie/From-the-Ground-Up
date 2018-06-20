package ftgumod.api.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockPredicateCompound extends BlockPredicate {

	private final Iterable<BlockPredicate> compound;

	public BlockPredicateCompound(Iterable<BlockPredicate> compound) {
		super(null, null, null);
		this.compound = compound;
	}

	public static BlockPredicate deserialize(JsonElement element) {
		if (element.isJsonArray()) {
			Set<BlockPredicate> compound = new HashSet<>();
			for (JsonElement e : element.getAsJsonArray())
				compound.add(deserialize(e));
			return new BlockPredicateCompound(compound);
		} else if (element.isJsonObject())
			return BlockPredicate.deserialize(element.getAsJsonObject());
		else throw new JsonSyntaxException("Expected decipher to be an object or an array of objects");
	}

	@Override
	public boolean test(WorldServer world, BlockPos pos, IBlockState state) {
		for (BlockPredicate predicate : compound)
			if (predicate.test(world, pos, state))
				return true;
		return false;
	}

	@Override
	public boolean test(WorldServer world, BlockPos pos, Block block, Map<IProperty<?>, Object> properties) {
		for (BlockPredicate predicate : compound)
			if (predicate.test(world, pos, block, properties))
				return true;
		return false;
	}

}
