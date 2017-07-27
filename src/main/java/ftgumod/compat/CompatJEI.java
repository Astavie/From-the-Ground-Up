package ftgumod.compat;

import java.util.Collection;
import java.util.HashSet;

import ftgumod.ItemList;
import ftgumod.ItemListWildcard;
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

	@SuppressWarnings({ "unchecked" })
	@Override
	public boolean run(Object... arg) {
		if (tech == null) {
			tech = new HashSet<Integer>();
			for (int i = TechnologyHandler.getTotalTechnologies(); i > 0; i--)
				tech.add(i);
		}

		if (arg[0] instanceof Collection) {
			Collection<Integer> input = (Collection<Integer>) arg[0];
			input.removeIf(integer -> integer < 0);

			Collection<Integer> add = new HashSet<Integer>(input);
			Collection<Integer> remove = new HashSet<Integer>(tech);
			add.removeAll(tech);
			remove.removeAll(input);

			for (int i : add) {
				Technology tech = TechnologyHandler.getTechnology(i);
				if (tech == null)
					continue;

				for (ItemList list : tech.getItems()) {
					ItemListWildcard items = new ItemListWildcard(list);
					if (!items.isEmpty())
						registry.addIngredientsAtRuntime(ItemStack.class, items.getRaw());
				}
			}

			for (int i : remove) {
				Technology tech = TechnologyHandler.getTechnology(i);
				if (tech == null || tech.researched)
					continue;

				for (ItemList list : tech.getItems()) {
					ItemListWildcard items = new ItemListWildcard(list);
					if (!items.isEmpty())
						registry.removeIngredientsAtRuntime(ItemStack.class, items.getRaw());
				}
			}

			tech = input;

			return true;
		}
		return false;
	}

	@Override
	public void register(IModRegistry registry) {
		CompatJEI.registry = registry.getIngredientRegistry();
	}

}
