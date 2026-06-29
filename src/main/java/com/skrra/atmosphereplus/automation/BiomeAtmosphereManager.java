package com.skrra.atmosphereplus.automation;

import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.presets.PresetLibraryManager;
import com.skrra.atmosphereplus.transitions.TransitionManager;
import com.skrra.atmosphereplus.transitions.TransitionSpeed;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.Locale;

public final class BiomeAtmosphereManager {
    private static final int CHECK_INTERVAL_TICKS = 10;
    private static int ticksUntilCheck = CHECK_INTERVAL_TICKS;
    private static boolean applyingAutomation = false;
    private static BiomeCategory pendingCategory = null;
    private static int pendingCategoryTicks = 0;

    private BiomeAtmosphereManager() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(BiomeAtmosphereManager::tick);
    }

    public static void tick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            pendingCategory = null;
            pendingCategoryTicks = 0;
            return;
        }

        BiomeAtmosphereConfig config = ConfigManager.get().biomeAtmospheres;
        if (config == null || !config.enabled || config.paused) {
            return;
        }

        if (--ticksUntilCheck > 0) {
            return;
        }
        ticksUntilCheck = CHECK_INTERVAL_TICKS;

        BiomeCategory category = detectCategory(client);
        if (category == null) {
            return;
        }

        String categoryId = category.name();
        config.lastDetectedCategory = categoryId;

        if (categoryId.equals(config.lastAppliedCategory)) {
            pendingCategory = category;
            pendingCategoryTicks = 0;
            return;
        }

        int minimumBiomeTimeMs = Math.max(0, config.minimumBiomeTimeMs);
        if (pendingCategory != category) {
            pendingCategory = category;
            pendingCategoryTicks = 0;
            if (minimumBiomeTimeMs > 0) {
                return;
            }
        } else {
            pendingCategoryTicks += CHECK_INTERVAL_TICKS;
            if (pendingCategoryTicks * 50 < minimumBiomeTimeMs) {
                return;
            }
        }

        String presetId = config.mappings == null ? "" : config.mappings.getOrDefault(categoryId, "");
        if (presetId == null || presetId.isBlank() || !PresetLibraryManager.exists(presetId)) {
            ConfigManager.save();
            return;
        }

        if (presetId.equals(config.lastAppliedPreset)) {
            config.lastAppliedCategory = categoryId;
            ConfigManager.save();
            return;
        }

        applyingAutomation = true;
        try {
            if (TransitionManager.transitionTo(presetId, TransitionSpeed.parse(config.transitionSpeed))) {
                config.lastDetectedCategory = categoryId;
                config.lastAppliedCategory = categoryId;
                config.lastAppliedPreset = presetId;
                pendingCategoryTicks = 0;
                ConfigManager.save();
            }
        } finally {
            applyingAutomation = false;
        }
    }

    public static void onManualAtmosphereChange() {
        if (applyingAutomation) {
            return;
        }

        BiomeAtmosphereConfig config = ConfigManager.get().biomeAtmospheres;
        if (config != null && config.enabled && config.manualChangesPause && !config.paused) {
            config.paused = true;
            ConfigManager.save();
        }
    }

    public static String currentCategoryLabel() {
        String id = ConfigManager.get().biomeAtmospheres == null ? "" : ConfigManager.get().biomeAtmospheres.lastDetectedCategory;
        return categoryLabel(id);
    }

    public static String categoryLabel(String id) {
        if (id == null || id.isBlank()) {
            return "Unknown";
        }

        try {
            return BiomeCategory.valueOf(id).label();
        } catch (IllegalArgumentException ignored) {
            return "Unknown";
        }
    }

    public static String minimumBiomeTimeLabel(int millis) {
        return switch (millis) {
            case 0 -> "Off";
            case 500 -> "0.5s";
            case 1000 -> "1s";
            case 2000 -> "2s";
            case 5000 -> "5s";
            default -> "1s";
        };
    }

    public static int nextMinimumBiomeTime(int current) {
        int[] values = {0, 500, 1000, 2000, 5000};
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) {
                return values[(i + 1) % values.length];
            }
        }
        return 1000;
    }

    private static BiomeCategory detectCategory(MinecraftClient client) {
        String dimension = String.valueOf(client.world.getRegistryKey().getValue()).toLowerCase(Locale.ROOT);
        if (dimension.contains("the_nether")) {
            return BiomeCategory.NETHER;
        }
        if (dimension.contains("the_end")) {
            return BiomeCategory.END;
        }

        BlockPos pos = client.player.getBlockPos();
        if (pos.getY() < 45) {
            return BiomeCategory.CAVE;
        }

        String biome = String.valueOf(client.world.getBiome(pos)).toLowerCase(Locale.ROOT);
        if (biome.contains("desert") || biome.contains("badlands") || biome.contains("savanna")) {
            return BiomeCategory.DESERT;
        }
        if (biome.contains("snow") || biome.contains("frozen") || biome.contains("ice") || biome.contains("cold")) {
            return BiomeCategory.SNOW;
        }
        if (biome.contains("swamp") || biome.contains("mangrove")) {
            return BiomeCategory.SWAMP;
        }
        if (biome.contains("ocean") || biome.contains("river") || biome.contains("beach")) {
            return BiomeCategory.OCEAN;
        }
        if (biome.contains("mountain") || biome.contains("peak") || biome.contains("slope") || biome.contains("grove") || biome.contains("meadow")) {
            return BiomeCategory.MOUNTAIN;
        }
        if (biome.contains("forest") || biome.contains("jungle") || biome.contains("taiga")) {
            return BiomeCategory.FOREST;
        }

        return BiomeCategory.PLAINS;
    }
}
