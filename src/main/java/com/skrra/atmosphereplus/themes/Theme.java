package com.skrra.atmosphereplus.themes;

public record Theme(
        String id,
        String displayName,
        int background,
        int panel,
        int panelAlt,
        int accent,
        int accentSoft,
        int text,
        int mutedText,
        int border
) {}
