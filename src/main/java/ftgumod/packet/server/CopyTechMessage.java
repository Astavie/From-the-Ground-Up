package ftgumod.packet.server;

import ftgumod.FTGUAPI;
import ftgumod.packet.MessageHandler;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.TechnologyUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CopyTechMessage implements IMessage {

	private int id;

	public CopyTechMessage() {
	}

	public CopyTechMessage(int id) {
		this.id = id;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
	}

	public static class CopyTechMessageHandler extends MessageHandler<CopyTechMessage> {

		@Override
		public IMessage handleMessage(EntityPlayer player, CopyTechMessage message, MessageContext ctx) {
			Technology tech = TechnologyHandler.getTechnology(message.id);

			if (tech != null && tech.isResearched(player)) {
				int index = -1;
				for (int i = 0; i < player.inventory.getSizeInventory(); i++)
					if (!player.inventory.getStackInSlot(i).isEmpty() && player.inventory.getStackInSlot(i).getItem() == FTGUAPI.i_parchmentEmpty)
						index = i;

				if (index != -1) {
					player.inventory.getStackInSlot(index).shrink(1);

					ItemStack result = new ItemStack(FTGUAPI.i_parchmentResearch);
					TechnologyUtil.getItemData(result).setString("FTGU", tech.getUnlocalizedName());

					if (player.inventory.getFirstEmptyStack() == -1)
						player.dropItem(result, true);
					else
						player.inventory.addItemStackToInventory(result);
				}
			}
			return null;
		}

	}

}
