package net.lepko.easycrafting.core;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public enum ConnectionHandler {
    INSTANCE;

    @SubscribeEvent
    public void serverPlayerJoined(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
    }

    @SubscribeEvent
    public void clientDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
    }
}
