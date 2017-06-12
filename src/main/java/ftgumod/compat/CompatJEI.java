package ftgumod.compat;

import java.util.Collection;
import java.util.HashSet;

import ftgumod.ItemList;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ingredients.IIngredientRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@mezz.jei.api.JEIPlugin
public class CompatJEI extends BlankModPlugin implements ICompat {

	private static IIngredientRegistry registry;

	private Collection<Integer> tech;

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public boolean run(Object... arg) {
		if (tech == null) {
			tech = new HashSet<Integer>();
			for (int i = TechnologyHandler.getTotalTechnologies(); i > 0; i--)
				tech.add(i);
		}

		if (arg[0] instanceof Collection) {
			Collection<Integer> add = new HashSet<Integer>((Collection<Integer>) arg[0]);
			Collection<Integer> remove = new HashSet<Integer>(tech);
			add.removeAll(tech);
			remove.removeAll((Collection<Integer>) arg[0]);

			for (int i : add)
				for (ItemList list : TechnologyHandler.getTechnology(i).getItems())
					registry.removeIngredientsAtRuntime(ItemStack.class, list.getRaw());

			for (int i : remove) {
				Technology tech = TechnologyHandler.getTechnology(i);
				if (tech.researched)
					continue;

				for (ItemList list : tech.getItems())
					registry.addIngredientsAtRuntime(ItemStack.class, list.getRaw());
			}

			tech = new HashSet<Integer>((Collection<Integer>) arg[0]);

			return true;
		}
		return false;
	}

	@Override
	public void register(IModRegistry registry) {
		CompatJEI.registry = registry.getIngredientRegistry();
	}

}
