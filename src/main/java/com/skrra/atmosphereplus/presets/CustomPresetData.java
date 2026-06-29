package com.skrra.atmosphereplus.presets;

import com.skrra.atmosphereplus.config.AtmosphereConfig;
import com.skrra.atmosphereplus.config.AtmosphereProfile;

public class CustomPresetData {
    public String id = "";
    public String displayName = "My Preset";
    public String description = "Saved custom atmosphere.";
    public AtmosphereProfile snapshot = new AtmosphereProfile("Preset Snapshot");

    public CustomPresetData() {
    }

    public CustomPresetData(String id, String displayName, String description, AtmosphereConfig config) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.snapshot = new AtmosphereProfile(displayName);
        this.snapshot.capture(config);
    }

    public void applyTo(AtmosphereConfig config) {
        if (snapshot != null) {
            snapshot.applyTo(config);
        }
    }
}
