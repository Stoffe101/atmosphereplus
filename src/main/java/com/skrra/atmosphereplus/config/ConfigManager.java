package com.skrra.atmosphereplus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
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
        } catch (Exception ignored) {
            config = new AtmosphereConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException ignored) {
        }
    }
}
