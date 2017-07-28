package ftgumod.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import ftgumod.Decipher;
import ftgumod.Decipher.DecipherGroup;
import ftgumod.FTGUAPI;
import ftgumod.compat.crafttweaker.util.Action.ActionAdd;
import ftgumod.compat.crafttweaker.util.CollectionBuilder;
import ftgumod.compat.crafttweaker.util.InputHelper;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.TechnologyHandler.ITEM_GROUP;
import ftgumod.technology.TechnologyUtil;
import ftgumod.technology.recipe.ResearchRecipe;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.ftgu.Scramble")
public class Scramble {

	@ZenMethod
	public static void addScramble(String tech, IIngredient ingredient, int[] slots) {
		ResearchRecipe research = TechnologyHandler.getResearch(tech);
		if (research == null) {
			CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] Technology " + tech + " does not exist or it doesn't have a research recipe. Command ignored!");
			return;
		}
		Object item = InputHelper.toObject(ingredient);
		if (!TechnologyHandler.unlock.containsKey(research))
			TechnologyHandler.registerDecipher(research, new Decipher());
		CraftTweakerAPI.apply(new Add(research, new DecipherGroup(item, slots)));
	}

	@ZenMethod
	public static void clearScrambles(String tech) {
		ResearchRecipe research = TechnologyHandler.getResearch(tech);
		if (research == null) {
			CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] Technology " + tech + " does not exist or it doesn't have a research recipe. Command ignored!");
			return;
		}
		if (!TechnologyHandler.unlock.containsKey(research)) {
			CraftTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No scrambles found for " + tech + ". Command ignored!");
			return;
		}
		CraftTweakerAPI.apply(new Clear(research));
	}

	private static class Add extends ActionAdd<DecipherGroup> {

		private final ResearchRecipe key;

		private Add(ResearchRecipe key, DecipherGroup group) {
			super(group, new CollectionBuilder<>(TechnologyHandler.unlock.get(key).list));
			this.key = key;
		}

		@Override
		public void apply() {
			super.apply();
			TechnologyHandler.unlock.get(key).recalculateSlots();
		}

		@Override
		public String describe() {
			return "[" + FTGUTweaker.name + "] Adding new scramble to " + key.output.getUnlocalizedName();
		}

	}

	private static class Clear implements IAction {

		private final ResearchRecipe key;

		private Clear(ResearchRecipe key) {
			this.key = key;
		}

		@Override
		public void apply() {
			TechnologyHandler.unlock.remove(key);

			ITEM_GROUP.UNDECIPHERED.clearItems();
			for (ResearchRecipe r : TechnologyHandler.unlock.keySet()) {
				ItemStack i = new ItemStack(FTGUAPI.i_parchmentIdea);
				TechnologyUtil.getItemData(i).setString("FTGU", r.output.getUnlocalizedName());
				ITEM_GROUP.UNDECIPHERED.addItem(i);
			}
		}

		@Override
		public String describe() {
			return "[" + FTGUTweaker.name + "] Clearing all " + key.output.getUnlocalizedName() + " scrambles";
		}

	}

}
