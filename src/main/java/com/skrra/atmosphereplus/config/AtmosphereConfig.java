package com.skrra.atmosphereplus.config;

public class AtmosphereConfig {
    public String theme = "midnight";
    public boolean compactMode = false;
    public float animationSpeed = 1.0f;

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

    public String activePreset = "";
    public AtmosphereProfile[] profiles = AtmosphereProfile.defaults();
}
