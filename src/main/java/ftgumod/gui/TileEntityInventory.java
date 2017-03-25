package ftgumod.gui;

import ftgumod.FTGU;
import ftgumod.gui.researchtable.ContainerResearchTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityLockable;

public abstract class TileEntityInventory extends TileEntityLockable implements IInventory {

	protected ItemStack[] stack;
	protected String name;

	public TileEntityInventory(int size, String name) {
		stack = new ItemStack[size];
		this.name = name;
		clear();
	}

	public ItemStack[] copy() {
		ItemStack[] stack = new ItemStack[this.stack.length];
		for (int i = 0; i < stack.length; i++)
			if (this.stack[i] != ItemStack.EMPTY)
				stack[i] = this.stack[i].copy();
		return stack;
	}

	public void copy(ItemStack... stack) {
		if (this.stack.length < stack.length)
			this.stack = new ItemStack[stack.length];
		for (int i = 0; i < stack.length; i++)
			if (stack[i] != ItemStack.EMPTY)
				this.stack[i] = stack[i].copy();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagList nbttaglist = compound.getTagList("Items", 10);
		stack = new ItemStack[getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbtTagCompound = nbttaglist.getCompoundTagAt(i);
			byte b0 = nbtTagCompound.getByte("Slot");

			if (b0 >= 0 && b0 < stack.length) {
				stack[b0] = new ItemStack(nbtTagCompound);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < stack.length; ++i) {
			if (stack[i] != ItemStack.EMPTY && stack[i] != null) {
				NBTTagCompound nbtTagCompound = new NBTTagCompound();
				nbtTagCompound.setByte("Slot", (byte) i);
				stack[i].writeToNBT(nbtTagCompound);
				nbttaglist.appendTag(nbtTagCompound);
			}
		}

		compound.setTag("Items", nbttaglist);
		return compound;
	}

	@Override
	public void clear() {
		for (int i = 0; i < stack.length; i++)
			stack[i] = ItemStack.EMPTY;
	}

	@Override
	public void closeInventory(EntityPlayer arg0) {

	}

	@Override
	public ItemStack decrStackSize(int arg0, int arg1) {
		if (stack[arg0] != ItemStack.EMPTY) {
			ItemStack itemstack;

			if (stack[arg0].getCount() <= arg1) {
				itemstack = stack[arg0];
				stack[arg0] = ItemStack.EMPTY;
				return itemstack;
			} else {
				itemstack = stack[arg0].splitStack(arg1);

				if (stack[arg0].getCount() == 0) {
					stack[arg0] = ItemStack.EMPTY;
				}

				return itemstack;
			}
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public int getField(int arg0) {
		return 0;
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public int getSizeInventory() {
		return stack.length;
	}

	@Override
	public ItemStack getStackInSlot(int arg0) {
		return stack[arg0];
	}

	@Override
	public boolean isItemValidForSlot(int arg0, ItemStack arg1) {
		return true;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer arg0) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer arg0) {

	}

	@Override
	public ItemStack removeStackFromSlot(int arg0) {
		ItemStack item = stack[arg0].copy();
		stack[arg0] = ItemStack.EMPTY;
		return item;
	}

	@Override
	public void setField(int arg0, int arg1) {

	}

	@Override
	public void setInventorySlotContents(int arg0, ItemStack arg1) {
		stack[arg0] = arg1;

		if (arg1 != ItemStack.EMPTY && arg1.getCount() > getInventoryStackLimit()) {
			arg1.setCount(getInventoryStackLimit());
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	public String getGuiID() {
		return FTGU.MODID + ":" + name;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack i: stack)
			if (i != ItemStack.EMPTY)
				return false;
		return true;
	}

}
