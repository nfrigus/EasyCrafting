package net.lepko.easycrafting.core.proxy;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.lepko.easycrafting.EasyCrafting;
import net.lepko.easycrafting.core.ConnectionHandler;
import net.lepko.easycrafting.core.GuiHandler;
import net.minecraft.item.Item;

public class Proxy {

    public void registerHandlers() {
        // Event Handlers
        FMLCommonHandler.instance().bus().register(ConnectionHandler.INSTANCE);

        // Gui Handlers
        NetworkRegistry.INSTANCE.registerGuiHandler(EasyCrafting.INSTANCE, GuiHandler.INSTANCE);
    }

    public void registerCommands() {
    }

	public void registerItemRenderer(Item item, int meta, String id) {
	}
}
