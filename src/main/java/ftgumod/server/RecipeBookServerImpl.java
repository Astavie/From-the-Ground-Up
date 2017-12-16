package ftgumod.server;

import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class RecipeBookServerImpl extends RecipeBookServer {

	private static final Logger LOGGER = LogManager.getLogger();
	private final EntityPlayerMP player; // Should only be used when reading nbt

	public RecipeBookServerImpl(EntityPlayerMP player) {
		this.player = player;
	}

	@Override
	public void add(List<IRecipe> recipes, EntityPlayerMP player) {
		recipes.removeIf(recipe -> TechnologyManager.INSTANCE.getLocked(recipe.getRecipeOutput()) != null);
		if (!recipes.isEmpty())
			super.add(recipes, player);
	}

	@Override
	public void read(NBTTagCompound tag) {
		this.isGuiOpen = tag.getBoolean("isGuiOpen");
		this.isFilteringCraftable = tag.getBoolean("isFilteringCraftable");
		NBTTagList nbttaglist = tag.getTagList("recipes", 8);

		List<IRecipe> list = new ArrayList<>();

		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			ResourceLocation resourcelocation = new ResourceLocation(nbttaglist.getStringTagAt(i));
			IRecipe irecipe = CraftingManager.getRecipe(resourcelocation);

			if (irecipe == null)
				LOGGER.info("Tried to load unrecognized recipe: {} removed now.", resourcelocation);
			else {
				this.unlock(irecipe);
				Technology tech = TechnologyManager.INSTANCE.getLocked(irecipe.getRecipeOutput());
				if (tech != null && !tech.isResearched(player))
					list.add(irecipe);
			}
		}

		NBTTagList nbttaglist1 = tag.getTagList("toBeDisplayed", 8);

		for (int j = 0; j < nbttaglist1.tagCount(); ++j) {
			ResourceLocation resourcelocation1 = new ResourceLocation(nbttaglist1.getStringTagAt(j));
			IRecipe irecipe1 = CraftingManager.getRecipe(resourcelocation1);

			if (irecipe1 == null)
				LOGGER.info("Tried to load unrecognized recipe: {} removed now.", resourcelocation1);
			else
				this.markNew(irecipe1);
		}

		if (!list.isEmpty())
			remove(list, player); // Invoke remove method so the client also gets updated
	}

	public void addRecipes(List<IRecipe> recipes, EntityPlayerMP player) {
		super.add(recipes, player);
	}

}
