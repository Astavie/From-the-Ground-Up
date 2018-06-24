package ftgumod.util.predicate;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidPredicate extends ItemPredicate {

	private final FluidStack fluid;

	public FluidPredicate(FluidStack fluid) {
		this.fluid = fluid;
	}

	public FluidPredicate(JsonObject object) {
		Fluid fluid = null;
		if (object.has("fluid")) {
			String name = JsonUtils.getString(object, "fluid");
			fluid = FluidRegistry.getFluid(name);
			if (fluid == null)
				throw new JsonSyntaxException("Unknown fluid '" + name + "'");
		}
		this.fluid = new FluidStack(fluid, JsonUtils.getInt(object, "count", Fluid.BUCKET_VOLUME));
	}

	@Override
	public boolean test(ItemStack item) {
		FluidStack stack = FluidUtil.getFluidContained(item);
		return stack != null && stack.containsFluid(fluid);
	}

}
