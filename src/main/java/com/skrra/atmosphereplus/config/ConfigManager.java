package com.skrra.atmosphereplus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skrra.atmosphereplus.util.SafeFileIo;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("atmosphereplus");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("atmosphereplus.json");

    private static AtmosphereConfig config = new AtmosphereConfig();

    private ConfigManager() {}

    public static AtmosphereConfig get() {
        return config;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try {
            String json = Files.readString(CONFIG_PATH);
            AtmosphereConfig loaded = GSON.fromJson(json, AtmosphereConfig.class);
            if (loaded != null) {
                config = loaded;
            }
        } catch (Exception exception) {
            LOGGER.warn("Could not read config from {}; resetting to defaults", CONFIG_PATH, exception);
            SafeFileIo.quarantineCorruptFile(CONFIG_PATH);
            config = new AtmosphereConfig();
            save();
        }
    }

    public static void save() {
        try {
            SafeFileIo.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException exception) {
            LOGGER.error("Failed to save config to {}", CONFIG_PATH, exception);
        }
    }
}
