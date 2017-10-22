package ftgumod.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nullable;

public class IngredientFluid extends Ingredient {

	private final FluidStack fluid;
	private final ItemStack[] matching;

	public IngredientFluid(FluidStack fluid) {
		super(0);
		this.fluid = fluid;
		this.matching = new ItemStack[] {FluidUtil.getFilledBucket(fluid)};
	}

	public IngredientFluid(Fluid fluid, int amount) {
		this(new FluidStack(fluid, amount));
	}

	public FluidStack getFluid() {
		return fluid;
	}

	@Override
	public ItemStack[] getMatchingStacks() {
		return matching;
	}

	@Override
	public boolean apply(@Nullable ItemStack stack) {
		if (stack == null)
			return false;
		else {
			FluidStack fluid = FluidUtil.getFluidContained(stack);
			return fluid != null && fluid.containsFluid(this.fluid);
		}
	}

}
