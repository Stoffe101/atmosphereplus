package com.skrra.atmosphereplus.automation;

import java.util.Locale;

public enum CaveHandlingMode {
    KEEP_CURRENT("Keep Current / Pause Automation"),
    APPLY_CAVE_PRESET("Apply Cave Preset"),
    IGNORE("Ignore Cave Handling");

    private final String label;

    CaveHandlingMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public CaveHandlingMode next() {
        CaveHandlingMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static CaveHandlingMode parse(String value) {
        if (value == null || value.isBlank()) {
            return KEEP_CURRENT;
        }

        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return KEEP_CURRENT;
        }
    }
}
