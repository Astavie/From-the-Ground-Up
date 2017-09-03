package ftgumod.technology;

import com.google.gson.JsonParseException;
import ftgumod.FTGU;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class TechnologyHandler {

	public static final Map<UUID, Map<Technology, AdvancementProgress>> progress = new HashMap<>();

	public static final Map<ResourceLocation, Technology> technologies = new HashMap<>();
	public static final Set<Technology> roots = new HashSet<>();

	public static final Set<String> start = new HashSet<>();
	public static final Set<String> headStart = new HashSet<>();
	public static final Set<String> vanilla = new HashSet<>();

	public static Map<ResourceLocation, String> cache;

	public static void init() {
		/*

		BASIC_CRAFTING = new Technology(Tree.MINECRAFT, null, new ItemStack(Blocks.GRASS), 0, 0, "basic_crafting", Items.WHEAT, Blocks.HAY_BLOCK, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, new ItemStack(Items.DYE, 1, 5), new ItemStack(Items.DYE, 1, 6), new ItemStack(Items.DYE, 1, 7), new ItemStack(Items.DYE, 1, 8), new ItemStack(Items.DYE, 1, 9), new ItemStack(Items.DYE, 1, 10), new ItemStack(Items.DYE, 1, 12), new ItemStack(Items.DYE, 1, 13), new ItemStack(Items.DYE, 1, 14), Items.SUGAR, Blocks.SLIME_BLOCK);
		WOODWORKING = new Technology(Tree.MINECRAFT, BASIC_CRAFTING, new ItemStack(Blocks.PLANKS), 2, 0, "woodworking", Blocks.PLANKS, Blocks.CRAFTING_TABLE, Blocks.WOODEN_SLAB, ITEM_GROUP.WOODEN_STAIRS);
		WRITING = new Technology(Tree.MINECRAFT, BASIC_CRAFTING, new ItemStack(Items.PAPER), 0, -2, "writing", Items.PAPER);
		WOODEN_TOOLS = new Technology(Tree.MINECRAFT, WOODWORKING, new ItemStack(Items.STICK), 4, -1, "wooden_tools", Items.STICK, Items.WOODEN_HOE, Items.WOODEN_PICKAXE);
		RESEARCH = new Technology(Tree.MINECRAFT, WRITING, new ItemStack(FTGUAPI.i_parchmentIdea), -2, -2, "research", FTGUAPI.i_parchmentEmpty, FTGUAPI.b_ideaTable, FTGUAPI.b_researchTable);

		STONECRAFT = new Technology(Tree.MINECRAFT, WOODWORKING, new ItemStack(Blocks.COBBLESTONE), 4, 1, "stonecraft", Blocks.SANDSTONE, new ItemStack(Blocks.STONE_SLAB, 1, 1), new ItemStack(Blocks.STONE_SLAB, 1, 3), new ItemStack(Blocks.STONE_SLAB2, 1, 0), Blocks.STONE_STAIRS, Blocks.SANDSTONE_STAIRS);
		CARPENTRY = new Technology(Tree.MINECRAFT, WOODEN_TOOLS, new ItemStack(Blocks.CHEST), 4, -3, "carpentry", ITEM_GROUP.WOODEN_DOOR, Blocks.TRAPDOOR, ITEM_GROUP.WOODEN_FENCE, ITEM_GROUP.WOODEN_FENCE_GATE, Blocks.CHEST, Items.BED, Items.WOODEN_AXE, Items.WOODEN_SHOVEL, Items.WOODEN_SWORD, Blocks.CARPET, Items.BANNER, Items.BOWL);
		STONEWORKING = new Technology(Tree.MINECRAFT, WOODEN_TOOLS, new ItemStack(Items.STONE_PICKAXE), 6, -1, "stoneworking", Items.STONE_AXE, Items.STONE_HOE, Items.STONE_PICKAXE, Items.STONE_SHOVEL, Items.STONE_SWORD);

		Technology[] a_stoneworking = {STONEWORKING};

		REFINEMENT = new Technology(Tree.MINECRAFT, STONECRAFT, a_stoneworking, new ItemStack(Items.IRON_INGOT), 6, 1, "refinement", Blocks.FURNACE, Items.IRON_INGOT, Items.GOLD_INGOT, Items.COAL, Items.REDSTONE, Items.DIAMOND, Items.EMERALD, new ItemStack(Items.DYE, 1, 4), Items.GOLD_NUGGET, Items.FLINT_AND_STEEL, Items.QUARTZ);
		BIBLIOGRAPHY = new Technology(Tree.MINECRAFT, WRITING, new ItemStack(Items.BOOK), 0, -4, "bibliography", Items.BOOK, Items.WRITABLE_BOOK, Items.WRITTEN_BOOK, Blocks.BOOKSHELF);
		ADVANCED_COMBAT = new Technology(Tree.MINECRAFT, STONEWORKING, new ItemStack(Items.SHIELD), 6, -3, "advanced_combat", Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.BOW, Items.ARROW, Items.ARMOR_STAND, Items.SHIELD);

		Technology[] a_refinement = {REFINEMENT};

		METAL_ARMOR = new Technology(Tree.MINECRAFT, ADVANCED_COMBAT, a_refinement, new ItemStack(Items.IRON_HELMET), 6, -5, "metal_armor", Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
		SMITHING = new Technology(Tree.MINECRAFT, REFINEMENT, new ItemStack(Blocks.ANVIL), 8, 1, "smithing", Blocks.ANVIL, Items.IRON_AXE, Items.IRON_DOOR, Items.IRON_HOE, Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_PICKAXE, Items.GOLDEN_HOE, Items.GOLDEN_SHOVEL, Items.GOLDEN_SWORD, Blocks.IRON_BARS, Blocks.IRON_TRAPDOOR, Items.BUCKET);
		BUILDING_BLOCKS = new Technology(Tree.MINECRAFT, REFINEMENT, new ItemStack(Blocks.BRICK_BLOCK), 8, 2, "building_blocks", Blocks.STAINED_GLASS, Blocks.GLASS_PANE, Blocks.STAINED_GLASS_PANE, Blocks.BRICK_BLOCK, Blocks.BRICK_STAIRS, Blocks.END_BRICKS, Blocks.NETHER_BRICK, Blocks.RED_NETHER_BRICK, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS, Blocks.STONEBRICK, new ItemStack(Blocks.STONE_SLAB, 1, 0), new ItemStack(Blocks.STONE_SLAB, 1, 4), new ItemStack(Blocks.STONE_SLAB, 1, 5), new ItemStack(Blocks.STONE_SLAB, 1, 6), new ItemStack(Blocks.STONE_SLAB, 1, 7), Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.PURPUR_SLAB, Blocks.PURPUR_STAIRS, Blocks.PRISMARINE, Blocks.GLOWSTONE, Blocks.BONE_BLOCK, Blocks.COAL_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.GOLD_BLOCK, Blocks.IRON_BLOCK, Blocks.LAPIS_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.REDSTONE_BLOCK);
		COOKING = new Technology(Tree.MINECRAFT, REFINEMENT, new ItemStack(Items.BEETROOT_SOUP), 8, 3, "cooking", Items.FISHING_ROD, Items.CARROT_ON_A_STICK, Items.CAKE, Items.PUMPKIN_PIE, Items.BEETROOT_SOUP, Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BREAD, Items.COOKIE);
		GILDED_CUISINE = new Technology(Tree.MINECRAFT, COOKING, new ItemStack(Items.GOLDEN_APPLE), 10, 3, "gilded_cuisine", Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.SPECKLED_MELON);
		BREWING = new Technology(Tree.MINECRAFT, GILDED_CUISINE, new ItemStack(Items.BREWING_STAND), 12, 3, "brewing", Items.BREWING_STAND, Items.FERMENTED_SPIDER_EYE, Items.MAGMA_CREAM, Blocks.MAGMA, Items.BLAZE_POWDER, Blocks.NETHER_WART_BLOCK);
		GEM_CUTTING = new Technology(Tree.MINECRAFT, SMITHING, new ItemStack(Items.DIAMOND), 8, -1, "gem_cutting", Items.DIAMOND_AXE, Items.DIAMOND_HOE, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_SWORD);

		Technology[] a_gemcutting = {GEM_CUTTING};

		GEM_ARMOR = new Technology(Tree.MINECRAFT, METAL_ARMOR, a_gemcutting, new ItemStack(Items.DIAMOND_HELMET), 6, -7, "gem_armor", Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
		BASIC_REDSTONE = new Technology(Tree.MINECRAFT, SMITHING, new ItemStack(Items.REDSTONE), 10, 1, "basic_redstone", Blocks.REDSTONE_TORCH, Blocks.WOODEN_PRESSURE_PLATE, Blocks.STONE_PRESSURE_PLATE, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.LEVER, Blocks.WOODEN_BUTTON, Blocks.STONE_BUTTON);
		ADVANCED_REDSTONE = new Technology(Tree.MINECRAFT, BASIC_REDSTONE, new ItemStack(Items.REPEATER), 12, 1, "advanced_redstone", Items.COMPARATOR, Items.REPEATER, Blocks.TRIPWIRE_HOOK, Blocks.TRAPPED_CHEST, Blocks.REDSTONE_LAMP);
		TIME_PLACE_DESTINATION = new Technology(Tree.MINECRAFT, BASIC_REDSTONE, new ItemStack(Items.COMPASS), 10, -1, "time_place_destination", Items.COMPASS, Items.CLOCK, Items.MAP);
		REDSTONE_MACHINERY = new Technology(Tree.MINECRAFT, ADVANCED_REDSTONE, new ItemStack(Blocks.PISTON), 14, 1, "redstone_machinery", Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.DISPENSER, Blocks.DROPPER, Blocks.DAYLIGHT_DETECTOR);
		EXPLOSIVES = new Technology(Tree.MINECRAFT, ADVANCED_REDSTONE, new ItemStack(Blocks.TNT), 12, -1, "explosives", Blocks.TNT, Items.FIRE_CHARGE, Items.FIREWORK_CHARGE, Items.FIREWORKS);
		PLAYER_TRANSPORTATION = new Technology(Tree.MINECRAFT, REDSTONE_MACHINERY, new ItemStack(Items.MINECART), 16, 1, "player_transportation", Blocks.RAIL, Items.MINECART, Items.FURNACE_MINECART, Items.BOAT, Items.ACACIA_BOAT, Items.BIRCH_BOAT, Items.DARK_OAK_BOAT, Items.JUNGLE_BOAT, Items.SPRUCE_BOAT);
		ITEM_TRANSPORTATION = new Technology(Tree.MINECRAFT, PLAYER_TRANSPORTATION, new ItemStack(Items.CHEST_MINECART), 16, -1, "item_transportation", Items.HOPPER_MINECART, Items.CHEST_MINECART, Blocks.HOPPER);
		ADVANCED_RAILS = new Technology(Tree.MINECRAFT, PLAYER_TRANSPORTATION, new ItemStack(Blocks.GOLDEN_RAIL), 16, 3, "advanced_rails", Blocks.ACTIVATOR_RAIL, Blocks.DETECTOR_RAIL, Blocks.GOLDEN_RAIL);
		MUSIC = new Technology(Tree.MINECRAFT, REDSTONE_MACHINERY, a_gemcutting, new ItemStack(Blocks.JUKEBOX), 14, -1, "music", Blocks.JUKEBOX, Blocks.NOTEBLOCK);

		Technology[] a_redstone = {MUSIC, ITEM_TRANSPORTATION, ADVANCED_RAILS, EXPLOSIVES, TIME_PLACE_DESTINATION, GEM_CUTTING, BREWING};

		ENCHANTING = new Technology(Tree.MINECRAFT, BIBLIOGRAPHY, a_redstone, new ItemStack(Blocks.ENCHANTING_TABLE), 0, -6, "enchanting", Blocks.ENCHANTING_TABLE);
		GLOWING_EYES = new Technology(Tree.MINECRAFT, ENCHANTING, new ItemStack(Items.ENDER_EYE), 0, -8, "glowing_eyes", Items.ENDER_EYE);
		ENDER_KNOWLEDGE = new Technology(Tree.MINECRAFT, GLOWING_EYES, new ItemStack(Items.END_CRYSTAL), 0, -10, "ender_knowledge", Items.END_CRYSTAL, Blocks.ENDER_CHEST, Blocks.BEACON);

		UNDECIPHERED_RESEARCH = new Technology(Tree.MINECRAFT, null, new ItemStack(Items.NETHER_STAR), -2, 0, "undeciphered_research", FTGUAPI.i_lookingGlass);

		minecraft = true;
		registerTechnology(BASIC_CRAFTING);
		registerTechnology(WOODWORKING);
		registerTechnology(WRITING);
		registerTechnology(WOODEN_TOOLS);
		registerTechnology(RESEARCH);

		addResearchedFromStart(BASIC_CRAFTING);
		addResearchedFromStart(WOODWORKING);
		addResearchedFromStart(WRITING);
		addResearchedFromStart(WOODEN_TOOLS);
		addResearchedFromStart(RESEARCH);

		registerTechnology(STONECRAFT);
		registerTechnology(CARPENTRY);
		registerTechnology(STONEWORKING);
		registerTechnology(REFINEMENT);
		registerTechnology(BIBLIOGRAPHY);
		registerTechnology(ADVANCED_COMBAT);
		registerTechnology(BUILDING_BLOCKS);
		registerTechnology(COOKING);

		addResearchedFromHeadstart(STONECRAFT);
		addResearchedFromHeadstart(CARPENTRY);
		addResearchedFromHeadstart(STONEWORKING);
		addResearchedFromHeadstart(REFINEMENT);
		addResearchedFromHeadstart(BIBLIOGRAPHY);
		addResearchedFromHeadstart(ADVANCED_COMBAT);
		addResearchedFromHeadstart(BUILDING_BLOCKS);
		addResearchedFromHeadstart(COOKING);

		registerTechnology(METAL_ARMOR);
		registerTechnology(SMITHING);
		registerTechnology(GILDED_CUISINE);
		registerTechnology(BREWING);

		registerTechnology(GEM_CUTTING);
		registerTechnology(GEM_ARMOR);
		registerTechnology(BASIC_REDSTONE);
		registerTechnology(ADVANCED_REDSTONE);
		registerTechnology(TIME_PLACE_DESTINATION);
		registerTechnology(REDSTONE_MACHINERY);
		registerTechnology(EXPLOSIVES);
		registerTechnology(PLAYER_TRANSPORTATION);
		registerTechnology(ITEM_TRANSPORTATION);
		registerTechnology(ADVANCED_RAILS);
		registerTechnology(MUSIC);

		registerTechnology(ENCHANTING);
		ENCHANTING.setCustomUnlock(true);

		registerTechnology(GLOWING_EYES);
		GLOWING_EYES.setCustomUnlock(true);

		registerTechnology(ENDER_KNOWLEDGE);
		ENDER_KNOWLEDGE.setCustomUnlock(true);

		registerTechnology(UNDECIPHERED_RESEARCH);
		UNDECIPHERED_RESEARCH.setCustomUnlock(true);
		minecraft = false;

		registerIdea(STONECRAFT, "C", 'C', ITEM_GROUP.UNSMOOTH_STONE);
		registerResearch(STONECRAFT, " P ", " C ", " W ", 'P', ITEM_GROUP.PICKAXE, 'C', ITEM_GROUP.UNSMOOTH_STONE, 'W', ITEM_GROUP.CRAFTING);

		registerIdea(CARPENTRY, "WS", 'W', Blocks.PLANKS, 'S', Blocks.WOOL);
		registerResearch(CARPENTRY, "SSS", "WAW", "WCW", 'S', Blocks.WOODEN_SLAB, 'W', Blocks.PLANKS, 'A', ITEM_GROUP.AXE, 'C', Blocks.CRAFTING_TABLE);

		registerIdea(STONEWORKING, "SC", 'S', Items.STICK, 'C', Blocks.COBBLESTONE);
		registerResearch(STONEWORKING, "CCC", " S ", " S ", 'C', Blocks.COBBLESTONE, 'S', Items.STICK);

		registerIdea(REFINEMENT, "CO", 'C', Items.COAL, 'O', ITEM_GROUP.ORE);
		registerResearch(REFINEMENT, "CCC", "COC", "CFC", 'C', Blocks.COBBLESTONE, 'O', ITEM_GROUP.ORE, 'F', Items.COAL);

		registerIdea(BIBLIOGRAPHY, "PL", 'P', Items.PAPER, 'L', Items.LEATHER);
		registerResearch(BIBLIOGRAPHY, "WWW", "LPL", "WWW", 'W', Blocks.PLANKS, 'L', Items.LEATHER, 'P', Items.PAPER);

		registerIdea(ADVANCED_COMBAT, "SL", 'S', ITEM_GROUP.SWORD, 'L', Items.LEATHER);
		registerResearch(ADVANCED_COMBAT, " L ", "SLI", " L ", 'L', Items.LEATHER, 'S', ITEM_GROUP.SWORD, 'I', Items.IRON_INGOT);

		registerIdea(METAL_ARMOR, "LI", 'L', ITEM_GROUP.LEATHER_ARMOR, 'I', ITEM_GROUP.INGOT);
		registerResearch(METAL_ARMOR, "III", "ILI", "   ", 'I', ITEM_GROUP.INGOT, 'L', Items.LEATHER_HELMET);

		registerIdea(SMITHING, "I", 'I', ITEM_GROUP.INGOT);
		registerResearch(SMITHING, "   ", " I ", "FCU", 'I', ITEM_GROUP.INGOT, 'F', Items.FLINT_AND_STEEL, 'C', ITEM_GROUP.CRAFTING, 'U', Blocks.FURNACE);

		registerIdea(BUILDING_BLOCKS, "SC", 'S', new ItemStack(Blocks.STONE), 'C', Items.CLAY_BALL);
		registerResearch(BUILDING_BLOCKS, "CCC", "CCC", "SFS", 'C', Items.CLAY_BALL, 'S', new ItemStack(Blocks.STONE), 'F', Blocks.FURNACE);

		registerIdea(COOKING, "HU", 'H', ITEM_GROUP.HEAT, 'U', ITEM_GROUP.UNCOOKED_FOOD);
		registerIdea(COOKING, "BS", 'B', Items.BOWL, 'S', ITEM_GROUP.STEW);
		registerResearch(COOKING, "VMF", "WIE", " B ", 'V', ITEM_GROUP.VEGETABLE, 'M', ITEM_GROUP.UNCOOKED_FOOD, 'F', ITEM_GROUP.FRUIT, 'W', Items.WHEAT, 'I', Items.MILK_BUCKET, 'E', Items.EGG, 'B', Items.BOWL);

		registerIdea(GILDED_CUISINE, "GC", 'G', Items.GOLD_INGOT, 'C', ITEM_GROUP.CARROTAPPLE);
		registerResearch(GILDED_CUISINE, "NNN", "NCN", "NNN", 'N', Items.GOLD_NUGGET, 'C', ITEM_GROUP.CARROTAPPLE);

		registerIdea(BREWING, "BW", 'B', new ItemStack(Items.POTIONITEM, 1, 0), 'W', Items.NETHER_WART);
		ResearchRecipe r_brewing = new ResearchRecipe(BREWING, " W ", " S ", " B ", 'B', new ItemStack(Items.POTIONITEM, 1, 0), 'W', Items.NETHER_WART, 'S', Items.SUGAR);

		registerResearch(r_brewing);
		registerDecipher(r_brewing, new Decipher(new DecipherGroup(Items.CAULDRON, 7)));

		registerIdea(GEM_CUTTING, "D", 'D', Items.DIAMOND);
		ResearchRecipe r_gem_cutting = new ResearchRecipe(GEM_CUTTING, " P ", " D ", " C ", 'C', ITEM_GROUP.CRAFTING, 'D', Items.DIAMOND, 'P', ITEM_GROUP.PICKAXE);

		registerResearch(r_gem_cutting);
		registerDecipher(r_gem_cutting, new Decipher(new DecipherGroup(Blocks.DIAMOND_ORE, 4)));

		registerIdea(GEM_ARMOR, "MD", 'M', ITEM_GROUP.METAL_ARMOR, 'D', Items.DIAMOND);
		registerResearch(GEM_ARMOR, "DDD", "DMD", "   ", 'D', Items.DIAMOND, 'M', ITEM_GROUP.METAL_HELMET);

		registerIdea(BASIC_REDSTONE, "R", 'R', Items.REDSTONE);
		registerResearch(BASIC_REDSTONE, "R  ", "SRR", "CCC", 'R', Items.REDSTONE, 'S', Items.STICK, 'C', ITEM_GROUP.COLORFUL);

		registerIdea(ADVANCED_REDSTONE, "RC", 'R', Blocks.REDSTONE_TORCH, 'C', ITEM_GROUP.COLORFUL);
		registerResearch(ADVANCED_REDSTONE, "CTR", "RTC", "CCC", 'C', ITEM_GROUP.COLORFUL, 'T', Blocks.REDSTONE_TORCH, 'R', Items.REDSTONE);

		registerIdea(TIME_PLACE_DESTINATION, "RP", 'R', Items.REDSTONE, 'P', Items.PAPER);
		registerResearch(TIME_PLACE_DESTINATION, "PGP", "IRI", "PGP", 'P', Items.PAPER, 'G', Items.GOLD_INGOT, 'I', Items.IRON_INGOT, 'R', Items.REDSTONE);

		registerIdea(REDSTONE_MACHINERY, "RIW", 'R', Items.REDSTONE, 'I', Items.IRON_INGOT, 'W', Blocks.PLANKS);
		registerResearch(REDSTONE_MACHINERY, "WWW", "RCL", "SSS", 'W', Blocks.PLANKS, 'R', Blocks.REDSTONE_BLOCK, 'C', Items.COMPARATOR, 'L', Blocks.REDSTONE_LAMP, 'S', Blocks.COBBLESTONE);

		registerIdea(EXPLOSIVES, "FG", 'F', Items.FLINT_AND_STEEL, 'G', Items.GUNPOWDER);
		registerResearch(EXPLOSIVES, "BSC", "SGS", " F ", 'B', Items.BLAZE_POWDER, 'S', Blocks.SAND, 'C', Items.COAL, 'G', Items.GUNPOWDER, 'F', Items.FLINT_AND_STEEL);

		registerIdea(PLAYER_TRANSPORTATION, "IS", 'I', Items.IRON_INGOT, 'S', Items.STICK);
		registerResearch(PLAYER_TRANSPORTATION, "W W", "WWW", "ISI", 'W', Blocks.PLANKS, 'I', Items.IRON_INGOT, 'S', Items.STICK);

		registerIdea(ITEM_TRANSPORTATION, "MC", 'M', Items.MINECART, 'C', Blocks.CHEST);
		registerResearch(ITEM_TRANSPORTATION, " C ", " M ", " I ", 'C', Blocks.CHEST, 'M', Items.MINECART, 'I', Blocks.RAIL);

		registerIdea(ADVANCED_RAILS, "TR", 'T', Blocks.RAIL, 'R', Items.REDSTONE);
		registerResearch(ADVANCED_RAILS, "OP ", "TTT", "RRR", 'O', Blocks.REDSTONE_TORCH, 'P', ITEM_GROUP.PRESSURE_PLATE, 'T', Blocks.RAIL, 'R', Items.REDSTONE);

		registerIdea(MUSIC, "ER", 'E', Items.REDSTONE, 'R', ITEM_GROUP.RECORD);
		registerResearch(MUSIC, " DI", " R ", "WWW", 'D', Items.DIAMOND, 'I', Items.IRON_INGOT, 'R', ITEM_GROUP.RECORD, 'W', Blocks.PLANKS);

		registerIdea(ENCHANTING, "E", 'E', Items.ENCHANTED_BOOK);
		ResearchRecipe r_enchanting = new ResearchRecipe(ENCHANTING, "BBB", "BEB", "OOO", 'B', Blocks.BOOKSHELF, 'E', Items.ENCHANTED_BOOK, 'O', Blocks.OBSIDIAN);

		registerResearch(r_enchanting);
		registerDecipher(r_enchanting, new Decipher(new DecipherGroup(Blocks.OBSIDIAN, 6, 7, 8), new DecipherGroup(Blocks.BOOKSHELF, 0, 1, 2, 3, 5)));

		registerIdea(GLOWING_EYES, "EB", 'E', Items.ENDER_PEARL, 'B', Items.BLAZE_POWDER);
		ResearchRecipe r_glowing_eyes = new ResearchRecipe(GLOWING_EYES, "OEO", "EBE", "OEO", 'O', Blocks.OBSIDIAN, 'E', Items.ENDER_PEARL, 'B', Items.BLAZE_POWDER);

		registerResearch(r_glowing_eyes);
		registerDecipher(r_glowing_eyes, new Decipher(new DecipherGroup(Blocks.SOUL_SAND, 1, 3, 5, 7)));

		registerIdea(ENDER_KNOWLEDGE, "D", 'D', ITEM_GROUP.DRAGON);
		ResearchRecipe r_ender_knowledge = new ResearchRecipe(ENDER_KNOWLEDGE, "WWW", " S ", " E ", 'S', Items.NETHER_STAR, 'W', new ItemStack(Items.SKULL, 1, 1), 'E', Blocks.END_ROD);

		registerResearch(r_ender_knowledge);
		registerDecipher(r_ender_knowledge, new Decipher(new DecipherGroup(Blocks.END_PORTAL, 7), new DecipherGroup(new ItemStack(Blocks.SKULL, 1, 1), 0, 1, 2), new DecipherGroup(Blocks.DRAGON_EGG, 4)));

		registerIdea(UNDECIPHERED_RESEARCH, "B", 'B', ITEM_GROUP.UNDECIPHERED);
		registerResearch(UNDECIPHERED_RESEARCH, "ONO", "NGN", "ONO", 'O', Blocks.OBSIDIAN, 'N', Items.GOLD_NUGGET, 'G', Blocks.GLASS_PANE);

		*/
	}

	public static Technology getLocked(ItemStack item) {
		if (!item.isEmpty())
			for (Technology t : technologies.values())
				for (Ingredient ingredient : t.getUnlock())
					if (ingredient.test(item))
						return t;
		return null;
	}

	public static AdvancementProgress getProgress(EntityPlayer player, Technology technology) {
		return progress.computeIfAbsent(player.getUniqueID(), uuid -> new HashMap<>()).computeIfAbsent(technology, tech -> {
			AdvancementProgress progress = new AdvancementProgress();
			progress.update(tech.getCriteria(), tech.getRequirements());

			CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
			for (String criterion : progress.getRemaningCriteria())
				if (cap.isResearched(tech.getRegistryName().toString() + "#" + criterion))
					progress.grantCriterion(criterion);

			return progress;
		});
	}

	public static void clear() {
		progress.clear();

		technologies.clear();
		roots.clear();

		start.clear();
		headStart.clear();
		vanilla.clear();
	}

	public static void reload(World world) {
		clear();

		File dir = new File(new File(world.getSaveHandler().getWorldDirectory(), "data"), "technologies");

		Map<ResourceLocation, String> json = new HashMap<>();

		if (dir.exists() && dir.isDirectory())
			for (File file : FileUtils.listFiles(dir, new String[] {"json"}, true)) {
				if (file.getParentFile().equals(dir) || file.getParentFile().getParentFile().equals(dir))
					continue;
				String relative = dir.toPath().relativize(file.toPath()).toString();

				int index = relative.indexOf('/');
				String domain = relative.substring(0, index);
				String name = FilenameUtils.removeExtension(relative.substring(index));

				ResourceLocation id = new ResourceLocation(domain, name);

				try {
					json.put(id, new String(Files.readAllBytes(file.toPath())));
				} catch (IOException e) {
					Technology.getLogger().error("Couldn't read technology {} from {}", id, file, e);
				}
			}
		else
			dir.mkdirs();

		cache = new HashMap<>(json);

		loadBuiltin().forEach(json::putIfAbsent);
		deserialize(json);
	}

	public static Map<ResourceLocation, String> loadBuiltin() {
		Map<ResourceLocation, String> json = new HashMap<>();
		Loader.instance().getActiveModList().forEach(mod -> CraftingHelper.findFiles(mod, "assets/" + mod.getModId() + "/technologies", null, (root, file) -> {
			String relative = root.relativize(file).toString();
			if (!"json".equals(FilenameUtils.getExtension(file.toString())) || relative.startsWith("_") || !relative.contains("/"))
				return true;

			String name = FilenameUtils.removeExtension(relative);
			ResourceLocation id = new ResourceLocation(mod.getModId(), name);

			try {
				json.put(id, new String(Files.readAllBytes(file)));
			} catch (IOException e) {
				Technology.getLogger().error("Couldn't read technology {} from {}", id, file, e);
				return false;
			}

			return true;
		}, true, true));

		return json;
	}

	public static void deserialize(Map<ResourceLocation, String> json) {
		Map<ResourceLocation, Technology.Builder> builders = new HashMap<>();
		Map<ResourceLocation, Technology> technologies = new HashMap<>();

		for (Map.Entry<ResourceLocation, String> file : json.entrySet())
			try {
				builders.put(file.getKey(), FTGU.GSON.fromJson(file.getValue(), Technology.Builder.class));
			} catch (JsonParseException e) {
				cache.remove(file.getKey());
				Technology.getLogger().error("Couldn't load technology " + file.getKey(), e);
			}

		boolean load = true;
		while (!builders.isEmpty() && load) {
			load = false;

			Iterator<Map.Entry<ResourceLocation, Technology.Builder>> iterator = builders.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<ResourceLocation, Technology.Builder> entry = iterator.next();

				if (entry.getValue().resolveParent(technologies)) {
					try {
						Technology technology = entry.getValue().build(entry.getKey(), new JsonContext(entry.getKey().getResourceDomain()));
						technologies.put(technology.getRegistryName(), technology);
						load = true;
					} catch (JsonParseException e) {
						cache.remove(entry.getKey());
						Technology.getLogger().error("Couldn't load technology " + entry.getKey(), e);
					}

					iterator.remove();
				}
			}

			if (!load)
				for (Map.Entry<ResourceLocation, Technology.Builder> entry : builders.entrySet()) {
					cache.remove(entry.getKey());
					Technology.getLogger().error("Couldn't load technology " + entry.getKey());
				}
		}

		technologies.values().forEach(tech -> {
			if (tech.hasParent())
				tech.getParent().getChildren().add(tech);

			if (tech.start)
				start.add(tech.getRegistryName().toString());
			if (tech.headStart)
				headStart.add(tech.getRegistryName().toString());
			if (tech.getRegistryName().getResourceDomain().equals(FTGU.MODID))
				vanilla.add(tech.getRegistryName().toString());

			if (tech.isRoot())
				roots.add(tech);
		});

		Technology.getLogger().info("Loaded " + technologies.size() + " technolog" + (technologies.size() != 1 ? "ies" : "y"));
		TechnologyHandler.technologies.putAll(technologies);
	}

	public enum GUI {
		IDEATABLE, RESEARCHTABLE
	}

}
