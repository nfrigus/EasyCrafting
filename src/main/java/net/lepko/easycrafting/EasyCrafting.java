package net.lepko.easycrafting;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.lepko.easycrafting.core.block.ModBlocks;
import net.lepko.easycrafting.core.config.ConfigHandler;
import net.lepko.easycrafting.core.network.PacketHandler;
import net.lepko.easycrafting.core.recipe.RecipeManager;
import net.lepko.easycrafting.core.util.ItemMap;

@Mod(modid = Ref.MOD_ID, useMetadata = true)
public class EasyCrafting {

	//TODO: block.shouldCheckWeakPower()
		//block.onNeighbourTileChange()
		//RIGHT CLICK SEARCH BAR DELETES ALL TEXT
		//CREDIT ElementalRobot50 for Auto Crafting Table textures


    @Instance(Ref.MOD_ID)
    public static EasyCrafting INSTANCE;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Ref.init();
        ConfigHandler.initialize(event.getSuggestedConfigurationFile());

        ModBlocks.setupBlocks();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        Ref.PROXY.registerHandlers();
        Ref.PROXY.registerCommands();

        PacketHandler.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ModBlocks.setupRecipes();
        //XXX: VersionHelper.performCheck();
    }

    @EventHandler
    public void available(FMLLoadCompleteEvent event) {
        ItemMap.build();

        // This fires after the recipes are sorted by forge; Mods should not add/remove recipes after this point!!
        RecipeManager.scanRecipes();
    }
}
