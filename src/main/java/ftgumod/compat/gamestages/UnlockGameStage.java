package ftgumod.compat.gamestages;

import com.google.gson.JsonObject;
import ftgumod.FTGU;
import ftgumod.api.technology.unlock.IUnlock;
import net.darkhax.gamestages.GameStages;
import net.darkhax.gamestages.capabilities.PlayerDataHandler;
import net.darkhax.gamestages.packet.PacketStage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.crafting.JsonContext;

public class UnlockGameStage implements IUnlock {

	private final String stage;
	private final ITextComponent message;

	public UnlockGameStage(String stage, ITextComponent message) {
		this.stage = stage;
		this.message = message;
	}

	@Override
	public boolean isDisplayed() {
		return false;
	}

	@Override
	public Ingredient getIcon() {
		return null;
	}

	@Override
	public boolean unlocks(ItemStack stack) {
		return false;
	}

	@Override
	public void unlock(EntityPlayerMP player) {
		PlayerDataHandler.getStageData(player).unlockStage(stage);
		GameStages.NETWORK.sendTo(new PacketStage(stage, true), player);
		player.sendMessage(message);
	}

	@Override
	public void lock(EntityPlayerMP player) {
	}

	public static class Factory implements IUnlock.Factory<UnlockGameStage> {

		@Override
		public UnlockGameStage deserialize(JsonObject object, JsonContext context, ResourceLocation technology) {
			String stage = JsonUtils.getString(object, "stage");
			ITextComponent message;
			if (object.has("message"))
				message = FTGU.GSON.fromJson(object.get("message"), ITextComponent.class);
			else
				message = new TextComponentTranslation("commands.gamestage.add.target", stage);

			return new UnlockGameStage(stage, message);
		}

	}

}
