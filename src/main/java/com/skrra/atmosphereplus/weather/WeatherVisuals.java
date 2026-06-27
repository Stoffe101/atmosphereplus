package com.skrra.atmosphereplus.weather;

import com.skrra.atmosphereplus.config.AtmosphereConfig;
import com.skrra.atmosphereplus.config.ConfigManager;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.world.biome.Biome;

public final class WeatherVisuals {
    private WeatherVisuals() {
    }

    public static WeatherMode mode() {
        return WeatherMode.fromConfig(ConfigManager.get().weatherMode);
    }

    public static boolean isOverridingWeather() {
        AtmosphereConfig config = ConfigManager.get();
        WeatherMode mode = mode();

        return config.weatherOverride && mode != WeatherMode.SERVER;
    }

    public static boolean shouldForceVisiblePrecipitation() {
        return switch (mode()) {
            case RAIN, THUNDER, SNOW -> isOverridingWeather();
            case SERVER, SUNNY -> false;
        };
    }

    public static boolean shouldVisuallyRain() {
        return switch (mode()) {
            case RAIN, THUNDER, SNOW -> isOverridingWeather();
            case SERVER, SUNNY -> false;
        };
    }

    public static boolean shouldVisuallyThunder() {
        return isOverridingWeather() && mode() == WeatherMode.THUNDER;
    }

    public static boolean shouldBlockThunderSound(SoundInstance sound) {
        if (ConfigManager.get().thunderSounds) {
            return false;
        }

        String id = sound.getId().toString().toLowerCase();
        return id.contains("thunder") || id.contains("lightning");
    }

    public static float rainGradient() {
        if (!isOverridingWeather()) {
            return Float.NaN;
        }

        return switch (mode()) {
            case SUNNY, SERVER -> 0.0F;
            case RAIN, SNOW -> clamp(ConfigManager.get().rainIntensity, 0.05F, 1.0F);
            case THUNDER -> clamp(Math.max(ConfigManager.get().rainIntensity, 0.55F), 0.05F, 1.0F);
        };
    }

    public static float thunderGradient() {
        if (!isOverridingWeather()) {
            return Float.NaN;
        }

        return mode() == WeatherMode.THUNDER ? 1.0F : 0.0F;
    }

    public static Biome.Precipitation precipitation(Biome.Precipitation original) {
        if (!isOverridingWeather()) {
            return original;
        }

        return switch (mode()) {
            case SNOW -> Biome.Precipitation.SNOW;
            case RAIN, THUNDER -> Biome.Precipitation.RAIN;
            case SUNNY, SERVER -> Biome.Precipitation.NONE;
        };
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
