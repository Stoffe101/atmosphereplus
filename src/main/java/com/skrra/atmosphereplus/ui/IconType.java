package com.skrra.atmosphereplus.ui;

public enum IconType {
    HOME("home"),
    WEATHER("weather"),
    TIME("time"),
    SKY("sky"),
    FOG("fog"),
    LIGHTING("lighting"),
    PARTICLES("particles"),
    THEMES("themes"),
    PRESETS("presets"),
    ADVANCED("advanced");

    public final String textureName;

    IconType(String textureName) {
        this.textureName = textureName;
    }
}
