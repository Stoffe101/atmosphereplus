package com.skrra.atmosphereplus.automation;

import java.util.LinkedHashMap;
import java.util.Map;

public class BiomeAtmosphereConfig {
    public boolean enabled = false;
    public boolean paused = false;
    public boolean manualChangesPause = true;
    public int transitionDurationMs = 1000;
    public String lastDetectedCategory = "";
    public String lastAppliedCategory = "";
    public String lastAppliedPreset = "";
    public Map<String, String> mappings = defaultMappings();

    public static BiomeAtmosphereConfig defaults() {
        return new BiomeAtmosphereConfig();
    }

    public static Map<String, String> defaultMappings() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put(BiomeCategory.PLAINS.name(), "golden_hour");
        result.put(BiomeCategory.FOREST.name(), "misty_morning");
        result.put(BiomeCategory.DESERT.name(), "warm_desert");
        result.put(BiomeCategory.SNOW.name(), "cold_blue");
        result.put(BiomeCategory.SWAMP.name(), "soft_mist");
        result.put(BiomeCategory.OCEAN.name(), "screenshot_clear");
        result.put(BiomeCategory.MOUNTAIN.name(), "starlit_night");
        result.put(BiomeCategory.CAVE.name(), "bright_caves");
        result.put(BiomeCategory.NETHER.name(), "dark_crimson");
        result.put(BiomeCategory.END.name(), "void_purple");
        return result;
    }
}
