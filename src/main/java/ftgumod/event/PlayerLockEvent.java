package ftgumod.event;

import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nullable;

@Cancelable
public class PlayerLockEvent extends PlayerEvent {

	private final ItemStack stack;
	@Nullable
	private final IRecipe recipe;

	public PlayerLockEvent(EntityPlayer player, ItemStack stack, @Nullable IRecipe recipe) {
		super(player);
		this.stack = stack;
		this.recipe = recipe;

		Technology tech = TechnologyHandler.getLocked(stack);
		setCanceled(tech == null || tech.isResearched(player));
	}

	public ItemStack getStack() {
		return stack;
	}

	@Nullable
	public IRecipe getRecipe() {
		return recipe;
	}

}
