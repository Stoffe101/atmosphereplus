package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.themes.CustomThemeManager;
import com.skrra.atmosphereplus.themes.ThemeManager;

public class ThemeStudioState {
    private String selectedThemeId = "";

    public String selectedThemeId() {
        if (selectedThemeId == null || selectedThemeId.isBlank() || ThemeManager.byId(selectedThemeId) == null) {
            selectedThemeId = ConfigManager.get().theme;
        }

        if (ThemeManager.byId(selectedThemeId) == null) {
            selectedThemeId = ThemeManager.defaultTheme().id();
        }

        return selectedThemeId;
    }

    public void selectTheme(String id) {
        if (id != null && ThemeManager.byId(id) != null) {
            selectedThemeId = id;
        }
    }

    public void selectCurrentTheme() {
        selectedThemeId = ConfigManager.get().theme;
    }

    public boolean selectedIsCustom() {
        return CustomThemeManager.isCustomTheme(selectedThemeId());
    }
}
