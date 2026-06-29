package com.skrra.atmosphereplus.transitions;

import com.skrra.atmosphereplus.config.AtmosphereProfile;

public class TransitionState {
    private final String targetPresetId;
    private final AtmosphereProfile start;
    private final AtmosphereProfile target;
    private final int durationMs;
    private int elapsedMs;

    public TransitionState(String targetPresetId, AtmosphereProfile start, AtmosphereProfile target, int durationMs) {
        this.targetPresetId = targetPresetId;
        this.start = start;
        this.target = target;
        this.durationMs = Math.max(0, durationMs);
        this.elapsedMs = 0;
    }

    public String targetPresetId() {
        return targetPresetId;
    }

    public AtmosphereProfile start() {
        return start;
    }

    public AtmosphereProfile target() {
        return target;
    }

    public int elapsedMs() {
        return elapsedMs;
    }

    public int durationMs() {
        return durationMs;
    }

    public void advance(int deltaMs) {
        elapsedMs = Math.min(durationMs, elapsedMs + Math.max(0, deltaMs));
    }

    public float progress() {
        if (durationMs <= 0) {
            return 1.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, elapsedMs / (float) durationMs));
    }

    public boolean complete() {
        return progress() >= 1.0f;
    }
}
