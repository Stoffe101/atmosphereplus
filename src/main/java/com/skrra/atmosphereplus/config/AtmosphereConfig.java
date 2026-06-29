package com.skrra.atmosphereplus.config;

import com.skrra.atmosphereplus.automation.BiomeAtmosphereConfig;

import java.util.ArrayList;
import java.util.List;

public class AtmosphereConfig {
    public int configVersion = ConfigSafety.LATEST_CONFIG_VERSION;

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

    public String activePreset = "";
    public String lastUiCategory = "QUICK";
    public int lastQuickProfile = 0;
    public String lastQuickPreset = "";

    public boolean favoritePresetGoldenHour = true;
    public boolean favoritePresetMidnight = true;
    public boolean favoritePresetCozyRain = false;
    public boolean favoritePresetDeepFog = false;
    public boolean favoritePresetBrightCaves = true;
    public boolean favoritePresetCloudsOff = false;

    public AtmosphereProfile[] profiles = AtmosphereProfile.defaults();
    public BiomeAtmosphereConfig biomeAtmospheres = BiomeAtmosphereConfig.defaults();
    public List<String> favoritePresetIds = new ArrayList<>();
}
