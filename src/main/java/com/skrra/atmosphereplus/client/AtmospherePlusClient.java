package com.skrra.atmosphereplus.client;

import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.config.ConfigSafety;
import com.skrra.atmosphereplus.keybind.AtmosphereKeybinds;
import com.skrra.atmosphereplus.themes.ThemeManager;
import net.fabricmc.api.ClientModInitializer;

public class AtmospherePlusClient implements ClientModInitializer {
    public static final String MOD_ID = "atmosphereplus";
    public static final String MOD_NAME = "Atmosphere+";
    public static final String VERSION = "0.2.0-beta.3";

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        ConfigSafety.repairAndMigrate();
        ThemeManager.init();
        AtmosphereKeybinds.register();
    }
}
