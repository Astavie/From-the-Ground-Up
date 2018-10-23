package ftgumod.api.technology.puzzle;

import ftgumod.api.FTGUAPI;
import ftgumod.api.inventory.ContainerResearch;
import ftgumod.api.inventory.InventoryCraftingPersistent;
import ftgumod.api.inventory.SlotCrafting;
import ftgumod.api.technology.recipe.IPuzzle;
import ftgumod.api.util.predicate.ItemPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.client.config.GuiUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PuzzleConnect implements IPuzzle {

	private final IInventory inventory = new InventoryBasic(null, false, 3);
	private final List<ContainerResearch> registry = new LinkedList<>();
	private final ResearchConnect research;

	public PuzzleConnect(ResearchConnect research) {
		this.research = research;
	}

	@Override
	public NBTBase write() {
		NBTTagList items = new NBTTagList();
		for (int i = 0; i < inventory.getSizeInventory(); ++i) {
			if (!inventory.getStackInSlot(i).isEmpty()) {
				NBTTagCompound compound = new NBTTagCompound();
				compound.setByte("Slot", (byte) i);
				inventory.getStackInSlot(i).writeToNBT(compound);
				items.appendTag(compound);
			}
		}
		return items;
	}

	@Override
	public void read(NBTBase tag) {
		NBTTagList items = (NBTTagList) tag;
		for (int i = 0; i < items.tagCount(); ++i) {
			NBTTagCompound compound = items.getCompoundTagAt(i);
			byte slot = compound.getByte("Slot");
			if (slot >= 0 && slot < inventory.getSizeInventory())
				inventory.setInventorySlotContents(i, new ItemStack(compound));
		}
	}

	@Override
	public ResearchConnect getRecipe() {
		return research;
	}

	private static boolean connects(ItemPredicate predicate, ItemStack stack) {
		if (predicate.getMatchingStacks().length == 0)
			return true;

		// Crafting Recipes
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

		// Furnace Recipes
		for (Map.Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
			if (predicate.test(entry.getValue()) && FTGUAPI.stackUtils.isStackOf(entry.getKey(), stack))
				return true;
			if (FTGUAPI.stackUtils.isEqual(entry.getValue(), stack))
				for (ItemStack s : predicate.getMatchingStacks())
					if (FTGUAPI.stackUtils.isStackOf(entry.getKey(), s))
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
		if (research.left.test(stack) || research.right.test(stack))
			return false;
		for (int i = 0; i < 3; i++)
			if (FTGUAPI.stackUtils.isEqual(stack, inventory.getStackInSlot(i)))
				return false;

		ItemPredicate left = index > 0 ? new ItemPredicate(inventory.getStackInSlot(index - 1)) : research.left;
		ItemPredicate right = index < 2 ? new ItemPredicate(inventory.getStackInSlot(index + 1)) : research.right;
		return connects(left, stack) && connects(right, stack);
	}

	@Override
	public void onInventoryChange(ContainerResearch container) {
		container.markDirty();
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
		mouseX -= gui.getGuiLeft();
		mouseY -= gui.getGuiTop();
		if (research.getTechnology().canResearch(gui.mc.player)) {
			if (mouseX >= 25 && mouseX < 43 && mouseY >= 34 && mouseY < 52)
				gui.drawHoveringText(gui.getItemToolTip(research.left.getDisplayStack()), mouseX, mouseY);
			if (mouseX >= 97 && mouseX < 115 && mouseY >= 34 && mouseY < 52)
				gui.drawHoveringText(gui.getItemToolTip(research.right.getDisplayStack()), mouseX, mouseY);
		} else if (mouseX >= 97 && mouseX < 119 && mouseY >= 35 && mouseY < 50) {
			List<String> text = Collections.singletonList(I18n.format(research.getTechnology().isResearched(gui.mc.player) ? "technology.complete.already" : "technology.complete.understand", research.getTechnology().getDisplayInfo().getTitle().getFormattedText()));
			GuiUtils.drawHoveringText(text, mouseX, mouseY, gui.width, gui.height, gui.width - mouseX - gui.getGuiLeft() - 16, Minecraft.getMinecraft().fontRenderer);
		}
	}

	@Override
	public void drawBackground(GuiContainer gui, int mouseX, int mouseY) {
		// Grid
		gui.drawTexturedModalRect(43 + gui.getGuiLeft(), 34 + gui.getGuiTop(), 0, 166, 54, 18);

		// Items
		if (research.getTechnology().canResearch(gui.mc.player)) {
			RenderHelper.enableGUIStandardItemLighting();
			gui.mc.getRenderItem().zLevel = 100.0F;

			GlStateManager.enableDepth();
			gui.mc.getRenderItem().renderItemAndEffectIntoGUI(gui.mc.player, research.left.getDisplayStack(), 26 + gui.getGuiLeft(), 35 + gui.getGuiTop());
			gui.mc.getRenderItem().renderItemOverlayIntoGUI(gui.mc.fontRenderer, research.left.getDisplayStack(), 26 + gui.getGuiLeft(), 35 + gui.getGuiTop(), null);

			gui.mc.getRenderItem().renderItemAndEffectIntoGUI(gui.mc.player, research.right.getDisplayStack(), 98 + gui.getGuiLeft(), 35 + gui.getGuiTop());
			gui.mc.getRenderItem().renderItemOverlayIntoGUI(gui.mc.fontRenderer, research.right.getDisplayStack(), 98 + gui.getGuiLeft(), 35 + gui.getGuiTop(), null);

			gui.mc.getRenderItem().zLevel = 0.0F;
		} else
			gui.drawTexturedModalRect(97 + gui.getGuiLeft(), 35 + gui.getGuiTop(), 54, 181, 22, 15);
	}

}
