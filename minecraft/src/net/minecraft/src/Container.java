package net.minecraft.src;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Container {
	public List field_20123_d = new ArrayList();
	public List slots = new ArrayList();
	public int windowId = 0;
	private short field_20917_a = 0;
	protected List field_20121_g = new ArrayList();
	private Set field_20918_b = new HashSet();

	protected void addSlot(Slot var1) {
		var1.slotNumber = this.slots.size();
		this.slots.add(var1);
		this.field_20123_d.add((Object)null);
	}

	public void updateCraftingResults() {
		for(int var1 = 0; var1 < this.slots.size(); ++var1) {
			ItemStack var2 = ((Slot)this.slots.get(var1)).getStack();
			ItemStack var3 = (ItemStack)this.field_20123_d.get(var1);
			if(!ItemStack.areItemStacksEqual(var3, var2)) {
				var3 = var2 == null ? null : var2.copy();
				this.field_20123_d.set(var1, var3);

				for(int var4 = 0; var4 < this.field_20121_g.size(); ++var4) {
					((ICrafting)this.field_20121_g.get(var4)).func_20159_a(this, var1, var3);
				}
			}
		}

	}

	public Slot getSlot(int var1) {
		return (Slot)this.slots.get(var1);
	}

	public ItemStack getStackInSlot(int var1) {
		Slot var2 = (Slot)this.slots.get(var1);
		return var2 != null ? var2.getStack() : null;
	}

	public ItemStack func_27280_a(int var1, int var2, boolean var3, EntityPlayer var4) {
		ItemStack var5 = null;
		if(var2 == 0 || var2 == 1) {
			InventoryPlayer var6 = var4.inventory;
			if(var1 == -999) {
				if(var6.getItemStack() != null && var1 == -999) {
					if(var2 == 0) {
						var4.dropPlayerItem(var6.getItemStack());
						var6.setItemStack((ItemStack)null);
					}

					if(var2 == 1) {
						var4.dropPlayerItem(var6.getItemStack().splitStack(1));
						if(var6.getItemStack().stackSize == 0) {
							var6.setItemStack((ItemStack)null);
						}
					}
				}
			} else {
				int var10;
				if(var3) {
					var5 = this.transferStackInSlot(var1);
				} else {
					Slot var12 = (Slot)this.slots.get(var1);
					if(var12 != null) {
						var12.onSlotChanged();
						ItemStack var13 = var12.getStack();
						ItemStack var14 = var6.getItemStack();
						if(var13 != null) {
							var5 = var13.copy();
						}

						if(var13 == null) {
							if(var14 != null && var12.isItemValid(var14)) {
								var10 = var2 == 0 ? var14.stackSize : 1;
								if(var10 > var12.getSlotStackLimit()) {
									var10 = var12.getSlotStackLimit();
								}

								var12.putStack(var14.splitStack(var10));
								if(var14.stackSize == 0) {
									var6.setItemStack((ItemStack)null);
								}
							}
						} else if(var14 == null) {
							var10 = var2 == 0 ? var13.stackSize : (var13.stackSize + 1) / 2;
							ItemStack var11 = var12.decrStackSize(var10);
							var6.setItemStack(var11);
							if(var13.stackSize == 0) {
								var12.putStack((ItemStack)null);
							}

							var12.onPickupFromSlot(var6.getItemStack());
						} else if(var12.isItemValid(var14)) {
							if(var13.itemID != var14.itemID || var13.getHasSubtypes() && var13.getItemDamage() != var14.getItemDamage()) {
								if(var14.stackSize <= var12.getSlotStackLimit()) {
									var12.putStack(var14);
									var6.setItemStack(var13);
								}
							} else {
								var10 = var2 == 0 ? var14.stackSize : 1;
								if(var10 > var12.getSlotStackLimit() - var13.stackSize) {
									var10 = var12.getSlotStackLimit() - var13.stackSize;
								}

								if(var10 > var14.getMaxStackSize() - var13.stackSize) {
									var10 = var14.getMaxStackSize() - var13.stackSize;
								}

								var14.splitStack(var10);
								if(var14.stackSize == 0) {
									var6.setItemStack((ItemStack)null);
								}

								var13.stackSize += var10;
							}
						} else if(var13.itemID == var14.itemID && var14.getMaxStackSize() > 1 && (!var13.getHasSubtypes() || var13.getItemDamage() == var14.getItemDamage())) {
							var10 = var13.stackSize;
							if(var10 > 0 && var10 + var14.stackSize <= var14.getMaxStackSize()) {
								var14.stackSize += var10;
								var13.splitStack(var10);
								if(var13.stackSize == 0) {
									var12.putStack((ItemStack)null);
								}

								var12.onPickupFromSlot(var6.getItemStack());
							}
						}
					}
				}
			}
		}

		return var5;
	}

	/**
	 * Real shift-click: moves the clicked stack between the player inventory
	 * and the other plain slots of this container (vanilla b1.7.3 only
	 * re-ran the click). Special slots — crafting result/grid, armor,
	 * furnace output — are never used as targets, so nothing can be lost.
	 * Runs identically on client and server, keeping SMP transactions in sync.
	 */
	protected ItemStack transferStackInSlot(int var1) {
		if(var1 < 0 || var1 >= this.slots.size()) {
			return null;
		}

		Slot var2 = (Slot)this.slots.get(var1);
		if(var2 == null || !var2.getHasStack()) {
			return null;
		}

		ItemStack var3 = var2.getStack();
		ItemStack var4 = var3.copy();
		boolean var5 = var2.inventory instanceof InventoryPlayer;

		for(int var6 = 0; var6 < 2 && var3.stackSize > 0; ++var6) {
			for(int var7 = 0; var7 < this.slots.size() && var3.stackSize > 0; ++var7) {
				Slot var8 = (Slot)this.slots.get(var7);
				if(var7 == var1 || var8.getClass() != Slot.class) {
					continue;
				}

				boolean var9 = var8.inventory instanceof InventoryPlayer;
				if(var9 == var5 || var8.inventory instanceof InventoryCrafting || var8.inventory instanceof InventoryCraftResult) {
					continue;
				}

				ItemStack var10 = var8.getStack();
				if(var6 == 0) {
					if(var10 == null || var10.itemID != var3.itemID || var10.getItemDamage() != var3.getItemDamage() || !var10.isStackable()) {
						continue;
					}

					int var11 = var10.getMaxStackSize();
					if(var11 > var8.getSlotStackLimit()) {
						var11 = var8.getSlotStackLimit();
					}

					int var12 = var11 - var10.stackSize;
					if(var12 > var3.stackSize) {
						var12 = var3.stackSize;
					}

					if(var12 > 0) {
						var10.stackSize += var12;
						var3.stackSize -= var12;
						var8.onSlotChanged();
					}
				} else if(var10 == null && var8.isItemValid(var3)) {
					int var13 = var3.getMaxStackSize();
					if(var13 > var8.getSlotStackLimit()) {
						var13 = var8.getSlotStackLimit();
					}

					if(var13 > var3.stackSize) {
						var13 = var3.stackSize;
					}

					ItemStack var14 = var3.copy();
					var14.stackSize = var13;
					var8.putStack(var14);
					var3.stackSize -= var13;
					var8.onSlotChanged();
				}
			}
		}

		if(var3.stackSize == 0) {
			var2.putStack((ItemStack)null);
		} else {
			var2.onSlotChanged();
		}

		return var3.stackSize == var4.stackSize ? null : var4;
	}

	public void onCraftGuiClosed(EntityPlayer var1) {
		InventoryPlayer var2 = var1.inventory;
		if(var2.getItemStack() != null) {
			var1.dropPlayerItem(var2.getItemStack());
			var2.setItemStack((ItemStack)null);
		}

	}

	public void onCraftMatrixChanged(IInventory var1) {
		this.updateCraftingResults();
	}

	public void putStackInSlot(int var1, ItemStack var2) {
		this.getSlot(var1).putStack(var2);
	}

	public void putStacksInSlots(ItemStack[] var1) {
		for(int var2 = 0; var2 < var1.length; ++var2) {
			this.getSlot(var2).putStack(var1[var2]);
		}

	}

	public void func_20112_a(int var1, int var2) {
	}

	public short func_20111_a(InventoryPlayer var1) {
		++this.field_20917_a;
		return this.field_20917_a;
	}

	public void func_20113_a(short var1) {
	}

	public void func_20110_b(short var1) {
	}

	public abstract boolean isUsableByPlayer(EntityPlayer var1);

	protected void func_28125_a(ItemStack var1, int var2, int var3, boolean var4) {
		int var5 = var2;
		if(var4) {
			var5 = var3 - 1;
		}

		Slot var6;
		ItemStack var7;
		if(var1.isStackable()) {
			while(var1.stackSize > 0 && (!var4 && var5 < var3 || var4 && var5 >= var2)) {
				var6 = (Slot)this.slots.get(var5);
				var7 = var6.getStack();
				if(var7 != null && var7.itemID == var1.itemID && (!var1.getHasSubtypes() || var1.getItemDamage() == var7.getItemDamage())) {
					int var8 = var7.stackSize + var1.stackSize;
					if(var8 <= var1.getMaxStackSize()) {
						var1.stackSize = 0;
						var7.stackSize = var8;
						var6.onSlotChanged();
					} else if(var7.stackSize < var1.getMaxStackSize()) {
						var1.stackSize -= var1.getMaxStackSize() - var7.stackSize;
						var7.stackSize = var1.getMaxStackSize();
						var6.onSlotChanged();
					}
				}

				if(var4) {
					--var5;
				} else {
					++var5;
				}
			}
		}

		if(var1.stackSize > 0) {
			if(var4) {
				var5 = var3 - 1;
			} else {
				var5 = var2;
			}

			while(!var4 && var5 < var3 || var4 && var5 >= var2) {
				var6 = (Slot)this.slots.get(var5);
				var7 = var6.getStack();
				if(var7 == null) {
					var6.putStack(var1.copy());
					var6.onSlotChanged();
					var1.stackSize = 0;
					break;
				}

				if(var4) {
					--var5;
				} else {
					++var5;
				}
			}
		}

	}
}
