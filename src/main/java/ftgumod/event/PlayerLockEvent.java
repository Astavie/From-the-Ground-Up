package ftgumod.event;

import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerLockEvent extends PlayerEvent {

	private final ItemStack stack;
	private boolean willLock;

	public PlayerLockEvent(EntityPlayer player, ItemStack stack) {
		super(player);
		this.stack = stack;

		Technology tech = TechnologyHandler.getLocked(stack);
		this.willLock = tech != null && !tech.isResearched(player);
	}

	public ItemStack getStack() {
		return stack;
	}

	public boolean willLock() {
		return willLock;
	}

	public void setWillLock(boolean willLock) {
		this.willLock = willLock;
	}

}
