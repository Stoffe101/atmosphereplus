package com.skrra.atmosphereplus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skrra.atmosphereplus.automation.BiomeAtmosphereManager;
import com.skrra.atmosphereplus.transitions.TransitionManager;
import com.skrra.atmosphereplus.transitions.TransitionRequest;
import com.skrra.atmosphereplus.transitions.TransitionSpeed;
import com.skrra.atmosphereplus.util.NotificationUtil;
import com.skrra.atmosphereplus.util.SafeFileIo;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class ProfileManager {
    public static final int PROFILE_COUNT = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger("atmosphereplus");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String LEGACY_BACKUP_FILE_NAME = "atmosphereplus-profiles-backup.json";
    private static final String BACKUP_PREFIX = "atmosphereplus-profiles-backup-";
    private static final String PRE_IMPORT_BACKUP_PREFIX = "atmosphereplus-profiles-before-import-";

    private ProfileManager() {
    }

    public static AtmosphereProfile[] profiles() {
        ensureProfiles();
        return ConfigManager.get().profiles;
    }

    public static AtmosphereProfile profile(int index) {
        ensureProfiles();
        return ConfigManager.get().profiles[index];
    }

    public static void saveCurrentTo(int index) {
        AtmosphereProfile profile = profile(index);
        if (profile.name == null || profile.name.isBlank()) {
            profile.name = "Profile " + (index + 1);
        }
        profile.capture(ConfigManager.get());
        ConfigManager.get().activePreset = "profile_" + index;
        ConfigManager.save();
        NotificationUtil.show("Saved " + profile.name);
    }

    public static void load(int index) {
        AtmosphereProfile profile = profile(index);
        if (!profile.saved) {
            NotificationUtil.show(profile.name + " is empty");
            return;
        }

        BiomeAtmosphereManager.onManualAtmosphereChange();
        TransitionManager.cancelTransition();
        profile.applyTo(ConfigManager.get());
        ConfigManager.get().activePreset = "profile_" + index;
        ConfigManager.save();
        NotificationUtil.show("Loaded " + profile.name);
    }

    public static void transitionTo(int index) {
        AtmosphereProfile profile = profile(index);
        if (!profile.saved) {
            NotificationUtil.show(profile.name + " is empty");
            return;
        }

        BiomeAtmosphereManager.onManualAtmosphereChange();
        if (TransitionManager.transitionTo(new TransitionRequest("profile_" + index, AtmosphereProfile.copyOf(profile), TransitionSpeed.NORMAL))) {
            ConfigManager.get().lastQuickProfile = index;
            ConfigManager.save();
            NotificationUtil.show("Transitioning to " + profile.name);
        }
    }

    public static void rename(int index, String newName) {
        AtmosphereProfile profile = profile(index);
        String cleaned = newName == null ? "" : newName.trim();

        if (cleaned.isBlank()) {
            cleaned = "Profile " + (index + 1);
        }

        if (cleaned.length() > 24) {
            cleaned = cleaned.substring(0, 24);
        }

        profile.name = cleaned;
        ConfigManager.save();
        NotificationUtil.show("Renamed to " + cleaned);
    }

    public static void clear(int index) {
        String oldName = profile(index).name;
        ConfigManager.get().profiles[index] = new AtmosphereProfile("Profile " + (index + 1));
        if (("profile_" + index).equals(ConfigManager.get().activePreset)) {
            ConfigManager.get().activePreset = "";
        }
        ConfigManager.save();
        NotificationUtil.show("Cleared " + (oldName == null || oldName.isBlank() ? "Profile " + (index + 1) : oldName));
    }

    public static void clearAll() {
        ConfigManager.get().profiles = AtmosphereProfile.defaults();
        if (ConfigManager.get().activePreset != null && ConfigManager.get().activePreset.startsWith("profile_")) {
            ConfigManager.get().activePreset = "";
        }
        ConfigManager.save();
        NotificationUtil.show("Cleared all profiles");
    }

    public static void exportProfiles() {
        ensureProfiles();

        Path path = SafeFileIo.timestampedPath(configDir(), BACKUP_PREFIX, ".json");
        try {
            SafeFileIo.writeString(path, GSON.toJson(ConfigManager.get().profiles));
            NotificationUtil.show("Exported profiles backup");
        } catch (IOException exception) {
            LOGGER.error("Failed to export profiles to {}", path, exception);
            NotificationUtil.show("Failed to export profiles");
        }
    }

    public static void importProfiles() {
        Path path = latestBackupPath();

        if (path == null) {
            NotificationUtil.show("No profile backup found");
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            AtmosphereProfile[] imported = GSON.fromJson(reader, AtmosphereProfile[].class);

            if (imported == null || imported.length == 0) {
                NotificationUtil.show("Profile backup was empty");
                return;
            }

            AtmosphereProfile[] merged = AtmosphereProfile.defaults();
            for (int i = 0; i < Math.min(imported.length, merged.length); i++) {
                if (imported[i] != null) {
                    merged[i] = imported[i];
                }
            }

            backupCurrentProfilesBeforeImport();

            ConfigManager.get().profiles = merged;
            ConfigManager.get().activePreset = "";
            ConfigManager.save();
            ensureProfiles();
            ConfigSafety.repairAndMigrate();
            NotificationUtil.show("Imported profiles backup");
        } catch (Exception exception) {
            LOGGER.warn("Could not read profiles backup from {}", path, exception);
            NotificationUtil.show("Failed to import profiles");
        }
    }

    public static boolean hasBackup() {
        return latestBackupPath() != null;
    }

    public static boolean isActive(int index) {
        return ("profile_" + index).equals(ConfigManager.get().activePreset);
    }

    private static Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    private static Path latestBackupPath() {
        Path newest = null;
        try (var stream = Files.list(configDir())) {
            newest = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.startsWith(BACKUP_PREFIX) && name.endsWith(".json");
                    })
                    .max(Comparator.comparing(path -> path.getFileName().toString()))
                    .orElse(null);
        } catch (IOException exception) {
            LOGGER.warn("Could not list profile backups in {}", configDir(), exception);
        }

        if (newest != null) {
            return newest;
        }

        Path legacy = configDir().resolve(LEGACY_BACKUP_FILE_NAME);
        return Files.exists(legacy) ? legacy : null;
    }

    private static void backupCurrentProfilesBeforeImport() {
        Path path = SafeFileIo.timestampedPath(configDir(), PRE_IMPORT_BACKUP_PREFIX, ".json");
        try {
            SafeFileIo.writeString(path, GSON.toJson(ConfigManager.get().profiles));
            LOGGER.info("Backed up current profiles to {} before import", path);
        } catch (IOException exception) {
            LOGGER.warn("Could not back up current profiles before import", exception);
        }
    }

    private static void ensureProfiles() {
        AtmosphereConfig config = ConfigManager.get();

        if (config.profiles == null || config.profiles.length < PROFILE_COUNT) {
            AtmosphereProfile[] oldProfiles = config.profiles;
            config.profiles = AtmosphereProfile.defaults();

            if (oldProfiles != null) {
                for (int i = 0; i < Math.min(oldProfiles.length, config.profiles.length); i++) {
                    if (oldProfiles[i] != null) {
                        config.profiles[i] = oldProfiles[i];
                    }
                }
            }
        }

        for (int i = 0; i < PROFILE_COUNT; i++) {
            if (config.profiles[i] == null) {
                config.profiles[i] = new AtmosphereProfile("Profile " + (i + 1));
            }
            if (config.profiles[i].name == null || config.profiles[i].name.isBlank()) {
                config.profiles[i].name = "Profile " + (i + 1);
            }
        }
    }
}
