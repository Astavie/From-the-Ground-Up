package ftgumod.api.technology.recipe;

import ftgumod.api.inventory.ContainerResearch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public interface IPuzzle {

	NBTBase write();

	void read(NBTBase tag);

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

	List<ITextComponent> getHints();

	Object getGui();

}
