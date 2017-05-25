package ftgumod.gui;

import ftgumod.FTGU;
import net.minecraft.entity.player.EntityPlayer;
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
	}

	public ItemStack[] copy() {
		ItemStack[] stack = new ItemStack[this.stack.length];
		for (int i = 0; i < stack.length; i++)
			if (this.stack[i] != null)
				stack[i] = this.stack[i].copy();
		return stack;
	}

	public void copy(ItemStack... stack) {
		if (this.stack.length < stack.length)
			this.stack = new ItemStack[stack.length];
		for (int i = 0; i < stack.length; i++)
			if (stack[i] != null)
				this.stack[i] = stack[i].copy();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagList nbttaglist = compound.getTagList("Items", 10);

		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbtTagCompound = nbttaglist.getCompoundTagAt(i);
			byte b0 = nbtTagCompound.getByte("Slot");

			if (b0 >= 0 && b0 < stack.length) {
				stack[b0] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < stack.length; ++i) {
			if (stack[i] != null) {
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
			stack[i] = null;
	}

	@Override
	public void closeInventory(EntityPlayer arg0) {

	}

	@Override
	public ItemStack decrStackSize(int arg0, int arg1) {
		if (stack[arg0] != null) {
			ItemStack itemstack;

			if (stack[arg0].stackSize <= arg1) {
				itemstack = stack[arg0];
				stack[arg0] = null;
				return itemstack;
			} else {
				itemstack = stack[arg0].splitStack(arg1);

				if (stack[arg0].stackSize == 0) {
					stack[arg0] = null;
				}

				return itemstack;
			}
		} else {
			return null;
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
		stack[arg0] = null;
		return item;
	}

	@Override
	public void setField(int arg0, int arg1) {

	}

	@Override
	public void setInventorySlotContents(int arg0, ItemStack arg1) {
		stack[arg0] = arg1;

		if (arg1 != null && arg1.stackSize > getInventoryStackLimit()) {
			arg1.stackSize = getInventoryStackLimit();
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

}
