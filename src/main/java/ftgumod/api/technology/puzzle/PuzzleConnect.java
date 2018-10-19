package ftgumod.api.technology.puzzle;

import ftgumod.api.FTGUAPI;
import ftgumod.api.inventory.ContainerResearch;
import ftgumod.api.inventory.InventoryCraftingPersistent;
import ftgumod.api.inventory.SlotCrafting;
import ftgumod.api.technology.recipe.IPuzzle;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class PuzzleConnect implements IPuzzle {

	private final IInventory inventory = new InventoryBasic(null, false, 3);
	private final List<ContainerResearch> registry = new LinkedList<>();
	private final ResearchConnect research;

	public PuzzleConnect(ResearchConnect research) {
		this.research = research;
	}

	@Override
	public ResearchConnect getRecipe() {
		return research;
	}

	@Override
	public boolean test() {
		return false;
	}

	@Override
	public void onStart(ContainerResearch container) {
		registry.add(container);
		InventoryCrafting crafting = new InventoryCraftingPersistent(inventory, 0, 3, 1);
		for (int i = 0; i < 3; i++) {
			final int j = i;
			container.addSlotToContainer(new SlotCrafting(container, crafting, i, 44 + i * 18, 35, 1, stack -> fits(stack, j)));
		}
	}

	private boolean fits(ItemStack stack, int index) {
		ItemStack left = index > 0 ? inventory.getStackInSlot(index - 1) : research.left;
		ItemStack right = index < 2 ? inventory.getStackInSlot(index + 1) : research.right;
		return connects(left, stack) || connects(stack, right);
	}

	private boolean connects(ItemStack s1, ItemStack s2) {
		if (s1.isEmpty() || s2.isEmpty())
			return false;
		for (IRecipe recipe : CraftingManager.REGISTRY) {
			if (FTGUAPI.stackUtils.isEqual(recipe.getRecipeOutput(), s1)) {
				for (Ingredient ingredient : recipe.getIngredients())
					if (ingredient.apply(s2))
						return true;
			} else if (FTGUAPI.stackUtils.isEqual(recipe.getRecipeOutput(), s2))
				for (Ingredient ingredient : recipe.getIngredients())
					if (ingredient.apply(s1))
						return true;
		}
		return false;
	}

	@Override
	public void onInventoryChange(ContainerResearch container) {
	}

	@Override
	public void onFinish() {
		NonNullList<ItemStack> remaining = ForgeHooks.defaultRecipeGetRemainingItems(new InventoryCraftingPersistent(inventory, 0, 3, 1));
		for (int i = 0; i < 3; i++)
			inventory.setInventorySlotContents(i, remaining.get(i));
	}

	@Override
	public void onRemove(@Nullable EntityPlayer player, World world, BlockPos pos) {
		if (player != null) {
			for (int i = 0; i < 3; i++) {
				ItemStack stack = inventory.getStackInSlot(i);
				if (!stack.isEmpty() && !player.addItemStackToInventory(stack))
					player.dropItem(stack, false);
			}
		} else InventoryHelper.dropInventoryItems(world, pos, inventory);

		for (ContainerResearch container : registry)
			container.removeSlots(3);
		registry.clear();
	}

	@Override
	public void setHints(List<ITextComponent> hints) {
	}

	@Override
	public void drawForeground(GuiContainer gui, int mouseX, int mouseY) {

	}

	@Override
	public void drawBackground(GuiContainer gui, int mouseX, int mouseY) {
		gui.drawTexturedModalRect(44 + gui.getGuiLeft(), 35 + gui.getGuiTop(), 0, 166, 18, 18);
		gui.drawTexturedModalRect(62 + gui.getGuiLeft(), 35 + gui.getGuiTop(), 0, 166, 18, 18);
		gui.drawTexturedModalRect(80 + gui.getGuiLeft(), 35 + gui.getGuiTop(), 0, 166, 18, 18);
	}

}
