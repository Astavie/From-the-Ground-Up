package ftgumod.util.predicate;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

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

	public ItemStack drain(ItemStack item) {
		IFluidHandlerItem handler = FluidUtil.getFluidHandler(item);
		if (handler != null) {
			if (handler instanceof FluidBucketWrapper && ((FluidBucketWrapper) handler).getFluid().isFluidEqual(fluid))
				return new ItemStack(Items.BUCKET);

			handler.drain(fluid, true);
			return handler.getContainer();
		}
		return item;
	}

}
