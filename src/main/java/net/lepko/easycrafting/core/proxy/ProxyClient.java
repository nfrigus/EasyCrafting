package net.lepko.easycrafting.core.proxy;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.CommandEasyCrafting;
import net.lepko.easycrafting.core.recipe.RecipeChecker;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.model.ModelLoader;

public class ProxyClient extends Proxy {

    @Override
    public void registerHandlers() {
        super.registerHandlers();

        FMLCommonHandler.instance().bus().register(RecipeChecker.INSTANCE);
    }

    @Override
    public void registerCommands() {
        super.registerCommands();

        ClientCommandHandler.instance.registerCommand(new CommandEasyCrafting());
    }

    @Override
    public void registerItemRenderer(Item item, int meta, String id) {
    	ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(Ref.MOD_ID + ":" + id, "inventory"));
    }
}
