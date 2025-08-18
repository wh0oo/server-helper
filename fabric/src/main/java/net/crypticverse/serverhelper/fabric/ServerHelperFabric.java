package net.crypticverse.serverhelper.fabric;

import net.crypticverse.serverhelper.ServerHelper;
import net.fabricmc.api.ModInitializer;

public class ServerHelperFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerHelper.init();
    }
}
