package com.skrra.atmosphereplus.environment;

public enum EnvironmentType {
    SURFACE("Surface"),
    UNDERGROUND("Underground"),
    CAVE("Cave"),
    NETHER("Nether"),
    END("End"),
    UNKNOWN("Unknown");

    private final String label;

    EnvironmentType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
