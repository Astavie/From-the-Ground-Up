package ftgumod.api.technology.unlock;

import ftgumod.api.FTGUAPI;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.List;

public class UnlockRecipe implements IUnlock {

	private final Ingredient recipe;

	public UnlockRecipe(Ingredient recipe) {
		this.recipe = recipe;
	}

	@Override
	public boolean isDisplayed() {
		return true;
	}

	@Override
	public Ingredient getIcon() {
		return recipe;
	}

	@Override
	public boolean unlocks(ItemStack stack) {
		return recipe.test(stack);
	}

	public List<IRecipe> getRecipeList() {
		List<IRecipe> recipes = NonNullList.create();
		for (IRecipe recipe : ForgeRegistries.RECIPES)
			if (unlocks(recipe.getRecipeOutput()))
				recipes.add(recipe);
		return recipes;
	}

	@Override
	public void unlock(EntityPlayerMP player) {
		FTGUAPI.technologyManager.addRecipes(getRecipeList(), player);
	}

	@Override
	public void lock(EntityPlayerMP player) {
		player.getRecipeBook().func_193834_b(getRecipeList(), player);
	}

}
