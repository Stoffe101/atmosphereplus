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
        // Removed in alpha 17 stability pass.
        // Cloud distance caused repeated broken/tiny/local cloud behavior.
        return vanillaDistance;
    }

    public static boolean experimentalRendererControlsEnabled() {
        return ConfigManager.get().experimentalRendererControls;
    }

    public static float cloudOpacityMultiplier() {
        return clamp(ConfigManager.get().cloudOpacity, 0.0F, 1.0F);
    }

    public static float cloudHeightMultiplier() {
        return clamp(ConfigManager.get().cloudHeight, 0.25F, 2.0F);
    }

    public static float skyBrightnessMultiplier() {
        return clamp(ConfigManager.get().skyBrightness, 0.0F, 2.0F);
    }

    public static float starBrightnessMultiplier() {
        return clamp(ConfigManager.get().starBrightness, 0.0F, 2.0F);
    }

    public static float sunMoonVisibilityMultiplier() {
        return clamp(ConfigManager.get().sunMoonVisibility, 0.0F, 1.0F);
    }

    public static boolean shaderAwareWarningsEnabled() {
        return ConfigManager.get().shaderAwareWarnings;
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


public static int adjustCloudColor(int color) {
    if (!experimentalRendererControlsEnabled()) {
        return color;
    }

    float opacity = cloudOpacityMultiplier();
    if (opacity >= 0.999F) {
        return color;
    }

    int a = (color >>> 24) & 0xFF;
    int rgb = color & 0x00FFFFFF;

    // Minecraft cloud colors can be RGB-only in some paths. Treat missing alpha as fully opaque.
    if (a == 0) {
        a = 255;
    }

    a = Math.max(0, Math.min(255, Math.round(a * opacity)));
    return (a << 24) | rgb;
}

public static int adjustSkyColor(int color) {
    if (!experimentalRendererControlsEnabled()) {
        return color;
    }

    float multiplier = skyBrightnessMultiplier();
    if (Math.abs(multiplier - 1.0F) < 0.001F) {
        return color;
    }

    int a = (color >>> 24) & 0xFF;
    int r = (color >>> 16) & 0xFF;
    int g = (color >>> 8) & 0xFF;
    int b = color & 0xFF;

    if (a == 0) {
        a = 255;
    }

    r = Math.max(0, Math.min(255, Math.round(r * multiplier)));
    g = Math.max(0, Math.min(255, Math.round(g * multiplier)));
    b = Math.max(0, Math.min(255, Math.round(b * multiplier)));

    return (a << 24) | (r << 16) | (g << 8) | b;
}

public static float adjustCloudHeight(float cloudHeight) {
    if (!experimentalRendererControlsEnabled()) {
        return cloudHeight;
    }

    return cloudHeight * cloudHeightMultiplier();
}

public static float adjustStarBrightness(float brightness) {
    if (!experimentalRendererControlsEnabled()) {
        return brightness;
    }

    return clamp(brightness * starBrightnessMultiplier(), 0.0F, 2.0F);
}

public static float adjustSunMoonAlpha(float alpha) {
    if (!experimentalRendererControlsEnabled()) {
        return alpha;
    }

    return clamp(alpha * sunMoonVisibilityMultiplier(), 0.0F, 1.0F);
}

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
