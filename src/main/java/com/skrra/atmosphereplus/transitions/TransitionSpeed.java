package com.skrra.atmosphereplus.transitions;

import java.util.Locale;

public enum TransitionSpeed {
    INSTANT("Instant", 0),
    FAST("Fast", 500),
    NORMAL("Normal", 1500),
    SLOW("Slow", 3000);

    private final String label;
    private final int durationMs;

    TransitionSpeed(String label, int durationMs) {
        this.label = label;
        this.durationMs = durationMs;
    }

    public String label() {
        return label;
    }

    public int durationMs() {
        return durationMs;
    }

    public TransitionSpeed next() {
        TransitionSpeed[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static TransitionSpeed parse(String value) {
        if (value == null || value.isBlank()) {
            return NORMAL;
        }

        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NORMAL;
        }
    }

    public static TransitionSpeed fromLegacyDuration(int millis) {
        if (millis <= 0) {
            return INSTANT;
        }
        if (millis <= 500) {
            return FAST;
        }
        if (millis >= 3000) {
            return SLOW;
        }
        return NORMAL;
    }
}
