package me.planetguy.nomore3x3;

import net.lepko.easycrafting.core.GuiHandler;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class BlockNotAWorkbench extends BlockWorkbench {
	
	
    public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer p, int a, float b, float c, float d){
        if (w.isRemote) {
        	p.displayGUIWorkbench(x, y-1, z);
            return true;
        } else {
        	//Minecraft.getMinecraft().displayGuiScreen(new GuiLazyCrafting(p.inventory, w, x, y, z));
            return true;
        }
    }

}
