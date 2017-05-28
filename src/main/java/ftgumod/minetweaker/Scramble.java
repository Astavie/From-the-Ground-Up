package ftgumod.minetweaker;

import java.util.Collection;

import ftgumod.Decipher;
import ftgumod.Decipher.DecipherGroup;
import ftgumod.FTGUAPI;
import ftgumod.minetweaker.util.BaseCollection;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceAdd;
import ftgumod.minetweaker.util.BaseInterface.BaseInterfaceRemove;
import ftgumod.minetweaker.util.InputHelper;
import ftgumod.technology.TechnologyHandler;
import ftgumod.technology.TechnologyHandler.ITEM_GROUP;
import ftgumod.technology.TechnologyUtil;
import ftgumod.technology.recipe.ResearchRecipe;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.ftgu.Scramble")
public class Scramble {

	protected static final String name = FTGUTweaker.name + " Scramble";

	@ZenMethod
	public static void addScrambled(String tech, IIngredient ingredient, int[] slots) {
		ResearchRecipe research = TechnologyHandler.getResearch(tech);
		if (research == null) {
			MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + ftgumod.minetweaker.Technology.name + " found for " + tech + ". Command ignored!");
			return;
		}
		Object item = InputHelper.toObject(ingredient);
		if (!TechnologyHandler.unlock.containsKey(research)) {
			TechnologyHandler.registerDecipher(research, new Decipher());
		}
		Integer[] s = new Integer[slots.length];
		for (int i = 0; i < s.length; i++) {
			s[i] = new Integer(slots[i]);
		}
		MineTweakerAPI.apply(new Add(research, new DecipherGroup(item, s)));
	}

	private static class Add extends BaseInterfaceAdd<DecipherGroup> {

		private final ResearchRecipe key;

		protected Add(ResearchRecipe key, DecipherGroup group) {
			super(name, group, new BaseCollection<DecipherGroup>(TechnologyHandler.unlock.get(key).list));
			this.key = key;
		}

		@Override
		public void apply() {
			add();
			TechnologyHandler.unlock.get(key).recalculateSlots();
		}

		@Override
		public void undo() {
			remove();
			TechnologyHandler.unlock.get(key).recalculateSlots();

			ITEM_GROUP.UNDECIPHERED.clearItems();
			for (ResearchRecipe r : TechnologyHandler.unlock.keySet()) {
				Decipher d = TechnologyHandler.unlock.get(r);
				if (d.list.size() == 0)
					continue;

				ItemStack i = new ItemStack(FTGUAPI.i_parchmentIdea);
				TechnologyUtil.getItemData(i).setString("FTGU", r.output.getUnlocalizedName());
				ITEM_GROUP.UNDECIPHERED.addItem(i);
			}
		}

		@Override
		protected String getRecipeInfo(DecipherGroup group) {
			return "<item:" + group.unlock.toString() + ">";
		}

	}

	@ZenMethod
	public static void removeScrambled(String tech) {
		ResearchRecipe research = TechnologyHandler.getResearch(tech);
		if (research == null) {
			MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + ftgumod.minetweaker.Technology.name + " found for " + tech + ". Command ignored!");
			return;
		}
		if (!TechnologyHandler.unlock.containsKey(research)) {
			MineTweakerAPI.logWarning("[" + FTGUTweaker.name + "] No " + name + " found for " + tech + ". Command ignored!");
			return;
		}
		MineTweakerAPI.apply(new Remove(research, TechnologyHandler.unlock.get(research).list));
	}

	private static class Remove extends BaseInterfaceRemove<DecipherGroup> {

		private final ResearchRecipe key;

		protected Remove(ResearchRecipe key, Collection<DecipherGroup> group) {
			super(name, group, new BaseCollection<DecipherGroup>(TechnologyHandler.unlock.get(key).list));
			this.key = key;
		}

		@Override
		public void undo() {
			add();
			TechnologyHandler.unlock.get(key).recalculateSlots();
		}

		@Override
		public void apply() {
			remove();
			TechnologyHandler.unlock.get(key).recalculateSlots();

			ITEM_GROUP.UNDECIPHERED.clearItems();
			for (ResearchRecipe r : TechnologyHandler.unlock.keySet()) {
				Decipher d = TechnologyHandler.unlock.get(r);
				if (d.list.size() == 0)
					continue;

				ItemStack i = new ItemStack(FTGUAPI.i_parchmentIdea);
				TechnologyUtil.getItemData(i).setString("FTGU", r.output.getUnlocalizedName());
				ITEM_GROUP.UNDECIPHERED.addItem(i);
			}
		}

		@Override
		protected String getRecipeInfo(DecipherGroup group) {
			return "<item:" + group.unlock.toString() + ">";
		}

	}

}
