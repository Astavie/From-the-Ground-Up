package ftgumod;

import java.util.HashMap;
import java.util.Map;
import ftgumod.compat.CompatBWM;
import ftgumod.compat.ICompat;
import ftgumod.gui.GuiHandler;
import ftgumod.gui.ideatable.TileEntityIdeaTable;
import ftgumod.gui.researchtable.TileEntityResearchTable;
import ftgumod.minetweaker.FTGUTweaker;
import ftgumod.packet.PacketDispatcher;
import ftgumod.proxy.ProxyCommon;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.CapabilityTechnology.DefaultImpl;
import ftgumod.technology.CapabilityTechnology.ITechnology;
import ftgumod.technology.CapabilityTechnology.Storage;
import ftgumod.technology.TechnologyHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = FTGU.MODID, version = FTGU.VERSION)
public class FTGU {

	public static final String MODID = "ftgumod";
	public static final String VERSION = "Minecraft 1.10.2";

	public static boolean headstart = false;
	public static boolean moddedOnly = false;

	public final Map<String, ICompat> compat = new HashMap<String, ICompat>();

	@Instance(value = FTGU.MODID)
	public static FTGU INSTANCE;

	@SidedProxy(clientSide = "ftgumod.proxy.ProxyClient", serverSide = "ftgumod.proxy.ProxyCommon")
	public static ProxyCommon PROXY;

	private void registerItem(Item item, String name) {
		item.setRegistryName(name);
		GameRegistry.register(item);
	}

	private void registerBlock(Block block, ItemBlock item, String name) {
		block.setRegistryName(name);
		GameRegistry.register(block);

		registerItem(item, name);
	}

	public boolean runCompat(String name, Object... arg) {
		ICompat compat = this.compat.get(name);
		if (compat != null)
			return compat.run(arg);
		return false;
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

		CapabilityManager.INSTANCE.register(ITechnology.class, new Storage(), DefaultImpl.class);

		MinecraftForge.EVENT_BUS.register(new CapabilityTechnology());
		MinecraftForge.EVENT_BUS.register(new EventHandler());

		PacketDispatcher.registerPackets();

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		headstart = config.get(Configuration.CATEGORY_GENERAL, "Headstart", false, "Set this to true to automatically research Stonecraft, Stoneworking, Carpentry, Refinement, Bibliography, Advanced Combat, Building Blocks and Cooking").getBoolean();
		moddedOnly = config.get(Configuration.CATEGORY_GENERAL, "Modded", false, "Set this to true to automatically research all vanilla technologies").getBoolean();

		config.save();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.addRecipe(new ItemStack(FTGUAPI.b_ideaTable), "F P", "SSS", "WBW", 'S', Blocks.WOODEN_SLAB, 'W', Blocks.PLANKS, 'B', Blocks.CRAFTING_TABLE, 'F', Items.FEATHER, 'P', FTGUAPI.i_parchmentEmpty);
		GameRegistry.addRecipe(new ItemStack(FTGUAPI.b_researchTable), "SSS", "CBC", "CWC", 'S', Blocks.WOODEN_SLAB, 'W', Blocks.PLANKS, 'B', Blocks.CRAFTING_TABLE, 'C', Blocks.COBBLESTONE);
		GameRegistry.addRecipe(new ItemStack(FTGUAPI.i_parchmentEmpty), "S", "P", "S", 'S', Items.STICK, 'P', Items.PAPER);
		GameRegistry.addRecipe(new ItemStack(FTGUAPI.i_lookingGlass), " N ", "NGN", "SN ", 'N', Items.GOLD_NUGGET, 'G', Blocks.GLASS_PANE, 'S', Items.STICK);

		Item r = FTGUAPI.i_parchmentResearch;

		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r, r, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r, r, r, r, r, r, r);

		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r, r, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r, r, r, r, r, r, r);

		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());

		PROXY.registerRenderers();

		TechnologyHandler.init();

		TechnologyHandler.BASIC_CRAFTING.researched = true;
		TechnologyHandler.WOODWORKING.researched = true;
		TechnologyHandler.WRITING.researched = true;
		TechnologyHandler.WOODEN_TOOLS.researched = true;
		TechnologyHandler.RESEARCH.researched = true;
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (Loader.isModLoaded("MineTweaker3"))
			try {
				FTGUTweaker.class.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		if (Loader.isModLoaded("betterwithmods"))
			compat.put("betterwithmods", new CompatBWM());
		PROXY.postInit();
	}

}
