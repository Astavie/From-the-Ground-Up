package ftgumod.event;

import ftgumod.Decipher;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.recipe.ResearchRecipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.List;

@Cancelable
public class PlayerInspectEvent extends PlayerEvent {

	private final EnumHand hand;
	private final List<ItemStack> glass;
	private final BlockPos pos;
	private final EnumFacing face;
	private final ItemStack block;

	public PlayerInspectEvent(EntityPlayer player, EnumHand hand, List<ItemStack> glass, BlockPos pos, EnumFacing face, ItemStack block) {
		super(player);
		this.hand = hand;
		this.glass = glass;
		this.pos = pos;
		this.face = face;
		this.block = block;

		boolean cancel = true;

		loop:
		for (ResearchRecipe r : TechnologyHandler.unlock.keySet())
			if (r.output.canResearch(player)) {
				Decipher d = TechnologyHandler.unlock.get(r);
				for (Decipher.DecipherGroup g : d.list)
					for (ItemStack s : g.unlock)
						if ((!s.isEmpty() && s.getMetadata() == OreDictionary.WILDCARD_VALUE && s.getItem() == block.getItem()) || ItemStack.areItemStacksEqual(s, block)) {
							cancel = false;
							break loop;
						}
			}
		setCanceled(cancel);
	}

	public EnumHand getHand() {
		return hand;
	}

	public BlockPos getPos() {
		return pos;
	}

	@Nullable
	public EnumFacing getFace() {
		return face;
	}

	public World getWorld() {
		return getEntityPlayer().world;
	}

	public List<ItemStack> getInspected() {
		return glass;
	}

	public ItemStack getBlock() {
		return block;
	}

}
