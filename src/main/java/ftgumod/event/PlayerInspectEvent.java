package ftgumod.event;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerInspectEvent extends PlayerEvent {

	private final EnumHand hand;
	private final EnumFacing face;
	private final BlockPos pos;
	private final IBlockState block;

	public PlayerInspectEvent(EntityPlayer player, EnumHand hand, BlockPos pos, IBlockState block, EnumFacing face) {
		super(player);
		this.hand = hand;
		this.pos = pos;
		this.block = block;
		this.face = face;
	}

	public EnumHand getHand() {
		return hand;
	}

	public EnumFacing getFace() {
		return face;
	}

	public BlockPos getPos() {
		return pos;
	}

	public World getWorld() {
		return getEntityPlayer().world;
	}

	public IBlockState getBlockState() {
		return block;
	}

	public Block getBlock() {
		return block.getBlock();
	}

}
