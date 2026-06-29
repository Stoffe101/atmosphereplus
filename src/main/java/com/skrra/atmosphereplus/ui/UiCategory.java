package com.skrra.atmosphereplus.ui;

public enum UiCategory {
    HOME("Home", "Overview and quick status", IconType.HOME),
    QUICK("Quick", "Fast access to profiles, presets and common toggles", IconType.PRESETS),
    WEATHER("Weather", "Visual weather controls", IconType.WEATHER),
    TIME("Time", "Day and night cycle", IconType.TIME),
    SKY("Sky", "Clouds and sky atmosphere", IconType.SKY),
    FOG("Fog", "Fog distance and density", IconType.FOG),
    LIGHTING("Lighting", "Brightness and atmosphere", IconType.LIGHTING),
    PARTICLES("Particles", "Particle visibility", IconType.PARTICLES),
    THEMES("Themes", "Browse and apply interface themes", IconType.THEMES),
    THEME_STUDIO("Theme Studio", "Create and edit custom themes", IconType.THEMES),
    PRESETS("Presets", "Quick visual moods", IconType.PRESETS),
    BIOME_ATMOSPHERES("Biome Atmospheres", "Preset automation by biome", IconType.SKY),
    PROFILES("Profiles", "Save and load custom atmospheres", IconType.PRESETS),
    ADVANCED("Advanced", "Extra settings and reset tools", IconType.ADVANCED);

    public final String title;
    public final String description;
    public final IconType icon;

    UiCategory(String title, String description, IconType icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
    }
}
