package com.skrra.atmosphereplus.presets;

import com.skrra.atmosphereplus.ui.IconType;

public record PresetReference(
        String id,
        String displayName,
        String description,
        IconType icon,
        boolean custom,
        boolean dimension
) {
    public PresetReference(String id, String displayName, String description, IconType icon, boolean custom) {
        this(id, displayName, description, icon, custom, false);
    }
}
