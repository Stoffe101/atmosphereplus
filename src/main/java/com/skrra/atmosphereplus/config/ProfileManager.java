package com.skrra.atmosphereplus.config;

import com.skrra.atmosphereplus.util.NotificationUtil;

public final class ProfileManager {
    public static final int PROFILE_COUNT = 5;

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

        profile.applyTo(ConfigManager.get());
        ConfigManager.get().activePreset = "profile_" + index;
        ConfigManager.save();
        NotificationUtil.show("Loaded " + profile.name);
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
        ConfigManager.get().profiles[index] = new AtmosphereProfile("Profile " + (index + 1));
        if (("profile_" + index).equals(ConfigManager.get().activePreset)) {
            ConfigManager.get().activePreset = "";
        }
        ConfigManager.save();
        NotificationUtil.show("Cleared Profile " + (index + 1));
    }

    public static boolean isActive(int index) {
        return ("profile_" + index).equals(ConfigManager.get().activePreset);
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
