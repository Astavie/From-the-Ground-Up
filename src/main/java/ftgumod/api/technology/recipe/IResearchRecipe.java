package ftgumod.api.technology.recipe;

import com.google.gson.JsonObject;
import ftgumod.api.technology.ITechnology;
import ftgumod.api.util.BlockSerializable;
import ftgumod.api.util.JsonContextPublic;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface IResearchRecipe {

	/**
	 * @param block     The new block that has been inspected
	 * @param inspected The already inspected block listed on the magnifying glass
	 * @return If the newly inspected block will help with researching this
	 */
	boolean inspect(BlockSerializable block, List<BlockSerializable> inspected);

	IPuzzle createInstance();

	ITechnology getTechnology();

	void setTechnology(ITechnology tech);

	interface Factory<T extends IResearchRecipe> {

		T deserialize(JsonObject object, JsonContextPublic context, ResourceLocation technology);

	}

}
