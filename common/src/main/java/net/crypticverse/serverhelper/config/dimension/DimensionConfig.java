package net.crypticverse.serverhelper.config.dimension;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.crypticverse.serverhelper.ServerHelper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;

public class DimensionConfig {
    public static boolean netherDisabled = false;
    public static boolean theEndDisabled = false;

    public static boolean isNetherDisabled() {
        return netherDisabled;
    }

    public static boolean isTheEndDisabled() {
        return theEndDisabled;
    }

    public static void loadConfig() {
        File file = ServerHelper.dimensionConfig.toFile();
        if (!file.exists()) {
            saveConfig();
            return;
        }
        try (FileReader fileReader = new FileReader(file)) {
            Gson gson = new GsonBuilder().create();
            Type configType = new TypeToken<DimensionConfigTemplate>() {}.getType();
            DimensionConfigTemplate config = gson.fromJson(fileReader, configType);

            netherDisabled = config.netherDisabled;
            theEndDisabled = config.theEndDisabled;
        } catch (IOException e) {
            ServerHelper.LOGGER.error("Error reading {}", file, e);
        }
    }

    public static void saveConfig() {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("netherDisabled", netherDisabled);
            jsonObject.addProperty("theEndDisabled", theEndDisabled);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(jsonObject);
            Files.write(ServerHelper.dimensionConfig, json.getBytes());
        } catch (IOException e) {
            ServerHelper.LOGGER.error("Error saving {}", ServerHelper.dimensionConfig, e);
        }
    }
}
