package net.lepko.easycrafting.core.block;

import net.lepko.easycrafting.core.inventory.ContainerEasyCrafting;
import net.lepko.easycrafting.core.inventory.gui.GuiEasyCrafting;
import net.lepko.easycrafting.core.inventory.gui.IGuiTile;
import net.lepko.easycrafting.core.util.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

public class TileEntityEasyCrafting extends TileEntity implements IInventory, IGuiTile {

    private ItemStack[] inventory;

    public TileEntityEasyCrafting() {
        inventory = new ItemStack[40 + 18]; // 40 = 5*8 crafting slots, 18 = 2*9 inventory slots
    }

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return inventory[slotIndex];
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack stack) {
        inventory[slotIndex] = stack;
        markDirty();
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        return InventoryUtils.decreaseStackSize(this, slotIndex, amount);
    }

    @Override
	public ItemStack removeStackFromSlot(int index) {
        return InventoryUtils.getStackInSlotOnClosing(this, index);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(getPos()) == this && player.getDistanceSq(getPos()) < 64;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        InventoryUtils.readStacksFromNBT(inventory, tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("Inventory", InventoryUtils.writeStacksToNBT(inventory));
        return tagCompound;
    }

    @Override
    public String getName() {
        return "container.easycrafting:table.easy_crafting";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemstack) {
        if (slotIndex >= 40) {
            return true;
        }
        return false;
    }

    @Override
    public Object getServerGuiElement(EntityPlayer player, TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityEasyCrafting) {
            return new ContainerEasyCrafting(player.inventory, ((TileEntityEasyCrafting) tileEntity));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(EntityPlayer player, TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityEasyCrafting) {
            return new GuiEasyCrafting(player.inventory, ((TileEntityEasyCrafting) tileEntity));
        }
        return null;
    }

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
	}
}