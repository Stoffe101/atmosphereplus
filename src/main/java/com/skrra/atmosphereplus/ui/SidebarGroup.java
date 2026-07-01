package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.config.AtmosphereConfig;
import com.skrra.atmosphereplus.config.ConfigManager;

/**
 * Grouping metadata for the sidebar navigation (0.6.0-beta.1).
 *
 * <p>Home and Quick are pinned and belong to {@link #PINNED}, which is never collapsible and has
 * no header row. The three collapsible groups map one-to-one to the persisted collapse flags on
 * {@code AtmosphereConfig}. This enum is pure data modeling — it carries no rendering or input
 * behavior on its own.
 */
public enum SidebarGroup {
    PINNED(null, false),
    VISUALS("Visuals", true),
    THEMES_PRESETS("Themes & Presets", true),
    DATA_TOOLS("Data & Tools", true);

    /** Header label shown above the group's items, or {@code null} for {@link #PINNED}. */
    public final String title;

    /** Whether this group renders a header that can toggle its expand/collapse state. */
    public final boolean collapsible;

    SidebarGroup(String title, boolean collapsible) {
        this.title = title;
        this.collapsible = collapsible;
    }

    /** Persisted collapse state for this group. Non-collapsible groups are never collapsed. */
    public boolean isCollapsed() {
        AtmosphereConfig config = ConfigManager.get();
        return switch (this) {
            case VISUALS -> config.sidebarGroupVisualsCollapsed;
            case THEMES_PRESETS -> config.sidebarGroupThemesPresetsCollapsed;
            case DATA_TOOLS -> config.sidebarGroupDataToolsCollapsed;
            default -> false;
        };
    }

    public void setCollapsed(boolean collapsed) {
        AtmosphereConfig config = ConfigManager.get();
        switch (this) {
            case VISUALS -> config.sidebarGroupVisualsCollapsed = collapsed;
            case THEMES_PRESETS -> config.sidebarGroupThemesPresetsCollapsed = collapsed;
            case DATA_TOOLS -> config.sidebarGroupDataToolsCollapsed = collapsed;
            default -> {
            }
        }
    }
}
