package com.skrra.atmosphereplus.presets;

import com.skrra.atmosphereplus.config.AtmosphereProfile;

import java.util.ArrayList;
import java.util.List;

public class PresetPackEntry {
    public String id = "";
    public String displayName = "";
    public String description = "";
    public AtmosphereProfile snapshot = new AtmosphereProfile("Preset Snapshot");
    public List<String> tags = new ArrayList<>();
}
