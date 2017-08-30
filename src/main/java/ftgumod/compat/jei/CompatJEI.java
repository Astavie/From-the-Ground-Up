package ftgumod.compat.jei;

import ftgumod.compat.ICompat;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
@JEIPlugin
public class CompatJEI implements ICompat, IModPlugin {

	private static IIngredientRegistry registry;

	private Collection<String> tech = TechnologyHandler.technologies.stream().map(tech -> tech.getRegistryName().toString()).collect(Collectors.toSet());

	@SuppressWarnings({"unchecked", "deprecation"})
	@Override
	public boolean run(Object... arg) {
		if (arg[0] instanceof Collection) {
			Collection<String> input = (Collection<String>) arg[0];
			input.removeIf(string -> string.endsWith(".unlock"));

			Collection<String> add = new HashSet<>(input);
			Collection<String> remove = new HashSet<>(tech);
			add.removeAll(tech);
			remove.removeAll(input);

			for (String s : add) {
				Technology t = TechnologyHandler.getTechnology(new ResourceLocation(s));
				if (t != null)
					for (Ingredient ingredient : t.getUnlock())
						registry.addIngredientsAtRuntime(ItemStack.class, Arrays.asList(ingredient.getMatchingStacks()));
			}

			for (String s : remove) {
				Technology tech = TechnologyHandler.getTechnology(new ResourceLocation(s));
				if (tech != null)
					for (Ingredient ingredient : tech.getUnlock())
						registry.removeIngredientsAtRuntime(ItemStack.class, Arrays.asList(ingredient.getMatchingStacks()));
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
