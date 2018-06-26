package ftgumod.api.technology.recipe;

import ftgumod.inventory.ContainerResearchTable;

public interface IPuzzle {

	/**
	 * @return This puzzle's research
	 */
	IResearchRecipe getRecipe();

	/**
	 * @return If the puzzle is completed
	 */
	boolean test();

	/**
	 * Fired when this technology is placed in its slot.
	 * Use this, for example, to add slots.
	 */
	void onStart(ContainerResearchTable container);

	/**
	 * Fired when the internal inventory of the container changes.
	 * Use this, for example, to send data to the client.
	 */
	void onInventoryChange(ContainerResearchTable container);

	/**
	 * Fired when a player clicks on the output slot to finish a research.
	 * Use this, for example, to remove items.
	 */
	void onFinish(ContainerResearchTable container);

	/**
	 * Fired when a player adds a new technology.
	 * Use this, for example, to remove added slots.
	 */
	void onRemove(ContainerResearchTable container);

}
