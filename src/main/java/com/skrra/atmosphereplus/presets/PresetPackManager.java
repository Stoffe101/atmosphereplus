package com.skrra.atmosphereplus.presets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skrra.atmosphereplus.client.AtmospherePlusClient;
import com.skrra.atmosphereplus.config.AtmosphereProfile;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class PresetPackManager {
    public static final int FORMAT_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PACK_FOLDER = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("atmosphereplus-preset-packs");

    private PresetPackManager() {
    }

    public static Path packFolder() {
        ensureFolder();
        return PACK_FOLDER;
    }

    public static List<Path> availablePackFiles() {
        ensureFolder();
        List<Path> files = new ArrayList<>();
        try (var stream = Files.list(PACK_FOLDER)) {
            stream.filter(path -> Files.isRegularFile(path)
                            && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .forEach(files::add);
        } catch (IOException ignored) {
        }
        return files;
    }

    public static ExportResult exportPack(String packName, String author, String description, List<String> presetIds) {
        ensureFolder();
        List<PresetPackEntry> entries = new ArrayList<>();
        for (String presetId : presetIds) {
            PresetReference ref = PresetLibraryManager.reference(presetId);
            AtmosphereProfile snapshot = PresetLibraryManager.snapshotForPreset(presetId);
            if (ref == null || snapshot == null) {
                continue;
            }

            PresetPackEntry entry = new PresetPackEntry();
            entry.id = ref.id();
            entry.displayName = ref.displayName();
            entry.description = ref.description();
            entry.snapshot = AtmosphereProfile.copyOf(snapshot);
            entry.tags = PresetLibraryManager.tagsForPreset(ref.id());
            entries.add(entry);
        }

        if (entries.isEmpty()) {
            return new ExportResult(false, "", null, 0, "No presets selected");
        }

        PresetPackData pack = new PresetPackData();
        pack.formatVersion = FORMAT_VERSION;
        pack.packName = cleanText(packName, "Atmosphere+ Preset Pack");
        pack.author = cleanText(author, "");
        pack.description = cleanText(description, "");
        pack.createdWith = AtmospherePlusClient.MOD_NAME + " " + AtmospherePlusClient.VERSION;
        pack.presets = entries;

        String filename = uniqueFilename(slug(pack.packName));
        Path path = PACK_FOLDER.resolve(filename);
        try {
            Files.writeString(path, GSON.toJson(pack));
            return new ExportResult(true, filename, path, entries.size(), "");
        } catch (IOException ex) {
            return new ExportResult(false, filename, path, 0, "Could not write preset pack");
        }
    }

    public static PackPreview preview(Path path) {
        List<String> warnings = new ArrayList<>();
        if (path == null || !Files.exists(path) || !Files.isRegularFile(path)) {
            warnings.add("File not found.");
            return new PackPreview(path, fileName(path), null, List.of(), warnings, false);
        }

        PresetPackData pack;
        try {
            pack = GSON.fromJson(Files.readString(path), PresetPackData.class);
        } catch (Exception ex) {
            warnings.add("Broken JSON.");
            return new PackPreview(path, fileName(path), null, List.of(), warnings, false);
        }

        if (pack == null) {
            warnings.add("Empty preset pack.");
            return new PackPreview(path, fileName(path), null, List.of(), warnings, false);
        }

        if (pack.formatVersion != FORMAT_VERSION) {
            warnings.add("Unsupported format version: " + pack.formatVersion + ".");
        }

        if (pack.packName == null || pack.packName.isBlank()) {
            warnings.add("Missing pack name.");
            pack.packName = "Unnamed Preset Pack";
        }

        List<PresetPackEntry> validEntries = new ArrayList<>();
        if (pack.presets == null || pack.presets.isEmpty()) {
            warnings.add("No presets in pack.");
        } else {
            for (PresetPackEntry entry : pack.presets) {
                if (isValidEntry(entry)) {
                    validEntries.add(entry);
                } else {
                    warnings.add("Skipped an invalid preset entry.");
                }
            }
        }

        boolean valid = pack.formatVersion == FORMAT_VERSION && !validEntries.isEmpty();
        return new PackPreview(path, fileName(path), pack, validEntries, warnings, valid);
    }

    public static ImportResult importPack(PackPreview preview) {
        if (preview == null || !preview.valid()) {
            return new ImportResult(false, 0, "Invalid preset pack");
        }

        List<CustomPresetData> staged = new ArrayList<>();
        for (PresetPackEntry entry : preview.validPresets()) {
            CustomPresetData data = new CustomPresetData();
            data.id = entry.id == null || entry.id.isBlank() ? entry.displayName : entry.id;
            data.displayName = entry.displayName;
            data.description = entry.description == null || entry.description.isBlank()
                    ? "Imported from " + preview.packName() + "."
                    : entry.description;
            data.snapshot = AtmosphereProfile.copyOf(entry.snapshot);
            data.tags = entry.tags == null ? new ArrayList<>() : new ArrayList<>(entry.tags);
            if (!data.tags.contains("CUSTOM")) {
                data.tags.add("CUSTOM");
            }
            staged.add(data);
        }

        int imported = PresetLibraryManager.importCustomPresets(staged);
        if (imported <= 0) {
            return new ImportResult(false, 0, "No presets imported");
        }

        return new ImportResult(true, imported, "");
    }

    private static boolean isValidEntry(PresetPackEntry entry) {
        return entry != null
                && entry.displayName != null
                && !entry.displayName.isBlank()
                && entry.snapshot != null;
    }

    private static void ensureFolder() {
        try {
            Files.createDirectories(PACK_FOLDER);
        } catch (IOException ignored) {
        }
    }

    private static String uniqueFilename(String slug) {
        String base = slug == null || slug.isBlank() ? "preset-pack" : slug;
        String candidate = base + ".json";
        int suffix = 2;
        while (Files.exists(PACK_FOLDER.resolve(candidate))) {
            candidate = base + "-" + suffix++ + ".json";
        }
        return candidate;
    }

    private static String slug(String value) {
        String result = value == null ? "preset-pack" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        result = result.replaceAll("^-+|-+$", "");
        return result.isBlank() ? "preset-pack" : result;
    }

    private static String cleanText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String fileName(Path path) {
        return path == null ? "" : path.getFileName().toString();
    }

    public record ExportResult(boolean success, String fileName, Path path, int count, String message) {
    }

    public record ImportResult(boolean success, int count, String message) {
    }

    public record PackPreview(Path path, String fileName, PresetPackData pack, List<PresetPackEntry> validPresets, List<String> warnings, boolean valid) {
        public String packName() {
            return pack == null || pack.packName == null || pack.packName.isBlank() ? fileName : pack.packName;
        }

        public String author() {
            return pack == null || pack.author == null ? "" : pack.author;
        }

        public String description() {
            return pack == null || pack.description == null ? "" : pack.description;
        }
    }
}
