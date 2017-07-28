package ftgumod.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;

public class FTGUTweaker {

	public static final String name = "FTGU";

	public FTGUTweaker() {
		CraftTweakerAPI.registerClass(Technology.class);
		CraftTweakerAPI.registerClass(Idea.class);
		CraftTweakerAPI.registerClass(Research.class);
		CraftTweakerAPI.registerClass(Scramble.class);
		CraftTweakerAPI.registerClass(Page.class);
	}

}
