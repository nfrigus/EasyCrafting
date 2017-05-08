package net.lepko.easycrafting.core.inventory.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;

public abstract class GuiTabbed extends GuiContainer implements IGuiTabbed {

    public TabGroup tabGroup = new TabGroup(this);
    private int currentTab = 0;

    public GuiTabbed(Container container) {
        super(container);
    }

    /* (non-Javadoc)
	 * @see net.lepko.easycrafting.core.inventory.gui.IGuiTabbed#initGui()
	 */
    @Override
    public void initGui() {
        super.initGui();
        initTabs();
        tabGroup.getTab(getCurrentTab()).onTabSelected();
    }

    /* (non-Javadoc)
	 * @see net.lepko.easycrafting.core.inventory.gui.IGuiTabbed#initTabs()
	 */
    @Override
	public abstract void initTabs();

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        tabGroup.drawBackground();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        tabGroup.drawForeground();

        Tab tab = tabGroup.getTabAt(mouseX, mouseY);
        if (tab != null) {
            drawCreativeTabHoveringText(tab.tooltip, mouseX - guiLeft, mouseY - guiTop);
        }

        RenderHelper.enableGUIStandardItemLighting();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (!tabGroup.mouseClick(mouseX, mouseY)) {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }

    // public accessors
    /* (non-Javadoc)
	 * @see net.lepko.easycrafting.core.inventory.gui.IGuiTabbed#guiLeft()
	 */
    @Override
	public int guiLeft() {
        return guiLeft;
    }

    /* (non-Javadoc)
	 * @see net.lepko.easycrafting.core.inventory.gui.IGuiTabbed#guiTop()
	 */
    @Override
	public int guiTop() {
        return guiTop;
    }

    /* (non-Javadoc)
	 * @see net.lepko.easycrafting.core.inventory.gui.IGuiTabbed#drawRectangle(int, int, int, int, int, int)
	 */
    @Override
	public void drawRectangle(int x, int y, int texLeft, int texTop, int width, int height) {
        drawTexturedModalRect(x, y, texLeft, texTop, width, height);
    }

    /* (non-Javadoc)
	 * @see net.lepko.easycrafting.core.inventory.gui.IGuiTabbed#itemRenderer()
	 */
    @Override
	public RenderItem itemRenderer() {
        return itemRender;
    }

    /* (non-Javadoc)
	 * @see net.lepko.easycrafting.core.inventory.gui.IGuiTabbed#mc()
	 */
    @Override
	public Minecraft mc() {
        return mc;
    }

	/* (non-Javadoc)
	 * @see net.lepko.easycrafting.core.inventory.gui.IGuiTabbed#getCurrentTab()
	 */
	@Override
	public int getCurrentTab() {
		return currentTab;
	}

	/* (non-Javadoc)
	 * @see net.lepko.easycrafting.core.inventory.gui.IGuiTabbed#setCurrentTab(int)
	 */
	@Override
	public void setCurrentTab(int currentTab) {
		this.currentTab = currentTab;
	}
}
