package com.skrra.atmosphereplus.presets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skrra.atmosphereplus.automation.BiomeAtmosphereManager;
import com.skrra.atmosphereplus.config.AtmosphereConfig;
import com.skrra.atmosphereplus.config.AtmosphereProfile;
import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.transitions.TransitionManager;
import com.skrra.atmosphereplus.ui.IconType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public final class PresetLibraryManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CUSTOM_PRESETS_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("atmosphereplus-presets.json");

    private static final Map<String, BuiltInPreset> BUILT_INS = new LinkedHashMap<>();
    private static final Map<String, CustomPresetData> CUSTOM_PRESETS = new LinkedHashMap<>();

    private PresetLibraryManager() {
    }

    public static void init() {
        registerBuiltIns();
        loadCustomPresets();
        cleanFavoritePresetIds();
    }

    public static Collection<PresetReference> builtIns() {
        List<PresetReference> refs = new ArrayList<>();
        for (BuiltInPreset preset : BUILT_INS.values()) {
            refs.add(preset.reference());
        }
        return refs;
    }

    public static Collection<PresetReference> customPresets() {
        List<PresetReference> refs = new ArrayList<>();
        for (CustomPresetData data : CUSTOM_PRESETS.values()) {
            refs.add(toReference(data));
        }
        return refs;
    }

    public static List<PresetReference> allReferences() {
        List<PresetReference> refs = new ArrayList<>();
        refs.addAll(builtIns());
        refs.addAll(customPresets());
        return refs;
    }

    public static List<PresetReference> favorites() {
        List<PresetReference> refs = new ArrayList<>();
        cleanFavoritePresetIds();
        for (String id : ConfigManager.get().favoritePresetIds) {
            PresetReference ref = reference(id);
            if (ref != null) {
                refs.add(ref);
            }
        }
        refs.sort(Comparator.comparing(PresetReference::displayName, String.CASE_INSENSITIVE_ORDER));
        return refs;
    }

    public static List<PresetReference> customPresetsSorted() {
        List<PresetReference> refs = new ArrayList<>(customPresets());
        refs.sort(Comparator.comparing(PresetReference::displayName, String.CASE_INSENSITIVE_ORDER));
        return refs;
    }

    public static List<PresetReference> builtInsSorted() {
        List<PresetReference> refs = new ArrayList<>(builtIns());
        refs.sort(Comparator.comparing(PresetReference::displayName, String.CASE_INSENSITIVE_ORDER));
        return refs;
    }

    public static boolean isFavorite(String id) {
        return id != null && ConfigManager.get().favoritePresetIds != null && ConfigManager.get().favoritePresetIds.contains(id);
    }

    public static void toggleFavorite(String id) {
        if (id == null || id.isBlank() || reference(id) == null) {
            return;
        }

        if (ConfigManager.get().favoritePresetIds == null) {
            ConfigManager.get().favoritePresetIds = new ArrayList<>();
        }

        if (ConfigManager.get().favoritePresetIds.contains(id)) {
            ConfigManager.get().favoritePresetIds.remove(id);
        } else {
            ConfigManager.get().favoritePresetIds.add(id);
        }

        cleanFavoritePresetIds();
        ConfigManager.save();
    }

    public static PresetReference reference(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        BuiltInPreset builtIn = BUILT_INS.get(id);
        if (builtIn != null) {
            return builtIn.reference();
        }

        CustomPresetData custom = CUSTOM_PRESETS.get(id);
        return custom == null ? null : toReference(custom);
    }

    public static boolean exists(String id) {
        return reference(id) != null;
    }

    public static boolean isCustomPreset(String id) {
        return id != null && CUSTOM_PRESETS.containsKey(id);
    }

    public static boolean hasCustomPresets() {
        return !CUSTOM_PRESETS.isEmpty();
    }

    public static boolean applyPreset(String id, boolean automation) {
        if (id == null || id.isBlank()) {
            return false;
        }

        if (!automation) {
            BiomeAtmosphereManager.onManualAtmosphereChange();
            return TransitionManager.transitionTo(id);
        }

        return applyPresetInstant(id);
    }

    public static boolean applyPresetInstant(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }

        BuiltInPreset builtIn = BUILT_INS.get(id);
        if (builtIn != null) {
            AtmosphereConfig config = ConfigManager.get();
            builtIn.applyTo(config);
            config.activePreset = id;
            ConfigManager.save();
            return true;
        }

        CustomPresetData custom = CUSTOM_PRESETS.get(id);
        if (custom != null) {
            custom.applyTo(ConfigManager.get());
            ConfigManager.get().activePreset = id;
            ConfigManager.save();
            return true;
        }

        return false;
    }

    public static AtmosphereProfile snapshotForPreset(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        BuiltInPreset builtIn = BUILT_INS.get(id);
        if (builtIn != null) {
            AtmosphereConfig scratch = new AtmosphereConfig();
            scratch.theme = ConfigManager.get().theme;
            builtIn.applyTo(scratch);
            AtmosphereProfile profile = new AtmosphereProfile(builtIn.displayName());
            profile.capture(scratch);
            return profile;
        }

        CustomPresetData custom = CUSTOM_PRESETS.get(id);
        if (custom != null && custom.snapshot != null) {
            return AtmosphereProfile.copyOf(custom.snapshot);
        }

        return null;
    }

    public static CustomPresetData saveCurrentAsPreset() {
        int next = CUSTOM_PRESETS.size() + 1;
        String name = "My Preset " + next;
        while (containsDisplayName(name)) {
            next++;
            name = "My Preset " + next;
        }

        String id = uniqueId(slug(name));
        CustomPresetData data = new CustomPresetData(id, name, "Saved custom atmosphere.", ConfigManager.get());
        CUSTOM_PRESETS.put(data.id, data);
        saveCustomPresets();
        return data;
    }

    public static CustomPresetData duplicatePreset(String sourceId) {
        PresetReference source = reference(sourceId);
        if (source == null) {
            return null;
        }

        String name = source.displayName() + " Copy";
        String id = uniqueId(slug(name));
        CustomPresetData data = new CustomPresetData();
        data.id = id;
        data.displayName = name;
        data.description = "Editable copy of " + source.displayName() + ".";

        if (source.custom()) {
            CustomPresetData original = CUSTOM_PRESETS.get(sourceId);
            if (original == null || original.snapshot == null) {
                return null;
            }
            data.snapshot = original.snapshot;
        } else {
            BuiltInPreset builtIn = BUILT_INS.get(sourceId);
            if (builtIn == null) {
                return null;
            }
            AtmosphereConfig scratch = new AtmosphereConfig();
            builtIn.applyTo(scratch);
            data.snapshot = new com.skrra.atmosphereplus.config.AtmosphereProfile(name);
            data.snapshot.capture(scratch);
        }

        CUSTOM_PRESETS.put(data.id, data);
        saveCustomPresets();
        return data;
    }

    public static boolean deleteCustomPreset(String id) {
        if (CUSTOM_PRESETS.remove(id) == null) {
            return false;
        }

        if (id.equals(ConfigManager.get().activePreset)) {
            ConfigManager.get().activePreset = "";
            ConfigManager.save();
        }

        saveCustomPresets();
        return true;
    }

    public static void loadCustomPresets() {
        CUSTOM_PRESETS.clear();
        if (!Files.exists(CUSTOM_PRESETS_PATH)) {
            saveCustomPresets();
            return;
        }

        try {
            CustomPresetFile file = GSON.fromJson(Files.readString(CUSTOM_PRESETS_PATH), CustomPresetFile.class);
            if (file == null || file.presets == null) {
                saveCustomPresets();
                return;
            }

            for (CustomPresetData data : file.presets) {
                if (isValidCustomPreset(data)) {
                    data.id = uniqueId(slug(data.id));
                    CUSTOM_PRESETS.put(data.id, data);
                }
            }
        } catch (Exception ignored) {
            CUSTOM_PRESETS.clear();
            saveCustomPresets();
        }
    }

    public static void saveCustomPresets() {
        try {
            Files.createDirectories(CUSTOM_PRESETS_PATH.getParent());
            CustomPresetFile file = new CustomPresetFile();
            file.presets = new ArrayList<>(CUSTOM_PRESETS.values());
            Files.writeString(CUSTOM_PRESETS_PATH, GSON.toJson(file));
        } catch (IOException ignored) {
        }
    }

    public static void cleanFavoritePresetIds() {
        if (ConfigManager.get().favoritePresetIds == null) {
            ConfigManager.get().favoritePresetIds = new ArrayList<>();
        }

        LinkedHashSet<String> cleaned = new LinkedHashSet<>();
        for (String id : ConfigManager.get().favoritePresetIds) {
            if (id != null && reference(id) != null) {
                cleaned.add(id);
            }
        }

        if (!ConfigManager.get().favoritePresetIds.equals(new ArrayList<>(cleaned))) {
            ConfigManager.get().favoritePresetIds = new ArrayList<>(cleaned);
            ConfigManager.save();
        }
    }

    private static boolean isValidCustomPreset(CustomPresetData data) {
        return data != null
                && data.id != null
                && !data.id.isBlank()
                && data.displayName != null
                && !data.displayName.isBlank()
                && data.snapshot != null;
    }

    private static boolean containsDisplayName(String name) {
        for (CustomPresetData data : CUSTOM_PRESETS.values()) {
            if (data.displayName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private static String uniqueId(String base) {
        String cleaned = base == null || base.isBlank() ? "custom_preset" : base;
        String candidate = cleaned;
        int suffix = 2;
        while (BUILT_INS.containsKey(candidate) || CUSTOM_PRESETS.containsKey(candidate)) {
            candidate = cleaned + "_" + suffix++;
        }
        return candidate;
    }

    private static String slug(String value) {
        if (value == null) {
            return "custom_preset";
        }

        String slug = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        slug = slug.replaceAll("^_+|_+$", "");
        return slug.isBlank() ? "custom_preset" : slug;
    }

    private static PresetReference toReference(CustomPresetData data) {
        return new PresetReference(data.id, data.displayName, data.description, IconType.PRESETS, true);
    }

    private static void registerBuiltIns() {
        if (!BUILT_INS.isEmpty()) {
            return;
        }

        add("golden_hour", "Golden Hour", "Warm sunny atmosphere.", IconType.SKY, c -> {
            c.timeOverride = true;
            c.visualTime = 6000;
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.rainIntensity = 0f;
            c.fogOverride = false;
            c.fullbright = false;
            c.gamma = 1.1f;
            mood(c, 0.85f, 1.10f, 1.25f, 0.75f, 1.0f);
        });
        add("warm_desert", "Warm Desert", "Dry heat with a bright sky.", IconType.SKY, c -> {
            c.timeOverride = true;
            c.visualTime = 7000;
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.rainIntensity = 0f;
            c.fogOverride = true;
            c.fogDistance = 1.25f;
            c.fogDensity = 0.55f;
            c.gamma = 1.15f;
            mood(c, 0.55f, 1.20f, 1.35f, 0.55f, 1.0f);
        });
        add("cold_blue", "Cold Blue", "Cool snow-friendly light.", IconType.FOG, c -> {
            c.timeOverride = true;
            c.visualTime = 9000;
            c.weatherOverride = true;
            c.weatherMode = "SNOW";
            c.rainIntensity = 0.35f;
            c.fogOverride = true;
            c.fogDistance = 0.9f;
            c.fogDensity = 1.05f;
            c.gamma = 1.05f;
            mood(c, 0.85f, 0.95f, 0.95f, 0.80f, 0.75f);
        });
        add("dark_crimson", "Dark Crimson", "Low, moody Nether contrast.", IconType.LIGHTING, c -> {
            c.timeOverride = false;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 0.55f;
            c.fogDensity = 1.45f;
            c.gamma = 0.95f;
            mood(c, 0.65f, 0.70f, 0.65f, 0.45f, 0.35f);
        });
        add("void_purple", "Void Purple", "Sparse, dim End ambience.", IconType.TIME, c -> {
            c.timeOverride = true;
            c.visualTime = 18000;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 1.15f;
            c.fogDensity = 0.75f;
            c.gamma = 1.0f;
            mood(c, 0.35f, 1.0f, 0.55f, 1.8f, 0.25f);
        });
        add("misty_morning", "Misty Morning", "Sunrise and soft fog.", IconType.FOG, c -> {
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.rainIntensity = 0.0f;
            c.timeOverride = true;
            c.visualTime = 1000;
            c.freezeVisualTime = true;
            c.fogOverride = true;
            c.fogDistance = 0.75f;
            c.fogDensity = 1.25f;
            c.gamma = 1.05f;
            c.particleAmount = 0.85f;
            c.cloudOverride = true;
            c.cloudMode = "FANCY";
            mood(c, 0.70f, 0.80f, 1.10f, 0.60f, 0.85f);
        });
        add("starlit_night", "Starlit Night", "Clear night visibility.", IconType.TIME, c -> {
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.timeOverride = true;
            c.visualTime = 18000;
            c.freezeVisualTime = true;
            c.fogOverride = true;
            c.fogDistance = 1.35f;
            c.fogDensity = 0.65f;
            c.gamma = 1.15f;
            c.particleAmount = 0.7f;
            c.cloudOverride = true;
            c.cloudMode = "OFF";
            mood(c, 1.0f, 1.0f, 0.65f, 2.0f, 0.45f);
        });
        add("storm_front", "Storm Front", "Heavy storm atmosphere.", IconType.WEATHER, c -> {
            c.weatherOverride = true;
            c.weatherMode = "THUNDER";
            c.rainIntensity = 0.9f;
            c.thunderSounds = true;
            c.timeOverride = true;
            c.visualTime = 13500;
            c.freezeVisualTime = true;
            c.fogOverride = true;
            c.fogDistance = 0.65f;
            c.fogDensity = 1.45f;
            c.gamma = 0.95f;
            c.particleAmount = 1.15f;
            c.cloudOverride = true;
            c.cloudMode = "FANCY";
            mood(c, 0.70f, 0.70f, 0.55f, 1.20f, 0.30f);
        });
        add("moonlit_fog", "Moonlit Fog", "Night with dense fog.", IconType.FOG, c -> {
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.timeOverride = true;
            c.visualTime = 18000;
            c.freezeVisualTime = true;
            c.fogOverride = true;
            c.fogDistance = 0.55f;
            c.fogDensity = 1.6f;
            c.gamma = 1.05f;
            c.particleAmount = 0.8f;
            c.cloudOverride = true;
            c.cloudMode = "FAST";
            mood(c, 0.75f, 0.65f, 0.55f, 1.65f, 0.35f);
        });
        add("screenshot_clear", "Screenshot Clear", "Clean sunny setup.", IconType.SKY, c -> {
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.timeOverride = true;
            c.visualTime = 6000;
            c.freezeVisualTime = true;
            c.fogOverride = false;
            c.gamma = 1.1f;
            c.particleAmount = 0.45f;
            c.cloudOverride = true;
            c.cloudMode = "OFF";
            mood(c, 0.0f, 1.15f, 1.20f, 0.50f, 1.0f);
        });
        add("shader_friendly", "Shader Friendly", "Safer with Iris shaders.", IconType.ADVANCED, c -> {
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.timeOverride = false;
            c.freezeVisualTime = false;
            c.fogOverride = false;
            c.fullbright = false;
            c.gamma = 1.0f;
            c.particleAmount = 0.85f;
            c.cloudOverride = false;
            c.cloudMode = "SERVER";
            neutralRenderer(c);
        });
        add("aurora_night", "Aurora Night", "Stars and dark sky.", IconType.TIME, c -> {
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.timeOverride = true;
            c.visualTime = 18000;
            c.freezeVisualTime = true;
            c.fogOverride = true;
            c.fogDistance = 1.2f;
            c.fogDensity = 0.7f;
            c.gamma = 1.2f;
            c.cloudOverride = true;
            c.cloudMode = "OFF";
            mood(c, 0.45f, 1.25f, 0.60f, 2.0f, 0.40f);
        });
        add("cinematic_sunset", "Cinematic Sunset", "Warm sky and clouds.", IconType.SKY, c -> {
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.timeOverride = true;
            c.visualTime = 12000;
            c.freezeVisualTime = true;
            c.fogOverride = true;
            c.fogDistance = 1.05f;
            c.fogDensity = 0.85f;
            c.gamma = 1.1f;
            c.cloudOverride = true;
            c.cloudMode = "FANCY";
            mood(c, 0.65f, 1.20f, 1.35f, 0.70f, 1.0f);
        });
        add("low_clouds", "Low Clouds", "Experimental cloud feel.", IconType.FOG, c -> {
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.fogOverride = true;
            c.fogDistance = 0.7f;
            c.fogDensity = 1.15f;
            c.cloudOverride = true;
            c.cloudMode = "FANCY";
            c.cloudDistanceOverride = true;
            c.cloudDistance = 8;
            mood(c, 0.82f, 0.45f, 0.90f, 0.90f, 0.60f);
        });
        add("midnight_calm", "Midnight Calm", "Clear calm midnight.", IconType.TIME, c -> {
            c.timeOverride = true;
            c.visualTime = 18000;
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.rainIntensity = 0f;
            mood(c, 0.95f, 1.0f, 0.70f, 1.55f, 0.55f);
        });
        add("cozy_rain", "Cozy Rain", "Rainy afternoon mood.", IconType.WEATHER, c -> {
            c.timeOverride = true;
            c.visualTime = 9000;
            c.weatherOverride = true;
            c.weatherMode = "RAIN";
            c.rainIntensity = 0.35f;
            c.fogOverride = false;
            mood(c, 0.80f, 0.90f, 0.85f, 0.65f, 0.70f);
        });
        add("thunder_night", "Thunder Night", "Dark stormy night.", IconType.LIGHTING, c -> {
            c.timeOverride = true;
            c.visualTime = 18000;
            c.weatherOverride = true;
            c.weatherMode = "THUNDER";
            c.rainIntensity = 0.85f;
            c.fogOverride = true;
            c.fogDistance = 0.65f;
            c.fogDensity = 1.35f;
            mood(c, 0.75f, 0.75f, 0.55f, 1.25f, 0.35f);
        });
        add("deep_fog", "Deep Fog", "Thick screenshot fog.", IconType.FOG, c -> {
            c.fogOverride = true;
            c.fogDistance = 0.35f;
            c.fogDensity = 1.75f;
            mood(c, 0.65f, 0.70f, 0.75f, 0.90f, 0.45f);
        });
        add("bright_caves", "Bright Caves", "Fullbright boost.", IconType.LIGHTING, c -> {
            c.fullbright = true;
            c.gamma = 2.0f;
            neutralRenderer(c);
        });
        add("performance_clear", "Performance Clear", "Cleaner gameplay visuals.", IconType.ADVANCED, c -> {
            c.weatherMode = "SUNNY";
            c.weatherOverride = true;
            c.rainIntensity = 0f;
            c.particleAmount = 0.35f;
            c.fogOverride = true;
            c.fogDistance = 1.5f;
            c.fogDensity = 0.5f;
            neutralRenderer(c);
        });
        add("soft_mist", "Soft Mist", "Light calm fog.", IconType.FOG, c -> {
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.fogOverride = true;
            c.fogDistance = 0.85f;
            c.fogDensity = 1.2f;
            mood(c, 0.72f, 0.85f, 0.95f, 0.80f, 0.65f);
        });
        add("cloudless_clear", "Cloudless Clear", "Clear with clouds off.", IconType.SKY, c -> {
            c.weatherOverride = true;
            c.weatherMode = "SUNNY";
            c.rainIntensity = 0f;
            c.cloudOverride = true;
            c.cloudMode = "OFF";
            c.cloudDistance = 12;
            mood(c, 0.0f, 1.0f, 1.15f, 0.60f, 1.0f);
        });
        add("fancy_clouds", "Fancy Clouds", "Force fancy clouds.", IconType.SKY, c -> {
            c.cloudOverride = true;
            c.cloudMode = "FANCY";
            c.cloudDistance = 12;
            mood(c, 0.95f, 1.0f, 1.0f, 0.80f, 1.0f);
        });
        add("vanilla_safe", "Vanilla Safe", "Restore server-friendly visuals.", IconType.ADVANCED, c -> {
            c.weatherOverride = false;
            c.weatherMode = "SERVER";
            c.timeOverride = false;
            c.visualTime = 6000;
            c.freezeVisualTime = false;
            c.fogOverride = false;
            c.fullbright = false;
            c.gamma = 1.0f;
            c.particleAmount = 1.0f;
            c.cloudOverride = false;
            c.cloudMode = "SERVER";
            neutralRenderer(c);
        });
        add("sodium_iris_safe", "Sodium/Iris Safe", "Minimal shader-sensitive overrides.", IconType.ADVANCED, c -> {
            c.weatherOverride = false;
            c.weatherMode = "SERVER";
            c.timeOverride = false;
            c.freezeVisualTime = false;
            c.fogOverride = false;
            c.fullbright = false;
            c.gamma = 1.0f;
            c.particleAmount = 0.85f;
            c.cloudOverride = false;
            c.cloudMode = "SERVER";
            neutralRenderer(c);
        });
    }

    private static void add(String id, String displayName, String description, IconType icon, Consumer<AtmosphereConfig> apply) {
        BUILT_INS.put(id, new BuiltInPreset(id, displayName, description, icon, apply));
    }

    private static void mood(AtmosphereConfig config, float cloudOpacity, float cloudHeight, float skyBrightness, float starBrightness, float sunVisibility) {
        config.experimentalRendererControls = true;
        config.cloudOpacity = cloudOpacity;
        config.cloudHeight = cloudHeight;
        config.skyBrightness = skyBrightness;
        config.starBrightness = starBrightness;
        config.sunMoonVisibility = sunVisibility;
        config.cloudDistanceOverride = false;
    }

    private static void neutralRenderer(AtmosphereConfig config) {
        config.experimentalRendererControls = false;
        config.cloudOpacity = 1.0f;
        config.cloudHeight = 1.0f;
        config.skyBrightness = 1.0f;
        config.starBrightness = 1.0f;
        config.sunMoonVisibility = 1.0f;
        config.cloudDistanceOverride = false;
    }

    private record BuiltInPreset(String id, String displayName, String description, IconType icon, Consumer<AtmosphereConfig> apply) {
        private void applyTo(AtmosphereConfig config) {
            apply.accept(config);
        }

        private PresetReference reference() {
            return new PresetReference(id, displayName, description, icon, false);
        }
    }

    private static class CustomPresetFile {
        List<CustomPresetData> presets = new ArrayList<>();
    }
}
