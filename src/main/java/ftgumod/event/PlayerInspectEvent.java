package ftgumod.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;

import javax.annotation.Nullable;
import java.util.List;

public class PlayerInspectEvent extends PlayerEvent {

	private final EnumHand hand;
	private final List<String> glass;
	private final BlockPos pos;
	private final EnumFacing face;
	private final ItemStack block;
	private boolean useful;

	public PlayerInspectEvent(EntityPlayer player, EnumHand hand, List<String> glass, BlockPos pos, EnumFacing face, ItemStack block, boolean useful) {
		super(player);
		this.hand = hand;
		this.glass = glass;
		this.pos = pos;
		this.face = face;
		this.block = block;
		this.useful = useful;
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

	public List<String> getInspectedList() {
		return glass;
	}

	public ItemStack getBlock() {
		return block;
	}

	public boolean isUseful() {
		return useful;
	}

	public void setUseful(boolean useful) {
		this.useful = useful;
	}

}
