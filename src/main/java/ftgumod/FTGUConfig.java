package ftgumod;

import java.io.File;

import ftgumod.technology.TechnologyManager;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = FTGU.MODID, name = "ftgumod/ftgumod")
public final class FTGUConfig {

    @Config.Comment("If enabled, researches can be copied")
    public static boolean allowResearchCopy = true;

    @Config.Comment("If disabled, default technologies will not be loaded")
    public static boolean loadDefaultTechnologies = true;

    @Config.Comment("If enabled, every player will get a research book when they join a new world or server")
    public static boolean giveResearchBook = true;

    @Config.Comment("Jei hide mode. You can hide nothing, locked recipes or locked recipes and items in JEI")
    public static HideJeiItems jeiHide = HideJeiItems.LOCKED_RECIPES;

    public static enum HideJeiItems {
        NOTHING,
        LOCKED_RECIPES,
        LOCKED_RECIPES_AND_ITEMS
    }

    @Mod.EventBusSubscriber(modid = FTGU.MODID)
    public static final class ConfigReloadListener {
        private static boolean cachedLoadDefaultTechnologies = loadDefaultTechnologies;

        @SubscribeEvent
        public static void onChanged(ConfigChangedEvent.OnConfigChangedEvent ev) {
            if(ev.getModID().equals(FTGU.MODID)) {
                ConfigManager.sync(FTGU.MODID, Config.Type.INSTANCE);

                if(loadDefaultTechnologies != cachedLoadDefaultTechnologies) {
                    if(FMLCommonHandler.instance().getMinecraftServerInstance() != null)
                        TechnologyManager.INSTANCE.reload(new File(FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0].getSaveHandler().getWorldDirectory(), "data"));
                    cachedLoadDefaultTechnologies = loadDefaultTechnologies;
                }
            }
        }
    }
}
