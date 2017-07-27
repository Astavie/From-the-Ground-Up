package ftgumod.compat.jei;

import ftgumod.ItemList;
import ftgumod.client.ItemListClient;
import ftgumod.compat.ICompat;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.HashSet;

@SideOnly(Side.CLIENT)
@JEIPlugin
public class CompatJEI implements ICompat, IModPlugin {

	private static IIngredientRegistry registry;

	private Collection<Integer> tech;

	@SuppressWarnings({"unchecked"})
	@Override
	public boolean run(Object... arg) {
		if (tech == null) {
			tech = new HashSet<>();
			for (int i = TechnologyHandler.getTotalTechnologies(); i > 0; i--)
				tech.add(i);
		}

		if (arg[0] instanceof Collection) {
			Collection<Integer> input = (Collection<Integer>) arg[0];
			input.removeIf(integer -> integer < 0);

			Collection<Integer> add = new HashSet<>(input);
			Collection<Integer> remove = new HashSet<>(tech);
			add.removeAll(tech);
			remove.removeAll(input);

			for (int i : add) {
				Technology t = TechnologyHandler.getTechnology(i);
				if (t != null)
					for (ItemList list : t.getUnlock()) {
						ItemListClient items = new ItemListClient(list);
						if (!items.isEmpty())
							registry.addIngredientsAtRuntime(ItemStack.class, items.getRaw());
					}
			}

			for (int i : remove) {
				Technology tech = TechnologyHandler.getTechnology(i);
				if (tech != null)
					for (ItemList list : tech.getUnlock()) {
						ItemListClient items = new ItemListClient(list);
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
