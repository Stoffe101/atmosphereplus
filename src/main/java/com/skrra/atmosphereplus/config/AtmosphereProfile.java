package com.skrra.atmosphereplus.config;

public class AtmosphereProfile {
    public String name = "Profile";
    public boolean saved = false;
    public boolean favorite = false;
    public boolean pinned = false;

    public String theme = "midnight";
    public boolean weatherOverride = false;
    public String weatherMode = "SERVER";
    public float rainIntensity = 1.0f;
    public boolean thunderSounds = true;

    public boolean timeOverride = false;
    public int visualTime = 6000;
    public boolean freezeVisualTime = false;

    public boolean fullbright = false;
    public float gamma = 1.0f;

    public boolean fogOverride = false;
    public float fogDistance = 1.0f;
    public float fogDensity = 1.0f;
    public boolean submersionFogOff = false;

    public float particleAmount = 1.0f;
    public boolean lowFire = false;

    public boolean cloudOverride = false;
    public String cloudMode = "SERVER";
    public int cloudDistance = 12;

    public boolean experimentalRendererControls = false;
    public float cloudOpacity = 1.0f;
    public float cloudHeight = 1.0f;
    public boolean cloudDistanceOverride = false;
    public float skyBrightness = 1.0f;
    public float starBrightness = 1.0f;
    public float sunMoonVisibility = 1.0f;
    public boolean shaderAwareWarnings = true;

    public boolean moodOverlayEnabled = false;
    public float moodOverlayRed = 1.0f;
    public float moodOverlayGreen = 1.0f;
    public float moodOverlayBlue = 1.0f;
    public float moodOverlayStrength = 0.0f;
    public float moodBrightness = 1.0f;
    public float moodContrast = 1.0f;
    public float moodSaturation = 1.0f;
    public float moodVignetteStrength = 0.0f;

    public AtmosphereProfile() {
    }

    public AtmosphereProfile(String name) {
        this.name = name;
    }

    public static AtmosphereProfile[] defaults() {
        AtmosphereProfile[] result = new AtmosphereProfile[5];
        for (int i = 0; i < result.length; i++) {
            result[i] = new AtmosphereProfile("Profile " + (i + 1));
        }
        return result;
    }

    public static AtmosphereProfile copyOf(AtmosphereProfile source) {
        AtmosphereProfile copy = new AtmosphereProfile(source == null ? "Profile" : source.name);
        if (source == null) {
            return copy;
        }

        copy.saved = source.saved;
        copy.favorite = source.favorite;
        copy.pinned = source.pinned;
        copy.theme = source.theme;
        copy.weatherOverride = source.weatherOverride;
        copy.weatherMode = source.weatherMode;
        copy.rainIntensity = source.rainIntensity;
        copy.thunderSounds = source.thunderSounds;
        copy.timeOverride = source.timeOverride;
        copy.visualTime = source.visualTime;
        copy.freezeVisualTime = source.freezeVisualTime;
        copy.fullbright = source.fullbright;
        copy.gamma = source.gamma;
        copy.fogOverride = source.fogOverride;
        copy.fogDistance = source.fogDistance;
        copy.fogDensity = source.fogDensity;
        copy.submersionFogOff = source.submersionFogOff;
        copy.particleAmount = source.particleAmount;
        copy.lowFire = source.lowFire;
        copy.cloudOverride = source.cloudOverride;
        copy.cloudMode = source.cloudMode;
        copy.cloudDistance = source.cloudDistance;
        copy.experimentalRendererControls = source.experimentalRendererControls;
        copy.cloudOpacity = source.cloudOpacity;
        copy.cloudHeight = source.cloudHeight;
        copy.cloudDistanceOverride = source.cloudDistanceOverride;
        copy.skyBrightness = source.skyBrightness;
        copy.starBrightness = source.starBrightness;
        copy.sunMoonVisibility = source.sunMoonVisibility;
        copy.shaderAwareWarnings = source.shaderAwareWarnings;
        copy.moodOverlayEnabled = source.moodOverlayEnabled;
        copy.moodOverlayRed = source.moodOverlayRed;
        copy.moodOverlayGreen = source.moodOverlayGreen;
        copy.moodOverlayBlue = source.moodOverlayBlue;
        copy.moodOverlayStrength = source.moodOverlayStrength;
        copy.moodBrightness = source.moodBrightness;
        copy.moodContrast = source.moodContrast;
        copy.moodSaturation = source.moodSaturation;
        copy.moodVignetteStrength = source.moodVignetteStrength;
        return copy;
    }

    public void capture(AtmosphereConfig config) {
        this.saved = true;
        this.theme = config.theme;
        this.weatherOverride = config.weatherOverride;
        this.weatherMode = config.weatherMode;
        this.rainIntensity = config.rainIntensity;
        this.thunderSounds = config.thunderSounds;
        this.timeOverride = config.timeOverride;
        this.visualTime = config.visualTime;
        this.freezeVisualTime = config.freezeVisualTime;
        this.fullbright = config.fullbright;
        this.gamma = config.gamma;
        this.fogOverride = config.fogOverride;
        this.fogDistance = config.fogDistance;
        this.fogDensity = config.fogDensity;
        this.submersionFogOff = config.submersionFogOff;
        this.particleAmount = config.particleAmount;
        this.lowFire = config.lowFire;
        this.cloudOverride = config.cloudOverride;
        this.cloudMode = config.cloudMode;
        this.cloudDistance = config.cloudDistance;
        this.experimentalRendererControls = config.experimentalRendererControls;
        this.cloudOpacity = config.cloudOpacity;
        this.cloudHeight = config.cloudHeight;
        this.cloudDistanceOverride = config.cloudDistanceOverride;
        this.skyBrightness = config.skyBrightness;
        this.starBrightness = config.starBrightness;
        this.sunMoonVisibility = config.sunMoonVisibility;
        this.shaderAwareWarnings = config.shaderAwareWarnings;
        this.moodOverlayEnabled = config.moodOverlayEnabled;
        this.moodOverlayRed = config.moodOverlayRed;
        this.moodOverlayGreen = config.moodOverlayGreen;
        this.moodOverlayBlue = config.moodOverlayBlue;
        this.moodOverlayStrength = config.moodOverlayStrength;
        this.moodBrightness = config.moodBrightness;
        this.moodContrast = config.moodContrast;
        this.moodSaturation = config.moodSaturation;
        this.moodVignetteStrength = config.moodVignetteStrength;
    }

    public void applyTo(AtmosphereConfig config) {
        config.theme = this.theme;
        config.weatherOverride = this.weatherOverride;
        config.weatherMode = this.weatherMode;
        config.rainIntensity = this.rainIntensity;
        config.thunderSounds = this.thunderSounds;
        config.timeOverride = this.timeOverride;
        config.visualTime = this.visualTime;
        config.freezeVisualTime = this.freezeVisualTime;
        config.fullbright = this.fullbright;
        config.gamma = this.gamma;
        config.fogOverride = this.fogOverride;
        config.fogDistance = this.fogDistance;
        config.fogDensity = this.fogDensity;
        config.submersionFogOff = this.submersionFogOff;
        config.particleAmount = this.particleAmount;
        config.lowFire = this.lowFire;
        config.cloudOverride = this.cloudOverride;
        config.cloudMode = this.cloudMode;
        config.cloudDistance = this.cloudDistance;
        config.experimentalRendererControls = this.experimentalRendererControls;
        config.cloudOpacity = this.cloudOpacity;
        config.cloudHeight = this.cloudHeight;
        config.cloudDistanceOverride = this.cloudDistanceOverride;
        config.skyBrightness = this.skyBrightness;
        config.starBrightness = this.starBrightness;
        config.sunMoonVisibility = this.sunMoonVisibility;
        config.shaderAwareWarnings = this.shaderAwareWarnings;
        config.moodOverlayEnabled = this.moodOverlayEnabled;
        config.moodOverlayRed = this.moodOverlayRed;
        config.moodOverlayGreen = this.moodOverlayGreen;
        config.moodOverlayBlue = this.moodOverlayBlue;
        config.moodOverlayStrength = this.moodOverlayStrength;
        config.moodBrightness = this.moodBrightness;
        config.moodContrast = this.moodContrast;
        config.moodSaturation = this.moodSaturation;
        config.moodVignetteStrength = this.moodVignetteStrength;
    }
}
