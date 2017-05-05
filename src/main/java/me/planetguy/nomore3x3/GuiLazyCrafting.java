package me.planetguy.nomore3x3;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class GuiLazyCrafting extends GuiCrafting {

    public GuiLazyCrafting(InventoryPlayer p_i1084_1_, World p_i1084_2_, int p_i1084_3_, int p_i1084_4_,
			int p_i1084_5_) {
		super(p_i1084_1_, p_i1084_2_, p_i1084_3_, p_i1084_4_, p_i1084_5_);
		// TODO Auto-generated constructor stub
	}

	/**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_)
    {
        this.fontRendererObj.drawString(I18n.format("Hi", new Object[0]), 28, 6, 4210752);
        this.fontRendererObj.drawString(I18n.format("Testing", new Object[0]), 8, this.ySize - 96 + 2, 4210752);
    }

}
