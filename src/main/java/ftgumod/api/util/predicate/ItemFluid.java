package ftgumod.api.util.predicate;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.api.util.JsonContextPublic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class ItemFluid extends ItemPredicate {

	private final FluidStack fluid;

	public ItemFluid(FluidStack fluid) {
		super(FluidUtil.getFilledBucket(fluid));
		this.fluid = fluid;
	}

	@Override
	public boolean apply(ItemStack itemStack) {
		FluidStack stack = FluidUtil.getFluidContained(itemStack);
		return stack != null && stack.containsFluid(fluid);
	}

	public static class Factory implements ItemPredicate.Factory<ItemFluid> {

		@Override
		public ItemFluid deserialize(JsonObject object, JsonContextPublic context) {
			String name = JsonUtils.getString(object, "fluid");
			Fluid fluid = FluidRegistry.getFluid(name);
			if (fluid == null)
				throw new JsonSyntaxException("Unknown fluid " + name);
			return new ItemFluid(new FluidStack(fluid, JsonUtils.getInt(object, "count", Fluid.BUCKET_VOLUME)));
		}

	}

}
