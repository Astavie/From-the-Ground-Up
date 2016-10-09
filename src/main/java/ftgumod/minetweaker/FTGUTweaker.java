package ftgumod.minetweaker;

import minetweaker.MineTweakerAPI;

public class FTGUTweaker {

	public static final String name = "FTGU";

	public FTGUTweaker() {
		MineTweakerAPI.registerClass(Technology.class);
		MineTweakerAPI.registerClass(Idea.class);
		MineTweakerAPI.registerClass(Research.class);
	}

}
