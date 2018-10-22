package ftgumod.proxy;

import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.api.technology.ITechnology;
import ftgumod.client.GuiHandlerClient;
import ftgumod.client.gui.toast.ToastTechnology;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.Technology;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SuppressWarnings("MethodCallSideOnly")
@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {

	public static final KeyBinding key = new KeyBinding("key." + FTGU.MODID + ".open", Keyboard.KEY_B, "key.categories." + FTGU.MODID);

	@Override
	public void displayToastTechnology(ITechnology technology) {
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
	public void autoResearch(Technology tech) {
		if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
			super.autoResearch(tech);
		} else {
			CapabilityTechnology.ITechnology cap = Minecraft.getMinecraft().player.getCapability(CapabilityTechnology.TECH_CAP, null);
			cap.setResearched(tech.getRegistryName().toString());
		}
	}

	@Override
	public void init(FMLInitializationEvent event) {
		key.setKeyConflictContext(KeyConflictContext.UNIVERSAL);
		ClientRegistry.registerKeyBinding(key);

		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

		renderItem.getItemModelMesher().register(Item.getItemFromBlock(Content.b_ideaTable), 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_ideaTable, "inventory"));
		renderItem.getItemModelMesher().register(Item.getItemFromBlock(Content.b_researchTable), 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_researchTable, "inventory"));

		renderItem.getItemModelMesher().register(Content.i_parchmentEmpty, 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_parchmentEmpty, "inventory"));
		renderItem.getItemModelMesher().register(Content.i_parchmentIdea, 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_parchmentIdea, "inventory"));
		renderItem.getItemModelMesher().register(Content.i_parchmentResearch, 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_parchmentResearch, "inventory"));
		renderItem.getItemModelMesher().register(Content.i_researchBook, 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_researchBook, "inventory"));
		renderItem.getItemModelMesher().register(Content.i_magnifyingGlass, 0, new ModelResourceLocation(FTGU.MODID + ":" + Content.n_magnifyingGlass, "inventory"));
	}

}
