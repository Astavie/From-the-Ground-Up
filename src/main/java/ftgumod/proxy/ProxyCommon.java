package ftgumod.proxy;

import ftgumod.GuiHandler;
import ftgumod.technology.Technology;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class ProxyCommon {

	public void showTechnologyToast(Technology technology) {
	}

	public IGuiHandler getGuiHandler() {
		return new GuiHandler();
	}

	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return ctx.getServerHandler().player;
	}

	public abstract RecipeBook getRecipeBook(EntityPlayer player);

	public void preInit() {
	}

	public void init() {
	}

	public void postInit() {
	}

}
