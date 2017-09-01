package ftgumod.proxy;

import ftgumod.FTGU;
import ftgumod.FTGUAPI;
import ftgumod.client.GuiHandlerClient;
import ftgumod.client.gui.toast.ToastTechnology;
import ftgumod.compat.jei.CompatJEI;
import ftgumod.technology.Technology;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("MethodCallSideOnly")
@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {

	@Override
	public void showTechnologyToast(Technology technology) {
		if (technology.getDisplay().shouldShowToast())
			Minecraft.getMinecraft().getToastGui().add(new ToastTechnology(technology));
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
	public void preInit() {
	}

	@Override
	public void init() {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

		renderItem.getItemModelMesher().register(Item.getItemFromBlock(FTGUAPI.b_ideaTable), 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_ideaTable, "inventory"));
		renderItem.getItemModelMesher().register(Item.getItemFromBlock(FTGUAPI.b_researchTable), 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_researchTable, "inventory"));

		renderItem.getItemModelMesher().register(FTGUAPI.i_parchmentEmpty, 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_parchmentEmpty, "inventory"));
		renderItem.getItemModelMesher().register(FTGUAPI.i_parchmentIdea, 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_parchmentIdea, "inventory"));
		renderItem.getItemModelMesher().register(FTGUAPI.i_parchmentResearch, 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_parchmentResearch, "inventory"));
		renderItem.getItemModelMesher().register(FTGUAPI.i_researchBook, 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_researchBook, "inventory"));
		renderItem.getItemModelMesher().register(FTGUAPI.i_lookingGlass, 0, new ModelResourceLocation(FTGU.MODID + ":" + FTGUAPI.n_lookingGlass, "inventory"));
	}

	@Override
	public void postInit() {
		if (Loader.isModLoaded("jei"))
			FTGU.INSTANCE.compat.put("jei", new CompatJEI());
	}

}
