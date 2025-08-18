package net.crypticverse.serverhelper.neoforge;

import net.crypticverse.serverhelper.ServerHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;

@Mod(ServerHelper.MOD_ID)
@EventBusSubscriber
public class ServerHelperNeoForge {
    public ServerHelperNeoForge(IEventBus eventBus) {
        eventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ServerHelper.configFolder =  FMLPaths.CONFIGDIR.get();
            ServerHelper.init();
        });
    }
}
