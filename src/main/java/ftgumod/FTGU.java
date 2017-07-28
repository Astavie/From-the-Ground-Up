package ftgumod;

import ftgumod.compat.ICompat;
import ftgumod.compat.immersiveengineering.CompatIE;
import ftgumod.crafttweaker.FTGUTweaker;
import ftgumod.packet.PacketDispatcher;
import ftgumod.proxy.ProxyCommon;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.CapabilityTechnology.DefaultImpl;
import ftgumod.technology.CapabilityTechnology.ITechnology;
import ftgumod.technology.CapabilityTechnology.Storage;
import ftgumod.technology.TechnologyHandler;
import ftgumod.tileentity.TileEntityIdeaTable;
import ftgumod.tileentity.TileEntityResearchTable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Mod(modid = FTGU.MODID)
public class FTGU {

	public static final String MODID = "ftgumod";

	private static final Field REGISTRY = ReflectionHelper.findField(CriteriaTriggers.class, "REGISTRY", "field_192139_s");

	public static boolean headStart = false;
	public static boolean moddedOnly = false;

	@Instance(value = FTGU.MODID)
	public static FTGU INSTANCE;

	@SidedProxy(clientSide = "ftgumod.proxy.ProxyClient", serverSide = "ftgumod.proxy.ProxyCommon")
	public static ProxyCommon PROXY;
	public final Map<String, ICompat> compat = new HashMap<>();

	private ResourceLocation getRecipeGroup(ItemStack output) {
		String s = output.getUnlocalizedName();
		int idx = s.lastIndexOf(":");
		if (idx >= 0)
			s = s.substring(idx + 1);
		return new ResourceLocation(MODID, s);
	}

	private ShapedOreRecipe addShapedRecipe(ItemStack output, Object... recipe) {
		ShapedOreRecipe r = new ShapedOreRecipe(getRecipeGroup(output), output, recipe);
		ForgeRegistries.RECIPES.register(r.setRegistryName(r.getGroup()));
		return r;
	}

	private ShapelessOreRecipe addShapelessRecipe(ItemStack output, Object... recipe) {
		ShapelessOreRecipe r = new ShapelessOreRecipe(getRecipeGroup(output), output, recipe);
		ForgeRegistries.RECIPES.register(r.setRegistryName(r.getGroup()));
		return r;
	}

	@SuppressWarnings({"unchecked"})
	private void registerCriterion(ICriterionTrigger criterion) {
		try {
			Map<ResourceLocation, ICriterionTrigger<?>> registry = (Map<ResourceLocation, ICriterionTrigger<?>>) REGISTRY.get(null);
			if (!registry.containsKey(criterion.getId()))
				registry.put(criterion.getId(), criterion);
			else
				throw new IllegalArgumentException("Duplicate criterion id " + criterion.getId());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void registerItem(Item item, String name) {
		item.setRegistryName(name);
		ForgeRegistries.ITEMS.register(item);
	}

	private void registerBlock(Block block, ItemBlock item, String name) {
		block.setRegistryName(name);
		ForgeRegistries.BLOCKS.register(block);

		registerItem(item, name);
	}

	public boolean runCompat(String name, Object... arg) {
		ICompat compat = this.compat.get(name);
		return compat != null && compat.run(arg);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		GameRegistry.registerTileEntity(TileEntityIdeaTable.class, FTGUAPI.n_ideaTable);
		GameRegistry.registerTileEntity(TileEntityResearchTable.class, FTGUAPI.n_researchTable);

		registerBlock(FTGUAPI.b_ideaTable, FTGUAPI.i_ideaTable, FTGUAPI.n_ideaTable);
		registerBlock(FTGUAPI.b_researchTable, FTGUAPI.i_researchTable, FTGUAPI.n_researchTable);

		registerItem(FTGUAPI.i_parchmentEmpty, FTGUAPI.n_parchmentEmpty);
		registerItem(FTGUAPI.i_parchmentIdea, FTGUAPI.n_parchmentIdea);
		registerItem(FTGUAPI.i_parchmentResearch, FTGUAPI.n_parchmentResearch);
		registerItem(FTGUAPI.i_researchBook, FTGUAPI.n_researchBook);
		registerItem(FTGUAPI.i_lookingGlass, FTGUAPI.n_lookingGlass);

		registerCriterion(FTGUAPI.c_technologyUnlocked);
		registerCriterion(FTGUAPI.c_technologyResearched);
		registerCriterion(FTGUAPI.c_itemLocked);
		registerCriterion(FTGUAPI.c_inspect);

		CapabilityManager.INSTANCE.register(ITechnology.class, new Storage(), DefaultImpl.class);

		MinecraftForge.EVENT_BUS.register(new CapabilityTechnology());
		MinecraftForge.EVENT_BUS.register(new EventHandler());

		PacketDispatcher.registerPackets();

		PROXY.preInit();

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		headStart = config.get(Configuration.CATEGORY_GENERAL, "Head-Start", false, "Set this to true to automatically research Stonecraft, Stoneworking, Carpentry, Refinement, Bibliography, Advanced Combat, Building Blocks and Cooking").getBoolean();
		moddedOnly = config.get(Configuration.CATEGORY_GENERAL, "Modded", false, "Set this to true to automatically research all vanilla technologies").getBoolean();

		config.save();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, PROXY.getGuiHandler());

		PROXY.init();

		TechnologyHandler.init();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (Loader.isModLoaded("immersiveengineering")) {
			ICompat compat = new CompatIE();
			MinecraftForge.EVENT_BUS.register(compat);
			this.compat.put("immersiveengineering", compat);
		}
		if (Loader.isModLoaded("crafttweaker"))
			try {
				FTGUTweaker.class.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		PROXY.postInit();
	}

}
