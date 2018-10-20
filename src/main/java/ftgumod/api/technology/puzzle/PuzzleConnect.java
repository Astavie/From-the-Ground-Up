package ftgumod.api.technology.puzzle;

import ftgumod.api.FTGUAPI;
import ftgumod.api.inventory.ContainerResearch;
import ftgumod.api.inventory.InventoryCraftingPersistent;
import ftgumod.api.inventory.SlotCrafting;
import ftgumod.api.technology.recipe.IPuzzle;
import ftgumod.api.util.predicate.ItemPredicate;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
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

	private static boolean connects(ItemPredicate predicate, ItemStack stack) {
		if (predicate.getMatchingStacks().length == 0)
			return true;
		for (IRecipe recipe : CraftingManager.REGISTRY) {
			if (predicate.test(recipe.getRecipeOutput())) {
				for (Ingredient ingredient : recipe.getIngredients())
					if (ingredient.test(stack))
						return true;
			} else if (FTGUAPI.stackUtils.isEqual(recipe.getRecipeOutput(), stack))
				for (Ingredient ingredient : recipe.getIngredients())
					for (ItemStack s : predicate.getMatchingStacks())
						if (ingredient.test(s))
							return true;
		}
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

	@Override
	public boolean test() {
		for (int i = 0; i < 3; i++)
			if (inventory.getStackInSlot(i).isEmpty())
				return false;
		return true;
	}

	private boolean fits(ItemStack stack, int index) {
		ItemPredicate left = index > 0 ? new ItemPredicate(inventory.getStackInSlot(index - 1)) : research.left;
		ItemPredicate right = index < 2 ? new ItemPredicate(inventory.getStackInSlot(index + 1)) : research.right;
		return connects(left, stack) && connects(right, stack);
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
		// Grid
		gui.drawTexturedModalRect(44 + gui.getGuiLeft(), 35 + gui.getGuiTop(), 0, 166, 54, 18);

		// Items
		gui.mc.getRenderItem().zLevel = 100.0F;

		GlStateManager.enableDepth();
		gui.mc.getRenderItem().renderItemAndEffectIntoGUI(gui.mc.player, research.left.getDisplayStack(), 26 + gui.getGuiLeft(), 35 + gui.getGuiTop());
		gui.mc.getRenderItem().renderItemOverlayIntoGUI(gui.mc.fontRenderer, research.left.getDisplayStack(), 26 + gui.getGuiLeft(), 35 + gui.getGuiTop(), null);

		gui.mc.getRenderItem().renderItemAndEffectIntoGUI(gui.mc.player, research.right.getDisplayStack(), 98 + gui.getGuiLeft(), 35 + gui.getGuiTop());
		gui.mc.getRenderItem().renderItemOverlayIntoGUI(gui.mc.fontRenderer, research.right.getDisplayStack(), 98 + gui.getGuiLeft(), 35 + gui.getGuiTop(), null);

		gui.mc.getRenderItem().zLevel = 0.0F;
	}

}
