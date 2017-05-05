package me.planetguy.nomore3x3;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;

@Mod(modid = "nomore3x3")
public class ModNM3x3 {
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent pie){
		Block naw = new BlockNotAWorkbench();
		GameRegistry.registerBlock(naw, ItemBlock.class, "notAWorkbench");
		naw.setCreativeTab(CreativeTabs.tabRedstone);
	}

}
