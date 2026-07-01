package com.skrra.atmosphereplus.automation;

import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.environment.EnvironmentDetector;
import com.skrra.atmosphereplus.environment.EnvironmentSnapshot;
import com.skrra.atmosphereplus.presets.PresetLibraryManager;
import com.skrra.atmosphereplus.presets.PresetReference;
import com.skrra.atmosphereplus.transitions.TransitionManager;
import com.skrra.atmosphereplus.transitions.TransitionSpeed;
import com.skrra.atmosphereplus.util.NotificationUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Locale;

public final class BiomeAtmosphereManager {
    private static final int CHECK_INTERVAL_TICKS = 10;
    private static int ticksUntilCheck = CHECK_INTERVAL_TICKS;
    private static boolean applyingAutomation = false;
    private static BiomeCategory pendingCategory = null;
    private static int pendingCategoryTicks = 0;
    private static String automationState = "Inactive";
    private static String lastNotificationKey = "";
    private static int notificationCooldownTicks = 0;
    private static boolean caveHandlingActive = false;

    private BiomeAtmosphereManager() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(BiomeAtmosphereManager::tick);
    }

    public static void tick(MinecraftClient client) {
        if (notificationCooldownTicks > 0) {
            notificationCooldownTicks--;
        }

        if (client == null || client.player == null || client.world == null) {
            pendingCategory = null;
            pendingCategoryTicks = 0;
            return;
        }

        BiomeAtmosphereConfig config = ConfigManager.get().biomeAtmospheres;
        if (config == null || !config.enabled || config.paused) {
            automationState = config != null && config.paused ? "Paused" : "Inactive";
            return;
        }

        if (--ticksUntilCheck > 0) {
            return;
        }
        ticksUntilCheck = CHECK_INTERVAL_TICKS;

        EnvironmentSnapshot environment = EnvironmentDetector.current();
        CaveHandlingMode caveHandling = CaveHandlingMode.parse(config.caveHandlingMode);
        if (handleCaveEnvironment(config, environment, caveHandling)) {
            return;
        }

        BiomeCategory category = detectCategory(client, environment, caveHandling);
        if (category == null) {
            automationState = "Unknown";
            return;
        }

        String categoryId = category.name();
        config.lastDetectedCategory = categoryId;
        String presetId = config.mappings == null ? "" : config.mappings.getOrDefault(categoryId, "");
        if (presetId == null || presetId.isBlank() || !PresetLibraryManager.exists(presetId)) {
            automationState = "Active";
            return;
        }

        if (categoryId.equals(config.lastAppliedCategory) && presetIsCurrentOrTransitioning(presetId)) {
            pendingCategory = category;
            pendingCategoryTicks = 0;
            config.lastAppliedCategory = categoryId;
            automationState = "Active";
            ConfigManager.save();
            return;
        }

        boolean dimensionCategory = category == BiomeCategory.NETHER || category == BiomeCategory.END;
        int minimumBiomeTimeMs = dimensionCategory ? 0 : Math.max(0, config.minimumBiomeTimeMs);
        if (pendingCategory != category) {
            pendingCategory = category;
            pendingCategoryTicks = 0;
            if (minimumBiomeTimeMs > 0) {
                automationState = "Waiting for biome dwell time";
                return;
            }
        } else {
            pendingCategoryTicks += CHECK_INTERVAL_TICKS;
            if (pendingCategoryTicks * 50 < minimumBiomeTimeMs) {
                automationState = "Waiting for biome dwell time";
                return;
            }
        }

        applyingAutomation = true;
        try {
            if (transitionToPreset(presetId, config)) {
                config.lastDetectedCategory = categoryId;
                config.lastAppliedCategory = categoryId;
                config.lastAppliedPreset = presetId;
                pendingCategoryTicks = 0;
                automationState = "Active";
                notifyAutomation(config, "biome:" + categoryId + ":" + presetId, "Biome Effects: " + category.label() + " -> " + presetName(presetId));
                ConfigManager.save();
            }
        } finally {
            applyingAutomation = false;
        }
    }

    public static String automationStateLabel() {
        return automationState;
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

    private static boolean handleCaveEnvironment(BiomeAtmosphereConfig config, EnvironmentSnapshot environment, CaveHandlingMode caveHandling) {
        boolean caveContext = environment != null
                && !environment.nether()
                && !environment.end()
                && (environment.underground() || environment.caveLike());

        if (!caveContext || caveHandling == CaveHandlingMode.IGNORE) {
            if (caveHandlingActive && caveHandling != CaveHandlingMode.IGNORE) {
                notifyAutomation(config, "surface_resumed", "Returned to surface: biome automation resumed");
            }
            caveHandlingActive = false;
            return false;
        }

        pendingCategory = null;
        pendingCategoryTicks = 0;
        caveHandlingActive = true;

        if (caveHandling == CaveHandlingMode.KEEP_CURRENT) {
            automationState = "Underground paused";
            notifyAutomation(config, "underground_paused", "Underground: biome automation paused");
            return true;
        }

        String cavePreset = config.cavePresetId == null ? "" : config.cavePresetId;
        if (cavePreset.isBlank() || !PresetLibraryManager.exists(cavePreset)) {
            automationState = "Underground paused";
            notifyAutomation(config, "underground_paused_no_cave_preset", "Underground: biome automation paused");
            return true;
        }

        if (cavePreset.equals(config.lastAppliedPreset) && "CAVE_HANDLING".equals(config.lastAppliedCategory)) {
            automationState = "Cave preset active";
            return true;
        }

        applyingAutomation = true;
        try {
            if (transitionToPreset(cavePreset, config)) {
                config.lastAppliedCategory = "CAVE_HANDLING";
                config.lastAppliedPreset = cavePreset;
                automationState = "Cave preset active";
                notifyAutomation(config, "cave_preset:" + cavePreset, "Cave preset active: " + presetName(cavePreset));
                ConfigManager.save();
            }
        } finally {
            applyingAutomation = false;
        }
        return true;
    }

    private static boolean transitionToPreset(String presetId, BiomeAtmosphereConfig config) {
        if (presetId.equals(TransitionManager.targetPresetId())) {
            return true;
        }

        return TransitionManager.transitionTo(presetId, TransitionSpeed.parse(config.transitionSpeed));
    }

    private static boolean presetIsCurrentOrTransitioning(String presetId) {
        return presetId != null
                && (presetId.equals(ConfigManager.get().activePreset) || presetId.equals(TransitionManager.targetPresetId()));
    }

    private static BiomeCategory detectCategory(MinecraftClient client, EnvironmentSnapshot environment, CaveHandlingMode caveHandling) {
        if (environment != null && environment.nether()) {
            return BiomeCategory.NETHER;
        }
        if (environment != null && environment.end()) {
            return BiomeCategory.END;
        }
        if (caveHandling == CaveHandlingMode.IGNORE && environment != null && environment.caveLike()) {
            return BiomeCategory.CAVE;
        }

        if (World.NETHER.equals(client.world.getRegistryKey())) {
            return BiomeCategory.NETHER;
        }
        if (World.END.equals(client.world.getRegistryKey())) {
            return BiomeCategory.END;
        }

        BlockPos pos = client.player.getBlockPos();
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

    private static String presetName(String presetId) {
        PresetReference reference = PresetLibraryManager.reference(presetId);
        return reference == null ? "Preset" : reference.displayName();
    }

    private static void notifyAutomation(BiomeAtmosphereConfig config, String key, String message) {
        if (config == null || !config.showAutomationToasts || key == null || key.equals(lastNotificationKey) || notificationCooldownTicks > 0) {
            return;
        }

        lastNotificationKey = key;
        notificationCooldownTicks = 40;
        NotificationUtil.show(message);
    }
}
