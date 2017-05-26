package ftgumod.technology.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ftgumod.ItemList;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyUtil;

public class IdeaRecipe {

	public final List<ItemList> recipe;
	public final Technology output;

	public IdeaRecipe(List<ItemList> recipe, Technology output) {
		this.output = output;
		this.recipe = recipe;
	}

	public IdeaRecipe(Technology output, Object... obj) {
		this.output = output;
		this.recipe = new ArrayList<ItemList>();

		char[] recipe = ((String) obj[0]).toCharArray();
		Map<Character, ItemList> items = new HashMap<Character, ItemList>();

		for (int i = 1; i < obj.length; i += 2)
			items.put((Character) obj[i], new ItemList(TechnologyUtil.toItem(obj[i + 1])));

		for (char c : recipe) {
			this.recipe.add(items.get(new Character(c)));
		}
	}

}
