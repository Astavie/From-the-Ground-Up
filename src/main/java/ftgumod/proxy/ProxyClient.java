package ftgumod.proxy;

import ftgumod.FTGU;
import ftgumod.FTGUAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ProxyClient extends ProxyCommon {

	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return Minecraft.getMinecraft().thePlayer;
	}

	@Override
	public void registerRenderers() {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

		renderItem.getItemModelMesher().register(Item.getItemFromBlock(FTGUAPI.b_ideaTable), 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_ideaTable, "inventory"));
		renderItem.getItemModelMesher().register(Item.getItemFromBlock(FTGUAPI.b_researchTable), 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_researchTable, "inventory"));

		renderItem.getItemModelMesher().register(FTGUAPI.i_parchmentEmpty, 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_parchmentEmpty, "inventory"));
		renderItem.getItemModelMesher().register(FTGUAPI.i_parchmentIdea, 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_parchmentIdea, "inventory"));
		renderItem.getItemModelMesher().register(FTGUAPI.i_parchmentResearch, 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_parchmentResearch, "inventory"));
		renderItem.getItemModelMesher().register(FTGUAPI.i_researchBook, 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_researchBook, "inventory"));
		renderItem.getItemModelMesher().register(FTGUAPI.i_lookingGlass, 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_lookingGlass, "inventory"));
	}

}
