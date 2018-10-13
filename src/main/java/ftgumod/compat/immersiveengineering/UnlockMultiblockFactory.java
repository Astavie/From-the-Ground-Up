package ftgumod.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDevices;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.api.technology.unlock.IUnlock;
import ftgumod.api.technology.unlock.UnlockRecipe;
import ftgumod.api.util.JsonContextPublic;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.apache.commons.lang3.EnumUtils;

import java.util.Locale;

public class UnlockMultiblockFactory implements IUnlock.Factory<UnlockRecipe> {

	@Override
	public UnlockRecipe deserialize(JsonObject object, JsonContextPublic context, ResourceLocation technology) {
		String name = JsonUtils.getString(object, "multiblock");
		MultiblockHandler.IMultiblock multiblock = null;

		for (MultiblockHandler.IMultiblock m : MultiblockHandler.getMultiblocks())
			if (m.getUniqueName().equals(name)) {
				multiblock = m;
				break;
			}
		if (multiblock == null) throw new JsonSyntaxException("Unknown multiblock " + name);

		CompatIE.UNLOCK.put(multiblock, technology);

		if (object.has("item")) {
			object.remove("type");
			object.remove("multiblock");
			return new UnlockRecipe(CraftingHelper.getIngredient(object, context));
		} else {
			String block = name.split(":")[1].replaceAll("(.)(\\p{Lu})", "$1_$2").toUpperCase(Locale.ROOT);

			ItemStack icon;
			if (EnumUtils.isValidEnum(BlockTypes_StoneDevices.class, block))
				icon = new ItemStack(IEContent.blockStoneDevice, BlockTypes_StoneDevices.valueOf(block).getMeta());
			else if (EnumUtils.isValidEnum(BlockTypes_MetalMultiblock.class, block))
				icon = new ItemStack(IEContent.blockMetalMultiblock, BlockTypes_MetalMultiblock.valueOf(block).getMeta());
			else throw new JsonSyntaxException("IE multiblock has no icon and no icon was specified!");

			return new UnlockRecipe(Ingredient.fromStacks(icon));
		}
	}

}
