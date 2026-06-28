package com.skrra.atmosphereplus.config;

import com.skrra.atmosphereplus.util.NotificationUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public final class ConfigSafety {
    public static final int LATEST_CONFIG_VERSION = 7;

    private ConfigSafety() {
    }

    public static void repairAndMigrate() {
        AtmosphereConfig config = ConfigManager.get();
        boolean changed = false;

        if (config.configVersion < LATEST_CONFIG_VERSION) {
            backupConfigBeforeMigration();

            // alpha 17 cleanup: cloud distance was removed because it caused broken cloud behavior.
            // alpha 18 release cleanup: keep stale experimental values safe and non-invasive.
            config.cloudDistanceOverride = false;
            config.cloudDistance = 12;

            config.configVersion = LATEST_CONFIG_VERSION;
            changed = true;
        }

        if (isBlank(config.theme)) {
            config.theme = "midnight";
            changed = true;
        }

        String repairedWeather = sanitizeMode(config.weatherMode, "SERVER", "SERVER", "SUNNY", "RAIN", "THUNDER", "SNOW");
        if (!repairedWeather.equals(config.weatherMode)) {
            config.weatherMode = repairedWeather;
            changed = true;
        }

        String repairedCloud = sanitizeMode(config.cloudMode, "SERVER", "SERVER", "OFF", "FAST", "FANCY");
        if (!repairedCloud.equals(config.cloudMode)) {
            config.cloudMode = repairedCloud;
            changed = true;
        }

        float rain = clamp(config.rainIntensity, 0.0f, 1.0f);
        if (rain != config.rainIntensity) {
            config.rainIntensity = rain;
            changed = true;
        }

        int time = Math.floorMod(config.visualTime, 24000);
        if (time != config.visualTime) {
            config.visualTime = time;
            changed = true;
        }

        float gamma = clamp(config.gamma, 0.0f, 2.0f);
        if (gamma != config.gamma) {
            config.gamma = gamma;
            changed = true;
        }

        float fogDistance = clamp(config.fogDistance, 0.0f, 2.0f);
        if (fogDistance != config.fogDistance) {
            config.fogDistance = fogDistance;
            changed = true;
        }

        float fogDensity = clamp(config.fogDensity, 0.0f, 2.0f);
        if (fogDensity != config.fogDensity) {
            config.fogDensity = fogDensity;
            changed = true;
        }

        float particles = clamp(config.particleAmount, 0.0f, 2.0f);
        if (particles != config.particleAmount) {
            config.particleAmount = particles;
            changed = true;
        }

        int cloudDistance = clamp(config.cloudDistance, 2, 32);
        if (cloudDistance != config.cloudDistance) {
            config.cloudDistance = cloudDistance;
            changed = true;
        }

        int quickProfile = clamp(config.lastQuickProfile, 0, ProfileManager.PROFILE_COUNT - 1);
        if (quickProfile != config.lastQuickProfile) {
            config.lastQuickProfile = quickProfile;
            changed = true;
        }

        if (config.activePreset == null) {
            config.activePreset = "";
            changed = true;
        }

        if (config.lastUiCategory == null || !isValidCategory(config.lastUiCategory)) {
            config.lastUiCategory = "QUICK";
            changed = true;
        }

        if (config.lastQuickPreset == null) {
            config.lastQuickPreset = "";
            changed = true;
        }

        config.cloudOpacity = clamp(config.cloudOpacity, 0.0f, 1.0f);
        config.cloudHeight = clamp(config.cloudHeight, 0.25f, 2.0f);
        config.cloudDistance = clamp(config.cloudDistance, 2, 32);
        config.skyBrightness = clamp(config.skyBrightness, 0.0f, 2.0f);
        config.starBrightness = clamp(config.starBrightness, 0.0f, 2.0f);
        config.sunMoonVisibility = clamp(config.sunMoonVisibility, 0.0f, 1.0f);

        if (repairProfiles(config)) {
            changed = true;
        }

        if (changed) {
            ConfigManager.save();
        }
    }

    private static boolean repairProfiles(AtmosphereConfig config) {
        boolean changed = false;

        if (config.profiles == null || config.profiles.length < ProfileManager.PROFILE_COUNT) {
            AtmosphereProfile[] old = config.profiles;
            config.profiles = AtmosphereProfile.defaults();

            if (old != null) {
                for (int i = 0; i < Math.min(old.length, config.profiles.length); i++) {
                    if (old[i] != null) {
                        config.profiles[i] = old[i];
                    }
                }
            }

            changed = true;
        }

        for (int i = 0; i < ProfileManager.PROFILE_COUNT; i++) {
            if (config.profiles[i] == null) {
                config.profiles[i] = new AtmosphereProfile("Profile " + (i + 1));
                changed = true;
            }

            AtmosphereProfile profile = config.profiles[i];

            if (isBlank(profile.name)) {
                profile.name = "Profile " + (i + 1);
                changed = true;
            }

            if (profile.name.length() > 24) {
                profile.name = profile.name.substring(0, 24);
                changed = true;
            }

            String weather = sanitizeMode(profile.weatherMode, "SERVER", "SERVER", "SUNNY", "RAIN", "THUNDER", "SNOW");
            if (!weather.equals(profile.weatherMode)) {
                profile.weatherMode = weather;
                changed = true;
            }

            String cloud = sanitizeMode(profile.cloudMode, "SERVER", "SERVER", "OFF", "FAST", "FANCY");
            if (!cloud.equals(profile.cloudMode)) {
                profile.cloudMode = cloud;
                changed = true;
            }

            float rain = clamp(profile.rainIntensity, 0.0f, 1.0f);
            if (rain != profile.rainIntensity) {
                profile.rainIntensity = rain;
                changed = true;
            }

            int time = Math.floorMod(profile.visualTime, 24000);
            if (time != profile.visualTime) {
                profile.visualTime = time;
                changed = true;
            }

            float gamma = clamp(profile.gamma, 0.0f, 2.0f);
            if (gamma != profile.gamma) {
                profile.gamma = gamma;
                changed = true;
            }

            profile.fogDistance = clamp(profile.fogDistance, 0.0f, 2.0f);
            profile.fogDensity = clamp(profile.fogDensity, 0.0f, 2.0f);
            profile.particleAmount = clamp(profile.particleAmount, 0.0f, 2.0f);
            profile.cloudDistance = clamp(profile.cloudDistance, 2, 32);
            profile.cloudDistanceOverride = false;
            profile.cloudOpacity = clamp(profile.cloudOpacity, 0.0f, 1.0f);
            profile.cloudHeight = clamp(profile.cloudHeight, 0.25f, 2.0f);
            profile.skyBrightness = clamp(profile.skyBrightness, 0.0f, 2.0f);
            profile.starBrightness = clamp(profile.starBrightness, 0.0f, 2.0f);
            profile.sunMoonVisibility = clamp(profile.sunMoonVisibility, 0.0f, 1.0f);
        }

        return changed;
    }

    private static void backupConfigBeforeMigration() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("atmosphereplus.json");
        Path backupPath = FabricLoader.getInstance().getConfigDir().resolve("atmosphereplus-before-alpha11-backup.json");

        if (!Files.exists(configPath)) {
            return;
        }

        try {
            Files.copy(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // The config repair should still continue even if backup creation fails.
        }
    }


private static boolean isValidCategory(String value) {
    if (value == null) {
        return false;
    }

    return switch (value.toUpperCase(Locale.ROOT)) {
        case "HOME", "QUICK", "WEATHER", "TIME", "SKY", "FOG", "LIGHTING", "PARTICLES", "THEMES", "PRESETS", "PROFILES", "ADVANCED" -> true;
        default -> false;
    };
}

    private static String sanitizeMode(String value, String fallback, String... allowed) {
        if (value == null) {
            return fallback;
        }

        String cleaned = value.trim().toUpperCase(Locale.ROOT);
        for (String allowedValue : allowed) {
            if (allowedValue.equals(cleaned)) {
                return cleaned;
            }
        }

        return fallback;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static float clamp(float value, float min, float max) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return min;
        }

        return Math.max(min, Math.min(max, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
