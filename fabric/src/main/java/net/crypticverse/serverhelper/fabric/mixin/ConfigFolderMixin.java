package net.crypticverse.serverhelper.fabric.mixin;

import net.crypticverse.serverhelper.config.ConfigFolderHelper;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.file.Path;

@Mixin(value = ConfigFolderHelper.class, remap = false)
public class ConfigFolderMixin {
    @Overwrite
    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve("serverhelper");
    }
}
