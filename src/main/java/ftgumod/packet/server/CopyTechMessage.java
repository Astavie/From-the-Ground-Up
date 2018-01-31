package ftgumod.packet.server;

import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.api.util.IStackUtils;
import ftgumod.packet.MessageHandler;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import ftgumod.util.StackUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

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
		public IMessage handleMessage(EntityPlayer player, CopyTechMessage message) {
			if (FTGU.copy) {
				Technology tech = TechnologyManager.INSTANCE.getTechnology(new ResourceLocation(message.id));

				if (tech != null && tech.canCopy() && tech.isResearched(player)) {
					int index = -1;
					for (int i = 0; i < player.inventory.getSizeInventory(); i++)
						if (!player.inventory.getStackInSlot(i).isEmpty() && player.inventory.getStackInSlot(i).getItem() == Content.i_parchmentEmpty)
							index = i;

					if (index != -1) {
						player.inventory.getStackInSlot(index).shrink(1);

						ItemStack result = StackUtils.INSTANCE.getParchment(tech.getRegistryName(), IStackUtils.Parchment.RESEARCH);
						if (player.inventory.getFirstEmptyStack() == -1)
							player.dropItem(result, true);
						else
							player.inventory.addItemStackToInventory(result);
					}
				}
			}
			return null;
		}

	}

}
