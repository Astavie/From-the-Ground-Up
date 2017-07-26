package ftgumod.event;

import ftgumod.FTGU;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerLockEvent extends PlayerEvent {

	private final ItemStack stack;

	public PlayerLockEvent(EntityPlayer player, ItemStack stack, IRecipe recipe) {
		super(player);
		this.stack = stack;

		if (recipe == null || !FTGU.PROXY.getRecipeBook(player).containsRecipe(recipe)) {
			Technology tech = TechnologyHandler.getLocked(stack);
			setCanceled(tech == null || tech.isResearched(player));
		}
	}

	public ItemStack getStack() {
		return stack;
	}

}
