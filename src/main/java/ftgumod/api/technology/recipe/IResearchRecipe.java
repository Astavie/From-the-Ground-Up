package ftgumod.api.technology.recipe;

import ftgumod.api.util.BlockSerializable;

import java.util.List;

public interface IResearchRecipe {

	/**
	 * @param block     The new block that has been inspected
	 * @param inspected The already inspected block listed on the magnifying glass
	 * @return If the newly inspected block will help with researching this
	 */
	boolean inspect(BlockSerializable block, List<BlockSerializable> inspected);

	IPuzzle createInstance();

}
