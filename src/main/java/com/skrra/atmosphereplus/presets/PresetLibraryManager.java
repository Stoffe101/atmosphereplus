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
import java.util.Set;
import java.util.function.Consumer;

public final class PresetLibraryManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CUSTOM_PRESETS_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("atmosphereplus-presets.json");

    private static final Map<String, BuiltInPreset> BUILT_INS = new LinkedHashMap<>();
    private static final Map<String, CustomPresetData> CUSTOM_PRESETS = new LinkedHashMap<>();
    private static final Set<String> NETHER_PRESET_IDS = Set.of(
            "dark_crimson",
            "nether_clear",
            "lava_bloom",
            "basalt_ash",
            "soul_haze",
            "nether_horror"
    );
    private static final Set<String> END_PRESET_IDS = Set.of(
            "void_purple",
            "end_clear",
            "chorus_dream",
            "dragon_night",
            "celestial_void"
    );
    private static final Set<String> CAVE_FRIENDLY_PRESET_IDS = Set.of(
            "bright_caves",
            "nether_clear",
            "end_clear",
            "sodium_iris_safe",
            "performance_clear"
    );

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
        List<PresetReference> refs = new ArrayList<>();
        for (BuiltInPreset preset : BUILT_INS.values()) {
            if (!preset.dimension()) {
                refs.add(preset.reference());
            }
        }
        refs.sort(Comparator.comparing(PresetReference::displayName, String.CASE_INSENSITIVE_ORDER));
        return refs;
    }

    public static List<PresetReference> dimensionPresetsSorted() {
        List<PresetReference> refs = new ArrayList<>();
        for (BuiltInPreset preset : BUILT_INS.values()) {
            if (preset.dimension()) {
                refs.add(preset.reference());
            }
        }
        refs.sort(Comparator.comparing(PresetReference::displayName, String.CASE_INSENSITIVE_ORDER));
        return refs;
    }

    public static List<PresetReference> netherPresetsSorted() {
        return builtInsForIdsSorted(NETHER_PRESET_IDS);
    }

    public static List<PresetReference> endPresetsSorted() {
        return builtInsForIdsSorted(END_PRESET_IDS);
    }

    public static List<PresetReference> caveFriendlyPresetsSorted() {
        return builtInsForIdsSorted(CAVE_FRIENDLY_PRESET_IDS);
    }

    public static boolean isNetherPreset(String id) {
        return NETHER_PRESET_IDS.contains(id);
    }

    public static boolean isEndPreset(String id) {
        return END_PRESET_IDS.contains(id);
    }

    public static boolean isCaveFriendlyPreset(String id) {
        return CAVE_FRIENDLY_PRESET_IDS.contains(id);
    }

    public static List<String> tagsForPreset(String id) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        PresetReference ref = reference(id);
        if (ref == null) {
            return new ArrayList<>();
        }

        if (ref.custom()) {
            CustomPresetData data = CUSTOM_PRESETS.get(id);
            if (data != null && data.tags != null) {
                tags.addAll(data.tags);
            }
            tags.add("CUSTOM");
        } else {
            tags.add("BUILT_IN");
            if (isNetherPreset(id)) {
                tags.add("NETHER");
                tags.add("DIMENSION");
            } else if (isEndPreset(id)) {
                tags.add("END");
                tags.add("DIMENSION");
            } else {
                tags.add("OVERWORLD");
            }

            if (isCaveFriendlyPreset(id)) {
                tags.add("CAVE");
                tags.add("VISIBILITY");
            }

            if (id.contains("horror")) {
                tags.add("HORROR");
            }
            if (id.contains("void") || id.contains("chorus") || id.contains("celestial")) {
                tags.add("FANTASY");
                tags.add("COSMIC");
            }
            if (id.contains("safe") || id.contains("shader")) {
                tags.add("SAFE");
                tags.add("SHADER_FRIENDLY");
            }
            if (id.contains("cinematic") || id.contains("misty") || id.contains("moonlit")) {
                tags.add("CINEMATIC");
            }
        }

        return new ArrayList<>(tags);
    }

    private static List<PresetReference> builtInsForIdsSorted(Set<String> ids) {
        List<PresetReference> refs = new ArrayList<>();
        for (String id : ids) {
            BuiltInPreset preset = BUILT_INS.get(id);
            if (preset != null) {
                refs.add(preset.reference());
            }
        }
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
            neutralColorGrade(config);
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

    public static PresetImportResult importCustomPresets(List<CustomPresetData> presets) {
        if (presets == null || presets.isEmpty()) {
            return new PresetImportResult(0, false, false);
        }

        List<CustomPresetData> staged = new ArrayList<>();
        LinkedHashSet<String> stagedIds = new LinkedHashSet<>();
        LinkedHashSet<String> stagedNames = new LinkedHashSet<>();
        boolean renamedIds = false;
        boolean renamedNames = false;
        for (CustomPresetData source : presets) {
            if (!isValidCustomPreset(source)) {
                continue;
            }

            CustomPresetData data = new CustomPresetData();
            String requestedId = slug(source.id == null || source.id.isBlank() ? source.displayName : source.id);
            String requestedName = source.displayName == null || source.displayName.isBlank() ? "Imported Preset" : source.displayName.trim();
            data.id = uniqueId(requestedId, stagedIds);
            data.displayName = uniqueDisplayName(requestedName, stagedNames);
            renamedIds = renamedIds || !data.id.equals(requestedId);
            renamedNames = renamedNames || !data.displayName.equals(requestedName);
            data.description = source.description == null || source.description.isBlank()
                    ? "Imported preset."
                    : source.description;
            data.snapshot = AtmosphereProfile.copyOf(source.snapshot);
            data.tags = source.tags == null ? new ArrayList<>() : new ArrayList<>(source.tags);
            if (!data.tags.contains("CUSTOM")) {
                data.tags.add("CUSTOM");
            }
            staged.add(data);
            stagedIds.add(data.id);
            stagedNames.add(data.displayName.toLowerCase(Locale.ROOT));
        }

        if (staged.isEmpty()) {
            return new PresetImportResult(0, false, false);
        }

        for (CustomPresetData data : staged) {
            CUSTOM_PRESETS.put(data.id, data);
        }
        saveCustomPresets();
        return new PresetImportResult(staged.size(), renamedIds, renamedNames);
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

    private static String uniqueDisplayName(String name) {
        return uniqueDisplayName(name, Set.of());
    }

    private static String uniqueDisplayName(String name, Set<String> reservedNames) {
        String cleaned = name == null || name.isBlank() ? "Imported Preset" : name.trim();
        if (!containsDisplayName(cleaned) && !reservedNames.contains(cleaned.toLowerCase(Locale.ROOT))) {
            return cleaned;
        }

        String candidate = cleaned + " Imported";
        if (!containsDisplayName(candidate) && !reservedNames.contains(candidate.toLowerCase(Locale.ROOT))) {
            return candidate;
        }

        int suffix = 2;
        candidate = cleaned + " Copy";
        while (containsDisplayName(candidate) || reservedNames.contains(candidate.toLowerCase(Locale.ROOT))) {
            candidate = cleaned + " Copy " + suffix++;
        }
        return candidate;
    }

    private static String uniqueId(String base) {
        return uniqueId(base, Set.of());
    }

    private static String uniqueId(String base, Set<String> reservedIds) {
        String cleaned = base == null || base.isBlank() ? "custom_preset" : base;
        String candidate = cleaned;
        int suffix = 2;
        while (BUILT_INS.containsKey(candidate) || CUSTOM_PRESETS.containsKey(candidate) || reservedIds.contains(candidate)) {
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
        addDimension("dark_crimson", "Dark Crimson", "Low, moody Nether contrast.", IconType.LIGHTING, c -> {
            c.timeOverride = false;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 0.42f;
            c.fogDensity = 1.75f;
            c.gamma = 0.80f;
            c.particleAmount = 0.85f;
            c.submersionFogOff = false;
            c.lowFire = false;
            mood(c, 0.58f, 0.70f, 0.50f, 0.45f, 0.30f);
            colorGrade(c, 0.80f, 0.08f, 0.05f, 0.58f, 0.82f, 1.24f, 1.30f, 0.34f);
        });
        addDimension("nether_clear", "Nether Clear", "Better Nether visibility without fullbright.", IconType.FOG, c -> {
            c.timeOverride = false;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 1.65f;
            c.fogDensity = 0.38f;
            c.gamma = 1.28f;
            c.fullbright = false;
            c.particleAmount = 0.55f;
            c.submersionFogOff = true;
            c.lowFire = true;
            mood(c, 0.50f, 1.0f, 1.25f, 0.55f, 0.65f);
            colorGrade(c, 1.0f, 0.55f, 0.32f, 0.20f, 1.12f, 1.03f, 1.05f, 0.08f);
        });
        addDimension("lava_bloom", "Lava Bloom", "Warm glowing Nether visibility.", IconType.LIGHTING, c -> {
            c.timeOverride = false;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 0.95f;
            c.fogDensity = 0.85f;
            c.gamma = 1.18f;
            c.particleAmount = 0.95f;
            c.submersionFogOff = true;
            c.lowFire = true;
            mood(c, 0.72f, 1.0f, 1.12f, 0.75f, 0.55f);
            colorGrade(c, 1.0f, 0.42f, 0.10f, 0.46f, 1.08f, 1.16f, 1.35f, 0.16f);
        });
        addDimension("basalt_ash", "Basalt Ash", "Smoky basalt delta mood.", IconType.FOG, c -> {
            c.timeOverride = false;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 0.55f;
            c.fogDensity = 1.55f;
            c.gamma = 0.82f;
            c.particleAmount = 1.05f;
            c.submersionFogOff = false;
            c.lowFire = false;
            mood(c, 0.65f, 1.0f, 0.58f, 0.40f, 0.28f);
            colorGrade(c, 0.45f, 0.43f, 0.40f, 0.36f, 0.82f, 1.18f, 0.55f, 0.22f);
        });
        addDimension("soul_haze", "Soul Haze", "Cooler soul valley haze.", IconType.FOG, c -> {
            c.timeOverride = false;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 1.10f;
            c.fogDensity = 0.75f;
            c.gamma = 0.92f;
            c.particleAmount = 0.45f;
            c.submersionFogOff = false;
            c.lowFire = false;
            mood(c, 0.62f, 1.0f, 0.74f, 0.95f, 0.42f);
            colorGrade(c, 0.18f, 0.70f, 0.85f, 0.42f, 0.88f, 1.10f, 0.88f, 0.16f);
        });
        addDimension("nether_horror", "Nether Horror", "Very dark spooky Nether fog.", IconType.ADVANCED, c -> {
            c.timeOverride = false;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 0.28f;
            c.fogDensity = 1.95f;
            c.gamma = 0.58f;
            c.particleAmount = 0.80f;
            c.submersionFogOff = false;
            c.lowFire = false;
            mood(c, 0.50f, 1.0f, 0.32f, 0.20f, 0.20f);
            colorGrade(c, 0.65f, 0.02f, 0.02f, 0.70f, 0.62f, 1.42f, 1.25f, 0.54f);
        });
        addDimension("void_purple", "Void Purple", "Sparse, dim End ambience.", IconType.TIME, c -> {
            c.timeOverride = true;
            c.visualTime = 18000;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 1.35f;
            c.fogDensity = 0.55f;
            c.gamma = 1.15f;
            c.particleAmount = 0.70f;
            c.submersionFogOff = false;
            c.lowFire = false;
            mood(c, 0.30f, 1.0f, 0.78f, 2.0f, 0.22f);
            colorGrade(c, 0.58f, 0.12f, 1.0f, 0.58f, 0.82f, 1.22f, 1.24f, 0.24f);
        });
        addDimension("end_clear", "End Clear", "Cleaner visibility in the End.", IconType.SKY, c -> {
            c.timeOverride = true;
            c.visualTime = 18000;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 1.75f;
            c.fogDensity = 0.35f;
            c.gamma = 1.25f;
            c.particleAmount = 0.35f;
            c.submersionFogOff = false;
            c.lowFire = false;
            mood(c, 0.18f, 1.0f, 1.18f, 1.85f, 0.52f);
            colorGrade(c, 0.66f, 0.58f, 1.0f, 0.26f, 1.20f, 1.04f, 1.12f, 0.04f);
        });
        addDimension("chorus_dream", "Chorus Dream", "Soft surreal purple End haze.", IconType.TIME, c -> {
            c.timeOverride = true;
            c.visualTime = 18000;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 1.05f;
            c.fogDensity = 0.82f;
            c.gamma = 1.12f;
            c.particleAmount = 0.55f;
            c.submersionFogOff = false;
            c.lowFire = false;
            mood(c, 0.36f, 1.0f, 0.96f, 2.0f, 0.34f);
            colorGrade(c, 1.0f, 0.24f, 0.92f, 0.56f, 1.08f, 0.86f, 1.36f, 0.08f);
        });
        addDimension("dragon_night", "Dragon Night", "Dark boss-fight End ambience.", IconType.LIGHTING, c -> {
            c.timeOverride = true;
            c.visualTime = 18000;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 0.70f;
            c.fogDensity = 1.25f;
            c.gamma = 0.78f;
            c.particleAmount = 0.75f;
            c.submersionFogOff = false;
            c.lowFire = false;
            mood(c, 0.32f, 1.0f, 0.50f, 1.55f, 0.16f);
            colorGrade(c, 0.26f, 0.10f, 0.78f, 0.62f, 0.70f, 1.40f, 1.16f, 0.34f);
        });
        addDimension("celestial_void", "Celestial Void", "Bright cosmic End clarity.", IconType.SKY, c -> {
            c.timeOverride = true;
            c.visualTime = 18000;
            c.weatherOverride = false;
            c.fogOverride = true;
            c.fogDistance = 1.85f;
            c.fogDensity = 0.30f;
            c.gamma = 1.38f;
            c.particleAmount = 0.25f;
            c.submersionFogOff = false;
            c.lowFire = false;
            mood(c, 0.14f, 1.0f, 1.32f, 2.0f, 0.72f);
            colorGrade(c, 0.32f, 0.70f, 1.0f, 0.42f, 1.24f, 0.98f, 1.34f, 0.08f);
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
        BUILT_INS.put(id, new BuiltInPreset(id, displayName, description, icon, false, apply));
    }

    private static void addDimension(String id, String displayName, String description, IconType icon, Consumer<AtmosphereConfig> apply) {
        BUILT_INS.put(id, new BuiltInPreset(id, displayName, description, icon, true, apply));
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

    private static void colorGrade(AtmosphereConfig config, float red, float green, float blue, float strength, float brightness, float contrast, float saturation, float vignette) {
        config.moodOverlayEnabled = strength > 0.001f
                || Math.abs(brightness - 1.0f) > 0.001f
                || Math.abs(contrast - 1.0f) > 0.001f
                || Math.abs(saturation - 1.0f) > 0.001f
                || vignette > 0.001f;
        config.moodOverlayRed = red;
        config.moodOverlayGreen = green;
        config.moodOverlayBlue = blue;
        config.moodOverlayStrength = strength;
        config.moodBrightness = brightness;
        config.moodContrast = contrast;
        config.moodSaturation = saturation;
        config.moodVignetteStrength = vignette;
    }

    private static void neutralColorGrade(AtmosphereConfig config) {
        config.moodOverlayEnabled = false;
        config.moodOverlayRed = 1.0f;
        config.moodOverlayGreen = 1.0f;
        config.moodOverlayBlue = 1.0f;
        config.moodOverlayStrength = 0.0f;
        config.moodBrightness = 1.0f;
        config.moodContrast = 1.0f;
        config.moodSaturation = 1.0f;
        config.moodVignetteStrength = 0.0f;
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

    private record BuiltInPreset(String id, String displayName, String description, IconType icon, boolean dimension, Consumer<AtmosphereConfig> apply) {
        private void applyTo(AtmosphereConfig config) {
            apply.accept(config);
        }

        private PresetReference reference() {
            return new PresetReference(id, displayName, description, icon, false, dimension);
        }
    }

    private static class CustomPresetFile {
        List<CustomPresetData> presets = new ArrayList<>();
    }

    public record PresetImportResult(int count, boolean renamedIds, boolean renamedNames) {
    }
}
