package silentbeta;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;

/**
 * 54-slot inventory in a single block. The vanilla chest GUI handles any
 * multiple of 9 rows, so no custom container or screen is needed —
 * displayGUIChest works in singleplayer and over the network.
 */
public class TileEntityVault extends TileEntity implements IInventory {
	public static final int SIZE = 54;

	private ItemStack[] contents = new ItemStack[SIZE];

	public int getSizeInventory() {
		return SIZE;
	}

	public ItemStack getStackInSlot(int slot) {
		return this.contents[slot];
	}

	public ItemStack decrStackSize(int slot, int count) {
		if(this.contents[slot] == null) {
			return null;
		}

		ItemStack result;
		if(this.contents[slot].stackSize <= count) {
			result = this.contents[slot];
			this.contents[slot] = null;
		} else {
			result = this.contents[slot].splitStack(count);
			if(this.contents[slot].stackSize == 0) {
				this.contents[slot] = null;
			}
		}

		this.onInventoryChanged();
		return result;
	}

	public void setInventorySlotContents(int slot, ItemStack stack) {
		this.contents[slot] = stack;
		if(stack != null && stack.stackSize > this.getInventoryStackLimit()) {
			stack.stackSize = this.getInventoryStackLimit();
		}

		this.onInventoryChanged();
	}

	public String getInvName() {
		return "Obsidian Vault";
	}

	public int getInventoryStackLimit() {
		return 64;
	}

	public boolean canInteractWith(EntityPlayer player) {
		if(this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this) {
			return false;
		}

		return player.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
	}

	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		NBTTagList items = tag.getTagList("Items");
		this.contents = new ItemStack[SIZE];

		for(int i = 0; i < items.tagCount(); ++i) {
			NBTTagCompound item = (NBTTagCompound)items.tagAt(i);
			int slot = item.getByte("Slot") & 255;
			if(slot >= 0 && slot < this.contents.length) {
				this.contents[slot] = new ItemStack(item);
			}
		}

	}

	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		NBTTagList items = new NBTTagList();

		for(int i = 0; i < this.contents.length; ++i) {
			if(this.contents[i] != null) {
				NBTTagCompound item = new NBTTagCompound();
				item.setByte("Slot", (byte)i);
				this.contents[i].writeToNBT(item);
				items.setTag(item);
			}
		}

		tag.setTag("Items", items);
	}
}
