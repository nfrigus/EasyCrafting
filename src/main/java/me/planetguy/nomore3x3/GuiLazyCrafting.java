package me.planetguy.nomore3x3;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.ImmutableList;

import me.planetguy.lib.util.Debug;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.block.ModBlocks;
import net.lepko.easycrafting.core.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.core.config.ConfigHandler;
import net.lepko.easycrafting.core.inventory.gui.IGuiTabbed;
import net.lepko.easycrafting.core.inventory.gui.Tab;
import net.lepko.easycrafting.core.inventory.gui.TabEasyCrafting;
import net.lepko.easycrafting.core.inventory.gui.TabGroup;
import net.lepko.easycrafting.core.inventory.slot.SlotEasyCraftingOutput;
import net.lepko.easycrafting.core.network.PacketHandler;
import net.lepko.easycrafting.core.network.message.MessageEasyCrafting;
import net.lepko.easycrafting.core.recipe.RecipeChecker;
import net.lepko.easycrafting.core.recipe.RecipeHelper;
import net.lepko.easycrafting.core.recipe.RecipeManager;
import net.lepko.easycrafting.core.recipe.WrappedRecipe;
import net.lepko.easycrafting.core.util.InventoryUtils;
import net.lepko.easycrafting.core.util.StackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class GuiLazyCrafting extends GuiContainer implements IGuiTabbed {

    public final boolean leftSide=true;
    
	public List<WrappedRecipe> shownRecipes=new ArrayList<WrappedRecipe>();
	public List<WrappedRecipe> craftableRecipes = ImmutableList.of();
	
	public int currentRowOffset = 0;
	public int maxRowOffset = 0;
	public float currentScrollValue = 0;
	public boolean wasClicking = false;
	public boolean isDraggingScrollBar = false;
	public boolean[] canCraftCache;
	
	public TileEntityEasyCrafting tileInventory=new TileEntityEasyCrafting();

	public GuiTextField searchField;
	
	public int currentTab=0;
	public TabGroup tabGroup=new TabGroup(this);
	
	//Static because Java is dumb
	public static ContainerWorkbench containerCache;
	public ContainerWorkbench container;

	public static boolean WORKER_LOCKED = false;
	
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Ref.RES_DOMAIN, "textures/gui/easycrafting_search.png");
	public static final ResourceLocation CRAFT_TEXTURE = new ResourceLocation(Ref.RES_DOMAIN, "textures/gui/easycrafting_craft.png");
    public static final ResourceLocation craftingTableGui = new ResourceLocation("textures/gui/container/crafting_table.png");
    
    public int firstCraftingSlotIndex=-1; 

	
    public GuiLazyCrafting(InventoryPlayer p_i1084_1_, World p_i1084_2_, int p_i1084_3_, int p_i1084_4_,
			int p_i1084_5_) {
        super(createAndSaveCW(p_i1084_1_, p_i1084_2_, p_i1084_3_, p_i1084_4_, p_i1084_5_));
        container=containerCache;
    	craftableRecipes = ImmutableList.of();
    	shownRecipes=new ArrayList<WrappedRecipe>();
		ySize = 235;
		Class c=GuiCrafting.class;
		
		//insert slots
        for (int g = 0; g < 5; ++g) {
            for (int h = 0; h < 8; ++h) {
            	Slot slot=new SlotEasyCraftingOutput(tileInventory, g*8+h, 8 + h * 18, 18 + g * 18);
                slot.slotNumber = container.inventorySlots.size();
                if(firstCraftingSlotIndex == -1) {
                	firstCraftingSlotIndex=slot.slotNumber;
                }
                container.inventorySlots.add(slot);
                container.inventoryItemStacks.add((Object)null);
            }
    	}
        
		initializeSlotPositions();
		hideCraftingSlots();
		
        RecipeChecker.INSTANCE.setRequested(true);
		updateSearch(true);
	}
    
    private static ContainerWorkbench createAndSaveCW(InventoryPlayer p_i1084_1_, World p_i1084_2_, int p_i1084_3_, int p_i1084_4_,
			int p_i1084_5_){
    	containerCache = new ContainerWorkbench(p_i1084_1_, p_i1084_2_, p_i1084_3_, p_i1084_4_, p_i1084_5_);
    	return containerCache;
    }
    
    private void initializeSlotPositions(){
    	for(int i=10; i<container.inventorySlots.size(); i++){
    		Slot slot=(Slot) container.inventorySlots.get(i);
    		slot.yDisplayPosition += 18*4-3;
    	}
    }
    
    private void hideCraftingSlots(){
    	//Hide crafting table slots
    	for(int i=0; i<10; i++){
    		Slot slot=(Slot) container.inventorySlots.get(i);
    		slot.yDisplayPosition = 9001;
    	}
    	//Show easy crafting slots 
    	for(int i=0; i<40; i++){
    		Slot slot=(Slot) container.inventorySlots.get(i+firstCraftingSlotIndex);
    		slot.yDisplayPosition = 18 + (i/8) * 18;
    	}
    }
    
    private void showCraftingSlots(){
    	//Show crafting table slots
    	Slot output = (Slot) container.inventorySlots.get(0);
    	output.xDisplayPosition=124;
    	output.yDisplayPosition=35;

        for (int l = 0; l < 3; ++l)
        {
            for (int i1 = 0; i1 < 3; ++i1)
            {
            	//The +1 is to not get the output slot
            	Slot craftSlot = (Slot) container.inventorySlots.get(l*3+i1+1);
            	craftSlot.xDisplayPosition = 30 + i1 * 18;
            	craftSlot.yDisplayPosition = 17 + l * 18;
            }
        }
        
    	//Hide easy crafting slots 
    	for(int i=0; i<40; i++){
    		Slot slot=(Slot) container.inventorySlots.get(i+firstCraftingSlotIndex);
    		slot.yDisplayPosition = 9001;
    	}
    }

    @Override
    public void initGui() {
        super.initGui();
        
        tabGroup=new TabGroup(this);
        
		tabGroup.addTab(new TabEasyCrafting(this, new ItemStack(Blocks.chest), "Craftable With Inventory"));
		tabGroup.addTab(new TabEasyCrafting(this, new ItemStack(Items.compass), "Search All Items"));
		tabGroup.addTab(new Tab(new ItemStack(Blocks.crafting_table), "3x3 Crafting Grid"));
        tabGroup.getTab(currentTab).onTabSelected();
        
		Keyboard.enableRepeatEvents(true);
		searchField = new GuiTextField(fontRendererObj, guiLeft + 82, guiTop + 6, 89, fontRendererObj.FONT_HEIGHT);
		searchField.setMaxStringLength(32);
		searchField.setEnableBackgroundDrawing(false);
		searchField.setVisible(true);
		searchField.setTextColor(0xFFFFFF);
		searchField.setCanLoseFocus(false);
		searchField.setFocused(true);
		searchField.setText("");
		
		refreshCraftingOutput();
    }
    
	@Override
	public void keyTyped(char par1, int par2) {
		if (!checkHotbarKeys(par2)) {
			if (searchField.textboxKeyTyped(par1, par2)) {
				updateSearch(true);
			} else {
				super.keyTyped(par1, par2);
			}
		}
	}
    
	@Override
	public void handleMouseInput() {
		int mouseScroll = Mouse.getEventDWheel();
		if (mouseScroll == 0) { // Bypass NEI fast transfer manager
			super.handleMouseInput();
		} else {
			setRowOffset(currentRowOffset + (mouseScroll > 0 ? -1 : 1));
		}
	}
	
	

	@Override
	public void drawScreen(int mouseX, int mouseY, float time) {
		// XXX: Check lock on worker thread
		WORKER_LOCKED  = !RecipeChecker.INSTANCE.done;

		// Handle scrollbar dragging
		boolean leftMouseDown = Mouse.isButtonDown(0);
		int left = guiLeft + 155;
		int top = guiTop + 18;
		int right = left + 14;
		int bottom = top + 89;

		if (!wasClicking && leftMouseDown && mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom) {
			isDraggingScrollBar = maxRowOffset > 0;
		} else if (!leftMouseDown) {
			isDraggingScrollBar = false;
		}

		wasClicking = leftMouseDown;

		if (isDraggingScrollBar) {
			setScrollValue((mouseY - top - 7.5F) / (bottom - top - 15.0F));
		}

		super.drawScreen(mouseX, mouseY, time);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1, 1, 1, 1);
		// Background
		if(currentTab == 2){
			mc.renderEngine.bindTexture(CRAFT_TEXTURE);
		}else{
			mc.renderEngine.bindTexture(GUI_TEXTURE);
		}
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		GL11.glColor4f(1, 1, 1, 1);

		// Tabs
        tabGroup.drawBackground();

        if(currentTab == 0 || currentTab == 1) {

        	// Scrollbar
        	int scrollTextureX = maxRowOffset == 0 ? 12 : 0;
        	drawTexturedModalRect(guiLeft + 156, guiTop + 17 + (int) (currentScrollValue * 73.0F), scrollTextureX, 240, 12, 16);

        	// Search
        	int searchTextureX = xSize - 90 - 7;
        	drawTexturedModalRect(guiLeft + searchTextureX, guiTop + 4, searchTextureX, 256 - 12, 90, 12);
        	searchField.drawTextBox();

        	// Output slot backgrounds
        	if (canCraftCache != null && getCurrentTab() != 0) {
        		int offset = currentRowOffset * 8;
        		for (int k = 0; k < 40 && k + offset < canCraftCache.length; k++) {
        			drawSlotBackground(inventorySlots.getSlot(k+firstCraftingSlotIndex), canCraftCache[k + offset]);
        		}
        	}

        }
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String title = "Easy Crafting";
		if (WORKER_LOCKED) {
			title = "Searching...";
		}
		fontRendererObj.drawString(title, 7, 6, 0x404040);

        tabGroup.drawForeground();

        Tab tab = tabGroup.getTabAt(mouseX, mouseY);
        if (tab != null) {
            drawCreativeTabHoveringText(tab.tooltip, mouseX - guiLeft, mouseY - guiTop);
        }

        RenderHelper.enableGUIStandardItemLighting();
	}

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
    	//TODO Lazy crafting table needs to intercept this here
        if (!tabGroup.mouseClick(mouseX, mouseY)) {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }
    
    public void drawRectangle(int x, int y, int texLeft, int texTop, int width, int height) {
        drawTexturedModalRect(x, y, texLeft, texTop, width, height);
    }
    
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}
	
	public void setScrollValue(float scrollValue) {
		currentScrollValue = MathHelper.clamp_float(scrollValue, 0F, 1F);
		currentRowOffset = MathHelper.clamp_int((int) (currentScrollValue * maxRowOffset + 0.5F), 0, maxRowOffset);
		setSlots();
	}
	
	private void setSlots() {
		if (shownRecipes != null) {
			int offset = currentRowOffset * 8;
			for (int i = 0; i < 40; i++) {
				if (i + offset >= shownRecipes.size() || i + offset < 0) {
					tileInventory.setInventorySlotContents(i, null);
				} else {
					WrappedRecipe recipe = shownRecipes.get(i + offset);
					ItemStack is = recipe.handler.getCraftingResult(recipe, recipe.usedIngredients);
					tileInventory.setInventorySlotContents(i, is);
				}
			}
		}
	}
	
	// button:
	// 0 -> left mouse
	// 1 -> action=0 or 1 -> right mouse
	// _____action=5 -> left click dragged over
	// 2 -> middle mouse
	// 5 -> action=5 -> right click dragged over
	//
	// action:
	// 0 -> click
	// 1 -> shift click
	// 2 -> swap with slot in hotbar (button is 0-8)
	// 3 -> pick block
	// 4 -> drop block
	// 5 -> dragged stack
	// 6 -> double click
	@Override
	public void handleMouseClick(Slot slot, int slotIndex, int button, int action) {
		if (slotIndex >= firstCraftingSlotIndex && slotIndex < firstCraftingSlotIndex+40) {
			Debug.mark();
			onCraftingSlotClick(slot, slotIndex, button, action);
		} else {
			Debug.mark();
	        RecipeChecker.INSTANCE.setRequested(true);
			super.handleMouseClick(slot, slotIndex, button, action);
		}
	}

	public void onCraftingSlotClick(Slot slot, int slotIndex, int button, int action) {
		slotIndex -= this.firstCraftingSlotIndex;
		
		Debug.dbg("Clicked: " + slot.getClass().getSimpleName() + "@" + slotIndex + ", button=" + button + ", action=" + action + ", stack=" + slot.getStack());

		if (action > 1 || button > 1 || !slot.getHasStack()) {
			return;
		}
		
		Debug.mark();
		
		ItemStack heldStack = mc.thePlayer.inventory.getItemStack();
		ItemStack slotStack = slot.getStack();

		WrappedRecipe recipe = null;
		int recipeIndex = slotIndex + currentRowOffset * 8;
		if (recipeIndex >= 0 && shownRecipes != null && recipeIndex < shownRecipes.size()) {
			WrappedRecipe r = shownRecipes.get(recipeIndex);
			if (StackUtils.areEqualNoSizeNoNBT(r.getOutput(), slotStack) && craftableRecipes != null && craftableRecipes.contains(r)) {
				recipe = r;
			}
		}
		
		if (recipe == null) {
			return;
		}
		
		Debug.mark();

		// slotStack already has a stack from recipe.handler.getCraftingResult()
		ItemStack finalStack = slotStack.copy();
		int finalStackSize = 0;

		if (heldStack == null) {
			finalStackSize = finalStack.stackSize;
		} else if (StackUtils.canStack(slotStack, heldStack) == 0) {
			finalStackSize = finalStack.stackSize + heldStack.stackSize;
		}

		if (finalStackSize > 0) {
			boolean isRightClick = button == 1;
			boolean isShiftClick = action == 1;

			Debug.dbg("Crafting recipe!");
			GLCSupport.craftThing(recipe, isRightClick, isShiftClick);

			if (isRightClick) { // Right click; craft until max stack
				int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(slotStack, heldStack);
				int timesCrafted = RecipeHelper.canCraft(recipe, mc.thePlayer.inventory, false, maxTimes, ConfigHandler.MAX_RECURSION);
				if (timesCrafted > 0) {
					finalStack.stackSize = finalStackSize + (timesCrafted - 1) * finalStack.stackSize;
					mc.thePlayer.inventory.setItemStack(finalStack);
				}
			} 
			else if (isShiftClick) {
				int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(slotStack, null);
				int timesCrafted = RecipeHelper.canCraftWithComponents(recipe, mc.thePlayer.inventory, false, maxTimes, ConfigHandler.MAX_RECURSION);
				if (timesCrafted > 0) {	
					finalStack.stackSize *= timesCrafted; //ignore finalStackSize; it might contain heldStack size
					RecipeHelper.canCraftWithComponents(recipe, mc.thePlayer.inventory, true, timesCrafted, ConfigHandler.MAX_RECURSION);
					InventoryUtils.addItemToInventory(mc.thePlayer.inventory, finalStack);
				}
			}
			else { // Left click; craft once
				finalStack.stackSize = finalStackSize;
				mc.thePlayer.inventory.setItemStack(finalStack);
			}
		}
	}
	

	public void drawSlotBackground(Slot slot, boolean canCraft) {
		int x = guiLeft + slot.xDisplayPosition;
		int y = guiTop + slot.yDisplayPosition;
		int color = canCraft ? 0x8000A000 : 0x80A00000;
		Gui.drawRect(x, y, x + 16, y + 16, color);
	}

	@SuppressWarnings("unchecked")
	public void updateSearch(boolean scrollToTop) {
		List<WrappedRecipe> all = currentTab == 0 ? craftableRecipes : RecipeManager.getAllRecipes();
		List<WrappedRecipe> list = new ArrayList<WrappedRecipe>();
		if (all == null || searchField == null) {
			return;
		}

		String LAST_SEARCH = searchField.getText().toLowerCase();
		if (!LAST_SEARCH.trim().isEmpty()) {
			for (WrappedRecipe recipe : all) {
				try {
					List<String> tips = recipe.getOutput().getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
					for (String tip : tips) {
						if (tip.toLowerCase().contains(LAST_SEARCH)) {
							list.add(recipe);
							break;
						}
					}
				}
				catch(NullPointerException ex)
				{
					Ref.LOGGER.info("Exception on Update Search. Continuing with search" + recipe.getOutput().toString());
				}
			}
			shownRecipes = list;
		} else {
			shownRecipes = all;
		}

		maxRowOffset = (int) (Math.ceil(shownRecipes.size() / 8.0D) - 5);
		maxRowOffset = maxRowOffset < 0 ? 0 : maxRowOffset;

		rebuildCanCraftCache();
		setRowOffset(scrollToTop ? 0 : currentRowOffset);

	}


	public void refreshCraftingOutput() {
		craftableRecipes = ImmutableList.copyOf(RecipeChecker.INSTANCE.recipes);
		updateSearch(false);
	}

	public void rebuildCanCraftCache() {
		canCraftCache = new boolean[shownRecipes.size()];
		for (int i = 0; i < shownRecipes.size(); i++) {
			canCraftCache[i] = craftableRecipes.contains(shownRecipes.get(i));
		}
	}

	public void setRowOffset(int rowOffset) {
		currentRowOffset = MathHelper.clamp_int(rowOffset, 0, maxRowOffset);
		currentScrollValue = MathHelper.clamp_float(currentRowOffset / (float) maxRowOffset, 0F, 1F);
		setSlots();
	}

	@Override
	public void initTabs() {
		//Do nothing here
	}

	@Override
	public int guiLeft() {
		return guiLeft;
	}

	@Override
	public int guiTop() {
		return guiTop;
	}

	@Override
	public RenderItem itemRenderer() {
		return itemRender;
	}

	@Override
	public Minecraft mc() {
		return mc;
	}

	@Override
	public int getCurrentTab() {
		return currentTab;
	}

	@Override
	public void setCurrentTab(int newTab) {
		if(newTab == 2 && currentTab != 2)
			showCraftingSlots();
		else if(newTab != 2 && currentTab == 2)
			hideCraftingSlots();
		this.currentTab = newTab;
	}

}
