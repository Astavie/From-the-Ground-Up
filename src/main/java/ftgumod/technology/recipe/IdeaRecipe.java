package ftgumod.technology.recipe;

import java.util.HashMap;
import java.util.Map;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyUtil;

public class IdeaRecipe {

	public final Object[] recipe;
	public final Technology output;

	public IdeaRecipe(Object[] recipe, Technology output) {
		this.output = output;
		this.recipe = recipe;
	}

	public IdeaRecipe(Technology output, Object... obj) {
		this.output = output;

		recipe = new Object[((String) obj[0]).length()];

		char[] recipe = ((String) obj[0]).toCharArray();
		Map<Character, Object> items = new HashMap<Character, Object>();

		for (int i = 1; i < obj.length; i += 2) {
			items.put((Character) obj[i], TechnologyUtil.toItem(obj[i + 1]));
		}

		for (int i = 0; i < recipe.length; i++) {
			this.recipe[i] = items.get(new Character(recipe[i]));
		}
	}

}
