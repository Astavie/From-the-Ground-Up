package ftgumod.api;

import ftgumod.api.technology.ITechnology;
import ftgumod.api.technology.ITechnologyManager;
import ftgumod.api.util.IStackUtils;

public class FTGUAPI {

	public static ITechnologyManager<? extends ITechnology> technologyManager;
	public static IStackUtils<? extends ITechnology> stackUtils;

}
