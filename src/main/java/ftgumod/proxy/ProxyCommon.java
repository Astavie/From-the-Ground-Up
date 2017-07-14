package ftgumod.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class ProxyCommon {

	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return ctx.getServerHandler().player;
	}

	public abstract RecipeBook getRecipeBook(EntityPlayer player);

	public abstract void preInit();

	public abstract void init();

	public abstract void postInit();

}
