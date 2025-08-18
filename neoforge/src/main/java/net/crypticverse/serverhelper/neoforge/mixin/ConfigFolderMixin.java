package net.crypticverse.serverhelper.neoforge.mixin;

import net.crypticverse.serverhelper.config.ConfigFolderHelper;
import net.neoforged.fml.loading.FMLPaths;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.file.Path;

@Mixin(value = ConfigFolderHelper.class, remap = false)
public class ConfigFolderMixin {
    @Overwrite
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get().resolve("serverhelper");
    }
}
