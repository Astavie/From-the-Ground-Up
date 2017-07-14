package ftgumod.event;

import ftgumod.FTGU;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerLockEvent extends PlayerEvent {

	private final ItemStack stack;
	private boolean willLock;

	public PlayerLockEvent(EntityPlayer player, ItemStack stack, IRecipe recipe) {
		super(player);
		this.stack = stack;

		if (recipe != null && FTGU.PROXY.getRecipeBook(player).containsRecipe(recipe))
			this.willLock = false;
		else {
			Technology tech = TechnologyHandler.getLocked(stack);
			this.willLock = tech != null && !tech.isResearched(player);
		}
	}

	public ItemStack getStack() {
		return stack;
	}

	public boolean isLocked() {
		return willLock;
	}

	public void setLocked(boolean willLock) {
		this.willLock = willLock;
	}

}
