package com.skrra.atmosphereplus.themes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skrra.atmosphereplus.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CustomThemeManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "atmosphereplus-themes.json";
    private static final String EXPORT_FILE_NAME = "atmosphereplus-theme-export.json";
    private static final String IMPORT_FILE_NAME = "atmosphereplus-theme-import.json";
    private static final int MAX_NAME_LENGTH = 28;
    private static final Map<String, CustomThemeData> CUSTOM_THEMES = new LinkedHashMap<>();

    private CustomThemeManager() {
    }

    public static void load() {
        CUSTOM_THEMES.clear();
        Path path = path();

        if (!Files.exists(path)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            Storage storage = GSON.fromJson(reader, Storage.class);
            if (storage == null || storage.themes == null) {
                return;
            }

            for (CustomThemeData data : storage.themes) {
                CustomThemeData repaired = sanitize(data);
                if (repaired != null) {
                    CUSTOM_THEMES.put(repaired.id, repaired);
                }
            }
        } catch (Exception exception) {
            CUSTOM_THEMES.clear();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(path().getParent());
            try (Writer writer = Files.newBufferedWriter(path())) {
                Storage storage = new Storage();
                storage.themes = CUSTOM_THEMES.values().toArray(new CustomThemeData[0]);
                GSON.toJson(storage, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static Map<String, CustomThemeData> all() {
        return CUSTOM_THEMES;
    }

    public static boolean hasCustomThemes() {
        return !CUSTOM_THEMES.isEmpty();
    }

    public static boolean isCustomTheme(String id) {
        return id != null && CUSTOM_THEMES.containsKey(id);
    }

    public static CustomThemeData get(String id) {
        return CUSTOM_THEMES.get(id);
    }

    public static CustomThemeData createTheme(String name, Theme baseTheme) {
        Theme base = baseTheme == null ? ThemeManager.defaultTheme() : baseTheme;
        CustomThemeData data = new CustomThemeData(nextId(name), cleanName(name, "Custom Theme"), base);
        CUSTOM_THEMES.put(data.id, data);
        save();
        ThemeManager.refreshCustomThemes();
        return data;
    }

    public static CustomThemeData duplicateTheme(String sourceId, String name) {
        Theme source = ThemeManager.byId(sourceId);
        if (source == null) {
            source = ThemeManager.current();
        }

        String fallbackName = source == null ? "Custom Theme Copy" : source.displayName() + " Copy";
        return createTheme(cleanName(name, fallbackName), source);
    }

    public static boolean renameTheme(String id, String newName) {
        CustomThemeData data = CUSTOM_THEMES.get(id);
        if (data == null) {
            return false;
        }

        data.displayName = cleanName(newName, data.displayName);
        save();
        ThemeManager.refreshCustomThemes();
        return true;
    }

    public static boolean saveTheme(CustomThemeData data) {
        CustomThemeData repaired = sanitize(data);
        if (repaired == null) {
            return false;
        }

        CUSTOM_THEMES.put(repaired.id, repaired);
        save();
        ThemeManager.refreshCustomThemes();
        return true;
    }

    public static boolean deleteTheme(String id) {
        if (id == null || CUSTOM_THEMES.remove(id) == null) {
            return false;
        }

        if (id.equals(ConfigManager.get().theme)) {
            Theme fallback = ThemeManager.defaultTheme();
            ConfigManager.get().theme = fallback == null ? "midnight" : fallback.id();
            ConfigManager.save();
        }

        save();
        ThemeManager.refreshCustomThemes();
        return true;
    }

    public static boolean exportTheme(String id) {
        CustomThemeData data = CUSTOM_THEMES.get(id);
        if (data == null) {
            return false;
        }

        try {
            Path path = exportPath();
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(data, writer);
            }
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    public static ImportResult importTheme() {
        Path path = importPath();
        if (!Files.exists(path)) {
            return ImportResult.missing();
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            CustomThemeData imported = GSON.fromJson(reader, CustomThemeData.class);
            CustomThemeData repaired = sanitize(imported);
            if (repaired == null) {
                return ImportResult.invalid();
            }

            repaired.id = uniqueId(repaired.id, repaired.displayName);
            repaired.displayName = cleanName(repaired.displayName, "Imported Theme");
            CUSTOM_THEMES.put(repaired.id, repaired);
            save();
            ThemeManager.refreshCustomThemes();
            return ImportResult.success(repaired);
        } catch (Exception exception) {
            return ImportResult.invalid();
        }
    }

    private static CustomThemeData sanitize(CustomThemeData data) {
        if (data == null || data.id == null || data.id.isBlank()) {
            return null;
        }

        String id = data.id.trim();
        if (ThemeManager.isBuiltInTheme(id)) {
            id = nextId(data.displayName);
        }

        Theme fallback = ThemeManager.defaultTheme();
        CustomThemeData repaired = new CustomThemeData();
        repaired.id = id;
        repaired.displayName = cleanName(data.displayName, "Custom Theme");
        repaired.background = repairColor(data.background, fallback.background());
        repaired.panel = repairColor(data.panel, fallback.panel());
        repaired.panelAlt = repairColor(data.panelAlt, fallback.panelAlt());
        repaired.accent = repairColor(data.accent, fallback.accent());
        repaired.accentSoft = repairColor(data.accentSoft, fallback.accentSoft());
        repaired.text = repairColor(data.text, fallback.text());
        repaired.mutedText = repairColor(data.mutedText, fallback.mutedText());
        repaired.border = repairColor(data.border, fallback.border());
        return repaired;
    }

    private static int repairColor(int color, int fallback) {
        return color == 0 ? fallback : color;
    }

    private static String cleanName(String name, String fallback) {
        String cleaned = name == null ? "" : name.trim();
        if (cleaned.isBlank()) {
            cleaned = fallback == null || fallback.isBlank() ? "Custom Theme" : fallback.trim();
        }

        if (cleaned.length() > MAX_NAME_LENGTH) {
            cleaned = cleaned.substring(0, MAX_NAME_LENGTH);
        }

        return cleaned;
    }

    private static String nextId(String name) {
        String base = cleanName(name, "custom_theme")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        if (base.isBlank()) {
            base = "custom_theme";
        }

        String id = "custom_" + base;
        int suffix = 2;
        while (ThemeManager.isBuiltInTheme(id) || CUSTOM_THEMES.containsKey(id)) {
            id = "custom_" + base + "_" + suffix;
            suffix++;
        }

        return id;
    }

    private static String uniqueId(String requestedId, String name) {
        String base = requestedId == null ? "" : requestedId.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9_]+", "_")
                .replaceAll("^_+|_+$", "");

        if (base.isBlank()) {
            base = nextId(name);
        }

        String id = base;
        int suffix = 2;
        while (ThemeManager.isBuiltInTheme(id) || CUSTOM_THEMES.containsKey(id)) {
            id = base + "_" + suffix;
            suffix++;
        }

        return id;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private static Path exportPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(EXPORT_FILE_NAME);
    }

    private static Path importPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(IMPORT_FILE_NAME);
    }

    public record ImportResult(Status status, CustomThemeData theme) {
        public static ImportResult success(CustomThemeData theme) {
            return new ImportResult(Status.SUCCESS, theme);
        }

        public static ImportResult missing() {
            return new ImportResult(Status.MISSING_FILE, null);
        }

        public static ImportResult invalid() {
            return new ImportResult(Status.INVALID_FILE, null);
        }
    }

    public enum Status {
        SUCCESS,
        MISSING_FILE,
        INVALID_FILE
    }

    private static final class Storage {
        private CustomThemeData[] themes = new CustomThemeData[0];
    }
}
