package ftgumod.api.inventory;

import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class InventoryCraftingPersistent extends InventoryCrafting {

	private final IInventory parent;
	private final int offset;
	private final int size;

	public InventoryCraftingPersistent(IInventory parent, int offset, int width, int height) {
		super(null, width, height);

		this.parent = parent;
		this.offset = offset;
		this.size = width * height;
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int index) {
		return index < 0 || index >= size ? ItemStack.EMPTY : parent.getStackInSlot(index + offset);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return getStackInSlot(index + offset).splitStack(count);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index >= 0 && index < size)
			parent.setInventorySlotContents(index + offset, stack);
		else throw new ArrayIndexOutOfBoundsException();
	}

	@Override
	public void markDirty() {
		parent.markDirty();
	}

	@Override
	public void clear() {
		for (int i = 0; i < size; i++)
			parent.removeStackFromSlot(i + offset);
	}

	@Override
	public void fillStackedContents(RecipeItemHelper helper) {
		for (int i = 0; i < size; i++)
			helper.accountStack(parent.getStackInSlot(i + offset));
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < size; i++)
			if (!parent.getStackInSlot(i + offset).isEmpty())
				return false;
		return true;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return index < 0 || index >= size ? ItemStack.EMPTY : parent.removeStackFromSlot(index + offset);
	}

}
