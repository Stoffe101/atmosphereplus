package com.skrra.atmosphereplus.ui;

public enum UiCategory {
    HOME("Home", "Overview and quick status", IconType.HOME),
    WEATHER("Weather", "Visual weather controls", IconType.WEATHER),
    TIME("Time", "Day and night cycle", IconType.TIME),
    SKY("Sky", "Sky, stars, sun and moon", IconType.SKY),
    FOG("Fog", "Fog distance and density", IconType.FOG),
    LIGHTING("Lighting", "Brightness and atmosphere", IconType.LIGHTING),
    PARTICLES("Particles", "Particle visibility", IconType.PARTICLES),
    THEMES("Themes", "Customize the Atmosphere+ interface", IconType.THEMES),
    PRESETS("Presets", "Quick visual moods", IconType.PRESETS),
    ADVANCED("Advanced", "Extra settings", IconType.ADVANCED);

    public final String title;
    public final String description;
    public final IconType icon;

    UiCategory(String title, String description, IconType icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
    }
}
