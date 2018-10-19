package ftgumod.api.technology.puzzle;

import com.google.gson.JsonObject;
import ftgumod.api.technology.ITechnology;
import ftgumod.api.technology.recipe.IPuzzle;
import ftgumod.api.technology.recipe.IResearchRecipe;
import ftgumod.api.util.BlockSerializable;
import ftgumod.api.util.JsonContextPublic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ResearchConnect implements IResearchRecipe {

	final ItemStack left;
	final ItemStack right;
	private ITechnology technology;

	public ResearchConnect(ItemStack left, ItemStack right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public boolean inspect(BlockSerializable block, List<BlockSerializable> inspected) {
		return false;
	}

	@Override
	public IPuzzle createInstance() {
		return new PuzzleConnect(this);
	}

	@Override
	public ITechnology getTechnology() {
		return technology;
	}

	@Override
	public void setTechnology(ITechnology tech) {
		this.technology = tech;
	}

	public static class Factory implements IResearchRecipe.Factory<ResearchConnect> {

		@Override
		public ResearchConnect deserialize(JsonObject object, JsonContextPublic context, ResourceLocation technology) {
			return null;
		}

	}

}
