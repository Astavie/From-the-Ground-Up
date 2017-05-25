package ftgumod.compat;

import java.util.Collection;
import java.util.HashSet;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import mezz.jei.Internal;
import mezz.jei.JeiRuntime;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@mezz.jei.api.JEIPlugin
public class CompatJEI implements ICompat, IModPlugin {

	private static IJeiHelpers jeiHelpers;

	private Collection<Integer> tech;

	@SuppressWarnings("unchecked")
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
				for (ItemStack stack : TechnologyHandler.getTechnology(i).getItems())
					jeiHelpers.getIngredientBlacklist().removeIngredientFromBlacklist(stack);

			for (int i : remove) {
				Technology tech = TechnologyHandler.getTechnology(i);
				if (tech.researched)
					continue;

				for (ItemStack stack : tech.getItems())
					jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(stack);
			}

			JeiRuntime runtime = Internal.getRuntime();
			if (runtime != null)
				runtime.getItemListOverlay().rebuildItemFilter();

			tech = new HashSet<Integer>((Collection<Integer>) arg[0]);

			return true;
		}
		return false;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {
	}

	@Override
	public void register(IModRegistry registry) {
		jeiHelpers = registry.getJeiHelpers();
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
	}

}
