package ftgumod.proxy;

import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.client.GuiHandlerClient;
import ftgumod.client.gui.toast.ToastTechnology;
import ftgumod.technology.Technology;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("MethodCallSideOnly")
@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {

	@Override
	public void displayToastTechnology(Technology technology) {
		if (technology.getDisplayInfo().shouldShowToast())
			Minecraft.getMinecraft().getToastGui().add(new ToastTechnology(technology));
	}

	@Override
	public void clearToasts() {
		Minecraft.getMinecraft().getToastGui().clear();
	}

	@Override
	public IGuiHandler getGuiHandler() {
		return new GuiHandlerClient();
	}

	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return ctx.side.isClient() ? Minecraft.getMinecraft().player : super.getPlayerEntity(ctx);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

		renderItem.getItemModelMesher().register(Item.getItemFromBlock(Content.b_ideaTable), 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_ideaTable, "inventory"));
		renderItem.getItemModelMesher().register(Item.getItemFromBlock(Content.b_researchTable), 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_researchTable, "inventory"));

		renderItem.getItemModelMesher().register(Content.i_parchmentEmpty, 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_parchmentEmpty, "inventory"));
		renderItem.getItemModelMesher().register(Content.i_parchmentIdea, 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_parchmentIdea, "inventory"));
		renderItem.getItemModelMesher().register(Content.i_parchmentResearch, 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_parchmentResearch, "inventory"));
		renderItem.getItemModelMesher().register(Content.i_researchBook, 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_researchBook, "inventory"));
		renderItem.getItemModelMesher().register(Content.i_lookingGlass, 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_lookingGlass, "inventory"));
	}

}
