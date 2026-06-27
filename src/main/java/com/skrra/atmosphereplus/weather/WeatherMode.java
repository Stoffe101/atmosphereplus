package com.skrra.atmosphereplus.weather;

public enum WeatherMode {
    SERVER,
    SUNNY,
    RAIN,
    THUNDER,
    SNOW;

    public static WeatherMode fromConfig(String value) {
        if (value == null || value.isBlank()) {
            return SERVER;
        }

        try {
            return WeatherMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return SERVER;
        }
    }
}
