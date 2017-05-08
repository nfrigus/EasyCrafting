package net.lepko.easycrafting.core.inventory.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;

public interface IGuiTabbed {

	void initGui();

	void initTabs();

	// public accessors
	int guiLeft();

	int guiTop();

	void drawRectangle(int x, int y, int texLeft, int texTop, int width, int height);

	RenderItem itemRenderer();

	Minecraft mc();

	int getCurrentTab();

	void setCurrentTab(int currentTab);

	void updateSearch(boolean b);

	void refreshCraftingOutput();

}