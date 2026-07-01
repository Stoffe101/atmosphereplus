package com.skrra.atmosphereplus.ui;

public enum UiCategory {
    HOME("Home", "Overview and quick status", IconType.HOME, SidebarGroup.PINNED),
    QUICK("Quick", "Fast access to profiles, presets and common toggles", IconType.PRESETS, SidebarGroup.PINNED),
    WEATHER("Weather", "Visual weather controls", IconType.WEATHER, SidebarGroup.VISUALS),
    TIME("Time", "Day and night cycle", IconType.TIME, SidebarGroup.VISUALS),
    SKY("Sky", "Clouds and sky atmosphere", IconType.SKY, SidebarGroup.VISUALS),
    FOG("Fog", "Fog distance and density", IconType.FOG, SidebarGroup.VISUALS),
    LIGHTING("Lighting", "Brightness and atmosphere", IconType.LIGHTING, SidebarGroup.VISUALS),
    PARTICLES("Particles", "Particle visibility", IconType.PARTICLES, SidebarGroup.VISUALS),
    THEMES("Themes", "Browse and apply interface themes", IconType.THEMES, SidebarGroup.THEMES_PRESETS),
    THEME_STUDIO("Theme Studio", "Create and edit custom themes", IconType.THEMES, SidebarGroup.THEMES_PRESETS),
    PRESETS("Presets", "Quick visual moods", IconType.PRESETS, SidebarGroup.THEMES_PRESETS),
    BIOME_ATMOSPHERES("Biome Effects", "Preset automation by biome", IconType.SKY, SidebarGroup.THEMES_PRESETS),
    PROFILES("Profiles", "Save and load custom atmospheres", IconType.PRESETS, SidebarGroup.DATA_TOOLS),
    ADVANCED("Advanced", "Extra settings and reset tools", IconType.ADVANCED, SidebarGroup.DATA_TOOLS);

    public final String title;
    public final String description;
    public final IconType icon;
    public final SidebarGroup group;

    UiCategory(String title, String description, IconType icon, SidebarGroup group) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.group = group;
    }
}
