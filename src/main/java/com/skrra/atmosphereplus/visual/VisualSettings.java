package com.skrra.atmosphereplus.visual;

import com.skrra.atmosphereplus.config.ConfigManager;
import net.minecraft.client.option.CloudRenderMode;

public final class VisualSettings {
    private static boolean savingGameOptions = false;

    private VisualSettings() {
    }

    public static void setSavingGameOptions(boolean saving) {
        savingGameOptions = saving;
    }

    public static boolean isSavingGameOptions() {
        return savingGameOptions;
    }

    public static boolean isCloudOverrideEnabled() {
        return ConfigManager.get().cloudOverride;
    }

    public static CloudRenderMode applyCloudMode(CloudRenderMode vanillaMode) {
        if (!isCloudOverrideEnabled()) {
            return vanillaMode;
        }

        String mode = ConfigManager.get().cloudMode;
        if (mode == null) {
            return vanillaMode;
        }

        return switch (mode.trim().toUpperCase()) {
            case "OFF", "HIDDEN" -> CloudRenderMode.OFF;
            case "FAST" -> CloudRenderMode.FAST;
            case "FANCY" -> CloudRenderMode.FANCY;
            default -> vanillaMode;
        };
    }

    public static int applyCloudDistance(int vanillaDistance) {
        // Disabled for now. In 1.21.11, forcing this through the option value can
        // cause broken tiny/local clouds and flickering/reverting behavior.
        // Cloud mode override remains active and stable.
        return vanillaDistance;
    }

    public static boolean isFogOverrideEnabled() {
        return ConfigManager.get().fogOverride;
    }

    public static float fogDistanceMultiplier() {
        return clamp(ConfigManager.get().fogDistance, 0.05F, 3.0F);
    }

    public static float fogDensityMultiplier() {
        return clamp(ConfigManager.get().fogDensity, 0.05F, 4.0F);
    }

    public static boolean isFullbrightEnabled() {
        return ConfigManager.get().fullbright;
    }

    public static float gammaMultiplier() {
        return clamp(ConfigManager.get().gamma, 0.0F, 2.0F);
    }

    public static double applyGammaOption(double vanillaGamma) {
        if (isFullbrightEnabled()) {
            return 15.0D;
        }

        float gamma = gammaMultiplier();

        if (Math.abs(gamma - 1.0F) < 0.001F) {
            return vanillaGamma;
        }

        if (gamma < 1.0F) {
            return Math.max(0.0D, vanillaGamma * gamma);
        }

        return Math.min(15.0D, vanillaGamma + (gamma - 1.0D) * 7.0D);
    }

    public static float applyBrightness(float value) {
        if (isFullbrightEnabled()) {
            return 1.0F;
        }

        float gamma = gammaMultiplier();

        if (gamma > 1.0F) {
            return clamp(value + (1.0F - value) * (gamma - 1.0F), 0.0F, 1.0F);
        }

        if (gamma < 1.0F) {
            return clamp(value * gamma, 0.0F, 1.0F);
        }

        return value;
    }

    public static float adjustFogStart(float original) {
        if (!isFogOverrideEnabled()) {
            return original;
        }

        float distance = fogDistanceMultiplier();
        float density = fogDensityMultiplier();
        return Math.max(0.0F, original * distance / density);
    }

    public static float adjustFogEnd(float original, float adjustedStart) {
        if (!isFogOverrideEnabled()) {
            return original;
        }

        float distance = fogDistanceMultiplier();
        float density = fogDensityMultiplier();
        return Math.max(adjustedStart + 1.0F, original * distance / density);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
