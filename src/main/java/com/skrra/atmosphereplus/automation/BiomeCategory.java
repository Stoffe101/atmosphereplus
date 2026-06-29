package com.skrra.atmosphereplus.automation;

public enum BiomeCategory {
    PLAINS("Plains / Default"),
    FOREST("Forest"),
    DESERT("Desert"),
    SNOW("Snow"),
    SWAMP("Swamp"),
    OCEAN("Ocean"),
    MOUNTAIN("Mountain"),
    CAVE("Cave / Underground"),
    NETHER("Nether"),
    END("End");

    private final String label;

    BiomeCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
