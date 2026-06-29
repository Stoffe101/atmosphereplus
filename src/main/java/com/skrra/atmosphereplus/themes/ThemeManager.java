package com.skrra.atmosphereplus.themes;

import com.skrra.atmosphereplus.config.ConfigManager;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ThemeManager {
    private static final Map<String, Theme> BUILT_IN_THEMES = new LinkedHashMap<>();
    private static final Map<String, Theme> THEMES = new LinkedHashMap<>();

    private ThemeManager() {}

    public static void init() {
        BUILT_IN_THEMES.clear();
        THEMES.clear();

        register(new Theme("midnight", "Midnight", 0xE60B1020, 0xE6172038, 0xEE202A46, 0xFF6C8CFF, 0x663F5EFF, 0xFFFFFFFF, 0xFFADB4C7, 0xFF354063));
        register(new Theme("void_purple", "Void Purple", 0xF7000000, 0xF20A0612, 0xF2160D2E, 0xFFA855F7, 0x774C1D95, 0xFFFFFFFF, 0xFFC9B6E8, 0xFF4B286E));
        register(new Theme("black_ice", "Black Ice", 0xF705080D, 0xF20A0F18, 0xF2131D2D, 0xFF67E8F9, 0x6630A9C4, 0xFFFFFFFF, 0xFFB8D7E1, 0xFF245365));
        register(new Theme("amoled", "AMOLED", 0xF5000000, 0xEE080808, 0xEE121212, 0xFF8E5DFF, 0x664D2C91, 0xFFFFFFFF, 0xFFB0B0B0, 0xFF303030));
        register(new Theme("cyber", "Cyber", 0xF5090615, 0xEE120B26, 0xEE20113E, 0xFFFF4FD8, 0x66FF4FD8, 0xFFFFFFFF, 0xFFC8B7E8, 0xFF633399));
        register(new Theme("forest", "Forest", 0xE60A1710, 0xEE12251A, 0xEE1A3324, 0xFF4ADE80, 0x664ADE80, 0xFFFFFFFF, 0xFFC0D5C8, 0xFF315C42));
        register(new Theme("cherry", "Cherry Blossom", 0xE6160A12, 0xEE28101D, 0xEE39172B, 0xFFFF8FC7, 0x66FF8FC7, 0xFFFFFFFF, 0xFFEBC1D7, 0xFF7A3656));
        register(new Theme("nether", "Nether", 0xE61C0808, 0xEE2A1010, 0xEE3A1717, 0xFFFF5C5C, 0x66FF5C5C, 0xFFFFFFFF, 0xFFD8AAAA, 0xFF713030));
        register(new Theme("end", "End", 0xE6130B1D, 0xEE20162C, 0xEE2C1E3D, 0xFFC084FC, 0x66C084FC, 0xFFFFFFFF, 0xFFD6C3EA, 0xFF514066));
        register(new Theme("sunset", "Sunset", 0xE6190D14, 0xEE2A1620, 0xEE3B2130, 0xFFFFB86B, 0x66FF8A3D, 0xFFFFFFFF, 0xFFE5C6B2, 0xFF76503C));
        register(new Theme("ocean", "Ocean", 0xE604111C, 0xEE0B1E31, 0xEE102B45, 0xFF38BDF8, 0x6638BDF8, 0xFFFFFFFF, 0xFFB9D9EA, 0xFF245A7A));
        CustomThemeManager.load();
        refreshCustomThemes();
    }

    private static void register(Theme theme) {
        BUILT_IN_THEMES.put(theme.id(), theme);
        THEMES.put(theme.id(), theme);
    }

    public static Theme current() {
        String id = ConfigManager.get().theme;
        return THEMES.getOrDefault(id, defaultTheme());
    }

    public static Theme defaultTheme() {
        return BUILT_IN_THEMES.get("midnight");
    }

    public static Theme byId(String id) {
        return THEMES.get(id);
    }

    public static Map<String, Theme> all() {
        return THEMES;
    }

    public static Map<String, Theme> builtIns() {
        return BUILT_IN_THEMES;
    }

    public static boolean isBuiltInTheme(String id) {
        return id != null && BUILT_IN_THEMES.containsKey(id);
    }

    public static boolean isCustomTheme(String id) {
        return CustomThemeManager.isCustomTheme(id);
    }

    public static void refreshCustomThemes() {
        THEMES.clear();
        THEMES.putAll(BUILT_IN_THEMES);
        for (CustomThemeData data : CustomThemeManager.all().values()) {
            THEMES.put(data.id, data.toTheme());
        }
    }

    public static void setTheme(String id) {
        if (THEMES.containsKey(id)) {
            ConfigManager.get().theme = id;
            ConfigManager.save();
        }
    }
}
