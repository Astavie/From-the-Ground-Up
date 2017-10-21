package ftgumod.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import ftgumod.FTGU;
import ftgumod.api.util.BlockPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.Set;

public class TriggerInspect extends TriggerFTGU<TriggerInspect.Instance> {

	public TriggerInspect(String id) {
		super(new ResourceLocation(FTGU.MODID, id));
	}

	@Override
	public Instance deserializeInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		return new Instance(BlockPredicate.deserialize(jsonObject));
	}

	public void trigger(EntityPlayerMP player, BlockPos pos, IBlockState state) {
		Set<Listener<Instance>> listeners = this.listeners.get(player.getAdvancements());
		if (listeners != null) {
			WorldServer world = player.getServerWorld();
			for (Listener<Instance> listener : listeners)
				if (listener.getCriterionInstance().test(world, pos, state))
					listener.grantCriterion(player.getAdvancements());
		}
	}

	public class Instance extends TriggerFTGU.Instance {

		private final BlockPredicate block;

		public Instance(BlockPredicate block) {
			this.block = block;
		}

		public boolean test(WorldServer world, BlockPos pos, IBlockState state) {
			return block.test(world, pos, state);
		}

	}

}
