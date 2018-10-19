package ftgumod.api.technology.recipe;

import ftgumod.api.inventory.ContainerResearch;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

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
	void onStart(ContainerResearch container);

	/**
	 * Fired when the internal inventory of the container changes.
	 * Use this, for example, to send data to the client.
	 */
	void onInventoryChange(ContainerResearch container);

	/**
	 * Fired when a player clicks on the output slot to finish a research.
	 * Use this, for example, to remove items.
	 */
	void onFinish();

	/**
	 * Fired when this puzzle is removed.
	 * Use this, for example, to remove added slots.
	 */
	void onRemove(@Nullable EntityPlayer player, World world, BlockPos pos);

	void setHints(List<ITextComponent> hints);

	@SideOnly(Side.CLIENT)
	void drawForeground(GuiContainer gui, int mouseX, int mouseY);

	@SideOnly(Side.CLIENT)
	void drawBackground(GuiContainer gui, int mouseX, int mouseY);

}
