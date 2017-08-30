package ftgumod.technology.recipe;

import ftgumod.Decipher;
import ftgumod.technology.Technology;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;

public class ResearchRecipe {

	private final NonNullList<Ingredient> ingredients;
	private final Technology output;
	private final Decipher decipher;

	public ResearchRecipe(Technology output, NonNullList<Ingredient> ingredients, @Nullable Decipher decipher) {
		this.output = output;
		this.ingredients = ingredients;
		this.decipher = decipher;
	}

	public boolean test(NonNullList<ItemStack> inventory) {
		for (int i = 0; i < 9; i++)
			if (!ingredients.get(i).test(inventory.get(i)))
				return false;
		return true;
	}

	public Technology getTechnology() {
		return output;
	}

	public boolean hasDecipher() {
		return decipher != null;
	}

	public Decipher getDecipher() {
		return decipher;
	}

	public boolean isEmpty(int index) {
		return ingredients.get(index) == Ingredient.EMPTY;
	}

	public Ingredient get(int index) {
		return ingredients.get(index);
	}

}
