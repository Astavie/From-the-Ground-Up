package ftgumod;

import ftgumod.block.BlockIdeaTable;
import ftgumod.block.BlockResearchTable;
import ftgumod.criterion.TriggerInspect;
import ftgumod.criterion.TriggerItemLocked;
import ftgumod.criterion.TriggerTechnology;
import ftgumod.item.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public final class FTGUAPI {

	public static final String n_ideaTable = "idea_table";
	public static final String n_researchTable = "research_table";

	public static final String n_parchmentEmpty = "parchment_empty";
	public static final String n_parchmentIdea = "parchment_idea";
	public static final String n_parchmentResearch = "parchment_research";
	public static final String n_researchBook = "research_book";
	public static final String n_lookingGlass = "looking_glass";

	public static final Block b_ideaTable = new BlockIdeaTable(n_ideaTable);
	public static final Block b_researchTable = new BlockResearchTable(n_researchTable);
	public static final Item i_parchmentEmpty = new ItemParchmentEmpty(n_parchmentEmpty);
	public static final Item i_parchmentIdea = new ItemParchmentIdea(n_parchmentIdea);
	public static final Item i_parchmentResearch = new ItemParchmentResearch(n_parchmentResearch);
	public static final Item i_researchBook = new ItemResearchBook(n_researchBook);
	public static final Item i_lookingGlass = new ItemLookingGlass(n_lookingGlass);

	public static final TriggerTechnology c_technologyUnlocked = new TriggerTechnology("technology_unlocked");
	public static final TriggerTechnology c_technologyResearched = new TriggerTechnology("technology_researched");
	public static final TriggerItemLocked c_itemLocked = new TriggerItemLocked("recipe_locked");
	public static final TriggerInspect c_inspect = new TriggerInspect("block_inspected");

	static final ItemBlock i_ideaTable = new ItemBlock(b_ideaTable);
	static final ItemBlock i_researchTable = new ItemBlock(b_researchTable);

}
