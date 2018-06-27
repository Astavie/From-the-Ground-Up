package ftgumod.api.technology.recipe;

import ftgumod.api.inventory.ContainerFTGU;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	void onStart(ContainerFTGU container);

	/**
	 * Fired when the internal inventory of the container changes.
	 * Use this, for example, to send data to the client.
	 */
	void onInventoryChange(ContainerFTGU container);

	/**
	 * Fired when a player clicks on the output slot to finish a research.
	 * Use this, for example, to remove items.
	 */
	void onFinish(ContainerFTGU container);

	/**
	 * Fired when a player adds a new technology.
	 * Use this, for example, to remove added slots.
	 */
	void onRemove(ContainerFTGU container);

	@SideOnly(Side.CLIENT)
	void drawForeground(GuiContainer gui, int mouseX, int mouseY);

	@SideOnly(Side.CLIENT)
	void drawBackground(GuiContainer gui, int mouseX, int mouseY);

}
