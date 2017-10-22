package ftgumod.util;

import ftgumod.Content;
import ftgumod.api.FTGUAPI;
import ftgumod.api.util.BlockSerializable;
import ftgumod.api.util.IStackUtils;
import ftgumod.item.ItemMagnifyingGlass;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.List;

public class StackUtils implements IStackUtils<Technology> {

	public static final StackUtils INSTANCE = new StackUtils();

	static {
		FTGUAPI.stackUtils = INSTANCE;
	}

	public NBTTagCompound getItemData(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}
		return nbt;
	}

	public boolean isStackOf(ItemStack ingredient, ItemStack stack) {
		return ingredient.getItem() == stack.getItem() && (ingredient.getMetadata() == OreDictionary.WILDCARD_VALUE || ingredient.getMetadata() == stack.getMetadata()) && (!ingredient.hasTagCompound() || ItemStack.areItemStackTagsEqual(ingredient, stack));
	}

	public ItemStack drain(ItemStack stack, FluidStack fluid) {
		IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
		if (handler != null) {
			handler.drain(fluid.amount, true);
			return handler.getContainer();
		}
		return stack;
	}

	@Override
	public ItemStack getParchment(ResourceLocation tech, Parchment type) {
		ItemStack stack = new ItemStack(type == Parchment.IDEA ? Content.i_parchmentIdea : Content.i_parchmentResearch);
		getItemData(stack).setString("FTGU", tech.toString());
		return stack;
	}

	@Nullable
	@Override
	public Technology getTechnology(ItemStack parchment) {
		return TechnologyManager.INSTANCE.technologies.get(new ResourceLocation(getItemData(parchment).getString("FTGU")));
	}

	@Override
	public List<BlockSerializable> getInspected(ItemStack inspector) {
		return ItemMagnifyingGlass.getInspected(inspector);
	}

}
