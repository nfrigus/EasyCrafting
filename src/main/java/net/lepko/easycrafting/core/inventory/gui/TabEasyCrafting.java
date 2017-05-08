package net.lepko.easycrafting.core.inventory.gui;

import me.planetguy.nomore3x3.GuiLazyCrafting;
import net.minecraft.item.ItemStack;

public class TabEasyCrafting extends Tab {
	/**
	 * 
	 */
	private final IGuiTabbed guiEasyCrafting;

	public TabEasyCrafting(IGuiTabbed guiLazyCrafting, ItemStack iconStack, String tooltip) {
		super(iconStack, tooltip);
		this.guiEasyCrafting = guiLazyCrafting;
	}

	@Override
	public void onTabSelected() {
		this.guiEasyCrafting.updateSearch(true);
	}
}