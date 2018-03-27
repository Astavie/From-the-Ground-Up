package ftgumod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ftgumod.command.CommandTechnology;
import ftgumod.compat.ICompat;
import ftgumod.compat.gamestages.CompatGameStages;
import ftgumod.compat.gamestages.UnlockGameStage;
import ftgumod.compat.immersiveengineering.CompatIE;
import ftgumod.compat.immersiveengineering.UnlockMultiblockFactory;
import ftgumod.packet.PacketDispatcher;
import ftgumod.proxy.ProxyCommon;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.CapabilityTechnology.DefaultImpl;
import ftgumod.technology.CapabilityTechnology.Storage;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import ftgumod.tileentity.TileEntityIdeaTable;
import ftgumod.tileentity.TileEntityResearchTable;
import ftgumod.util.EmptyNBTPredicate;
import ftgumod.util.FluidPredicate;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.advancements.critereon.ItemPredicates;
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
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = FTGU.MODID)
public class FTGU {

	public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Technology.Builder.class, new Technology.Deserializer()).registerTypeAdapter(AdvancementRewards.class, new AdvancementRewards.Deserializer()).registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer()).registerTypeHierarchyAdapter(Style.class, new Style.Serializer()).registerTypeAdapterFactory(new EnumTypeAdapterFactory()).create();

	public static final String MODID = "ftgumod";

	public static boolean copy = true;
	public static boolean custom = false;

	@Instance(value = FTGU.MODID)
	public static FTGU INSTANCE;

	@SidedProxy(clientSide = "ftgumod.proxy.ProxyClient", serverSide = "ftgumod.proxy.ProxyCommon")
	public static ProxyCommon PROXY;
	public final Map<String, ICompat> compat = new HashMap<>();

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
		GameRegistry.registerTileEntity(TileEntityIdeaTable.class, Content.n_ideaTable);
		GameRegistry.registerTileEntity(TileEntityResearchTable.class, Content.n_researchTable);

		registerBlock(Content.b_ideaTable, Content.i_ideaTable, Content.n_ideaTable);
		registerBlock(Content.b_researchTable, Content.i_researchTable, Content.n_researchTable);

		registerItem(Content.i_parchmentEmpty, Content.n_parchmentEmpty);
		registerItem(Content.i_parchmentIdea, Content.n_parchmentIdea);
		registerItem(Content.i_parchmentResearch, Content.n_parchmentResearch);
		registerItem(Content.i_researchBook, Content.n_researchBook);
		registerItem(Content.i_magnifyingGlass, Content.n_magnifyingGlass);

		CriteriaTriggers.register(Content.c_technologyUnlocked);
		CriteriaTriggers.register(Content.c_technologyResearched);
		CriteriaTriggers.register(Content.c_itemLocked);
		CriteriaTriggers.register(Content.c_inspect);

		ItemPredicates.register(new ResourceLocation(MODID, "fluid"), FluidPredicate::new);
		ItemPredicates.register(new ResourceLocation(MODID, "enchantment"), object -> new ItemPredicate() {

			@Override
			public boolean test(ItemStack item) {
				return EnchantmentHelper.getEnchantments(item).size() > 0;
			}

		});
		ItemPredicates.register(new ResourceLocation(MODID, "empty_nbt"), EmptyNBTPredicate::factory);

		CapabilityManager.INSTANCE.register(CapabilityTechnology.ITechnology.class, new Storage(), DefaultImpl::new);

		MinecraftForge.EVENT_BUS.register(new CapabilityTechnology());
		MinecraftForge.EVENT_BUS.register(new EventHandler());

		PacketDispatcher.registerPackets();

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		copy = config.get(Configuration.CATEGORY_GENERAL, "Copy", true, "Enables technology copying").getBoolean();
		custom = config.get(Configuration.CATEGORY_GENERAL, "Custom", false, "Disables loading of built-in technologies").getBoolean();

		config.save();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, PROXY.getGuiHandler());
		PROXY.init(event);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (Loader.isModLoaded("immersiveengineering")) {
			MinecraftForge.EVENT_BUS.register(new CompatIE());
			TechnologyManager.INSTANCE.registerUnlock(new ResourceLocation("immersiveengineering", "multiblock"), new UnlockMultiblockFactory());
		}
		if (Loader.isModLoaded("gamestages")) {
			ICompat compat = new CompatGameStages();
			this.compat.put("gamestages", compat);
			MinecraftForge.EVENT_BUS.register(compat);

			TechnologyManager.INSTANCE.registerUnlock(new ResourceLocation("gamestages", "stage"), new UnlockGameStage.Factory());
		}
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandTechnology());
	}

}
