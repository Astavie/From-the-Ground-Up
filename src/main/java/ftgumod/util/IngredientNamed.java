package ftgumod.util;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public class IngredientNamed extends Ingredient {

	private final Ingredient ingredient;
	private final ITextComponent name;

	public IngredientNamed(Ingredient ingredient, ITextComponent name) {
		this.ingredient = ingredient;
		this.name = name;
	}

	@Override
	public ItemStack[] getMatchingStacks() {
		return ingredient.getMatchingStacks();
	}

	@Override
	public boolean apply(@Nullable ItemStack p_apply_1_) {
		return ingredient.apply(p_apply_1_);
	}

	@Override
	public IntList getValidItemStacksPacked() {
		return ingredient.getValidItemStacksPacked();
	}

	public ITextComponent getName() {
		return name;
	}

}
