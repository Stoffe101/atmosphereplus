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

    public float particleAmount = 1.0f;

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
        this.particleAmount = config.particleAmount;
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
        config.particleAmount = this.particleAmount;
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
    }
}
