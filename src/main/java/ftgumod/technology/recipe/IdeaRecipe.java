package ftgumod.technology.recipe;

import ftgumod.technology.Technology;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class IdeaRecipe {

	private final NonNullList<Ingredient> recipe;
	private final int needed;
	private final Technology output;

	public IdeaRecipe(NonNullList<Ingredient> recipe, int needed, Technology output) {
		this.output = output;
		this.needed = needed;
		this.recipe = recipe;
	}

	public boolean test(Collection<ItemStack> inventory) {
		Set<Ingredient> copy = new HashSet<>(recipe);
		int count = 0;

		for (ItemStack stack : inventory) {
			Iterator<Ingredient> iterator = copy.iterator();
			while (iterator.hasNext())
				if (iterator.next().test(stack)) {
					iterator.remove();
					count++;
					break;
				}
		}

		return count >= needed;
	}

	public Technology getTechnology() {
		return output;
	}

}
