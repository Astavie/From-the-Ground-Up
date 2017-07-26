package ftgumod.compat;

import ftgumod.ItemList;
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

	@SuppressWarnings({"unchecked", "deprecation"})
	@Override
	public boolean run(Object... arg) {
		if (tech == null) {
			tech = new HashSet<>();
			for (int i = TechnologyHandler.getTotalTechnologies(); i > 0; i--)
				tech.add(i);
		}

		if (arg[0] instanceof Collection) {
			Collection<Integer> add = new HashSet<>((Collection<Integer>) arg[0]);
			Collection<Integer> remove = new HashSet<>(tech);
			add.removeAll(tech);
			remove.removeAll((Collection<Integer>) arg[0]);

			for (int i : add) {
				Technology t = TechnologyHandler.getTechnology(i);
				if (t != null)
					for (ItemList list : t.getUnlock())
						registry.removeIngredientsAtRuntime(ItemStack.class, list.getRaw());
			}

			for (int i : remove) {
				Technology tech = TechnologyHandler.getTechnology(i);
				if (tech != null) {
					if (tech.isResearched())
						continue;

					for (ItemList list : tech.getUnlock())
						registry.addIngredientsAtRuntime(ItemStack.class, list.getRaw());
				}
			}

			tech = new HashSet<>((Collection<Integer>) arg[0]);

			return true;
		}
		return false;
	}

	@Override
	public void register(IModRegistry registry) {
		CompatJEI.registry = registry.getIngredientRegistry();
	}

}
