package com.skrra.atmosphereplus.config;

public class AtmosphereProfile {
    public String name = "Profile";
    public boolean saved = false;

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
    }
}
