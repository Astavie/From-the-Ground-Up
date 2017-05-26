package ftgumod.technology.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ftgumod.ItemList;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyUtil;

public class ResearchRecipe {

	public final List<ItemList> recipe = new ArrayList<ItemList>();
	public final Technology output;

	public ResearchRecipe(Technology output, Object... obj) {
		this.output = output;

		char[] r1 = ((String) obj[0]).toCharArray();
		char[] r2 = ((String) obj[1]).toCharArray();
		char[] r3 = ((String) obj[2]).toCharArray();

		char[] recipe = new char[9];
		recipe[0] = r1[0];
		recipe[1] = r1[1];
		recipe[2] = r1[2];
		recipe[3] = r2[0];
		recipe[4] = r2[1];
		recipe[5] = r2[2];
		recipe[6] = r3[0];
		recipe[7] = r3[1];
		recipe[8] = r3[2];

		Map<Character, ItemList> items = new HashMap<Character, ItemList>();

		for (int i = 3; i < obj.length; i += 2)
			items.put((Character) obj[i], new ItemList(TechnologyUtil.toItem(obj[i + 1])));

		for (char c : recipe) {
			this.recipe.add(items.get(new Character(c)));
		}
	}

}
