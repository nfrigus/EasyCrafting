package net.lepko.easycrafting.core.block;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.inventory.ContainerAutoCrafting;
import net.lepko.easycrafting.core.inventory.gui.GuiAutoCrafting;
import net.lepko.easycrafting.core.inventory.gui.IGuiTile;
import net.lepko.easycrafting.core.util.InventoryUtils;
import net.lepko.easycrafting.core.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.*;

public class TileEntityAutoCrafting extends TileEntity implements ITickable, ISidedInventory, IGuiTile {

    private static class FakeContainer extends Container {
        private FakeContainer() {
        }

        @Override
        public boolean canInteractWith(EntityPlayer player) {
            return true;
        }
    }

    private static class StackReference {
        public final IInventory inv;
        public final int slot;

        public StackReference(IInventory inv, int slot) {
            this.inv = inv;
            this.slot = slot;
        }

        public ItemStack getCopy(int size) {
            return StackUtils.copyStack(inv.getStackInSlot(slot), size);
        }

        public ItemStack decreaseStackSize(int amt) {
            return InventoryUtils.decreaseStackSize(inv, slot, amt);
        }
    }

    public static enum Mode {
        PULSE,
        ALWAYS,
        POWERED,
        UNPOWERED;

        public final String tooltip;

        Mode() {
            tooltip = String.format("mode.easycrafting:%s.tooltip", this.toString().toLowerCase(Locale.ENGLISH));
        }
    }

    private static final Mode[] VALID_MODES = Mode.values();
    private static final int UPDATE_INTERVAL = 5;

    private Mode mode = Mode.ALWAYS;
    private int lastUpdate = 0;

    private ItemStack[] inventory = new ItemStack[26];
    private boolean inputRestricted = false;
    List<Integer> inputsNeeded = new ArrayList<Integer>(9);

    public final int[] SLOTS = InventoryUtils.createSlotArray(10, inventory.length);

    private boolean poweredNow = false;
    private boolean poweredPrev = false;
    private boolean inventoryChanged = false;
    private int pendingRequests = 0;

    public boolean scheduledRecipeCheck = false;
    private InventoryCrafting craftingGrid = new InventoryCrafting(new FakeContainer(), 3, 3);
    private IRecipe currentRecipe = null;
    private boolean lastCraftingSuccess = true;

    public void setMode(int index) {
        if (index >= 0 && index < VALID_MODES.length) {
            mode = VALID_MODES[index];
        }
    }

    public Mode getMode() {
        return mode;
    }

    public void cycleModes(int mouseButton) {
        if (mouseButton == 0) {
            if (mode == null || mode.ordinal() + 1 >= VALID_MODES.length) {
                setMode(0);
            } else {
                setMode(mode.ordinal() + 1);
            }
        } else if (mouseButton == 1) {
            if (mode == null || mode.ordinal() - 1 < 0) {
                setMode(VALID_MODES.length - 1);
            } else {
                setMode(mode.ordinal() - 1);
            }
        }
    }

    private void checkForRecipe() {
        @SuppressWarnings("unchecked")
        List<IRecipe> recipeList = (List<IRecipe>) CraftingManager.getInstance().getRecipeList();

        InventoryUtils.setContents(craftingGrid, this);

        for (IRecipe recipe : recipeList) {
            if (recipe.matches(craftingGrid, worldObj)) {
                currentRecipe = recipe;
                setInventorySlotContents(9, currentRecipe.getCraftingResult(craftingGrid));
                return;
            }
        }

        currentRecipe = null;
        setInventorySlotContents(9, null);
    }

    private boolean isReplaceableInCraftingGridSlot(int slot, ItemStack stack) {
        craftingGrid.setInventorySlotContents(slot, stack);
        boolean result = currentRecipe.matches(craftingGrid, worldObj) && StackUtils.areIdentical(currentRecipe.getCraftingResult(craftingGrid), getStackInSlot(9));
        craftingGrid.setInventorySlotContents(slot, getStackInSlot(slot));
        return result;
    }

    /**
     * Combines the test for whether we can craft (pulled out of tryCrafting) with code to prepare the data for input
     * restriction checking, ie, do we need to restrict any inputs to ensure we maintain room for all ingredients?
     *
     * @return refs if we will be ready to craft something *now*, otherwise null
     */
    private StackReference[] verifyCraftability() {
        if (currentRecipe == null || getStackInSlot(9) == null) {
            inputRestricted = false; // no input restrictions if there's no recipe to defend!
            return null;
        }

        boolean[] found = new boolean[9];
        StackReference[] refs = new StackReference[9];

        for (int o = 0; o < 9; o++) {
            found[o] = craftingGrid.getStackInSlot(o) == null;
        }

        invLoop:
        for (int invSlot = 10; invSlot < 18; invSlot++) {
            ItemStack stack = ItemStack.copyItemStack(getStackInSlot(invSlot));
            if (stack != null && stack.stackSize > 0) {
                for (int gridSlot = 0; gridSlot < 9; gridSlot++) {
                    if (!found[gridSlot]) {
                        if (isReplaceableInCraftingGridSlot(gridSlot, stack)) {
                            refs[gridSlot] = new StackReference(this, invSlot);
                            found[gridSlot] = true;
                            if (--stack.stackSize <= 0) {
                                continue invLoop;
                            }
                        }
                    }
                }
            }
        }

        inputsNeeded.clear();
        List<ItemStack> inputsCounted = new ArrayList<ItemStack>(9);
        boolean canCraft = true;

        for (int gridSlot = 0; gridSlot < 9; gridSlot++) {
            ItemStack test = ItemStack.copyItemStack(getStackInSlot(gridSlot));
            if (test != null) {
                test.stackSize = 1;

                canCraft = canCraft && found[gridSlot];

                alreadyReserved: { // make sure it's a *unique* ingredient
                    for (ItemStack x : inputsCounted) {
                        if (ItemStack.areItemStacksEqual(x, test))
                            break alreadyReserved;
                    }

                    if (! found[gridSlot]) {
                        inputsNeeded.add(gridSlot);
                    }

                    inputsCounted.add(test);
                }
            }
        }

        if (canCraft) {
            inputRestricted = false; // no restrictions if we already have all needed inputs
            return refs;
        } else {
            // we can't craft, so we'll go ahead and check what's needed and how many empty slots we have for inputs
            int inputSlotsEmpty = 0;

            for (int invSlot = 10; invSlot < 18; invSlot++) {
                ItemStack stack = ItemStack.copyItemStack(getStackInSlot(invSlot));
                if (stack == null || stack.stackSize <= 0)
                    inputSlotsEmpty ++;
            }

            inputRestricted = (inputSlotsEmpty <= inputsNeeded.size());
            return null;
        }
    }

    private boolean tryCrafting() {
        StackReference[] refs = verifyCraftability();
        if (refs == null) return false;

        // replace all ingredients with found stacks
        for (int i = 0; i < 9; i++) {
            if (craftingGrid.getStackInSlot(i) != null) {
                ItemStack is = refs[i].getCopy(1);
                craftingGrid.setInventorySlotContents(i, is);
            }
        }

        boolean craftingCompleted = false;

        // test the recipe to make sure all replacements play nice with each other
        ItemStack result = currentRecipe.getCraftingResult(craftingGrid);
        if (currentRecipe.matches(craftingGrid, worldObj) && StackUtils.areIdentical(result, getStackInSlot(9))) {
            if (InventoryUtils.addItemToInventory(this, result, 18, 26)) {
                FakePlayer fakePlayer = FakePlayerFactory.get((WorldServer) worldObj, Ref.GAME_PROFILE);
                FMLCommonHandler.instance().firePlayerCraftingEvent(fakePlayer, result, craftingGrid);
                result.onCrafting(worldObj, fakePlayer, result.stackSize);

                for (int i=0; i<9; i++) {
                    StackReference ref = refs[i];                             if (ref       == null) continue;
                    ItemStack stack = ref.decreaseStackSize(1);               if (stack     == null) continue;
                    Item stackItem = stack.getItem();                         if (stackItem == null) continue;
                    ItemStack container = stackItem.getContainerItem(stack);  if (container == null) continue;
                    if (container.isItemStackDamageable() && container.getItemDamage() > container.getMaxDamage())
                        continue;

                    // attempt to let the ingredient remain in its slot?
                    //TODO port properly?
                    /*
                    if (! stackItem.doesContainerItemLeaveCraftingGrid(stack)) {
                        ItemStack actualStack = getStackInSlot(ref.slot);

                        if (actualStack == null || actualStack.stackSize == 0) { // slot is now empty, so just put container stack there
                            setInventorySlotContents(ref.slot, container);
                            container = null;
                        } else {
                            // if we can't put it back in the same slot, try to shove it somewhere in inputs
                            if (container != null && InventoryUtils.addItemToInventory(this, container.copy(), 10, 18))
                                container = null;
                        }
                    }
                    */

                    if (container != null) {
                        if (!InventoryUtils.addItemToInventory(this, container.copy(), 18, 26)) {
                            InventoryUtils.dropItem(worldObj, getPos().getX() + 0.5, getPos().getY() + 1, getPos().getZ() + 0.5, container);
                            // XXX: try other inventories?
                        }
                    }
                }

                craftingCompleted = true;
            }
        }

        // restore original items from ghost slots
        InventoryUtils.setContents(craftingGrid, this);
        verifyCraftability(); // crafting will have changed contents, so re-run verify to ensure correct input blocking
        return craftingCompleted;
    }

    /* TileEntity */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        InventoryUtils.readStacksFromNBT(inventory, tag.getTagList("Inventory", Constants.NBT.TAG_COMPOUND));
        setMode(tag.getByte("Mode"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Inventory", InventoryUtils.writeStacksToNBT(inventory));
        tag.setByte("Mode", (byte) mode.ordinal());
        return tag;
    }

    @Override
    public void validate() {
        super.validate();
        checkForRecipe();
    }

    @Override
    public void update() {
        if (scheduledRecipeCheck) {
            scheduledRecipeCheck = false;
            checkForRecipe();
        }

        poweredPrev = poweredNow;
        poweredNow = worldObj.isBlockIndirectlyGettingPowered(getPos()) != 0;

        if (!poweredPrev && poweredNow) {
            pendingRequests++;
        }

        if (!worldObj.isRemote && ++lastUpdate > UPDATE_INTERVAL) {
            lastUpdate = 0;

            if (lastCraftingSuccess || inventoryChanged) {
                if (inventoryChanged) verifyCraftability();
                inventoryChanged = false;

                if (mode == Mode.ALWAYS || (mode == Mode.POWERED && poweredNow) || (mode == Mode.UNPOWERED && !poweredNow)) {
                    lastCraftingSuccess = tryCrafting();
                } else if (mode == Mode.PULSE && pendingRequests > 0) {
                    if (lastCraftingSuccess = tryCrafting()) {
                        pendingRequests--;
                    }
                }
            }
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        inventoryChanged = true;
    }

    /* IInventory */

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return inventory[slotIndex];
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
    public void setInventorySlotContents(int slotIndex, ItemStack stack) {
        inventoryChanged = true;
        inventory[slotIndex] = stack;
    }

    @Override
    public String getName() {
        return "container.easycrafting:table.auto_crafting";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(getPos()) == this && getPos().distanceSq(player.getPosition()) < 64;
    }

    @Override
	public void openInventory(EntityPlayer player) {
    }

    @Override
	public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack stack) {
        return slotIndex >= 10 && slotIndex < 18;
    }

    /* ISidedInventory */

    @Override
	public int[] getSlotsForFace(EnumFacing side) {
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int slotIndex, ItemStack stack, EnumFacing side) {
        if (inventoryChanged) verifyCraftability();

        if (slotIndex < 10 || slotIndex >= 18) return false;  // not valid insert slot
        if (! inputRestricted)                 return true;   // plenty of space
        if (getStackInSlot(slotIndex) != null) return true;   // slot not empty, allow normal item stacking behavior

        for (int testSlot : inputsNeeded)
            if (isReplaceableInCraftingGridSlot(testSlot, stack))
                return true;

        return false;
    }

    @Override
    public boolean canExtractItem(int slotIndex, ItemStack stack, EnumFacing side) {
        return slotIndex >= 18 && slotIndex < inventory.length;
    }

    @Override
    public Object getServerGuiElement(EntityPlayer player, TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityAutoCrafting) {
            return new ContainerAutoCrafting(player.inventory, ((TileEntityAutoCrafting) tileEntity));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(EntityPlayer player, TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityAutoCrafting) {
            return new GuiAutoCrafting(player.inventory, ((TileEntityAutoCrafting) tileEntity));
        }
        return null;
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
