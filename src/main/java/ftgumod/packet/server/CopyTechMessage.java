package ftgumod.packet.server;

import ftgumod.FTGUAPI;
import ftgumod.packet.MessageHandler;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import ftgumod.util.StackUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CopyTechMessage implements IMessage {

	private String id;

	public CopyTechMessage() {
	}

	public CopyTechMessage(Technology technology) {
		this.id = technology.getRegistryName().toString();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, id);
	}

	public static class CopyTechMessageHandler extends MessageHandler<CopyTechMessage> {

		@Override
		public IMessage handleMessage(EntityPlayer player, CopyTechMessage message, MessageContext ctx) {
			Technology tech = TechnologyHandler.technologies.get(new ResourceLocation(message.id));

			if (tech != null && tech.isResearched(player)) {
				int index = -1;
				for (int i = 0; i < player.inventory.getSizeInventory(); i++)
					if (!player.inventory.getStackInSlot(i).isEmpty() && player.inventory.getStackInSlot(i).getItem() == FTGUAPI.i_parchmentEmpty)
						index = i;

				if (index != -1) {
					player.inventory.getStackInSlot(index).shrink(1);

					ItemStack result = new ItemStack(FTGUAPI.i_parchmentResearch);
					StackUtils.getItemData(result).setString("FTGU", tech.getRegistryName().toString());

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
