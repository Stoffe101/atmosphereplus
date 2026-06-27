package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.client.AtmospherePlusClient;
import com.skrra.atmosphereplus.config.AtmosphereProfile;
import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.config.ProfileManager;
import com.skrra.atmosphereplus.keybind.AtmosphereKeybinds;
import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.widgets.ActionButtonWidget;
import com.skrra.atmosphereplus.ui.widgets.AtmosphereWidget;
import com.skrra.atmosphereplus.ui.widgets.CategoryButton;
import com.skrra.atmosphereplus.ui.widgets.ChoiceButtonWidget;
import com.skrra.atmosphereplus.ui.widgets.PresetCardWidget;
import com.skrra.atmosphereplus.ui.widgets.SliderWidget;
import com.skrra.atmosphereplus.ui.widgets.TimePresetButtonWidget;
import com.skrra.atmosphereplus.ui.widgets.ToggleWidget;
import com.skrra.atmosphereplus.util.NotificationUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AtmosphereScreen extends Screen {
    private final List<AtmosphereWidget> widgets = new ArrayList<>();

    private UiCategory selected = UiCategory.HOME;
    private String searchQuery = "";
    private boolean searchFocused = false;
    private int renamingProfileIndex = -1;
    private String renameProfileText = "";

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int contentBottom = 0;

    private int searchResultCount = 0;

    private int windowX;
    private int windowY;
    private int windowW;
    private int windowH;

    private int searchX;
    private int searchY;
    private int searchW;
    private int searchH;

    private int closeX;
    private int closeY;
    private int closeSize;

    public AtmosphereScreen() {
        super(Text.literal("Atmosphere+"));
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        widgets.clear();
        searchResultCount = 0;

        windowW = Math.min(920, width - 24);
        windowH = Math.min(520, height - 24);
        windowX = (width - windowW) / 2;
        windowY = (height - windowH) / 2;
        contentBottom = windowY + windowH - 18;

        searchW = Math.min(260, windowW / 3);
        searchH = 20;
        searchX = windowX + windowW / 2 - searchW / 2;
        searchY = windowY + 14;

        closeSize = 20;
        closeX = windowX + windowW - 36;
        closeY = windowY + 14;

        int sidebarX = windowX + 12;
        int sidebarY = windowY + 98;
        int sidebarW = 174;

        if (!isSearching()) {
            int i = 0;
            for (UiCategory category : visibleCategories()) {
                widgets.add(new CategoryButton(sidebarX, sidebarY + i * 25, sidebarW, category, () -> selected, c -> {
                    selected = c;
                    searchFocused = false;
                    scrollOffset = 0;
                    rebuildWidgets();
                }));
                i++;
            }
        }

        int contentX = windowX + 224;
        int contentY = windowY + 116 - scrollOffset;
        int contentW = windowW - 260;
        int finalY = contentY;

        if (isSearching()) {
            finalY = addSearchResultWidgets(contentX, contentY, contentW);
            maxScroll = Math.max(0, finalY + scrollOffset - contentBottom);
            scrollOffset = Math.min(scrollOffset, maxScroll);
            return;
        }

        switch (selected) {
            case WEATHER -> finalY = addWeatherWidgets(contentX, contentY, contentW);
            case TIME -> finalY = addTimeWidgets(contentX, contentY, contentW);
            case SKY -> finalY = addSkyWidgets(contentX, contentY, contentW);
            case LIGHTING -> finalY = addLightingWidgets(contentX, contentY, contentW);
            case FOG -> finalY = addFogWidgets(contentX, contentY, contentW);
            case PARTICLES -> finalY = addParticlesWidgets(contentX, contentY, contentW);
            case THEMES -> finalY = addThemeWidgets(contentX, contentY, contentW);
            case PRESETS -> finalY = addPresetWidgets(contentX, contentY, contentW);
            case PROFILES -> finalY = addProfilesWidgets(contentX, contentY, contentW);
            case ADVANCED -> finalY = addAdvancedWidgets(contentX, contentY, contentW);
            default -> {
            }
        }

        maxScroll = Math.max(0, finalY + scrollOffset - contentBottom);
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }

private int addWeatherWidgets(int contentX, int contentY, int contentW) {
    widgets.add(new ToggleWidget(contentX, contentY, contentW, "Override server weather visually", "Makes weather visuals client-side and independent from the server.", () -> ConfigManager.get().weatherOverride, v -> {
        ConfigManager.get().weatherOverride = v;
            clearActivePreset();
        if (!v) {
            ConfigManager.get().weatherMode = "SERVER";
        } else if ("SERVER".equalsIgnoreCase(ConfigManager.get().weatherMode)) {
            ConfigManager.get().weatherMode = "SUNNY";
        }
        ConfigManager.save();
        rebuildWidgets();
    }));

    int modeW = (contentW - 20) / 3;
    int row1Y = contentY + 54;
    int row2Y = contentY + 104;

    addWeatherChoice(contentX, row1Y, modeW, "Server", "Use normal server weather.", IconType.WEATHER, "SERVER");
    addWeatherChoice(contentX + modeW + 10, row1Y, modeW, "Sunny", "Force clear visual weather.", IconType.SKY, "SUNNY");
    addWeatherChoice(contentX + (modeW + 10) * 2, row1Y, modeW, "Rain", "Force rainy visual mood.", IconType.WEATHER, "RAIN");

    addWeatherChoice(contentX, row2Y, modeW, "Thunder", "Force stormy visual mood.", IconType.LIGHTING, "THUNDER");
    addWeatherChoice(contentX + modeW + 10, row2Y, modeW, "Snow", "Force snowy visual mood later.", IconType.FOG, "SNOW");

    widgets.add(new SliderWidget(contentX, contentY + 158, contentW, "Rain intensity", "Adjusts how strong rain visuals should appear.", 0f, 1f, () -> ConfigManager.get().rainIntensity, v -> {
        ConfigManager.get().rainIntensity = v;
            clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%"));

    widgets.add(new ToggleWidget(contentX, contentY + 220, contentW, "Thunder sounds", "Controls whether thunder audio should be allowed by Atmosphere+.", () -> ConfigManager.get().thunderSounds, v -> {
        ConfigManager.get().thunderSounds = v;
            clearActivePreset();
        ConfigManager.save();
    }));

    addResetButton(contentX, contentY + 274, contentW, "Reset Weather", "Server weather, full rain intensity, thunder sounds on.", IconType.WEATHER, this::resetWeather);

    return contentY + 320;
}

    private void addWeatherChoice(int x, int y, int width, String label, String description, IconType icon, String mode) {
        widgets.add(new ChoiceButtonWidget(x, y, width, label, description, icon, () -> mode.equalsIgnoreCase(ConfigManager.get().weatherMode), () -> {
            ConfigManager.get().weatherMode = mode;
            clearActivePreset();
            ConfigManager.get().weatherOverride = !"SERVER".equalsIgnoreCase(mode);
            ConfigManager.save();
            rebuildWidgets();
        }));
    }

private int addTimeWidgets(int contentX, int contentY, int contentW) {
    widgets.add(new ToggleWidget(contentX, contentY, contentW, "Override visual time", "Changes the client-side visual day/night cycle without touching the server.", () -> ConfigManager.get().timeOverride, v -> {
        ConfigManager.get().timeOverride = v;
            clearActivePreset();
        ConfigManager.save();
    }));

    int buttonW = (contentW - 20) / 3;
    int row1Y = contentY + 54;
    int row2Y = contentY + 98;

    addTimePresetButton(contentX, row1Y, buttonW, "Sunrise", "0", 0);
    addTimePresetButton(contentX + buttonW + 10, row1Y, buttonW, "Morning", "1000", 1000);
    addTimePresetButton(contentX + (buttonW + 10) * 2, row1Y, buttonW, "Day", "6000", 6000);

    addTimePresetButton(contentX, row2Y, buttonW, "Sunset", "12000", 12000);
    addTimePresetButton(contentX + buttonW + 10, row2Y, buttonW, "Night", "15000", 15000);
    addTimePresetButton(contentX + (buttonW + 10) * 2, row2Y, buttonW, "Midnight", "18000", 18000);

    widgets.add(new SliderWidget(contentX, contentY + 148, contentW, "Visual time", "0 sunrise, 6000 day, 12000 sunset, 18000 midnight.", 0f, 24000f, () -> (float) ConfigManager.get().visualTime, v -> {
        ConfigManager.get().visualTime = Math.round(v);
            clearActivePreset();
        ConfigManager.save();
    }, this::formatMinecraftTime));

    widgets.add(new ToggleWidget(contentX, contentY + 210, contentW, "Freeze visual time", "Reserved for future smooth time animation modes.", () -> ConfigManager.get().freezeVisualTime, v -> {
        ConfigManager.get().freezeVisualTime = v;
            clearActivePreset();
        ConfigManager.save();
    }));

    addResetButton(contentX, contentY + 264, contentW, "Reset Time", "Return to server time and default visual time.", IconType.TIME, this::resetTime);

    return contentY + 310;
}

    private void addTimePresetButton(int x, int y, int width, String label, String timeLabel, int time) {
        widgets.add(new TimePresetButtonWidget(x, y, width, label, timeLabel, () -> ConfigManager.get().timeOverride && ConfigManager.get().visualTime == time, () -> {
            ConfigManager.get().timeOverride = true;
            ConfigManager.get().visualTime = time;
            ConfigManager.save();
            rebuildWidgets();
        }));
    }


private int addSkyWidgets(int contentX, int contentY, int contentW) {
    widgets.add(new ToggleWidget(contentX, contentY, contentW, "Override clouds visually", "Uses client-only cloud mode settings without changing server state.", () -> ConfigManager.get().cloudOverride, v -> {
        ConfigManager.get().cloudOverride = v;
        clearActivePreset();
        ConfigManager.save();
    }));

    int modeW = (contentW - 20) / 3;
    int rowY = contentY + 54;
    addCloudChoice(contentX, rowY, modeW, "Server", "Use normal Minecraft cloud settings.", "SERVER");
    addCloudChoice(contentX + modeW + 10, rowY, modeW, "Off", "Hide clouds visually.", "OFF");
    addCloudChoice(contentX + (modeW + 10) * 2, rowY, modeW, "Fast", "Force fast clouds visually.", "FAST");
    addCloudChoice(contentX, rowY + 50, modeW, "Fancy", "Force fancy clouds visually.", "FANCY");

    widgets.add(new ActionButtonWidget(contentX + modeW + 10, rowY + 50, modeW * 2 + 10, "Cloud Distance Disabled", "Distance override was unstable in 1.21.11, so this build keeps vanilla distance.", IconType.SKY, () -> {
        NotificationUtil.show("Cloud distance override is disabled for now");
    }));

    addResetButton(contentX, contentY + 158, contentW, "Reset Sky / Clouds", "Return cloud visuals to server/default settings.", IconType.SKY, this::resetSky);

    return contentY + 204;
}

private void addCloudChoice(int x, int y, int width, String label, String description, String mode) {
    widgets.add(new ChoiceButtonWidget(x, y, width, label, description, IconType.SKY, () -> mode.equalsIgnoreCase(ConfigManager.get().cloudMode), () -> {
        ConfigManager.get().cloudMode = mode;
        ConfigManager.get().cloudOverride = !"SERVER".equalsIgnoreCase(mode);
        clearActivePreset();
        ConfigManager.save();
        rebuildWidgets();
    }));
}

private int addProfilesWidgets(int contentX, int contentY, int contentW) {
    int toolbarW = (contentW - 20) / 3;

    widgets.add(new ActionButtonWidget(contentX, contentY, toolbarW, "Export", "Export all profiles to config/atmosphereplus-profiles-backup.json.", IconType.PRESETS, () -> {
        ProfileManager.exportProfiles();
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(contentX + toolbarW + 10, contentY, toolbarW, "Import", "Import profiles from the backup file if it exists.", IconType.PRESETS, () -> {
        ProfileManager.importProfiles();
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(contentX + (toolbarW + 10) * 2, contentY, toolbarW, "Clear All", "Clear all profile slots.", IconType.ADVANCED, () -> {
        ProfileManager.clearAll();
        rebuildWidgets();
    }));

    int cardW = (contentW - 12) / 2;
    int y = contentY + 52;
    int rowStep = 120;

    AtmosphereProfile[] profiles = ProfileManager.profiles();
    for (int i = 0; i < profiles.length; i++) {
        int col = i % 2;
        int row = i / 2;
        int x = contentX + col * (cardW + 12);
        int slotY = y + row * rowStep;
        int slot = i;
        AtmosphereProfile profile = profiles[i];
        String title = profile.saved ? profile.name : "Profile " + (i + 1);
        String description = profile.saved ? "Click to load this saved atmosphere." : "Empty slot. Save current settings below.";

        widgets.add(new PresetCardWidget(x, slotY, cardW, title, description, IconType.PRESETS, () -> ProfileManager.isActive(slot), () -> {
            if (ProfileManager.profile(slot).saved) {
                ProfileManager.load(slot);
            } else {
                ProfileManager.saveCurrentTo(slot);
            }
            rebuildWidgets();
        }));

        int buttonW = (cardW - 12) / 3;
        widgets.add(new ActionButtonWidget(x, slotY + 76, buttonW, "Save", "Overwrite this slot with current settings.", IconType.PRESETS, () -> {
            ProfileManager.saveCurrentTo(slot);
            rebuildWidgets();
        }));

        widgets.add(new ActionButtonWidget(x + buttonW + 6, slotY + 76, buttonW, "Rename", "Rename this profile slot.", IconType.PRESETS, () -> {
            startRenamingProfile(slot);
        }));

        widgets.add(new ActionButtonWidget(x + (buttonW + 6) * 2, slotY + 76, buttonW, "Clear", "Clear this profile slot.", IconType.ADVANCED, () -> {
            ProfileManager.clear(slot);
            rebuildWidgets();
        }));
    }

    int rows = (profiles.length + 1) / 2;
    return y + rows * rowStep + 2;
}

private int addLightingWidgets(int contentX, int contentY, int contentW) {
    widgets.add(new ToggleWidget(contentX, contentY, contentW, "Fullbright", "Attempts to force maximum client-side lightmap brightness.", () -> ConfigManager.get().fullbright, v -> {
        ConfigManager.get().fullbright = v;
            clearActivePreset();
        ConfigManager.save();
    }));

    widgets.add(new SliderWidget(contentX, contentY + 54, contentW, "Gamma", "Adjusts client-side lightmap brightness curve.", 0f, 2f, () -> ConfigManager.get().gamma, v -> {
        ConfigManager.get().gamma = v;
            clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%"));

    addResetButton(contentX, contentY + 116, contentW, "Reset Lighting", "Disable fullbright and restore gamma to 100%.", IconType.LIGHTING, this::resetLighting);

    return contentY + 162;
}

private int addFogWidgets(int contentX, int contentY, int contentW) {
    widgets.add(new ToggleWidget(contentX, contentY, contentW, "Override fog visually", "Enables client-side fog distance and density controls.", () -> ConfigManager.get().fogOverride, v -> {
        ConfigManager.get().fogOverride = v;
            clearActivePreset();
        ConfigManager.save();
    }));

    widgets.add(new SliderWidget(contentX, contentY + 54, contentW, "Fog distance", "Lower means closer fog. Higher pushes fog farther away.", 0f, 2f, () -> ConfigManager.get().fogDistance, v -> {
        ConfigManager.get().fogDistance = v;
            clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%"));

    widgets.add(new SliderWidget(contentX, contentY + 116, contentW, "Fog density", "Higher values make fog feel thicker and closer.", 0f, 2f, () -> ConfigManager.get().fogDensity, v -> {
        ConfigManager.get().fogDensity = v;
            clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%"));

    addResetButton(contentX, contentY + 178, contentW, "Reset Fog", "Disable fog override and restore normal fog values.", IconType.FOG, this::resetFog);

    return contentY + 224;
}

private int addParticlesWidgets(int contentX, int contentY, int contentW) {
    widgets.add(new SliderWidget(contentX, contentY, contentW, "Particle amount", "Controls the visual amount of new client particles Atmosphere+ will allow.", 0f, 2f, () -> ConfigManager.get().particleAmount, v -> {
        ConfigManager.get().particleAmount = v;
            clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%"));

    addResetButton(contentX, contentY + 62, contentW, "Reset Particles", "Restore normal particle amount.", IconType.PARTICLES, this::resetParticles);

    return contentY + 108;
}

private int addAdvancedWidgets(int contentX, int contentY, int contentW) {
    addResetButton(contentX, contentY, contentW, "Reset All Visuals", "Reset weather, time, sky, fog, lighting and particles.", IconType.ADVANCED, this::resetAllVisuals);

    widgets.add(new ActionButtonWidget(contentX, contentY + 48, contentW, "Go to Profiles", "Save or load your own custom atmosphere slots.", IconType.PRESETS, () -> {
        selected = UiCategory.PROFILES;
        scrollOffset = 0;
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(contentX, contentY + 96, contentW, "Performance Clear", "Apply a low-visual-intensity profile.", IconType.PRESETS, () -> {
        applyPerformanceClear();
        NotificationUtil.show("Applied Performance Clear");
        rebuildWidgets();
    }));

    return contentY + 144;
}

    private int addThemeWidgets(int contentX, int contentY, int contentW) {
        int themeW = (contentW - 10) / 2;
        int index = 0;

        for (String themeId : ThemeManager.all().keySet()) {
            int col = index % 2;
            int row = index / 2;
            int x = contentX + col * (themeW + 10);
            int y = contentY + row * 48;
            String label = ThemeManager.all().get(themeId).displayName();

            widgets.add(new ToggleWidget(x, y, themeW, label, "Switches the Atmosphere+ interface to the " + label + " theme.", () -> ConfigManager.get().theme.equals(themeId), v -> {
                if (v) {
                    ThemeManager.setTheme(themeId);
                }
            }));

            index++;
        }

        return contentY + ((index + 1) / 2) * 48;
    }

private int addPresetWidgets(int contentX, int contentY, int contentW) {
    int cardW = (contentW - 12) / 2;
    int y = contentY;

    widgets.add(new PresetCardWidget(contentX, y, cardW, "Golden Hour", "Sunny, warm noon lighting and soft atmosphere.", IconType.SKY, this::isGoldenHourActive, () -> {
        applyGoldenHour();
        NotificationUtil.show("Applied Golden Hour");
        rebuildWidgets();
    }));

    widgets.add(new PresetCardWidget(contentX + cardW + 12, y, cardW, "Midnight Calm", "Permanent midnight visuals with calm weather.", IconType.TIME, this::isMidnightCalmActive, () -> {
        applyMidnightCalm();
        NotificationUtil.show("Applied Midnight Calm");
        rebuildWidgets();
    }));

    y += 84;

    widgets.add(new PresetCardWidget(contentX, y, cardW, "Cozy Rain", "Light rain mood with afternoon lighting.", IconType.WEATHER, this::isCozyRainActive, () -> {
        applyCozyRain();
        NotificationUtil.show("Applied Cozy Rain");
        rebuildWidgets();
    }));

    widgets.add(new PresetCardWidget(contentX + cardW + 12, y, cardW, "Thunder Night", "Dark night with thunderstorm mood.", IconType.LIGHTING, this::isThunderNightActive, () -> {
        applyThunderNight();
        NotificationUtil.show("Applied Thunder Night");
        rebuildWidgets();
    }));

    y += 84;

    widgets.add(new PresetCardWidget(contentX, y, cardW, "Deep Fog", "Thick custom fog for atmospheric screenshots.", IconType.FOG, this::isDeepFogActive, () -> {
        applyDeepFog();
        NotificationUtil.show("Applied Deep Fog");
        rebuildWidgets();
    }));

    widgets.add(new PresetCardWidget(contentX + cardW + 12, y, cardW, "Bright Caves", "Fullbright and boosted gamma for dark areas.", IconType.LIGHTING, this::isBrightCavesActive, () -> {
        applyBrightCaves();
        NotificationUtil.show("Applied Bright Caves");
        rebuildWidgets();
    }));

    y += 84;

    widgets.add(new PresetCardWidget(contentX, y, cardW, "Performance Clear", "Reduces rain, fog and particles.", IconType.ADVANCED, this::isPerformanceClearActive, () -> {
        applyPerformanceClear();
        NotificationUtil.show("Applied Performance Clear");
        rebuildWidgets();
    }));

    widgets.add(new PresetCardWidget(contentX + cardW + 12, y, cardW, "Soft Mist", "Light custom fog with calm weather.", IconType.FOG, this::isSoftMistActive, () -> {
        applySoftMist();
        NotificationUtil.show("Applied Soft Mist");
        rebuildWidgets();
    }));

    y += 84;

    widgets.add(new PresetCardWidget(contentX, y, cardW, "Cloudless Clear", "Clear weather with clouds hidden.", IconType.SKY, this::isCloudlessClearActive, () -> {
        applyCloudlessClear();
        NotificationUtil.show("Applied Cloudless Clear");
        rebuildWidgets();
    }));

    widgets.add(new PresetCardWidget(contentX + cardW + 12, y, cardW, "Fancy Clouds", "Force fancy cloud rendering mode.", IconType.SKY, this::isFancyCloudsActive, () -> {
        applyFancyClouds();
        NotificationUtil.show("Applied Fancy Clouds");
        rebuildWidgets();
    }));

    return y + 84;
}

private int addSearchResultWidgets(int contentX, int contentY, int contentW) {
    int y = contentY;

    y = addSearchTimePreset(y, contentX, contentW, "Time Preset · Sunrise", "sunrise dawn morning day night time preset 0", "Sunrise", "0", 0);
    y = addSearchTimePreset(y, contentX, contentW, "Time Preset · Morning", "morning sunrise day time preset 1000", "Morning", "1000", 1000);
    y = addSearchTimePreset(y, contentX, contentW, "Time Preset · Day", "day noon morning time preset 6000", "Day", "6000", 6000);
    y = addSearchTimePreset(y, contentX, contentW, "Time Preset · Sunset", "sunset evening dusk day night time preset 12000", "Sunset", "12000", 12000);
    y = addSearchTimePreset(y, contentX, contentW, "Time Preset · Night", "night dark midnight sunset time preset 15000", "Night", "15000", 15000);
    y = addSearchTimePreset(y, contentX, contentW, "Time Preset · Midnight", "midnight night dark time preset 18000", "Midnight", "18000", 18000);

    for (UiCategory category : UiCategory.values()) {
        y = addSearchCategoryJump(y, contentX, contentW, category);
    }

    y = addSearchAction(y, contentX, contentW, "Reset · All Visuals", "reset all visuals default restore", "Reset All Visuals", "Reset weather, time, fog, lighting and particles.", IconType.ADVANCED, this::resetAllVisuals);
    y = addSearchAction(y, contentX, contentW, "Reset · Weather", "reset weather server rain thunder", "Reset Weather", "Restore server weather mode.", IconType.WEATHER, this::resetWeather);
    y = addSearchAction(y, contentX, contentW, "Reset · Time", "reset time server day night", "Reset Time", "Return to server time.", IconType.TIME, this::resetTime);
    y = addSearchAction(y, contentX, contentW, "Reset · Fog", "reset fog distance density", "Reset Fog", "Disable custom fog.", IconType.FOG, this::resetFog);
    y = addSearchAction(y, contentX, contentW, "Reset · Lighting", "reset lighting fullbright gamma brightness", "Reset Lighting", "Disable fullbright and restore gamma.", IconType.LIGHTING, this::resetLighting);
    y = addSearchAction(y, contentX, contentW, "Reset · Particles", "reset particles particle amount", "Reset Particles", "Restore normal particles.", IconType.PARTICLES, this::resetParticles);
    y = addSearchAction(y, contentX, contentW, "Reset · Sky Clouds", "reset sky clouds cloud", "Reset Sky / Clouds", "Restore default cloud visuals.", IconType.SKY, this::resetSky);

    y = addSearchProfiles(y, contentX, contentW);

    y = addSearchToggle(y, contentX, contentW, "Weather · Override server weather visually", "weather override server visual rain sunny thunder atmosphere", () -> ConfigManager.get().weatherOverride, v -> {
        ConfigManager.get().weatherOverride = v;
            clearActivePreset();
        if (!v) ConfigManager.get().weatherMode = "SERVER";
        ConfigManager.save();
    });

    y = addSearchWeatherChoice(y, contentX, contentW, "Weather Mode · Server", "weather mode server normal", IconType.WEATHER, "SERVER");
    y = addSearchWeatherChoice(y, contentX, contentW, "Weather Mode · Sunny", "weather mode sunny clear sun", IconType.SKY, "SUNNY");
    y = addSearchWeatherChoice(y, contentX, contentW, "Weather Mode · Rain", "weather mode rain rainy water", IconType.WEATHER, "RAIN");
    y = addSearchWeatherChoice(y, contentX, contentW, "Weather Mode · Thunder", "weather mode thunder storm lightning", IconType.LIGHTING, "THUNDER");
    y = addSearchWeatherChoice(y, contentX, contentW, "Weather Mode · Snow", "weather mode snow snowy winter", IconType.FOG, "SNOW");

    y = addSearchSlider(y, contentX, contentW, "Weather · Rain intensity", "weather rain intensity strength opacity storm water", 0f, 1f, () -> ConfigManager.get().rainIntensity, v -> {
        ConfigManager.get().rainIntensity = v;
            clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%");

    y = addSearchToggle(y, contentX, contentW, "Weather · Thunder sounds", "weather thunder sound lightning storm audio", () -> ConfigManager.get().thunderSounds, v -> {
        ConfigManager.get().thunderSounds = v;
            clearActivePreset();
        ConfigManager.save();
    });

    y = addSearchToggle(y, contentX, contentW, "Time · Override visual time", "time day night visual override sunrise sunset noon midnight", () -> ConfigManager.get().timeOverride, v -> {
        ConfigManager.get().timeOverride = v;
            clearActivePreset();
        ConfigManager.save();
    });

    y = addSearchSlider(y, contentX, contentW, "Time · Visual time", "time day night slider sunrise sunset noon midnight visual", 0f, 24000f, () -> (float) ConfigManager.get().visualTime, v -> {
        ConfigManager.get().visualTime = Math.round(v);
            clearActivePreset();
        ConfigManager.save();
    }, this::formatMinecraftTime);

    y = addSearchToggle(y, contentX, contentW, "Time · Freeze visual time", "time freeze pause stop day night cycle visual", () -> ConfigManager.get().freezeVisualTime, v -> {
        ConfigManager.get().freezeVisualTime = v;
            clearActivePreset();
        ConfigManager.save();
    });

    y = addSearchToggle(y, contentX, contentW, "Sky · Override clouds visually", "sky cloud clouds override visual off fast fancy", () -> ConfigManager.get().cloudOverride, v -> {
        ConfigManager.get().cloudOverride = v;
        clearActivePreset();
        ConfigManager.save();
    });
    y = addSearchCloudChoice(y, contentX, contentW, "Cloud Mode · Server", "cloud mode server normal", "SERVER");
    y = addSearchCloudChoice(y, contentX, contentW, "Cloud Mode · Off", "cloud mode off hidden hide clouds", "OFF");
    y = addSearchCloudChoice(y, contentX, contentW, "Cloud Mode · Fast", "cloud mode fast clouds", "FAST");
    y = addSearchCloudChoice(y, contentX, contentW, "Cloud Mode · Fancy", "cloud mode fancy clouds", "FANCY");

    y = addSearchToggle(y, contentX, contentW, "Lighting · Fullbright", "lighting fullbright brightness cave dark gamma light", () -> ConfigManager.get().fullbright, v -> {
        ConfigManager.get().fullbright = v;
            clearActivePreset();
        ConfigManager.save();
    });

    y = addSearchSlider(y, contentX, contentW, "Lighting · Gamma", "lighting gamma brightness exposure cave dark light", 0f, 2f, () -> ConfigManager.get().gamma, v -> {
        ConfigManager.get().gamma = v;
            clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%");

    y = addSearchToggle(y, contentX, contentW, "Fog · Override fog visually", "fog override custom mode on off server", () -> ConfigManager.get().fogOverride, v -> {
        ConfigManager.get().fogOverride = v;
            clearActivePreset();
        ConfigManager.save();
    });

    y = addSearchSlider(y, contentX, contentW, "Fog · Fog distance", "fog distance density view mist haze nether end water lava", 0f, 2f, () -> ConfigManager.get().fogDistance, v -> {
        ConfigManager.get().fogDistance = v;
            clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%");

    y = addSearchSlider(y, contentX, contentW, "Fog · Fog density", "fog density strength thickness mist haze", 0f, 2f, () -> ConfigManager.get().fogDensity, v -> {
        ConfigManager.get().fogDensity = v;
            clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%");

    y = addSearchSlider(y, contentX, contentW, "Particles · Particle amount", "particles particle amount rain snow smoke explosion fire bubbles", 0f, 2f, () -> ConfigManager.get().particleAmount, v -> {
        ConfigManager.get().particleAmount = v;
            clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%");

    y = addSearchPresetCard(y, contentX, contentW, "Preset · Golden Hour", "preset golden hour sunny warm noon lighting atmosphere", "Golden Hour", "Sunny, warm noon lighting and soft atmosphere.", IconType.SKY, this::isGoldenHourActive, this::applyGoldenHour);
    y = addSearchPresetCard(y, contentX, contentW, "Preset · Midnight Calm", "preset midnight calm night clear quiet", "Midnight Calm", "Permanent midnight visuals with calm weather.", IconType.TIME, this::isMidnightCalmActive, this::applyMidnightCalm);
    y = addSearchPresetCard(y, contentX, contentW, "Preset · Cozy Rain", "preset cozy rain rainy afternoon", "Cozy Rain", "Light rain mood with afternoon lighting.", IconType.WEATHER, this::isCozyRainActive, this::applyCozyRain);
    y = addSearchPresetCard(y, contentX, contentW, "Preset · Thunder Night", "preset thunder night storm lightning dark", "Thunder Night", "Dark night with thunderstorm mood.", IconType.LIGHTING, this::isThunderNightActive, this::applyThunderNight);
    y = addSearchPresetCard(y, contentX, contentW, "Preset · Deep Fog", "preset deep fog foggy mist haze", "Deep Fog", "Thick custom fog for atmospheric screenshots.", IconType.FOG, this::isDeepFogActive, this::applyDeepFog);
    y = addSearchPresetCard(y, contentX, contentW, "Preset · Bright Caves", "preset bright caves fullbright gamma lighting", "Bright Caves", "Fullbright and boosted gamma for dark areas.", IconType.LIGHTING, this::isBrightCavesActive, this::applyBrightCaves);
    y = addSearchPresetCard(y, contentX, contentW, "Preset · Soft Mist", "preset soft mist fog light haze", "Soft Mist", "Light custom fog with calm weather.", IconType.FOG, this::isSoftMistActive, this::applySoftMist);
    y = addSearchPresetCard(y, contentX, contentW, "Preset · Performance Clear", "preset performance clear particles low sunny", "Performance Clear", "Reduces visual intensity settings.", IconType.ADVANCED, this::isPerformanceClearActive, this::applyPerformanceClear);
    y = addSearchPresetCard(y, contentX, contentW, "Preset · Cloudless Clear", "preset cloudless clear clouds off sky sunny", "Cloudless Clear", "Clear weather with clouds hidden.", IconType.SKY, this::isCloudlessClearActive, this::applyCloudlessClear);
    y = addSearchPresetCard(y, contentX, contentW, "Preset · Fancy Clouds", "preset fancy clouds cloud distance sky", "Fancy Clouds", "Force fancy cloud rendering mode.", IconType.SKY, this::isFancyCloudsActive, this::applyFancyClouds);

    for (String themeId : ThemeManager.all().keySet()) {
        String name = ThemeManager.all().get(themeId).displayName();
        y = addSearchToggle(y, contentX, contentW, "Theme · " + name, "theme ui color accent dark black purple " + name + " " + themeId, () -> ConfigManager.get().theme.equals(themeId), v -> {
            if (v) {
                ThemeManager.setTheme(themeId);
            }
        });
    }

    return y;
}

    private int addSearchCategoryJump(int y, int x, int width, UiCategory category) {
        if (!matchesSearch("Category · " + category.title, category.title, category.description)) {
            return y;
        }

        widgets.add(new ChoiceButtonWidget(x, y, width, "Category · " + category.title, "Open the " + category.title + " category.", category.icon, () -> selected == category, () -> {
            selected = category;
            searchQuery = "";
            searchFocused = false;
            scrollOffset = 0;
            rebuildWidgets();
        }));
        searchResultCount++;
        return y + 52;
    }

    private int addSearchTimePreset(int y, int x, int width, String searchLabel, String keywords, String label, String timeLabel, int time) {
        if (!matchesSearch(searchLabel, keywords)) {
            return y;
        }

        widgets.add(new TimePresetButtonWidget(x, y, width, label, timeLabel, () -> ConfigManager.get().timeOverride && ConfigManager.get().visualTime == time, () -> {
            ConfigManager.get().timeOverride = true;
            ConfigManager.get().visualTime = time;
            ConfigManager.save();
            rebuildWidgets();
        }));
        searchResultCount++;
        return y + 46;
    }

    private int addSearchPresetCard(int y, int x, int width, String searchLabel, String keywords, String title, String description, IconType icon, Runnable action) {
        if (!matchesSearch(searchLabel, keywords, title, description)) {
            return y;
        }

        widgets.add(new PresetCardWidget(x, y, width, title, description, icon, () -> {
            action.run();
            rebuildWidgets();
        }));
        searchResultCount++;
        return y + 84;
    }


    private int addSearchAction(int y, int x, int width, String searchLabel, String keywords, String label, String description, IconType icon, Runnable action) {
        if (!matchesSearch(searchLabel, keywords, label, description)) {
            return y;
        }

        widgets.add(new ActionButtonWidget(x, y, width, label, description, icon, () -> {
            action.run();
            rebuildWidgets();
        }));
        searchResultCount++;
        return y + 48;
    }

    private int addSearchPresetCard(int y, int x, int width, String searchLabel, String keywords, String title, String description, IconType icon, java.util.function.Supplier<Boolean> activeSupplier, Runnable action) {
        if (!matchesSearch(searchLabel, keywords, title, description)) {
            return y;
        }

        widgets.add(new PresetCardWidget(x, y, width, title, description, icon, activeSupplier, () -> {
            action.run();
            NotificationUtil.show("Applied " + title);
            rebuildWidgets();
        }));
        searchResultCount++;
        return y + 84;
    }

    private void addResetButton(int x, int y, int width, String label, String description, IconType icon, Runnable action) {
        widgets.add(new ActionButtonWidget(x, y, width, label, description, icon, () -> {
            action.run();
            rebuildWidgets();
        }));
    }


    private void setActivePreset(String presetId) {
        ConfigManager.get().activePreset = presetId;
    }

    private void clearActivePreset() {
        ConfigManager.get().activePreset = "";
    }

    private boolean isPresetActive(String presetId) {
        return presetId.equals(ConfigManager.get().activePreset);
    }

    private void resetSky() {
        ConfigManager.get().cloudOverride = false;
        ConfigManager.get().cloudMode = "SERVER";
        ConfigManager.get().cloudDistance = 12;
        clearActivePreset();
        ConfigManager.save();
        NotificationUtil.show("Reset Sky / Clouds");
    }

    private void resetWeather() {
        ConfigManager.get().weatherOverride = false;
        ConfigManager.get().weatherMode = "SERVER";
        ConfigManager.get().rainIntensity = 1.0f;
        ConfigManager.get().thunderSounds = true;
        clearActivePreset();
        ConfigManager.save();
        NotificationUtil.show("Reset Weather");
    }

    private void resetTime() {
        ConfigManager.get().timeOverride = false;
        ConfigManager.get().visualTime = 6000;
        ConfigManager.get().freezeVisualTime = false;
        clearActivePreset();
        ConfigManager.save();
        NotificationUtil.show("Reset Time");
    }

    private void resetFog() {
        ConfigManager.get().fogOverride = false;
        ConfigManager.get().fogDistance = 1.0f;
        ConfigManager.get().fogDensity = 1.0f;
        clearActivePreset();
        ConfigManager.save();
        NotificationUtil.show("Reset Fog");
    }

    private void resetLighting() {
        ConfigManager.get().fullbright = false;
        ConfigManager.get().gamma = 1.0f;
        clearActivePreset();
        ConfigManager.save();
        NotificationUtil.show("Reset Lighting");
    }

    private void resetParticles() {
        ConfigManager.get().particleAmount = 1.0f;
        clearActivePreset();
        ConfigManager.save();
        NotificationUtil.show("Reset Particles");
    }

    private void resetAllVisuals() {
        resetWeather();
        ConfigManager.get().timeOverride = false;
        ConfigManager.get().visualTime = 6000;
        ConfigManager.get().freezeVisualTime = false;
        ConfigManager.get().fogOverride = false;
        ConfigManager.get().fogDistance = 1.0f;
        ConfigManager.get().fogDensity = 1.0f;
        ConfigManager.get().fullbright = false;
        ConfigManager.get().gamma = 1.0f;
        ConfigManager.get().particleAmount = 1.0f;
        ConfigManager.get().cloudOverride = false;
        ConfigManager.get().cloudMode = "SERVER";
        ConfigManager.get().cloudDistance = 12;
        clearActivePreset();
        ConfigManager.save();
        NotificationUtil.show("Reset All Visuals");
    }

    private void applyGoldenHour() {
        ConfigManager.get().timeOverride = true;
        ConfigManager.get().visualTime = 6000;
        ConfigManager.get().weatherOverride = true;
        ConfigManager.get().weatherMode = "SUNNY";
        ConfigManager.get().rainIntensity = 0f;
        ConfigManager.get().fogOverride = false;
        ConfigManager.get().fullbright = false;
        ConfigManager.get().gamma = 1.1f;
        setActivePreset("golden_hour");
        ConfigManager.save();
    }

    private boolean isGoldenHourActive() {
        return isPresetActive("golden_hour");
    }

    private void applyMidnightCalm() {
        ConfigManager.get().timeOverride = true;
        ConfigManager.get().visualTime = 18000;
        ConfigManager.get().weatherOverride = true;
        ConfigManager.get().weatherMode = "SUNNY";
        ConfigManager.get().rainIntensity = 0f;
        setActivePreset("midnight_calm");
        ConfigManager.save();
    }

    private boolean isMidnightCalmActive() {
        return isPresetActive("midnight_calm");
    }

    private void applyCozyRain() {
        ConfigManager.get().timeOverride = true;
        ConfigManager.get().visualTime = 9000;
        ConfigManager.get().weatherOverride = true;
        ConfigManager.get().weatherMode = "RAIN";
        ConfigManager.get().rainIntensity = 0.35f;
        ConfigManager.get().fogOverride = false;
        setActivePreset("cozy_rain");
        ConfigManager.save();
    }

    private boolean isCozyRainActive() {
        return isPresetActive("cozy_rain");
    }

    private void applyThunderNight() {
        ConfigManager.get().timeOverride = true;
        ConfigManager.get().visualTime = 18000;
        ConfigManager.get().weatherOverride = true;
        ConfigManager.get().weatherMode = "THUNDER";
        ConfigManager.get().rainIntensity = 0.85f;
        ConfigManager.get().fogOverride = true;
        ConfigManager.get().fogDistance = 0.65f;
        ConfigManager.get().fogDensity = 1.35f;
        setActivePreset("thunder_night");
        ConfigManager.save();
    }

    private boolean isThunderNightActive() {
        return isPresetActive("thunder_night");
    }

    private void applyDeepFog() {
        ConfigManager.get().fogOverride = true;
        ConfigManager.get().fogDistance = 0.35f;
        ConfigManager.get().fogDensity = 1.75f;
        setActivePreset("deep_fog");
        ConfigManager.save();
    }

    private boolean isDeepFogActive() {
        return isPresetActive("deep_fog");
    }

    private void applySoftMist() {
        ConfigManager.get().weatherOverride = true;
        ConfigManager.get().weatherMode = "SUNNY";
        ConfigManager.get().fogOverride = true;
        ConfigManager.get().fogDistance = 0.85f;
        ConfigManager.get().fogDensity = 1.2f;
        setActivePreset("soft_mist");
        ConfigManager.save();
    }

    private boolean isSoftMistActive() {
        return isPresetActive("soft_mist");
    }

    private void applyBrightCaves() {
        ConfigManager.get().fullbright = true;
        ConfigManager.get().gamma = 2.0f;
        setActivePreset("bright_caves");
        ConfigManager.save();
    }

    private boolean isBrightCavesActive() {
        return isPresetActive("bright_caves");
    }

    private void applyPerformanceClear() {
        ConfigManager.get().weatherMode = "SUNNY";
        ConfigManager.get().weatherOverride = true;
        ConfigManager.get().rainIntensity = 0f;
        ConfigManager.get().particleAmount = 0.35f;
        ConfigManager.get().fogOverride = true;
        ConfigManager.get().fogDistance = 1.5f;
        ConfigManager.get().fogDensity = 0.5f;
        setActivePreset("performance_clear");
        ConfigManager.save();
    }

    private boolean isPerformanceClearActive() {
        return isPresetActive("performance_clear");
    }

    private void applyCloudlessClear() {
        ConfigManager.get().weatherOverride = true;
        ConfigManager.get().weatherMode = "SUNNY";
        ConfigManager.get().rainIntensity = 0f;
        ConfigManager.get().cloudOverride = true;
        ConfigManager.get().cloudMode = "OFF";
        ConfigManager.get().cloudDistance = 12;
        setActivePreset("cloudless_clear");
        ConfigManager.save();
    }

    private boolean isCloudlessClearActive() {
        return isPresetActive("cloudless_clear");
    }

    private void applyFancyClouds() {
        ConfigManager.get().cloudOverride = true;
        ConfigManager.get().cloudMode = "FANCY";
        ConfigManager.get().cloudDistance = 12;
        setActivePreset("fancy_clouds");
        ConfigManager.save();
    }

    private boolean isFancyCloudsActive() {
        return isPresetActive("fancy_clouds");
    }

    private int addSearchCloudChoice(int y, int x, int width, String label, String keywords, String mode) {
        if (!matchesSearch(label, keywords)) {
            return y;
        }

        widgets.add(new ChoiceButtonWidget(x, y, width, label, "Search result: set cloud mode to " + mode.toLowerCase(Locale.ROOT), IconType.SKY, () -> mode.equalsIgnoreCase(ConfigManager.get().cloudMode), () -> {
            ConfigManager.get().cloudMode = mode;
            ConfigManager.get().cloudOverride = !"SERVER".equalsIgnoreCase(mode);
            clearActivePreset();
            ConfigManager.save();
            rebuildWidgets();
        }));
        searchResultCount++;
        return y + 52;
    }


private int addSearchProfiles(int y, int x, int width) {
        y = addSearchAction(y, x, width, "Profiles · Export", "profile profiles export backup save file", "Export Profiles", "Export all profile slots to a backup file.", IconType.PRESETS, () -> {
            ProfileManager.exportProfiles();
            rebuildWidgets();
        });

        y = addSearchAction(y, x, width, "Profiles · Import", "profile profiles import backup load file", "Import Profiles", "Import profile slots from backup file.", IconType.PRESETS, () -> {
            ProfileManager.importProfiles();
            rebuildWidgets();
        });

        y = addSearchAction(y, x, width, "Profiles · Clear All", "profile profiles clear all delete reset", "Clear All Profiles", "Clear every profile slot.", IconType.ADVANCED, () -> {
            ProfileManager.clearAll();
            rebuildWidgets();
        });

        AtmosphereProfile[] profiles = ProfileManager.profiles();
        for (int i = 0; i < profiles.length; i++) {
            int slot = i;
            AtmosphereProfile profile = profiles[i];
            String title = profile.saved ? profile.name : "Profile " + (i + 1);
            String keywords = "profile profiles save load clear rename delete slot " + (i + 1) + " " + title;
            if (!matchesSearch("Profile · " + title, keywords)) {
                continue;
            }

            widgets.add(new PresetCardWidget(x, y, width, title, profile.saved ? "Click to load this profile." : "Empty slot. Click to save current settings.", IconType.PRESETS, () -> ProfileManager.isActive(slot), () -> {
                if (ProfileManager.profile(slot).saved) {
                    ProfileManager.load(slot);
                } else {
                    ProfileManager.saveCurrentTo(slot);
                }
                rebuildWidgets();
            }));
            searchResultCount++;
            y += 84;

            int buttonW = (width - 12) / 3;
            widgets.add(new ActionButtonWidget(x, y, buttonW, "Save", "Overwrite this profile slot with current settings.", IconType.PRESETS, () -> {
                ProfileManager.saveCurrentTo(slot);
                rebuildWidgets();
            }));
            searchResultCount++;

            widgets.add(new ActionButtonWidget(x + buttonW + 6, y, buttonW, "Rename", "Rename this profile slot.", IconType.PRESETS, () -> startRenamingProfile(slot)));
            searchResultCount++;

            widgets.add(new ActionButtonWidget(x + (buttonW + 6) * 2, y, buttonW, "Clear", "Clear this profile slot.", IconType.ADVANCED, () -> {
                ProfileManager.clear(slot);
                rebuildWidgets();
            }));
            searchResultCount++;
            y += 48;
        }

        return y;
    }

    private int addSearchWeatherChoice(int y, int x, int width, String label, String keywords, IconType icon, String mode) {
        if (!matchesSearch(label, keywords)) {
            return y;
        }

        widgets.add(new ChoiceButtonWidget(x, y, width, label, "Search result: set weather mode to " + mode.toLowerCase(Locale.ROOT), icon, () -> mode.equalsIgnoreCase(ConfigManager.get().weatherMode), () -> {
            ConfigManager.get().weatherMode = mode;
            clearActivePreset();
            ConfigManager.get().weatherOverride = !"SERVER".equalsIgnoreCase(mode);
            ConfigManager.save();
            rebuildWidgets();
        }));
        searchResultCount++;
        return y + 52;
    }

    private int addSearchToggle(int y, int x, int width, String label, String keywords,
                                java.util.function.Supplier<Boolean> getter,
                                java.util.function.Consumer<Boolean> setter) {
        if (!matchesSearch(label, keywords)) {
            return y;
        }

        widgets.add(new ToggleWidget(x, y, width, label, "Search result: " + label, getter, setter));
        searchResultCount++;
        return y + 52;
    }

    private int addSearchSlider(int y, int x, int width, String label, String keywords,
                                float min,
                                float max,
                                java.util.function.Supplier<Float> getter,
                                java.util.function.Consumer<Float> setter,
                                java.util.function.Function<Float, String> formatter) {
        if (!matchesSearch(label, keywords)) {
            return y;
        }

        widgets.add(new SliderWidget(x, y, width, label, "Search result: " + label, min, max, getter, setter, formatter));
        searchResultCount++;
        return y + 64;
    }

    private boolean isSearching() {
        return !searchQuery.trim().isEmpty();
    }

    private boolean matchesSearch(String... values) {
        String query = searchQuery.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return false;
        }

        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            builder.append(value).append(' ');
        }

        String haystack = builder.toString().toLowerCase(Locale.ROOT);
        if (haystack.contains(query)) {
            return true;
        }

        for (String word : query.split("\\s+")) {
            if (!haystack.contains(word)) {
                return false;
            }
        }

        return true;
    }

    private List<UiCategory> visibleCategories() {
        String query = searchQuery.trim().toLowerCase(Locale.ROOT);
        List<UiCategory> result = new ArrayList<>();

        for (UiCategory category : UiCategory.values()) {
            if (query.isEmpty()
                    || category.title.toLowerCase(Locale.ROOT).contains(query)
                    || category.description.toLowerCase(Locale.ROOT).contains(query)) {
                result.add(category);
            }
        }

        return result;
    }

    private String formatMinecraftTime(float value) {
        int time = Math.round(value) % 24000;

        if (time < 1000) {
            return time + " Sunrise";
        }
        if (time < 6000) {
            return time + " Morning";
        }
        if (time < 12000) {
            return time + " Day";
        }
        if (time < 13000) {
            return time + " Sunset";
        }
        if (time < 18000) {
            return time + " Night";
        }

        return time + " Midnight";
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackdrop(context);

        Theme theme = ThemeManager.current();

        UiRender.borderedRect(context, windowX, windowY, windowW, windowH, theme.background(), theme.border());
        UiRender.gradientHorizontal(context, windowX + 1, windowY + 1, windowW - 2, 48, theme.panel(), theme.panelAlt());
        UiRender.rect(context, windowX + 1, windowY + 48, windowW - 2, 1, theme.border());

        drawBranding(context, theme);
        drawSearchBar(context, theme);
        drawTopButtons(context, theme, mouseX, mouseY);
        drawSidebar(context, theme);
        drawContentHeader(context, theme);
        renderContentBackground(context, theme);

        renderSidebarWidgets(context, mouseX, mouseY, delta);

        context.enableScissor(windowX + 204, windowY + 105, windowX + windowW - 14, windowY + windowH - 14);
        renderContentWidgets(context, mouseX, mouseY, delta);
        context.disableScissor();

        renderScrollIndicator(context, theme);
        renderProfileRenameOverlay(context, theme);
        renderTooltip(context, mouseX, mouseY);
    }

    private void renderSidebarWidgets(DrawContext context, int mouseX, int mouseY, float delta) {
        for (AtmosphereWidget widget : widgets) {
            if (widget instanceof CategoryButton) {
                widget.render(context, textRenderer, mouseX, mouseY, delta);
            }
        }
    }

    private void renderContentWidgets(DrawContext context, int mouseX, int mouseY, float delta) {
        for (AtmosphereWidget widget : widgets) {
            if (!(widget instanceof CategoryButton)) {
                widget.render(context, textRenderer, mouseX, mouseY, delta);
            }
        }
    }

    private void renderBackdrop(DrawContext context) {
        context.fill(0, 0, this.width, this.height, 0xBB05070D);

        int gridColor = 0x10000000 | 0x00101018;
        for (int x = 0; x < width; x += 32) {
            context.fill(x, 0, x + 1, height, gridColor);
        }
        for (int y = 0; y < height; y += 32) {
            context.fill(0, y, width, y + 1, gridColor);
        }

        for (int i = 0; i < 20; i++) {
            int x = (i * 67 + 31) % Math.max(1, this.width);
            int y = (i * 43 + 19) % Math.max(1, this.height);
            int size = 1 + (i % 3);
            context.fill(x, y, x + size, y + size, 0x224F8BFF);
        }
    }

    private void drawBranding(DrawContext context, Theme theme) {
        int logoX = windowX + 16;
        int logoY = windowY + 13;

        UiRender.borderedRect(context, logoX, logoY, 24, 24, theme.accentSoft(), theme.accent());
        IconRenderer.drawCentered(context, IconType.WEATHER, logoX + 12, logoY + 12, 18);

        UiRender.text(context, textRenderer, AtmospherePlusClient.MOD_NAME, windowX + 48, windowY + 12, theme.text());
        UiRender.text(context, textRenderer, "visual control suite", windowX + 48, windowY + 27, theme.mutedText());
    }

    private void drawSearchBar(DrawContext context, Theme theme) {
        int border = searchFocused ? theme.accent() : isSearching() ? theme.accentSoft() : theme.border();
        int fill = isSearching() ? theme.accentSoft() : theme.panelAlt();

        UiRender.borderedRect(context, searchX, searchY, searchW, searchH, fill, border);

        String displayed = searchQuery.isEmpty() ? "Search settings..." : searchQuery;
        int textColor = searchQuery.isEmpty() ? theme.mutedText() : theme.text();

        drawSearchIcon(context, searchX + 8, searchY + 4, searchFocused || isSearching() ? theme.accent() : theme.mutedText());
        UiRender.text(context, textRenderer, displayed, searchX + 31, searchY + 6, textColor);

        if (searchFocused && shouldShowCaret() && !searchQuery.isEmpty()) {
            int caretX = Math.min(searchX + searchW - 28, searchX + 31 + textRenderer.getWidth(searchQuery));
            context.fill(caretX + 2, searchY + 5, caretX + 3, searchY + searchH - 5, theme.accent());
        }

        if (!searchQuery.isEmpty()) {
            UiRender.text(context, textRenderer, "×", searchX + searchW - 14, searchY + 6, theme.text());
        }
    }

    private boolean shouldShowCaret() {
        return (System.currentTimeMillis() / 500L) % 2L == 0L;
    }

    private void drawSearchIcon(DrawContext context, int x, int y, int color) {
        UiRender.border(context, x, y, 11, 11, color);
        context.fill(x + 10, y + 10, x + 13, y + 13, color);
        context.fill(x + 12, y + 12, x + 15, y + 15, color);
    }

    private void drawTopButtons(DrawContext context, Theme theme, int mouseX, int mouseY) {
        int versionW = 112;
        int versionX = closeX - versionW - 8;

        UiRender.borderedRect(context, versionX, closeY, versionW, closeSize, theme.panelAlt(), theme.border());
        UiRender.centeredText(context, textRenderer, AtmospherePlusClient.VERSION, versionX + versionW / 2, closeY + 6, theme.mutedText());

        boolean closeHover = UiRender.hovered(mouseX, mouseY, closeX, closeY, closeSize, closeSize);
        UiRender.borderedRect(context, closeX, closeY, closeSize, closeSize, closeHover ? theme.accentSoft() : theme.panelAlt(), closeHover ? theme.accent() : theme.border());
        UiRender.centeredText(context, textRenderer, "×", closeX + closeSize / 2, closeY + 6, closeHover ? theme.text() : theme.mutedText());
    }

    private void drawSidebar(DrawContext context, Theme theme) {
        int x = windowX + 12;
        int y = windowY + 58;
        int w = 174;
        int h = windowH - 70;

        UiRender.panel(context, x, y, w, h, theme.panel(), theme.border(), theme.accent());

        UiRender.text(context, textRenderer, isSearching() ? "Search Mode" : "Navigation", x + 14, y + 12, theme.mutedText());
        UiRender.rect(context, x + 14, y + 29, w - 28, 1, theme.border());

        if (isSearching()) {
            UiRender.centeredText(context, textRenderer, "Direct results", x + w / 2, y + 62, theme.accent());
            UiRender.centeredText(context, textRenderer, "Categories hidden", x + w / 2, y + 84, theme.mutedText());
            UiRender.centeredText(context, textRenderer, "Clear search to browse", x + w / 2, y + 104, theme.mutedText());

            UiRender.borderedRect(context, x + 20, y + 138, w - 40, 24, theme.accentSoft(), theme.border());
            UiRender.centeredText(context, textRenderer, searchResultCount + " result" + (searchResultCount == 1 ? "" : "s"), x + w / 2, y + 146, theme.text());
            return;
        }

        if (visibleCategories().isEmpty()) {
            UiRender.centeredText(context, textRenderer, "No category matches", x + w / 2, y + 64, theme.mutedText());
        }
    }

    private void drawContentHeader(DrawContext context, Theme theme) {
        int x = windowX + 204;
        int y = windowY + 58;
        int w = windowW - 218;

        UiRender.borderedRect(context, x, y, w, 38, theme.panel(), theme.border());

        if (isSearching()) {
            UiRender.borderedRect(context, x + 10, y + 8, 22, 22, theme.accentSoft(), theme.accent());
            IconRenderer.drawCentered(context, IconType.ADVANCED, x + 21, y + 19, 18);

            UiRender.text(context, textRenderer, "SEARCH MODE", x + 42, y + 7, theme.accent());
            UiRender.text(context, textRenderer, searchResultCount + " direct setting result" + (searchResultCount == 1 ? "" : "s") + " for \"" + searchQuery + "\"", x + 42, y + 22, theme.text());

            int chipW = 82;
            UiRender.borderedRect(context, x + w - chipW - 10, y + 9, chipW, 18, theme.accentSoft(), theme.accent());
            UiRender.centeredText(context, textRenderer, "DIRECT EDIT", x + w - chipW / 2 - 10, y + 14, theme.text());
            return;
        }

        UiRender.borderedRect(context, x + 10, y + 8, 22, 22, theme.accentSoft(), theme.accent());
        IconRenderer.drawCentered(context, selected.icon, x + 21, y + 19, 18);

        UiRender.text(context, textRenderer, selected.title, x + 42, y + 8, theme.text());
        UiRender.text(context, textRenderer, selected.description, x + 42, y + 22, theme.mutedText());
    }

    private void renderContentBackground(DrawContext context, Theme theme) {
        int x = windowX + 204;
        int y = windowY + 104;
        int w = windowW - 218;
        int h = windowH - 116;

        UiRender.panel(context, x, y, w, h, theme.panel(), theme.border(), theme.accent());

        if (isSearching()) {
            if (searchResultCount == 0) {
                UiRender.centeredText(context, textRenderer, "No direct settings found", x + w / 2, y + 112, theme.text());
                UiRender.centeredText(context, textRenderer, "Try clouds, cloudless, profile, sunrise, rain, deep fog, bright caves, reset, fullbright, or gamma.", x + w / 2, y + 134, theme.mutedText());
            } else {
                UiRender.centeredText(context, textRenderer, "Search mode: edit matching controls directly here.", x + w / 2, y + h - 26, theme.mutedText());
            }
            return;
        }

        if (selected == UiCategory.HOME) {
            renderHome(context, theme, x, y, w, h);
        }
    }

    private void renderHome(DrawContext context, Theme theme, int x, int y, int w, int h) {
        UiRender.centeredText(context, textRenderer, "Welcome to Atmosphere+", x + w / 2, y + 48, theme.text());
        UiRender.centeredText(context, textRenderer, "A client-side atmosphere and visual customization suite.", x + w / 2, y + 68, theme.mutedText());

        int cardW = (w - 54) / 3;
        int cardY = y + 112;

        drawMiniCard(context, theme, x + 16, cardY, cardW, "Weather", "Override visuals", IconType.WEATHER);
        drawMiniCard(context, theme, x + 27 + cardW, cardY, cardW, "Time", "Control day/night", IconType.TIME);
        drawMiniCard(context, theme, x + 38 + cardW * 2, cardY, cardW, "Presets", "One-click moods", IconType.PRESETS);

        UiRender.centeredText(context, textRenderer, "Tip: search for sunrise, rain, golden hour, fog distance, or a theme.", x + w / 2, y + h - 58, theme.mutedText());
        UiRender.centeredText(context, textRenderer, "Weather, time, clouds, fog, lighting, particles and profiles are now hooked.", x + w / 2, y + h - 40, theme.accent());
    }

    private void drawMiniCard(DrawContext context, Theme theme, int x, int y, int w, String title, String description, IconType icon) {
        UiRender.card(context, x, y, w, 76, theme.panelAlt(), theme.border());
        UiRender.borderedRect(context, x + 12, y + 12, 22, 22, theme.accentSoft(), theme.accent());
        IconRenderer.drawCentered(context, icon, x + 23, y + 23, 18);
        UiRender.text(context, textRenderer, title, x + 44, y + 13, theme.text());
        UiRender.text(context, textRenderer, description, x + 44, y + 28, theme.mutedText());
        UiRender.rect(context, x + 12, y + 56, w - 24, 3, theme.accentSoft());
        UiRender.rect(context, x + 12, y + 56, (w - 24) / 2, 3, theme.accent());
    }

    private void renderComingSoon(DrawContext context, Theme theme, int x, int y, int w, int h) {
        UiRender.borderedRect(context, x + w / 2 - 18, y + 58, 36, 36, theme.accentSoft(), theme.accent());
        IconRenderer.drawCentered(context, selected.icon, x + w / 2, y + 76, 22);
        UiRender.centeredText(context, textRenderer, "Coming soon", x + w / 2, y + 112, theme.text());
        UiRender.centeredText(context, textRenderer, "This category is ready for the next milestone.", x + w / 2, y + 132, theme.mutedText());
    }


    private void startRenamingProfile(int slot) {
        AtmosphereProfile profile = ProfileManager.profile(slot);
        renamingProfileIndex = slot;
        renameProfileText = profile.name == null || profile.name.isBlank() ? "Profile " + (slot + 1) : profile.name;
        searchFocused = false;
        NotificationUtil.show("Type a new name, Enter to save, Esc to cancel");
    }

    private void finishRenamingProfile() {
        if (renamingProfileIndex < 0) {
            return;
        }

        ProfileManager.rename(renamingProfileIndex, renameProfileText);
        renamingProfileIndex = -1;
        renameProfileText = "";
        rebuildWidgets();
    }

    private void cancelRenamingProfile() {
        renamingProfileIndex = -1;
        renameProfileText = "";
        NotificationUtil.show("Rename cancelled");
        rebuildWidgets();
    }

    private boolean isRenamingProfile() {
        return renamingProfileIndex >= 0;
    }

    private void renderProfileRenameOverlay(DrawContext context, Theme theme) {
        if (!isRenamingProfile()) {
            return;
        }

        int modalW = Math.min(360, windowW - 80);
        int modalH = 116;
        int x = windowX + windowW / 2 - modalW / 2;
        int y = windowY + windowH / 2 - modalH / 2;

        context.fill(0, 0, width, height, 0xAA000000);
        UiRender.borderedRect(context, x, y, modalW, modalH, theme.panel(), theme.accent());

        UiRender.text(context, textRenderer, "Rename Profile " + (renamingProfileIndex + 1), x + 16, y + 14, theme.text());
        UiRender.text(context, textRenderer, "Enter saves · Esc cancels · Backspace deletes", x + 16, y + 29, theme.mutedText());
        UiRender.rect(context, x + 16, y + 42, modalW - 32, 1, theme.border());

        int fieldX = x + 16;
        int fieldY = y + 56;
        int fieldW = modalW - 32;
        UiRender.borderedRect(context, fieldX, fieldY, fieldW, 24, theme.panelAlt(), theme.accent());

        String visible = renameProfileText;
        while (textRenderer.getWidth(visible) > fieldW - 24 && visible.length() > 0) {
            visible = visible.substring(1);
        }

        UiRender.text(context, textRenderer, visible, fieldX + 8, fieldY + 8, theme.text());

        if (shouldShowCaret()) {
            int caretX = Math.min(fieldX + fieldW - 12, fieldX + 8 + textRenderer.getWidth(visible));
            context.fill(caretX + 2, fieldY + 6, caretX + 3, fieldY + 19, theme.accent());
        }

        UiRender.centeredText(context, textRenderer, "New name: " + renameProfileText.length() + "/24", x + modalW / 2, y + 92, theme.mutedText());
    }

    private void renderScrollIndicator(DrawContext context, Theme theme) {
        if (maxScroll <= 0) {
            return;
        }

        int trackX = windowX + windowW - 21;
        int trackY = windowY + 108;
        int trackH = windowH - 128;
        int thumbH = Math.max(24, trackH * trackH / (trackH + maxScroll));
        int thumbY = trackY + (trackH - thumbH) * scrollOffset / maxScroll;

        context.fill(trackX, trackY, trackX + 3, trackY + trackH, theme.panelAlt());
        context.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, theme.accent());
    }

    private void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        if (isRenamingProfile()) {
            return;
        }

        for (AtmosphereWidget widget : widgets) {
            if (widget.isHoveredPublic(mouseX, mouseY) && widget.getTooltip() != null && !widget.getTooltip().isEmpty()) {
                context.drawTooltip(textRenderer, List.of(Text.literal(widget.getTooltip())), mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (AtmosphereKeybinds.matchesOpenMenuMouse(click)) {
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }

        if (UiRender.hovered(click.x(), click.y(), closeX, closeY, closeSize, closeSize)) {
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }

        if (isRenamingProfile()) {
            return true;
        }

        if (UiRender.hovered(click.x(), click.y(), searchX, searchY, searchW, searchH)) {
            searchFocused = true;

            if (!searchQuery.isEmpty() && click.x() >= searchX + searchW - 22) {
                searchQuery = "";
                scrollOffset = 0;
                rebuildWidgets();
            }

            return true;
        } else {
            searchFocused = false;
        }

        for (AtmosphereWidget widget : widgets) {
            if (widget.mouseClicked(click.x(), click.y(), click.button())) {
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        for (AtmosphereWidget widget : widgets) {
            if (widget.mouseDragged(click.x(), click.y(), click.button(), offsetX, offsetY)) {
                return true;
            }
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        for (AtmosphereWidget widget : widgets) {
            if (widget.mouseReleased(click.x(), click.y(), click.button())) {
                return true;
            }
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    if (maxScroll > 0 && UiRender.hovered(mouseX, mouseY, windowX + 204, windowY + 104, windowW - 218, windowH - 116)) {
        int oldOffset = scrollOffset;
        int newOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * 18));

        if (newOffset != oldOffset) {
            scrollOffset = newOffset;
            rebuildWidgets();
        }

        return true;
    }

    return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
}

    @Override
    public boolean keyPressed(KeyInput input) {
        if (AtmosphereKeybinds.matchesOpenMenu(input)) {
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }

        if (isRenamingProfile()) {
            if (input.isEscape()) {
                cancelRenamingProfile();
                return true;
            }

            if (input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
                finishRenamingProfile();
                return true;
            }

            if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE && !renameProfileText.isEmpty()) {
                renameProfileText = renameProfileText.substring(0, renameProfileText.length() - 1);
                return true;
            }

            return true;
        }

        if (searchFocused) {
            if (input.isEscape()) {
                searchFocused = false;
                return true;
            }

            if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                scrollOffset = 0;
                rebuildWidgets();
                return true;
            }

            if (input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (isRenamingProfile() && input.isValidChar() && renameProfileText.length() < 24) {
            String typed = input.asString();
            if (!typed.equals("\n") && !typed.equals("\r")) {
                renameProfileText += typed;
            }
            return true;
        }

        if (searchFocused && input.isValidChar() && searchQuery.length() < 32) {
            searchQuery += input.asString();
            scrollOffset = 0;
            rebuildWidgets();
            return true;
        }

        return super.charTyped(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
