package net.crypticverse.serverhelper;

import net.crypticverse.serverhelper.config.ConfigFolderHelper;
import net.crypticverse.serverhelper.config.dimension.DimensionConfig;
import net.crypticverse.serverhelper.config.filter.FilterConfig;
import net.crypticverse.serverhelper.config.ranks.RanksConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerHelper {
    public static final String MOD_ID = "serverhelper";
    public static Path configFolder = ConfigFolderHelper.getConfigDir();
    public static final Path filterConfigFile = configFolder.resolve("chatfilter.json");
    public static final Path ranksConfigFile = configFolder.resolve("ranks.json");
    public static final Path dimensionConfig = configFolder.resolve("dimensions.json");
    public static final Logger LOGGER = LoggerFactory.getLogger(ServerHelper.class);

    public static void init() {
        if (!Files.exists(configFolder)) {
            try {
                Files.createDirectories(configFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FilterConfig.loadConfig();
        DimensionConfig.loadConfig();
        RanksConfig.loadConfig();
        Runtime.getRuntime().addShutdownHook(new Thread(FilterConfig::saveConfig));
        Runtime.getRuntime().addShutdownHook(new Thread(DimensionConfig::saveConfig));
        Runtime.getRuntime().addShutdownHook(new Thread(RanksConfig::save));
    }
}
