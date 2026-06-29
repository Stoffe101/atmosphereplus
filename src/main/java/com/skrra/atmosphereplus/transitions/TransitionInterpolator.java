package com.skrra.atmosphereplus.transitions;

import com.skrra.atmosphereplus.config.AtmosphereConfig;
import com.skrra.atmosphereplus.config.AtmosphereProfile;

public final class TransitionInterpolator {
    private TransitionInterpolator() {
    }

    public static float easeInOut(float progress) {
        float t = Math.max(0.0f, Math.min(1.0f, progress));
        return t * t * (3.0f - 2.0f * t);
    }

    public static void apply(AtmosphereConfig config, AtmosphereProfile start, AtmosphereProfile target, float easedProgress, boolean complete) {
        boolean endBooleans = complete;

        config.weatherOverride = target.weatherOverride || (!endBooleans && start.weatherOverride);
        config.weatherMode = target.weatherMode;
        config.rainIntensity = lerp(start.rainIntensity, target.rainIntensity, easedProgress);
        config.thunderSounds = target.thunderSounds || (!endBooleans && start.thunderSounds);

        config.timeOverride = target.timeOverride || (!endBooleans && start.timeOverride);
        config.visualTime = interpolateMinecraftTime(start.visualTime, target.visualTime, easedProgress);
        config.freezeVisualTime = target.freezeVisualTime || (!endBooleans && start.freezeVisualTime);

        config.fullbright = target.fullbright || (!endBooleans && start.fullbright);
        config.gamma = lerp(start.gamma, target.gamma, easedProgress);

        config.fogOverride = target.fogOverride || (!endBooleans && start.fogOverride);
        config.fogDistance = lerp(start.fogDistance, target.fogDistance, easedProgress);
        config.fogDensity = lerp(start.fogDensity, target.fogDensity, easedProgress);
        config.submersionFogOff = target.submersionFogOff || (!endBooleans && start.submersionFogOff);

        config.particleAmount = lerp(start.particleAmount, target.particleAmount, easedProgress);
        config.lowFire = target.lowFire || (!endBooleans && start.lowFire);

        config.cloudOverride = target.cloudOverride || (!endBooleans && start.cloudOverride);
        config.cloudMode = target.cloudMode;
        config.cloudDistance = Math.round(lerp(start.cloudDistance, target.cloudDistance, easedProgress));

        config.experimentalRendererControls = target.experimentalRendererControls || (!endBooleans && start.experimentalRendererControls);
        config.cloudOpacity = lerp(start.cloudOpacity, target.cloudOpacity, easedProgress);
        config.cloudHeight = lerp(start.cloudHeight, target.cloudHeight, easedProgress);
        config.cloudDistanceOverride = target.cloudDistanceOverride || (!endBooleans && start.cloudDistanceOverride);
        config.skyBrightness = lerp(start.skyBrightness, target.skyBrightness, easedProgress);
        config.starBrightness = lerp(start.starBrightness, target.starBrightness, easedProgress);
        config.sunMoonVisibility = lerp(start.sunMoonVisibility, target.sunMoonVisibility, easedProgress);
        config.shaderAwareWarnings = target.shaderAwareWarnings;

        config.moodOverlayEnabled = target.moodOverlayEnabled || (!endBooleans && start.moodOverlayEnabled);
        config.moodOverlayRed = lerp(start.moodOverlayRed, target.moodOverlayRed, easedProgress);
        config.moodOverlayGreen = lerp(start.moodOverlayGreen, target.moodOverlayGreen, easedProgress);
        config.moodOverlayBlue = lerp(start.moodOverlayBlue, target.moodOverlayBlue, easedProgress);
        config.moodOverlayStrength = lerp(start.moodOverlayStrength, target.moodOverlayStrength, easedProgress);
        config.moodBrightness = lerp(start.moodBrightness, target.moodBrightness, easedProgress);
        config.moodContrast = lerp(start.moodContrast, target.moodContrast, easedProgress);
        config.moodSaturation = lerp(start.moodSaturation, target.moodSaturation, easedProgress);
        config.moodVignetteStrength = lerp(start.moodVignetteStrength, target.moodVignetteStrength, easedProgress);

        if (complete) {
            target.applyTo(config);
        }
    }

    private static float lerp(float start, float target, float progress) {
        return start + (target - start) * progress;
    }

    private static int interpolateMinecraftTime(int start, int target, float progress) {
        int normalizedStart = Math.floorMod(start, 24000);
        int normalizedTarget = Math.floorMod(target, 24000);
        int delta = normalizedTarget - normalizedStart;

        if (delta > 12000) {
            delta -= 24000;
        } else if (delta < -12000) {
            delta += 24000;
        }

        return Math.floorMod(Math.round(normalizedStart + delta * progress), 24000);
    }
}
