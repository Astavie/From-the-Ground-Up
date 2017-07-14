package ftgumod.server;

import ftgumod.ItemList;
import ftgumod.technology.TechnologyHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBookServer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class RecipeBookServerImpl extends RecipeBookServer {

	@Override
	public void add(List<IRecipe> recipes, EntityPlayerMP player) {
		recipes.removeIf(recipe -> TechnologyHandler.getLocked(recipe.getRecipeOutput()) != null);
		if (!recipes.isEmpty())
			super.add(recipes, player);
	}

	private List<IRecipe> getRecipeList(List<ItemList> list) {
		List<IRecipe> recipes = new ArrayList<>();
		for (ItemList item : list)
			for (IRecipe r : ForgeRegistries.RECIPES)
				if (item.contains(r.getRecipeOutput()))
					recipes.add(r);
		return recipes;
	}

	public void addItems(List<ItemList> list, EntityPlayerMP player) {
		super.add(getRecipeList(list), player);
	}

	public void removeItems(List<ItemList> list, EntityPlayerMP player) {
		remove(getRecipeList(list), player);
	}

}
