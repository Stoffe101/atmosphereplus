package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.client.AtmospherePlusClient;
import com.skrra.atmosphereplus.compat.CompatibilityUtil;
import com.skrra.atmosphereplus.config.AtmosphereProfile;
import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.config.ProfileManager;
import com.skrra.atmosphereplus.keybind.AtmosphereKeybinds;
import com.skrra.atmosphereplus.themes.CustomThemeData;
import com.skrra.atmosphereplus.themes.CustomThemeManager;
import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.widgets.ActionButtonWidget;
import com.skrra.atmosphereplus.ui.widgets.AtmosphereWidget;
import com.skrra.atmosphereplus.ui.widgets.CategoryButton;
import com.skrra.atmosphereplus.ui.widgets.ChoiceButtonWidget;
import com.skrra.atmosphereplus.ui.widgets.InfoCardWidget;
import com.skrra.atmosphereplus.ui.widgets.PresetCardWidget;
import com.skrra.atmosphereplus.ui.widgets.SectionLabelWidget;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AtmosphereScreen extends Screen {
    private final List<AtmosphereWidget> widgets = new ArrayList<>();

    private UiCategory selected = UiCategory.QUICK;
    private String searchQuery = "";
    private boolean searchFocused = false;
    private int renamingProfileIndex = -1;
    private String renamingThemeId = "";
    private String renameProfileText = "";
    private int selectedProfileIndex = 0;
    private final ThemeStudioState themeStudioState = new ThemeStudioState();

    private String confirmTitle = "";
    private String confirmMessage = "";
    private Runnable confirmAction = null;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int contentBottom = 0;

    private int sidebarScrollOffset = 0;
    private int sidebarMaxScroll = 0;

    private int searchResultCount = 0;

    private int lastScreenWidth = -1;
    private int lastScreenHeight = -1;
    private String lastLayoutKey = "";
    private LayoutProfile layoutProfile = null;

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
        loadLastSelectedTab();
        rebuildWidgets();
    }


private void loadLastSelectedTab() {
    try {
        String saved = ConfigManager.get().lastUiCategory;
        if (saved != null && !saved.isBlank()) {
            selected = UiCategory.valueOf(saved.toUpperCase(Locale.ROOT));
        }
    } catch (Exception ignored) {
        selected = UiCategory.QUICK;
        ConfigManager.get().lastUiCategory = selected.name();
        ConfigManager.save();
    }
}

private void selectCategory(UiCategory category) {
    selected = category;
    ConfigManager.get().lastUiCategory = category.name();
    ConfigManager.save();
}



private LayoutProfile layout() {
    if (layoutProfile == null) {
        layoutProfile = LayoutProfile.create(width, height);
    }
    return layoutProfile;
}

private int sidebarPanelTop() {
    return windowY + layout().topBarHeight() + 10;
}

private int sidebarPanelBottom() {
    return windowY + windowH - layout().outerMargin();
}

private int sidebarLeft() {
    return windowX + layout().outerMargin();
}

private int sidebarWidth() {
    return layout().sidebarWidth();
}

private int sidebarRight() {
    return sidebarLeft() + sidebarWidth();
}

private int sidebarListTop() {
    return sidebarPanelTop() + layout().sidebarHeaderHeight();
}

private int sidebarStep() {
    int count = Math.max(1, visibleCategories().size());
    int available = Math.max(24, sidebarPanelBottom() - sidebarListTop() - 4);
    return layout().sidebarStep(count, available);
}


private int sidebarViewportHeight() {
    return Math.max(24, sidebarPanelBottom() - sidebarListTop() - 4);
}

private int sidebarContentHeight() {
    if (isSearching()) {
        return 0;
    }

    return Math.max(0, visibleCategories().size() * sidebarStep());
}

private void clampSidebarScroll() {
    sidebarMaxScroll = Math.max(0, sidebarContentHeight() - sidebarViewportHeight());
    sidebarScrollOffset = Math.max(0, Math.min(sidebarScrollOffset, sidebarMaxScroll));
}

private boolean isMouseOverSidebar(double mouseX, double mouseY) {
    return UiRender.hovered(mouseX, mouseY, sidebarLeft(), sidebarListTop() - 2, sidebarWidth(), sidebarViewportHeight() + 2);
}

private int contentGap() {
    return layout().contentGap();
}

private int contentLeft() {
    return sidebarRight() + contentGap();
}

private int contentRight() {
    return windowX + windowW - layout().outerMargin();
}

private int contentWidth() {
    return Math.max(120, contentRight() - contentLeft());
}

private int contentPadding() {
    return layout().contentPadding();
}

private int contentScrollbarReserve() {
    return 10;
}

private int contentWidgetLeft() {
    return contentLeft() + contentPadding();
}

private int contentWidgetWidth() {
    return Math.max(100, contentWidth() - contentPadding() * 2 - contentScrollbarReserve());
}





    private void rebuildWidgets() {
    layoutProfile = LayoutProfile.create(width, height);
    lastLayoutKey = layoutProfile.key();
    lastScreenWidth = width;
    lastScreenHeight = height;
    widgets.clear();
    searchResultCount = 0;

    windowW = Math.max(1, layoutProfile.maxWindowWidth());
    windowH = Math.max(1, layoutProfile.maxWindowHeight());
    windowX = (width - windowW) / 2;
    windowY = (height - windowH) / 2;
    contentBottom = windowY + windowH - layout().outerMargin() - contentPadding();

    searchW = Math.min(260, Math.max(120, windowW / 3));
    searchH = 20;
    searchX = windowX + windowW / 2 - searchW / 2;
    searchY = windowY + 14;

    closeSize = 20;
    closeX = windowX + windowW - 36;
    closeY = windowY + 14;

    int sidebarX = sidebarLeft();
    int sidebarY = sidebarListTop();
    int sidebarW = sidebarWidth();
    int sidebarStep = sidebarStep();
    clampSidebarScroll();

    if (!isSearching()) {
        int i = 0;
        for (UiCategory category : visibleCategories()) {
            widgets.add(new CategoryButton(sidebarX, sidebarY + i * sidebarStep - sidebarScrollOffset, sidebarW, category, () -> selected, c -> {
                selectCategory(c);
                searchFocused = false;
                scrollOffset = 0;
                rebuildWidgets();
            }));
            i++;
        }
    }

    int contentX = contentWidgetLeft();
    int contentY = windowY + layout().contentTopOffset() - scrollOffset;
    int contentW = contentWidgetWidth();
    int finalY = contentY;

    if (isSearching()) {
        finalY = addSearchResultWidgets(contentX, contentY, contentW);
        maxScroll = Math.max(0, finalY + scrollOffset - contentBottom);
        scrollOffset = Math.min(scrollOffset, maxScroll);
        return;
    }

    switch (selected) {
        case QUICK -> finalY = addQuickWidgets(contentX, contentY, contentW);
        case WEATHER -> finalY = addWeatherWidgets(contentX, contentY, contentW);
        case TIME -> finalY = addTimeWidgets(contentX, contentY, contentW);
        case SKY -> finalY = addSkyWidgets(contentX, contentY, contentW);
        case LIGHTING -> finalY = addLightingWidgets(contentX, contentY, contentW);
        case FOG -> finalY = addFogWidgets(contentX, contentY, contentW);
        case PARTICLES -> finalY = addParticlesWidgets(contentX, contentY, contentW);
        case THEMES -> finalY = addThemeWidgets(contentX, contentY, contentW);
        case THEME_STUDIO -> finalY = ThemeStudioPage.addWidgets(widgets, themeStudioState, themeStudioActions(), contentX, contentY, contentW);
        case PRESETS -> finalY = addPresetWidgets(contentX, contentY, contentW);
        case PROFILES -> finalY = addProfilesWidgets(contentX, contentY, contentW);
        case ADVANCED -> finalY = addAdvancedWidgets(contentX, contentY, contentW);
        default -> {
        }
    }

    maxScroll = Math.max(0, finalY + scrollOffset - contentBottom);
    scrollOffset = Math.min(scrollOffset, maxScroll);
}



private List<Integer> quickProfileOrder() {
    List<Integer> order = new ArrayList<>();
    for (int i = 0; i < ProfileManager.PROFILE_COUNT; i++) {
        order.add(i);
    }

    order.sort(
            Comparator
                    .comparing((Integer i) -> !ProfileManager.profile(i).pinned)
                    .thenComparing((Integer i) -> !ProfileManager.profile(i).favorite)
                    .thenComparing((Integer i) -> !ProfileManager.profile(i).saved)
                    .thenComparingInt(Integer::intValue)
    );

    return order;
}

private String quickProfileLabel(int slot) {
    AtmosphereProfile profile = ProfileManager.profile(slot);
    String base = (profile.name == null || profile.name.isBlank()) ? "Profile " + (slot + 1) : profile.name;
    String prefix = "";
    if (profile.pinned) prefix += "[P] ";
    if (profile.favorite) prefix += "[F] ";
    return prefix + base;
}

private String quickProfileDescription(int slot) {
    AtmosphereProfile profile = ProfileManager.profile(slot);
    if (!profile.saved) {
        return "Empty slot · open manager";
    }

    StringBuilder builder = new StringBuilder("Load saved profile");
    if (profile.pinned || profile.favorite) {
        builder.append(" · ");
        if (profile.pinned) builder.append("Pinned");
        if (profile.pinned && profile.favorite) builder.append(" · ");
        if (profile.favorite) builder.append("Favorite");
    }
    return builder.toString();
}

private boolean isPresetFavorite(String presetId) {
    return switch (presetId) {
        case "golden_hour" -> ConfigManager.get().favoritePresetGoldenHour;
        case "midnight" -> ConfigManager.get().favoritePresetMidnight;
        case "cozy_rain" -> ConfigManager.get().favoritePresetCozyRain;
        case "deep_fog" -> ConfigManager.get().favoritePresetDeepFog;
        case "bright_caves" -> ConfigManager.get().favoritePresetBrightCaves;
        case "clouds_off" -> ConfigManager.get().favoritePresetCloudsOff;
        default -> false;
    };
}

private void setPresetFavorite(String presetId, boolean value) {
    switch (presetId) {
        case "golden_hour" -> ConfigManager.get().favoritePresetGoldenHour = value;
        case "midnight" -> ConfigManager.get().favoritePresetMidnight = value;
        case "cozy_rain" -> ConfigManager.get().favoritePresetCozyRain = value;
        case "deep_fog" -> ConfigManager.get().favoritePresetDeepFog = value;
        case "bright_caves" -> ConfigManager.get().favoritePresetBrightCaves = value;
        case "clouds_off" -> ConfigManager.get().favoritePresetCloudsOff = value;
        default -> {
        }
    }
    ConfigManager.save();
}

private void togglePresetFavorite(String presetId, String label) {
    boolean newValue = !isPresetFavorite(presetId);
    setPresetFavorite(presetId, newValue);
    NotificationUtil.toggled(label + " favorite", newValue);
    rebuildWidgets();
}

private void applyQuickPreset(String presetId, String label, Runnable action) {
    ConfigManager.get().lastQuickPreset = presetId;
    action.run();
    ConfigManager.save();
    NotificationUtil.applied(label);
    rebuildWidgets();
}

private void runLastQuickPreset() {
    String preset = ConfigManager.get().lastQuickPreset;
    switch (preset) {
        case "golden_hour" -> applyQuickPreset("golden_hour", "Golden Hour", this::applyGoldenHour);
        case "midnight" -> applyQuickPreset("midnight", "Midnight Calm", this::applyMidnightCalm);
        case "cozy_rain" -> applyQuickPreset("cozy_rain", "Cozy Rain", this::applyCozyRain);
        case "deep_fog" -> applyQuickPreset("deep_fog", "Deep Fog", this::applyDeepFog);
        case "bright_caves" -> applyQuickPreset("bright_caves", "Bright Caves", this::applyBrightCaves);
        case "clouds_off" -> applyQuickPreset("clouds_off", "Clouds Off", () -> {
            ConfigManager.get().cloudOverride = true;
            ConfigManager.get().cloudMode = "OFF";
            clearActivePreset();
        });
        default -> NotificationUtil.show("No last preset yet");
    }
}

private void runLastQuickProfile() {
    int slot = ConfigManager.get().lastQuickProfile;
    if (ProfileManager.profile(slot).saved) {
        ProfileManager.load(slot);
        NotificationUtil.show("Loaded last profile: " + quickProfileLabel(slot));
        rebuildWidgets();
    } else {
        NotificationUtil.show("Last profile slot is empty");
    }
}




private String quickProfileDisplayName(int slot) {
    AtmosphereProfile profile = ProfileManager.profile(slot);
    String base = (profile.name == null || profile.name.isBlank()) ? "Profile " + (slot + 1) : profile.name;
    return base;
}

private int responsiveColumns(int contentW, int preferred, int minCardWidth) {
    int count = Math.max(1, contentW / minCardWidth);
    return Math.max(1, Math.min(preferred, count));
}

private int responsiveCardWidth(int contentW, int cols, int gap) {
    return (contentW - gap * (cols - 1)) / cols;
}

private int addQuickWidgets(int contentX, int contentY, int contentW) {
    int y = contentY;
    int gap = layout().sectionGap();

    widgets.add(new SectionLabelWidget(contentX, y, contentW, "Quick", "Fast access"));
    y += 28;

    int actionCols = layout().quickActionColumns(contentW);
    int actionW = responsiveCardWidth(contentW, actionCols, gap);
    int actionIndex = 0;

    widgets.add(new ActionButtonWidget(contentX + (actionIndex % actionCols) * (actionW + gap), y + (actionIndex / actionCols) * 46, actionW, "Last Profile", "Load last profile.", IconType.PRESETS, this::runLastQuickProfile));
    actionIndex++;
    widgets.add(new ActionButtonWidget(contentX + (actionIndex % actionCols) * (actionW + gap), y + (actionIndex / actionCols) * 46, actionW, "Last Preset", "Apply last preset.", IconType.SKY, this::runLastQuickPreset));
    actionIndex++;
    widgets.add(new ActionButtonWidget(contentX + (actionIndex % actionCols) * (actionW + gap), y + (actionIndex / actionCols) * 46, actionW, ConfigManager.get().fullbright ? "Fullbright: On" : "Fullbright: Off", "Click to toggle fullbright.", IconType.LIGHTING, () -> {
        ConfigManager.get().fullbright = !ConfigManager.get().fullbright;
        clearActivePreset();
        ConfigManager.save();
        NotificationUtil.toggled("Fullbright", ConfigManager.get().fullbright);
        rebuildWidgets();
    }));
    actionIndex++;
    widgets.add(new ActionButtonWidget(contentX + (actionIndex % actionCols) * (actionW + gap), y + (actionIndex / actionCols) * 46, actionW, "Clear Weather", "Set clear weather.", IconType.WEATHER, () -> {
        ConfigManager.get().weatherOverride = true;
        ConfigManager.get().weatherMode = "SUNNY";
        ConfigManager.get().rainIntensity = 0.0f;
        clearActivePreset();
        ConfigManager.save();
        NotificationUtil.applied("Clear Weather");
        rebuildWidgets();
    }));
    actionIndex++;
    widgets.add(new ActionButtonWidget(contentX + (actionIndex % actionCols) * (actionW + gap), y + (actionIndex / actionCols) * 46, actionW, "Server Visuals", "Use server visuals.", IconType.HOME, () -> {
        resetWeather();
        resetTime();
        resetSky();
        NotificationUtil.applied("Server Visuals");
        rebuildWidgets();
    }));
    actionIndex++;
    widgets.add(new ActionButtonWidget(contentX + (actionIndex % actionCols) * (actionW + gap), y + (actionIndex / actionCols) * 46, actionW, "Reset All", "Reset all visuals.", IconType.ADVANCED, this::confirmResetAllVisuals));

    y += ((actionIndex / actionCols) + 1) * 46 + 8;

    widgets.add(new SectionLabelWidget(contentX, y, contentW, "Profiles", "Pinned first"));
    y += 28;

    List<Integer> ordered = quickProfileOrder();
    int profileCols = layout().quickProfileColumns(contentW);
    int profileW = responsiveCardWidth(contentW, profileCols, gap);

    for (int i = 0; i < ordered.size(); i++) {
        int slot = ordered.get(i);
        int x = contentX + (i % profileCols) * (profileW + gap);
        int rowY = y + (i / profileCols) * 46;
        String desc = ProfileManager.profile(slot).saved ? "Load" : "Open";

        widgets.add(new ChoiceButtonWidget(x, rowY, profileW, quickProfileDisplayName(slot), desc, IconType.PRESETS, () -> ConfigManager.get().lastQuickProfile == slot, () -> {
            ConfigManager.get().lastQuickProfile = slot;
            ConfigManager.save();
            if (ProfileManager.profile(slot).saved) {
                ProfileManager.load(slot);
                NotificationUtil.applied(quickProfileDisplayName(slot));
            } else {
                selectedProfileIndex = slot;
                selectCategory(UiCategory.PROFILES);
                scrollOffset = 0;
                NotificationUtil.show("Open profile manager for slot " + (slot + 1));
            }
            rebuildWidgets();
        }));
    }

    y += ((ordered.size() - 1) / profileCols + 1) * 46 + 8;

    widgets.add(new SectionLabelWidget(contentX, y, contentW, "Presets", "Most used"));
    y += 28;

    int presetCols = responsiveColumns(contentW, 2, 210);
    int presetW = responsiveCardWidth(contentW, presetCols, gap);
    int presetIndex = 0;

    widgets.add(new ChoiceButtonWidget(contentX + (presetIndex % presetCols) * (presetW + gap), y + (presetIndex / presetCols) * 46, presetW, "Golden Hour", "Warm day", IconType.SKY, () -> ConfigManager.get().lastQuickPreset.equals("golden_hour"), () -> applyQuickPreset("golden_hour", "Golden Hour", this::applyGoldenHour)));
    presetIndex++;
    widgets.add(new ChoiceButtonWidget(contentX + (presetIndex % presetCols) * (presetW + gap), y + (presetIndex / presetCols) * 46, presetW, "Midnight", "Clear night", IconType.TIME, () -> ConfigManager.get().lastQuickPreset.equals("midnight"), () -> applyQuickPreset("midnight", "Midnight Calm", this::applyMidnightCalm)));
    presetIndex++;
    widgets.add(new ChoiceButtonWidget(contentX + (presetIndex % presetCols) * (presetW + gap), y + (presetIndex / presetCols) * 46, presetW, "Cozy Rain", "Rain mood", IconType.WEATHER, () -> ConfigManager.get().lastQuickPreset.equals("cozy_rain"), () -> applyQuickPreset("cozy_rain", "Cozy Rain", this::applyCozyRain)));
    presetIndex++;
    widgets.add(new ChoiceButtonWidget(contentX + (presetIndex % presetCols) * (presetW + gap), y + (presetIndex / presetCols) * 46, presetW, "Deep Fog", "Cinematic fog", IconType.FOG, () -> ConfigManager.get().lastQuickPreset.equals("deep_fog"), () -> applyQuickPreset("deep_fog", "Deep Fog", this::applyDeepFog)));
    presetIndex++;
    widgets.add(new ChoiceButtonWidget(contentX + (presetIndex % presetCols) * (presetW + gap), y + (presetIndex / presetCols) * 46, presetW, "Bright Caves", "Bright boost", IconType.LIGHTING, () -> ConfigManager.get().lastQuickPreset.equals("bright_caves"), () -> applyQuickPreset("bright_caves", "Bright Caves", this::applyBrightCaves)));
    presetIndex++;
    widgets.add(new ChoiceButtonWidget(contentX + (presetIndex % presetCols) * (presetW + gap), y + (presetIndex / presetCols) * 46, presetW, "More Presets", "Open full list", IconType.SKY, () -> false, () -> {
        selected = UiCategory.PRESETS;
        scrollOffset = 0;
        rebuildWidgets();
    }));

    y += ((presetIndex / presetCols) + 1) * 46 + 8;

    widgets.add(new SectionLabelWidget(contentX, y, contentW, "Manage", "More options"));
    y += 28;

    int manageCols = layout().manageColumns(contentW);
    int manageW = responsiveCardWidth(contentW, manageCols, gap);

    widgets.add(new ActionButtonWidget(contentX, y, manageW, "Profiles", "Rename, pin, favorite and save.", IconType.PRESETS, () -> {
        selected = UiCategory.PROFILES;
        scrollOffset = 0;
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(contentX + (manageCols > 1 ? manageW + gap : 0), y + (manageCols > 1 ? 0 : 46), manageW, "All Presets", "Open the full preset page.", IconType.SKY, () -> {
        selected = UiCategory.PRESETS;
        scrollOffset = 0;
        rebuildWidgets();
    }));

    return y + (manageCols > 1 ? 42 : 88);
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

    int gap = layout().sectionGap();
    int modeCols = responsiveColumns(contentW, 3, 150);
    int modeW = responsiveCardWidth(contentW, modeCols, gap);
    int modeY = contentY + 54;
    int weatherIndex = 0;

    addWeatherChoice(contentX + (weatherIndex % modeCols) * (modeW + gap), modeY + (weatherIndex / modeCols) * 50, modeW, "Server", "Use normal server weather.", IconType.WEATHER, "SERVER");
    weatherIndex++;
    addWeatherChoice(contentX + (weatherIndex % modeCols) * (modeW + gap), modeY + (weatherIndex / modeCols) * 50, modeW, "Sunny", "Force clear visual weather.", IconType.SKY, "SUNNY");
    weatherIndex++;
    addWeatherChoice(contentX + (weatherIndex % modeCols) * (modeW + gap), modeY + (weatherIndex / modeCols) * 50, modeW, "Rain", "Force rainy visual mood.", IconType.WEATHER, "RAIN");
    weatherIndex++;
    addWeatherChoice(contentX + (weatherIndex % modeCols) * (modeW + gap), modeY + (weatherIndex / modeCols) * 50, modeW, "Thunder", "Force stormy visual mood.", IconType.LIGHTING, "THUNDER");
    weatherIndex++;
    addWeatherChoice(contentX + (weatherIndex % modeCols) * (modeW + gap), modeY + (weatherIndex / modeCols) * 50, modeW, "Snow", "Force snowy visual mood later.", IconType.FOG, "SNOW");
    weatherIndex++;

    int sliderY = modeY + ((weatherIndex + modeCols - 1) / modeCols) * 50 + 4;

    widgets.add(new SliderWidget(contentX, sliderY, contentW, "Rain intensity", "Adjusts how strong rain visuals should appear.", 0f, 1f, () -> ConfigManager.get().rainIntensity, v -> {
        ConfigManager.get().rainIntensity = v;
            clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%"));

    widgets.add(new ToggleWidget(contentX, sliderY + 62, contentW, "Thunder sounds", "Controls whether thunder audio should be allowed by Atmosphere+.", () -> ConfigManager.get().thunderSounds, v -> {
        ConfigManager.get().thunderSounds = v;
            clearActivePreset();
        ConfigManager.save();
    }));

    addResetButton(contentX, sliderY + 116, contentW, "Reset Weather", "Server weather, full rain intensity, thunder sounds on.", IconType.WEATHER, this::resetWeather);

    return sliderY + 162;
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

    int gap = layout().sectionGap();
    int timeCols = responsiveColumns(contentW, 3, 132);
    int buttonW = responsiveCardWidth(contentW, timeCols, gap);
    int buttonY = contentY + 54;
    int timeIndex = 0;

    addTimePresetButton(contentX + (timeIndex % timeCols) * (buttonW + gap), buttonY + (timeIndex / timeCols) * 44, buttonW, "Sunrise", "0", 0);
    timeIndex++;
    addTimePresetButton(contentX + (timeIndex % timeCols) * (buttonW + gap), buttonY + (timeIndex / timeCols) * 44, buttonW, "Morning", "1000", 1000);
    timeIndex++;
    addTimePresetButton(contentX + (timeIndex % timeCols) * (buttonW + gap), buttonY + (timeIndex / timeCols) * 44, buttonW, "Day", "6000", 6000);
    timeIndex++;
    addTimePresetButton(contentX + (timeIndex % timeCols) * (buttonW + gap), buttonY + (timeIndex / timeCols) * 44, buttonW, "Sunset", "12000", 12000);
    timeIndex++;
    addTimePresetButton(contentX + (timeIndex % timeCols) * (buttonW + gap), buttonY + (timeIndex / timeCols) * 44, buttonW, "Night", "15000", 15000);
    timeIndex++;
    addTimePresetButton(contentX + (timeIndex % timeCols) * (buttonW + gap), buttonY + (timeIndex / timeCols) * 44, buttonW, "Midnight", "18000", 18000);
    timeIndex++;

    int sliderY = buttonY + ((timeIndex + timeCols - 1) / timeCols) * 44 + 6;

    widgets.add(new SliderWidget(contentX, sliderY, contentW, "Visual time", "0 sunrise, 6000 day, 12000 sunset, 18000 midnight.", 0f, 24000f, () -> (float) ConfigManager.get().visualTime, v -> {
        ConfigManager.get().visualTime = Math.round(v);
            clearActivePreset();
        ConfigManager.save();
    }, this::formatMinecraftTime));

    widgets.add(new ToggleWidget(contentX, sliderY + 62, contentW, "Freeze visual time", "Reserved for future smooth time animation modes.", () -> ConfigManager.get().freezeVisualTime, v -> {
        ConfigManager.get().freezeVisualTime = v;
            clearActivePreset();
        ConfigManager.save();
    }));

    addResetButton(contentX, sliderY + 116, contentW, "Reset Time", "Return to server time and default visual time.", IconType.TIME, this::resetTime);

    return sliderY + 162;
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
    int y = contentY;

    widgets.add(new SectionLabelWidget(contentX, y, contentW, "Clouds", "Stable controls"));
    y += 30;

    widgets.add(new ToggleWidget(contentX, y, contentW, "Override clouds visually", "Client-side cloud mode only. Works best outside shader packs.", () -> ConfigManager.get().cloudOverride, v -> {
        ConfigManager.get().cloudOverride = v;
        clearActivePreset();
        ConfigManager.save();
    }));

    int gap = layout().sectionGap();
    int cloudCols = responsiveColumns(contentW, 3, 150);
    int modeW = responsiveCardWidth(contentW, cloudCols, gap);
    int rowY = y + 54;
    int cloudIndex = 0;
    addCloudChoice(contentX + (cloudIndex % cloudCols) * (modeW + gap), rowY + (cloudIndex / cloudCols) * 50, modeW, "Server", "Use normal Minecraft cloud settings.", "SERVER");
    cloudIndex++;
    addCloudChoice(contentX + (cloudIndex % cloudCols) * (modeW + gap), rowY + (cloudIndex / cloudCols) * 50, modeW, "Off", "Hide clouds visually.", "OFF");
    cloudIndex++;
    addCloudChoice(contentX + (cloudIndex % cloudCols) * (modeW + gap), rowY + (cloudIndex / cloudCols) * 50, modeW, "Fast", "Force fast clouds visually.", "FAST");
    cloudIndex++;
    addCloudChoice(contentX + (cloudIndex % cloudCols) * (modeW + gap), rowY + (cloudIndex / cloudCols) * 50, modeW, "Fancy", "Force fancy cloud rendering mode.", "FANCY");
    cloudIndex++;

    y = rowY + ((cloudIndex + cloudCols - 1) / cloudCols) * 50 + 4;

    widgets.add(new SectionLabelWidget(contentX, y, contentW, "Renderer", "Known working"));
    y += 30;

    widgets.add(new ToggleWidget(contentX, y, contentW, "Enable renderer controls", "Enables the working alpha renderer hooks. Shader packs may ignore them.", () -> ConfigManager.get().experimentalRendererControls, v -> {
        ConfigManager.get().experimentalRendererControls = v;
        ConfigManager.save();
        if (v && CompatibilityUtil.isIrisLoaded() && ConfigManager.get().shaderAwareWarnings) {
            NotificationUtil.show("Iris detected: shaders may override renderer hooks");
        }
        rebuildWidgets();
    }));

    y += 54;

    int rendererCols = responsiveColumns(contentW, 2, 210);
    int halfW = responsiveCardWidth(contentW, rendererCols, gap);

    widgets.add(new SliderWidget(contentX, y, halfW, "Cloud height", "Working without shaders. Changes cloud layer height.", 0.25f, 2.0f, () -> ConfigManager.get().cloudHeight, v -> {
        ConfigManager.get().cloudHeight = v;
        ConfigManager.get().experimentalRendererControls = true;
        ConfigManager.get().cloudDistanceOverride = false;
        clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%"));

    widgets.add(new SliderWidget(contentX + (rendererCols > 1 ? halfW + gap : 0), y + (rendererCols > 1 ? 0 : 62), halfW, "Star brightness", "Working without shaders. Shader packs may override stars.", 0f, 2f, () -> ConfigManager.get().starBrightness, v -> {
        ConfigManager.get().starBrightness = v;
        ConfigManager.get().experimentalRendererControls = true;
        ConfigManager.get().cloudDistanceOverride = false;
        clearActivePreset();
        ConfigManager.save();
    }, value -> Math.round(value * 100f) + "%"));

    y += rendererCols > 1 ? 62 : 124;

    widgets.add(new ActionButtonWidget(contentX, y, halfW, "Reset Renderer", "Reset cloud height, stars and experimental renderer values.", IconType.ADVANCED, () -> {
        resetRendererSettings();
        clearActivePreset();
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(contentX + (rendererCols > 1 ? halfW + gap : 0), y + (rendererCols > 1 ? 0 : 46), halfW, "Shader note", CompatibilityUtil.isIrisLoaded() ? "Iris detected. Shaders can override these hooks." : "Vanilla/Sodium path detected.", IconType.SKY, () -> {
        NotificationUtil.show(CompatibilityUtil.isIrisLoaded() ? "Shaders may override cloud height and stars" : "Renderer hooks should be easiest to test without shaders");
    }));

    y += rendererCols > 1 ? 46 : 92;

    widgets.add(new SectionLabelWidget(contentX, y, contentW, "Notes", "Renderer limits"));
    y += 30;

    widgets.add(new PresetCardWidget(contentX, y, contentW, "Cloud distance removed", "Removed for stability. Cloud height and stars are kept.", IconType.SKY, () -> false, () -> {
        NotificationUtil.show("Cloud distance was removed for stability");
    }));

    y += 64;

    addResetButton(contentX, y, contentW, "Reset Sky / Clouds", "Reset stable cloud settings and renderer values.", IconType.SKY, this::resetSky);

    return y + 52;
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


private void selectProfile(int slot) {
    selectedProfileIndex = Math.max(0, Math.min(ProfileManager.PROFILE_COUNT - 1, slot));
    rebuildWidgets();
}


private int addProfilesWidgets(int contentX, int contentY, int contentW) {
    selectedProfileIndex = Math.max(0, Math.min(ProfileManager.PROFILE_COUNT - 1, selectedProfileIndex));

    int toolbarW = (contentW - 20) / 3;

    widgets.add(new ActionButtonWidget(contentX, contentY, toolbarW, "Export", "Export all profiles to config/atmosphereplus-profiles-backup.json.", IconType.PRESETS, () -> {
        ProfileManager.exportProfiles();
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(contentX + toolbarW + 10, contentY, toolbarW, "Import", "Import profiles from the backup file after confirmation.", IconType.PRESETS, this::confirmImportProfiles));

    widgets.add(new ActionButtonWidget(contentX + (toolbarW + 10) * 2, contentY, toolbarW, "Clear All", "Clear all profile slots after confirmation.", IconType.ADVANCED, this::confirmClearAllProfiles));

    int y = contentY + 54;
    int leftW = Math.min(250, Math.max(210, contentW / 3));
    int gap = 16;
    int rightX = contentX + leftW + gap;
    int rightW = contentW - leftW - gap;

    AtmosphereProfile[] profiles = ProfileManager.profiles();

    for (int i = 0; i < profiles.length; i++) {
        int slot = i;
        AtmosphereProfile profile = profiles[i];
        String title = quickProfileLabel(slot);
        String description = profile.saved ? "Saved slot · click to manage" : "Empty slot · click to manage";

        widgets.add(new ChoiceButtonWidget(contentX, y + i * 46, leftW, title, description, IconType.PRESETS, () -> selectedProfileIndex == slot, () -> {
            selectedProfileIndex = slot;
            rebuildWidgets();
        }));
    }

    AtmosphereProfile selectedProfile = ProfileManager.profile(selectedProfileIndex);
    String selectedTitle = quickProfileLabel(selectedProfileIndex);
    String selectedDescription = selectedProfile.saved
            ? "Selected profile. Load it, rename it, or adjust its quick status."
            : "Selected empty slot. Save current settings to create it.";

    widgets.add(new PresetCardWidget(rightX, y, rightW, selectedTitle, selectedDescription, IconType.PRESETS, () -> ProfileManager.isActive(selectedProfileIndex), () -> {
        if (ProfileManager.profile(selectedProfileIndex).saved) {
            ProfileManager.load(selectedProfileIndex);
            ConfigManager.get().lastQuickProfile = selectedProfileIndex;
            ConfigManager.save();
            NotificationUtil.applied(quickProfileLabel(selectedProfileIndex));
        } else {
            ProfileManager.saveCurrentTo(selectedProfileIndex);
            ConfigManager.get().lastQuickProfile = selectedProfileIndex;
            ConfigManager.save();
            NotificationUtil.show("Saved current settings to " + quickProfileLabel(selectedProfileIndex));
        }
        rebuildWidgets();
    }));

    int halfW = (rightW - 10) / 2;
    int actionY = y + 86;

    widgets.add(new ActionButtonWidget(rightX, actionY, halfW, "Load", selectedProfile.saved ? "Load this saved atmosphere profile." : "This slot is empty. Save it first.", IconType.PRESETS, () -> {
        if (ProfileManager.profile(selectedProfileIndex).saved) {
            ProfileManager.load(selectedProfileIndex);
            ConfigManager.get().lastQuickProfile = selectedProfileIndex;
            ConfigManager.save();
            NotificationUtil.applied(quickProfileLabel(selectedProfileIndex));
        } else {
            NotificationUtil.show("Profile " + (selectedProfileIndex + 1) + " is empty");
        }
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(rightX + halfW + 10, actionY, halfW, "Save Current", "Overwrite this slot with your current Atmosphere+ settings.", IconType.PRESETS, () -> {
        ProfileManager.saveCurrentTo(selectedProfileIndex);
        ConfigManager.get().lastQuickProfile = selectedProfileIndex;
        ConfigManager.save();
        NotificationUtil.show("Saved " + quickProfileLabel(selectedProfileIndex));
        rebuildWidgets();
    }));

    actionY += 46;

    widgets.add(new ActionButtonWidget(rightX, actionY, halfW, "Rename", "Rename this selected profile slot.", IconType.PRESETS, () -> startRenamingProfile(selectedProfileIndex)));
    widgets.add(new ActionButtonWidget(rightX + halfW + 10, actionY, halfW, "Clear", "Clear this selected profile slot after confirmation.", IconType.ADVANCED, () -> confirmClearProfile(selectedProfileIndex)));

    actionY += 46;

    widgets.add(new ActionButtonWidget(rightX, actionY, halfW, selectedProfile.favorite ? "Favorite: On" : "Favorite: Off", "Show this profile earlier in the Quick tab.", IconType.PRESETS, () -> {
        selectedProfile.favorite = !selectedProfile.favorite;
        ConfigManager.save();
        NotificationUtil.toggled(quickProfileLabel(selectedProfileIndex) + " favorite", selectedProfile.favorite);
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(rightX + halfW + 10, actionY, halfW, selectedProfile.pinned ? "Pinned: On" : "Pinned: Off", "Pinned profiles always appear first in the Quick tab.", IconType.ADVANCED, () -> {
        selectedProfile.pinned = !selectedProfile.pinned;
        ConfigManager.save();
        NotificationUtil.toggled(quickProfileLabel(selectedProfileIndex) + " pinned", selectedProfile.pinned);
        rebuildWidgets();
    }));

    actionY += 46;

    widgets.add(new ActionButtonWidget(rightX, actionY, rightW, "Quick menu", "Return to the Quick tab with your latest profile ordering.", IconType.HOME, () -> {
        selected = UiCategory.QUICK;
        scrollOffset = 0;
        rebuildWidgets();
    }));

    return Math.max(y + profiles.length * 46, actionY + 42);
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


private int addCompatibilityWidgets(int contentX, int contentY, int contentW) {
    int y = contentY;
    int cardW = (contentW - 12) / 2;

    widgets.add(new ActionButtonWidget(contentX, y, contentW, "Compatibility Status", CompatibilityUtil.renderCompatibilitySummary(), IconType.ADVANCED, () -> {
        NotificationUtil.show(CompatibilityUtil.renderCompatibilitySummary());
    }));

    y += 42;

    widgets.add(new ActionButtonWidget(contentX, y, cardW, "Sodium", CompatibilityUtil.sodiumStatus(), IconType.LIGHTING, () -> {
        NotificationUtil.show("Sodium: " + CompatibilityUtil.sodiumStatus());
    }));

    widgets.add(new ActionButtonWidget(contentX + cardW + 12, y, cardW, "Iris / Shaders", CompatibilityUtil.irisStatus(), IconType.SKY, () -> {
        NotificationUtil.show("Iris: shader packs can override sky/cloud renderer hooks");
    }));

    y += 42;

    widgets.add(new ActionButtonWidget(contentX, y, cardW, "Reset Renderer", "Reset cloud height, stars and experimental renderer values.", IconType.ADVANCED, () -> {
        resetRendererSettings();
        clearActivePreset();
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(contentX + cardW + 12, y, cardW, "Shader Safe Reset", "Reset fog, sky/clouds, lighting and renderer values.", IconType.FOG, () -> {
        resetFog();
        resetSky();
        resetLighting();
        resetRendererSettings();
        NotificationUtil.show("Shader-sensitive visuals reset");
        rebuildWidgets();
    }));

    y += 42;

    widgets.add(new ActionButtonWidget(contentX, y, cardW, "Vanilla Safe Mode", "Reset all visual overrides to vanilla-style defaults.", IconType.HOME, () -> {
        applyVanillaSafeMode();
        NotificationUtil.show("Applied Vanilla Safe Mode");
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(contentX + cardW + 12, y, cardW, "Sodium/Iris Safe", "Safe setup for Sodium/Iris and shader packs.", IconType.SKY, () -> {
        applySodiumIrisSafeMode();
        NotificationUtil.show("Applied Sodium/Iris Safe Mode");
        rebuildWidgets();
    }));

    y += 42;

    widgets.add(new ActionButtonWidget(contentX, y, cardW, ConfigManager.get().shaderAwareWarnings ? "Warnings: On" : "Warnings: Off", "Toggle shader-aware warning messages.", IconType.ADVANCED, () -> {
        ConfigManager.get().shaderAwareWarnings = !ConfigManager.get().shaderAwareWarnings;
        ConfigManager.save();
        NotificationUtil.toggled("Shader-aware warnings", ConfigManager.get().shaderAwareWarnings);
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(contentX + cardW + 12, y, cardW, "Known Working", "Cloud height and star brightness work best without shader packs.", IconType.PRESETS, () -> {
        NotificationUtil.show("Known working: cloud height and stars. Shader-limited: most shader packs.");
    }));

    return y + 46;
}

private int addAdvancedWidgets(int contentX, int contentY, int contentW) {
    int cardW = (contentW - 12) / 2;
    int y = contentY;

    y = addCompatibilityWidgets(contentX, y, contentW);
    y += 10;

    addResetButton(contentX, y, cardW, "Reset All Visuals", "Reset weather, time, sky, fog, lighting and particles after confirmation.", IconType.ADVANCED, this::confirmResetAllVisuals);

    widgets.add(new ActionButtonWidget(contentX + cardW + 12, y, cardW, "Performance Clear", "Apply a low-visual-intensity preset for cleaner gameplay.", IconType.PRESETS, () -> {
        applyPerformanceClear();
        NotificationUtil.show("Applied Performance Clear");
        rebuildWidgets();
    }));

    y += 52;

    widgets.add(new ActionButtonWidget(contentX, y, cardW, "Shader Friendly Preset", "Use a safer setup when Iris shader packs override fog/sky/lighting.", IconType.ADVANCED, () -> {
        applyShaderFriendly();
        NotificationUtil.show("Applied Shader Friendly");
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(contentX + cardW + 12, y, cardW, "Screenshot Clear", "Apply a clean sunny setup with no clouds and fewer particles.", IconType.SKY, () -> {
        applyScreenshotClear();
        NotificationUtil.show("Applied Screenshot Clear");
        rebuildWidgets();
    }));

    y += 52;

    addResetButton(contentX, y, cardW, "Reset Weather", "Restore server weather, full rain intensity and thunder sounds.", IconType.WEATHER, this::resetWeather);
    addResetButton(contentX + cardW + 12, y, cardW, "Reset Time", "Return to server time and default visual time.", IconType.TIME, this::resetTime);

    y += 52;

    addResetButton(contentX, y, cardW, "Reset Sky / Clouds", "Return cloud visuals to server/default settings.", IconType.SKY, this::resetSky);
    addResetButton(contentX + cardW + 12, y, cardW, "Reset Fog", "Disable custom fog and restore default fog values.", IconType.FOG, this::resetFog);

    y += 52;

    addResetButton(contentX, y, cardW, "Reset Lighting", "Disable fullbright and restore gamma to 100%.", IconType.LIGHTING, this::resetLighting);
    addResetButton(contentX + cardW + 12, y, cardW, "Reset Particles", "Restore normal particle amount.", IconType.PARTICLES, this::resetParticles);

    y += 52;

    widgets.add(new ActionButtonWidget(contentX, y, cardW, "Profiles", "Go to save, load, rename, export or import profiles.", IconType.PRESETS, () -> {
        selected = UiCategory.PROFILES;
        scrollOffset = 0;
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(contentX + cardW + 12, y, cardW, "Presets", "Go to built-in atmosphere presets.", IconType.SKY, () -> {
        selected = UiCategory.PRESETS;
        scrollOffset = 0;
        rebuildWidgets();
    }));

    y += 52;

    widgets.add(new ActionButtonWidget(contentX, y, cardW, "Export Profiles", "Write profile slots to config/atmosphereplus-profiles-backup.json.", IconType.PRESETS, () -> {
        ProfileManager.exportProfiles();
        rebuildWidgets();
    }));

    widgets.add(new ActionButtonWidget(contentX + cardW + 12, y, cardW, "Import Profiles", "Import profile backup after confirmation.", IconType.PRESETS, this::confirmImportProfiles));

    y += 52;

    widgets.add(new ActionButtonWidget(contentX, y, cardW, "Clear All Profiles", "Clear all profile slots after confirmation.", IconType.ADVANCED, this::confirmClearAllProfiles));

    widgets.add(new ActionButtonWidget(contentX + cardW + 12, y, cardW, "Version Info", "Current dev build: " + AtmospherePlusClient.VERSION, IconType.ADVANCED, () -> {
        NotificationUtil.show("Atmosphere+ " + AtmospherePlusClient.VERSION);
    }));

    y += 52;

    widgets.add(new ActionButtonWidget(contentX, y, contentW, "Known Issues", "Cloud distance is disabled for now; check KNOWN_ISSUES.md in the ZIP.", IconType.FOG, () -> {
        NotificationUtil.show("Known issue: cloud distance disabled for now");
    }));

    return y + 52;
}

    private int addThemeWidgets(int contentX, int contentY, int contentW) {
        int y = contentY;
        widgets.add(new SectionLabelWidget(contentX, y, contentW, "Built-in Themes", "Browse and apply bundled themes"));
        y += 30;

        int columns = contentW >= 520 ? 2 : 1;
        int gap = 10;
        int themeW = (contentW - gap * (columns - 1)) / columns;
        int index = 0;

        for (String themeId : ThemeManager.builtIns().keySet()) {
            int col = index % columns;
            int row = index / columns;
            int x = contentX + col * (themeW + gap);
            int widgetY = y + row * 48;
            String label = ThemeManager.builtIns().get(themeId).displayName();

            widgets.add(new ToggleWidget(x, widgetY, themeW, label, "Switches the Atmosphere+ interface to the " + label + " theme.", () -> ConfigManager.get().theme.equals(themeId), v -> {
                if (v) {
                    ThemeManager.setTheme(themeId);
                    themeStudioState.selectCurrentTheme();
                }
            }));

            index++;
        }

        y += ((index + columns - 1) / columns) * 48 + 10;
        widgets.add(new SectionLabelWidget(contentX, y, contentW, "Custom Themes", "Custom theme library"));
        y += 30;

        if (!CustomThemeManager.hasCustomThemes()) {
            widgets.add(new InfoCardWidget(
                    contentX,
                    y,
                    contentW,
                    66,
                    "No custom themes yet",
                    "Open Theme Studio to create a theme or duplicate an existing one.",
                    IconType.THEMES
            ));

            return y + 76;
        }

        index = 0;
        for (CustomThemeData data : CustomThemeManager.all().values()) {
            int col = index % columns;
            int row = index / columns;
            int x = contentX + col * (themeW + gap);
            int widgetY = y + row * 48;
            String themeId = data.id;

            widgets.add(new ToggleWidget(x, widgetY, themeW, data.displayName, "Apply this custom theme.", () -> ConfigManager.get().theme.equals(themeId), v -> {
                if (v) {
                    ThemeManager.setTheme(themeId);
                    themeStudioState.selectTheme(themeId);
                }
            }));

            index++;
        }

        return y + ((index + columns - 1) / columns) * 48 + 10;
    }

private ThemeStudioPage.Actions themeStudioActions() {
    return new ThemeStudioPage.Actions() {
        @Override
        public void createTheme() {
            createCustomTheme();
        }

        @Override
        public void duplicateTheme(String sourceId) {
            duplicateCustomTheme(sourceId);
        }

        @Override
        public void renameTheme(String themeId) {
            startRenamingTheme(themeId);
        }

        @Override
        public void deleteTheme(String themeId) {
            confirmDeleteTheme(themeId);
        }

        @Override
        public void saveTheme(String themeId) {
            saveCustomTheme(themeId);
        }

        @Override
        public void reloadThemes() {
            reloadCustomThemes();
        }

        @Override
        public void applyTheme(String themeId) {
            AtmosphereScreen.this.applyTheme(themeId);
        }

        @Override
        public void selectTheme(String themeId) {
            themeStudioState.selectTheme(themeId);
            rebuildWidgets();
        }

        @Override
        public void revertTheme() {
            revertCustomTheme();
        }

        @Override
        public void resetTheme() {
            resetCustomTheme();
        }

        @Override
        public void toggleAdvancedMode() {
            themeStudioState.setAdvancedMode(!themeStudioState.advancedMode());
            themeStudioState.clearHexFocus();
            rebuildWidgets();
        }
    };
}

private void createCustomTheme() {
    CustomThemeData created = CustomThemeManager.createTheme("Custom Theme", ThemeManager.current());
    ThemeManager.setTheme(created.id);
    themeStudioState.selectTheme(created.id);
    NotificationUtil.show("Created " + created.displayName);
    rebuildWidgets();
}

private void duplicateCustomTheme(String sourceId) {
    Theme source = ThemeManager.byId(sourceId);
    if (source == null) {
        source = ThemeManager.current();
    }

    CustomThemeData created = CustomThemeManager.duplicateTheme(source.id(), source.displayName() + " Copy");
    ThemeManager.setTheme(created.id);
    themeStudioState.selectTheme(created.id);
    NotificationUtil.show("Duplicated " + source.displayName());
    rebuildWidgets();
}

private void applyTheme(String themeId) {
    if (ThemeManager.byId(themeId) == null) {
        NotificationUtil.show("Theme is unavailable");
        reloadCustomThemes();
        return;
    }

    ThemeManager.setTheme(themeId);
    themeStudioState.selectTheme(themeId);
    NotificationUtil.applied(ThemeManager.current().displayName());
    rebuildWidgets();
}

private void saveCustomTheme(String themeId) {
    if (!CustomThemeManager.isCustomTheme(themeId)) {
        NotificationUtil.show("Duplicate a built-in theme before saving");
        return;
    }

    CustomThemeData draft = themeStudioState.draft();
    if (draft == null) {
        NotificationUtil.show("No custom theme draft to save");
        return;
    }

    CustomThemeManager.saveTheme(new CustomThemeData(draft));
    themeStudioState.revert();
    NotificationUtil.show("Saved " + draft.displayName);
    rebuildWidgets();
}

private void revertCustomTheme() {
    if (!themeStudioState.selectedIsCustom()) {
        NotificationUtil.show("Built-in themes are read-only");
        return;
    }

    themeStudioState.revert();
    NotificationUtil.show("Reverted unsaved theme changes");
    rebuildWidgets();
}

private void resetCustomTheme() {
    if (!themeStudioState.selectedIsCustom()) {
        NotificationUtil.show("Built-in themes are read-only");
        return;
    }

    themeStudioState.resetTo(ThemeManager.defaultTheme());
    NotificationUtil.show("Reset draft to Midnight colors");
    rebuildWidgets();
}

private void reloadCustomThemes() {
    CustomThemeManager.load();
    ThemeManager.refreshCustomThemes();

    if (ThemeManager.byId(ConfigManager.get().theme) == null) {
        ConfigManager.get().theme = ThemeManager.defaultTheme().id();
        ConfigManager.save();
    }

    themeStudioState.selectCurrentTheme();
    NotificationUtil.show("Reloaded custom themes");
    rebuildWidgets();
}

private void confirmDeleteTheme(String themeId) {
    CustomThemeData data = CustomThemeManager.get(themeId);
    if (data == null) {
        NotificationUtil.show("Built-in themes are read-only");
        return;
    }

    showConfirmation(
            "Delete " + data.displayName + "?",
            "This removes the custom theme from config/atmosphereplus-themes.json.",
            () -> {
                CustomThemeManager.deleteTheme(data.id);
                themeStudioState.selectCurrentTheme();
                NotificationUtil.show("Deleted " + data.displayName);
                rebuildWidgets();
            }
    );
}


private int presetGridColumns(int contentW) {
    if (contentW >= 900) {
        return 3;
    }
    if (contentW >= 520) {
        return 2;
    }
    return 1;
}

private int presetCardWidth(int contentW, int columns, int gap) {
    return (contentW - gap * (columns - 1)) / columns;
}

private int addPresetWidgets(int contentX, int contentY, int contentW) {
    int gap = 10;
    int columns = presetGridColumns(contentW);
    int cardW = presetCardWidth(contentW, columns, gap);
    int rowH = 64;
    int y = contentY;

    widgets.add(new SectionLabelWidget(contentX, y, contentW, "Cinematic", "New mood presets"));
    y += 30;

    int index = 0;
    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Aurora Night", "Stars and dark sky.", IconType.TIME, this::isAuroraNightActive, () -> {
        applyAuroraNight();
        NotificationUtil.show("Applied Aurora Night");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Cinematic Sunset", "Warm sky and clouds.", IconType.SKY, this::isCinematicSunsetActive, () -> {
        applyCinematicSunset();
        NotificationUtil.show("Applied Cinematic Sunset");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Low Clouds", "Experimental cloud feel.", IconType.FOG, this::isLowCloudsActive, () -> {
        applyLowClouds();
        NotificationUtil.show("Applied Low Clouds");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Misty Morning", "Sunrise and soft fog.", IconType.FOG, this::isMistyMorningActive, () -> {
        applyMistyMorning();
        NotificationUtil.show("Applied Misty Morning");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Starlit Night", "Clear night visibility.", IconType.TIME, this::isStarlitNightActive, () -> {
        applyStarlitNight();
        NotificationUtil.show("Applied Starlit Night");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Storm Front", "Heavy storm atmosphere.", IconType.WEATHER, this::isStormFrontActive, () -> {
        applyStormFront();
        NotificationUtil.show("Applied Storm Front");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Moonlit Fog", "Night with dense fog.", IconType.FOG, this::isMoonlitFogActive, () -> {
        applyMoonlitFog();
        NotificationUtil.show("Applied Moonlit Fog");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Screenshot Clear", "Clean sunny setup.", IconType.SKY, this::isScreenshotClearActive, () -> {
        applyScreenshotClear();
        NotificationUtil.show("Applied Screenshot Clear");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Shader Friendly", "Safer with Iris shaders.", IconType.ADVANCED, this::isShaderFriendlyActive, () -> {
        applyShaderFriendly();
        NotificationUtil.show("Applied Shader Friendly");
        rebuildWidgets();
    });

    y += ((index + columns - 1) / columns) * rowH + 10;

    widgets.add(new SectionLabelWidget(contentX, y, contentW, "Classic", "Stable presets"));
    y += 30;
    index = 0;

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Golden Hour", "Warm sunny atmosphere.", IconType.SKY, this::isGoldenHourActive, () -> {
        applyGoldenHour();
        NotificationUtil.show("Applied Golden Hour");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Midnight Calm", "Clear calm midnight.", IconType.TIME, this::isMidnightCalmActive, () -> {
        applyMidnightCalm();
        NotificationUtil.show("Applied Midnight Calm");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Cozy Rain", "Rainy afternoon mood.", IconType.WEATHER, this::isCozyRainActive, () -> {
        applyCozyRain();
        NotificationUtil.show("Applied Cozy Rain");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Thunder Night", "Dark stormy night.", IconType.LIGHTING, this::isThunderNightActive, () -> {
        applyThunderNight();
        NotificationUtil.show("Applied Thunder Night");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Deep Fog", "Thick screenshot fog.", IconType.FOG, this::isDeepFogActive, () -> {
        applyDeepFog();
        NotificationUtil.show("Applied Deep Fog");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Bright Caves", "Fullbright boost.", IconType.LIGHTING, this::isBrightCavesActive, () -> {
        applyBrightCaves();
        NotificationUtil.show("Applied Bright Caves");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Performance Clear", "Cleaner gameplay visuals.", IconType.ADVANCED, this::isPerformanceClearActive, () -> {
        applyPerformanceClear();
        NotificationUtil.show("Applied Performance Clear");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Soft Mist", "Light calm fog.", IconType.FOG, this::isSoftMistActive, () -> {
        applySoftMist();
        NotificationUtil.show("Applied Soft Mist");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Cloudless Clear", "Clear with clouds off.", IconType.SKY, this::isCloudlessClearActive, () -> {
        applyCloudlessClear();
        NotificationUtil.show("Applied Cloudless Clear");
        rebuildWidgets();
    });

    index = addPresetCard(index, columns, contentX, y, cardW, gap, rowH, "Fancy Clouds", "Force fancy clouds.", IconType.SKY, this::isFancyCloudsActive, () -> {
        applyFancyClouds();
        NotificationUtil.show("Applied Fancy Clouds");
        rebuildWidgets();
    });

    int rows = (index + columns - 1) / columns;
    return y + rows * rowH;
}

private int addPresetCard(int index, int columns, int contentX, int contentY, int cardW, int gap, int rowH, String title, String description, IconType icon, java.util.function.Supplier<Boolean> activeSupplier, Runnable action) {
    int col = index % columns;
    int row = index / columns;
    int x = contentX + col * (cardW + gap);
    int y = contentY + row * rowH;

    widgets.add(new PresetCardWidget(x, y, cardW, title, description, icon, activeSupplier, action));
    return index + 1;
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


y = addSearchAction(y, contentX, contentW, "Compatibility · Status", "compatibility sodium iris mod menu shaders status", "Compatibility Status", "Show detected compatibility status.", IconType.ADVANCED, () -> {
    NotificationUtil.show(CompatibilityUtil.renderCompatibilitySummary());
    rebuildWidgets();
});

y = addSearchAction(y, contentX, contentW, "Compatibility · Shader Safe Reset", "compatibility shaders iris fog sky lighting reset safe", "Shader Safe Reset", "Reset shader-sensitive visual settings.", IconType.FOG, () -> {
    resetFog();
    resetSky();
    resetLighting();
    rebuildWidgets();
});


y = addSearchAction(y, contentX, contentW, "Preset · Misty Morning", "preset misty morning sunrise fog clouds cinematic atmosphere", "Misty Morning", "Apply sunrise, soft fog and fancy clouds.", IconType.FOG, () -> {
    applyMistyMorning();
    rebuildWidgets();
});

y = addSearchAction(y, contentX, contentW, "Preset · Starlit Night", "preset starlit night midnight clear stars dark atmosphere", "Starlit Night", "Apply clear midnight with a light visibility boost.", IconType.TIME, () -> {
    applyStarlitNight();
    rebuildWidgets();
});

y = addSearchAction(y, contentX, contentW, "Preset · Storm Front", "preset storm thunder rain dark fog cinematic atmosphere", "Storm Front", "Apply dark thunderstorm atmosphere.", IconType.WEATHER, () -> {
    applyStormFront();
    rebuildWidgets();
});

y = addSearchAction(y, contentX, contentW, "Preset · Moonlit Fog", "preset moonlit fog night cinematic atmosphere", "Moonlit Fog", "Apply moonlit night with thick fog.", IconType.FOG, () -> {
    applyMoonlitFog();
    rebuildWidgets();
});

y = addSearchAction(y, contentX, contentW, "Preset · Screenshot Clear", "preset screenshot clear sunny no clouds particles clean", "Screenshot Clear", "Apply a clean sunny screenshot setup.", IconType.SKY, () -> {
    applyScreenshotClear();
    rebuildWidgets();
});

y = addSearchAction(y, contentX, contentW, "Preset · Shader Friendly", "preset shader friendly iris sodium reset safe", "Shader Friendly", "Apply minimal shader-sensitive overrides.", IconType.ADVANCED, () -> {
    applyShaderFriendly();
    rebuildWidgets();
});

    y = addSearchAction(y, contentX, contentW, "Quick · Fullbright", "quick fullbright lighting toggle bright caves", ConfigManager.get().fullbright ? "Fullbright: On" : "Fullbright: Off", "Click to toggle fullbright instantly.", IconType.LIGHTING, () -> {
        ConfigManager.get().fullbright = !ConfigManager.get().fullbright;
        clearActivePreset();
        ConfigManager.save();
        rebuildWidgets();
    });

    y = addSearchAction(y, contentX, contentW, "Quick · Clouds Off", "quick clouds off hide sky", "Clouds Off", "Hide clouds visually.", IconType.SKY, () -> {
        ConfigManager.get().cloudOverride = true;
        ConfigManager.get().cloudMode = "OFF";
        clearActivePreset();
        ConfigManager.save();
        rebuildWidgets();
    });

    y = addSearchAction(y, contentX, contentW, "Quick · Clear Weather", "quick clear weather sunny rain off", "Clear Weather", "Force clear visual weather.", IconType.WEATHER, () -> {
        ConfigManager.get().weatherOverride = true;
        ConfigManager.get().weatherMode = "SUNNY";
        ConfigManager.get().rainIntensity = 0.0f;
        clearActivePreset();
        ConfigManager.save();
        rebuildWidgets();
    });

    y = addSearchAction(y, contentX, contentW, "Reset · All Visuals", "reset all visuals default restore", "Reset All Visuals", "Reset weather, time, sky, fog, lighting and particles.", IconType.ADVANCED, this::confirmResetAllVisuals);
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


y = addSearchAction(y, contentX, contentW, "Preset · Aurora Night", "preset aurora night stars sky brightness cinematic", "Aurora Night", "Apply a star-focused cinematic night.", IconType.TIME, () -> {
    applyAuroraNight();
    rebuildWidgets();
});

y = addSearchAction(y, contentX, contentW, "Preset · Cinematic Sunset", "preset cinematic sunset sky clouds sun atmosphere", "Cinematic Sunset", "Apply warm sunset atmosphere.", IconType.SKY, () -> {
    applyCinematicSunset();
    rebuildWidgets();
});

y = addSearchAction(y, contentX, contentW, "Preset · Low Clouds", "preset low clouds cloud height distance opacity experimental", "Low Clouds", "Apply experimental low-cloud atmosphere.", IconType.FOG, () -> {
    applyLowClouds();
    rebuildWidgets();
});

y = addSearchToggle(y, contentX, contentW, "Sky · Experimental renderer controls", "sky cloud opacity height brightness stars sun moon experimental", () -> ConfigManager.get().experimentalRendererControls, v -> {
    ConfigManager.get().experimentalRendererControls = v;
    ConfigManager.save();
});

y = addSearchToggle(y, contentX, contentW, "Sky · Shader-aware warnings", "shader warnings iris sodium sky clouds", () -> ConfigManager.get().shaderAwareWarnings, v -> {
    ConfigManager.get().shaderAwareWarnings = v;
    ConfigManager.save();
});

y = addSearchToggle(y, contentX, contentW, "Sky · Cloud distance attempt", "cloud distance attempt experimental chunks", () -> ConfigManager.get().cloudDistanceOverride, v -> {
    ConfigManager.get().cloudDistanceOverride = v;
    ConfigManager.get().experimentalRendererControls = v || ConfigManager.get().experimentalRendererControls;
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
        ConfigManager.get().cloudDistanceOverride = false; // alpha 17 preset safety
        ConfigManager.get().cloudDistance = 12;
        ConfigManager.get().activePreset = presetId;
    }

    private void clearActivePreset() {
        ConfigManager.get().activePreset = "";
    }

    private boolean isPresetActive(String presetId) {
        return presetId.equals(ConfigManager.get().activePreset);
    }

    private void resetSky() {
        resetSkySilently();
        clearActivePreset();
        ConfigManager.save();
        NotificationUtil.show("Reset Sky / Clouds");
    }



    private void confirmResetAllVisuals() {
        showConfirmation(
                "Reset all visuals?",
                "This will reset weather, time, clouds, fog, lighting and particles to default settings.",
                () -> {
                    resetAllVisuals();
                    rebuildWidgets();
                }
        );
    }

    private void confirmClearProfile(int slot) {
        selectedProfileIndex = Math.max(0, Math.min(ProfileManager.PROFILE_COUNT - 1, slot));
        AtmosphereProfile profile = ProfileManager.profile(slot);
        String name = profile.name == null || profile.name.isBlank() ? "Profile " + (slot + 1) : profile.name;

        showConfirmation(
                "Clear " + name + "?",
                "This will delete this saved profile slot. Export first if you want a backup.",
                () -> {
                    ProfileManager.clear(slot);
                    rebuildWidgets();
                }
        );
    }

    private void confirmClearAllProfiles() {
        showConfirmation(
                "Clear all profiles?",
                "This will reset every profile slot. Export first if you want a backup.",
                () -> {
                    ProfileManager.clearAll();
                    rebuildWidgets();
                }
        );
    }

    private void confirmImportProfiles() {
        showConfirmation(
                "Import profiles?",
                "This will overwrite the current profile slots from the backup file.",
                () -> {
                    ProfileManager.importProfiles();
                    rebuildWidgets();
                }
        );
    }

    private void showConfirmation(String title, String message, Runnable action) {
        confirmTitle = title == null ? "Are you sure?" : title;
        confirmMessage = message == null ? "" : message;
        confirmAction = action;
        searchFocused = false;
        renamingProfileIndex = -1;
        renamingThemeId = "";
        renameProfileText = "";
    }

    private boolean isConfirmingAction() {
        return confirmAction != null;
    }

    private void cancelConfirmation() {
        confirmTitle = "";
        confirmMessage = "";
        confirmAction = null;
        NotificationUtil.show("Cancelled");
    }

    private void acceptConfirmation() {
        Runnable action = confirmAction;
        confirmTitle = "";
        confirmMessage = "";
        confirmAction = null;

        if (action != null) {
            action.run();
        }
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


private void setRendererMood(float cloudOpacity, float cloudHeight, float skyBrightness, float starBrightness, float sunVisibility) {
    ConfigManager.get().experimentalRendererControls = true;
    ConfigManager.get().cloudOpacity = cloudOpacity;
    ConfigManager.get().cloudHeight = cloudHeight;
    ConfigManager.get().skyBrightness = skyBrightness;
    ConfigManager.get().starBrightness = starBrightness;
    ConfigManager.get().sunMoonVisibility = sunVisibility;
    ConfigManager.get().cloudDistanceOverride = false;
}

private void setRendererNeutral() {
    ConfigManager.get().experimentalRendererControls = false;
    ConfigManager.get().cloudOpacity = 1.0f;
    ConfigManager.get().cloudHeight = 1.0f;
    ConfigManager.get().skyBrightness = 1.0f;
    ConfigManager.get().starBrightness = 1.0f;
    ConfigManager.get().sunMoonVisibility = 1.0f;
    ConfigManager.get().cloudDistanceOverride = false;
}


private void resetRendererSettings() {
    ConfigManager.get().experimentalRendererControls = false;
    ConfigManager.get().cloudOpacity = 1.0f;
    ConfigManager.get().cloudHeight = 1.0f;
    ConfigManager.get().cloudDistanceOverride = false;
    ConfigManager.get().cloudDistance = 12;
    ConfigManager.get().skyBrightness = 1.0f;
    ConfigManager.get().starBrightness = 1.0f;
    ConfigManager.get().sunMoonVisibility = 1.0f;
    ConfigManager.save();
    NotificationUtil.show("Renderer settings reset");
}

private void resetRendererSettingsSilently() {
    ConfigManager.get().experimentalRendererControls = false;
    ConfigManager.get().cloudOpacity = 1.0f;
    ConfigManager.get().cloudHeight = 1.0f;
    ConfigManager.get().cloudDistanceOverride = false;
    ConfigManager.get().cloudDistance = 12;
    ConfigManager.get().skyBrightness = 1.0f;
    ConfigManager.get().starBrightness = 1.0f;
    ConfigManager.get().sunMoonVisibility = 1.0f;
}

private void applyVanillaSafeMode() {
    resetWeatherSilently();
    resetTimeSilently();
    resetFogSilently();
    resetLightingSilently();
    resetSkySilently();
    ConfigManager.get().particleAmount = 1.0f;
    resetRendererSettingsSilently();
    setActivePreset("vanilla_safe");
    ConfigManager.save();
}

private boolean isVanillaSafeModeActive() {
    return isPresetActive("vanilla_safe");
}

private void applySodiumIrisSafeMode() {
    ConfigManager.get().weatherOverride = false;
    ConfigManager.get().weatherMode = "SERVER";
    ConfigManager.get().rainIntensity = 1.0f;
    ConfigManager.get().timeOverride = false;
    ConfigManager.get().freezeVisualTime = false;
    ConfigManager.get().fogOverride = false;
    ConfigManager.get().fullbright = false;
    ConfigManager.get().gamma = 1.0f;
    ConfigManager.get().particleAmount = 0.85f;
    ConfigManager.get().cloudOverride = false;
    ConfigManager.get().cloudMode = "SERVER";
    resetRendererSettingsSilently();
    setActivePreset("sodium_iris_safe");
    ConfigManager.save();
}

private boolean isSodiumIrisSafeModeActive() {
    return isPresetActive("sodium_iris_safe");
}

private void resetWeatherSilently() {
    ConfigManager.get().weatherOverride = false;
    ConfigManager.get().weatherMode = "SERVER";
    ConfigManager.get().rainIntensity = 1.0f;
    ConfigManager.get().thunderSounds = true;
}

private void resetTimeSilently() {
    ConfigManager.get().timeOverride = false;
    ConfigManager.get().visualTime = 6000;
    ConfigManager.get().freezeVisualTime = false;
}

private void resetFogSilently() {
    ConfigManager.get().fogOverride = false;
    ConfigManager.get().fogDistance = 1.0f;
    ConfigManager.get().fogDensity = 1.0f;
}

private void resetLightingSilently() {
    ConfigManager.get().fullbright = false;
    ConfigManager.get().gamma = 1.0f;
}

private void resetSkySilently() {
    ConfigManager.get().cloudOverride = false;
    ConfigManager.get().cloudMode = "SERVER";
    ConfigManager.get().cloudDistance = 12;
    ConfigManager.get().cloudDistanceOverride = false;
    ConfigManager.get().cloudOpacity = 1.0f;
    ConfigManager.get().cloudHeight = 1.0f;
    ConfigManager.get().skyBrightness = 1.0f;
    ConfigManager.get().starBrightness = 1.0f;
    ConfigManager.get().sunMoonVisibility = 1.0f;
    ConfigManager.get().experimentalRendererControls = false;
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
    setRendererMood(0.85f, 1.10f, 1.25f, 0.75f, 1.0f);
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
    setRendererMood(0.95f, 1.0f, 0.70f, 1.55f, 0.55f);
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
    setRendererMood(0.80f, 0.90f, 0.85f, 0.65f, 0.70f);
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
    setRendererMood(0.75f, 0.75f, 0.55f, 1.25f, 0.35f);
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
    setRendererMood(0.65f, 0.70f, 0.75f, 0.90f, 0.45f);
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
    setRendererMood(0.72f, 0.85f, 0.95f, 0.80f, 0.65f);
        setActivePreset("soft_mist");
        ConfigManager.save();
    }

    private boolean isSoftMistActive() {
        return isPresetActive("soft_mist");
    }

    private void applyBrightCaves() {
        ConfigManager.get().fullbright = true;
        ConfigManager.get().gamma = 2.0f;
    setRendererNeutral();
        setActivePreset("bright_caves");
        ConfigManager.save();
    }

    private boolean isBrightCavesActive() {
        return isPresetActive("bright_caves");
    }


private void applyMistyMorning() {
    ConfigManager.get().weatherOverride = true;
    ConfigManager.get().weatherMode = "SUNNY";
    ConfigManager.get().rainIntensity = 0.0f;
    ConfigManager.get().timeOverride = true;
    ConfigManager.get().visualTime = 1000;
    ConfigManager.get().freezeVisualTime = true;
    ConfigManager.get().fogOverride = true;
    ConfigManager.get().fogDistance = 0.75f;
    ConfigManager.get().fogDensity = 1.25f;
    ConfigManager.get().fullbright = false;
    ConfigManager.get().gamma = 1.05f;
    ConfigManager.get().particleAmount = 0.85f;
    ConfigManager.get().cloudOverride = true;
    ConfigManager.get().cloudMode = "FANCY";
    setRendererMood(0.70f, 0.80f, 1.10f, 0.60f, 0.85f);
    setActivePreset("misty_morning");
    ConfigManager.save();
}

private boolean isMistyMorningActive() {
    return isPresetActive("misty_morning");
}

private void applyStarlitNight() {
    ConfigManager.get().weatherOverride = true;
    ConfigManager.get().weatherMode = "SUNNY";
    ConfigManager.get().rainIntensity = 0.0f;
    ConfigManager.get().timeOverride = true;
    ConfigManager.get().visualTime = 18000;
    ConfigManager.get().freezeVisualTime = true;
    ConfigManager.get().fogOverride = true;
    ConfigManager.get().fogDistance = 1.35f;
    ConfigManager.get().fogDensity = 0.65f;
    ConfigManager.get().fullbright = false;
    ConfigManager.get().gamma = 1.15f;
    ConfigManager.get().particleAmount = 0.7f;
    ConfigManager.get().cloudOverride = true;
    ConfigManager.get().cloudMode = "OFF";
    setRendererMood(1.0f, 1.0f, 0.65f, 2.0f, 0.45f);
    setActivePreset("starlit_night");
    ConfigManager.save();
}

private boolean isStarlitNightActive() {
    return isPresetActive("starlit_night");
}

private void applyStormFront() {
    ConfigManager.get().weatherOverride = true;
    ConfigManager.get().weatherMode = "THUNDER";
    ConfigManager.get().rainIntensity = 0.9f;
    ConfigManager.get().thunderSounds = true;
    ConfigManager.get().timeOverride = true;
    ConfigManager.get().visualTime = 13500;
    ConfigManager.get().freezeVisualTime = true;
    ConfigManager.get().fogOverride = true;
    ConfigManager.get().fogDistance = 0.65f;
    ConfigManager.get().fogDensity = 1.45f;
    ConfigManager.get().fullbright = false;
    ConfigManager.get().gamma = 0.95f;
    ConfigManager.get().particleAmount = 1.15f;
    ConfigManager.get().cloudOverride = true;
    ConfigManager.get().cloudMode = "FANCY";
    setRendererMood(0.70f, 0.70f, 0.55f, 1.20f, 0.30f);
    setActivePreset("storm_front");
    ConfigManager.save();
}

private boolean isStormFrontActive() {
    return isPresetActive("storm_front");
}

private void applyMoonlitFog() {
    ConfigManager.get().weatherOverride = true;
    ConfigManager.get().weatherMode = "SUNNY";
    ConfigManager.get().rainIntensity = 0.0f;
    ConfigManager.get().timeOverride = true;
    ConfigManager.get().visualTime = 18000;
    ConfigManager.get().freezeVisualTime = true;
    ConfigManager.get().fogOverride = true;
    ConfigManager.get().fogDistance = 0.55f;
    ConfigManager.get().fogDensity = 1.6f;
    ConfigManager.get().fullbright = false;
    ConfigManager.get().gamma = 1.05f;
    ConfigManager.get().particleAmount = 0.8f;
    ConfigManager.get().cloudOverride = true;
    ConfigManager.get().cloudMode = "FAST";
    setRendererMood(0.75f, 0.65f, 0.55f, 1.65f, 0.35f);
    setActivePreset("moonlit_fog");
    ConfigManager.save();
}

private boolean isMoonlitFogActive() {
    return isPresetActive("moonlit_fog");
}

private void applyScreenshotClear() {
    ConfigManager.get().weatherOverride = true;
    ConfigManager.get().weatherMode = "SUNNY";
    ConfigManager.get().rainIntensity = 0.0f;
    ConfigManager.get().timeOverride = true;
    ConfigManager.get().visualTime = 6000;
    ConfigManager.get().freezeVisualTime = true;
    ConfigManager.get().fogOverride = false;
    ConfigManager.get().fogDistance = 1.0f;
    ConfigManager.get().fogDensity = 1.0f;
    ConfigManager.get().fullbright = false;
    ConfigManager.get().gamma = 1.1f;
    ConfigManager.get().particleAmount = 0.45f;
    ConfigManager.get().cloudOverride = true;
    ConfigManager.get().cloudMode = "OFF";
    setRendererMood(0.0f, 1.15f, 1.20f, 0.50f, 1.0f);
    setActivePreset("screenshot_clear");
    ConfigManager.save();
}

private boolean isScreenshotClearActive() {
    return isPresetActive("screenshot_clear");
}

private void applyShaderFriendly() {
    ConfigManager.get().weatherOverride = true;
    ConfigManager.get().weatherMode = "SUNNY";
    ConfigManager.get().rainIntensity = 0.0f;
    ConfigManager.get().timeOverride = false;
    ConfigManager.get().visualTime = 6000;
    ConfigManager.get().freezeVisualTime = false;
    ConfigManager.get().fogOverride = false;
    ConfigManager.get().fogDistance = 1.0f;
    ConfigManager.get().fogDensity = 1.0f;
    ConfigManager.get().fullbright = false;
    ConfigManager.get().gamma = 1.0f;
    ConfigManager.get().particleAmount = 0.85f;
    ConfigManager.get().cloudOverride = false;
    ConfigManager.get().cloudMode = "SERVER";
    setRendererNeutral();
    setActivePreset("shader_friendly");
    ConfigManager.save();
}

private boolean isShaderFriendlyActive() {
    return isPresetActive("shader_friendly");
}


private void applyAuroraNight() {
    ConfigManager.get().weatherOverride = true;
    ConfigManager.get().weatherMode = "SUNNY";
    ConfigManager.get().rainIntensity = 0.0f;
    ConfigManager.get().timeOverride = true;
    ConfigManager.get().visualTime = 18000;
    ConfigManager.get().freezeVisualTime = true;
    ConfigManager.get().fogOverride = true;
    ConfigManager.get().fogDistance = 1.2f;
    ConfigManager.get().fogDensity = 0.7f;
    ConfigManager.get().gamma = 1.2f;
    ConfigManager.get().cloudOverride = true;
    ConfigManager.get().cloudMode = "OFF";
    ConfigManager.get().experimentalRendererControls = true;
    ConfigManager.get().skyBrightness = 0.75f;
    ConfigManager.get().starBrightness = 1.75f;
    ConfigManager.get().sunMoonVisibility = 0.8f;
    setRendererMood(0.45f, 1.25f, 0.60f, 2.0f, 0.40f);
    setActivePreset("aurora_night");
    ConfigManager.save();
}

private boolean isAuroraNightActive() {
    return isPresetActive("aurora_night");
}

private void applyCinematicSunset() {
    ConfigManager.get().weatherOverride = true;
    ConfigManager.get().weatherMode = "SUNNY";
    ConfigManager.get().rainIntensity = 0.0f;
    ConfigManager.get().timeOverride = true;
    ConfigManager.get().visualTime = 12000;
    ConfigManager.get().freezeVisualTime = true;
    ConfigManager.get().fogOverride = true;
    ConfigManager.get().fogDistance = 1.05f;
    ConfigManager.get().fogDensity = 0.85f;
    ConfigManager.get().gamma = 1.1f;
    ConfigManager.get().cloudOverride = true;
    ConfigManager.get().cloudMode = "FANCY";
    ConfigManager.get().experimentalRendererControls = true;
    ConfigManager.get().cloudOpacity = 0.75f;
    ConfigManager.get().cloudHeight = 1.15f;
    ConfigManager.get().skyBrightness = 1.2f;
    ConfigManager.get().sunMoonVisibility = 1.0f;
    setRendererMood(0.65f, 1.20f, 1.35f, 0.70f, 1.0f);
    setActivePreset("cinematic_sunset");
    ConfigManager.save();
}

private boolean isCinematicSunsetActive() {
    return isPresetActive("cinematic_sunset");
}

private void applyLowClouds() {
    ConfigManager.get().weatherOverride = true;
    ConfigManager.get().weatherMode = "SUNNY";
    ConfigManager.get().fogOverride = true;
    ConfigManager.get().fogDistance = 0.7f;
    ConfigManager.get().fogDensity = 1.15f;
    ConfigManager.get().cloudOverride = true;
    ConfigManager.get().cloudMode = "FANCY";
    ConfigManager.get().experimentalRendererControls = true;
    ConfigManager.get().cloudOpacity = 0.85f;
    ConfigManager.get().cloudHeight = 0.65f;
    ConfigManager.get().cloudDistanceOverride = true;
    ConfigManager.get().cloudDistance = 8;
    setRendererMood(0.82f, 0.45f, 0.90f, 0.90f, 0.60f);
    setActivePreset("low_clouds");
    ConfigManager.save();
}

private boolean isLowCloudsActive() {
    return isPresetActive("low_clouds");
}


private void applyRendererTest() {
    ConfigManager.get().weatherOverride = true;
    ConfigManager.get().weatherMode = "SUNNY";
    ConfigManager.get().rainIntensity = 0.0f;
    ConfigManager.get().timeOverride = true;
    ConfigManager.get().visualTime = 18000;
    ConfigManager.get().freezeVisualTime = true;
    ConfigManager.get().fogOverride = true;
    ConfigManager.get().fogDistance = 1.15f;
    ConfigManager.get().fogDensity = 0.75f;
    ConfigManager.get().cloudOverride = true;
    ConfigManager.get().cloudMode = "FANCY";
    ConfigManager.get().experimentalRendererControls = true;
    ConfigManager.get().cloudOpacity = 0.45f;
    ConfigManager.get().cloudHeight = 0.65f;
    ConfigManager.get().skyBrightness = 0.65f;
    ConfigManager.get().starBrightness = 2.0f;
    ConfigManager.get().sunMoonVisibility = 0.35f;
    setRendererMood(0.45f, 0.65f, 0.65f, 2.0f, 0.35f);
    setActivePreset("renderer_test");
    ConfigManager.save();
}

private boolean isRendererTestActive() {
    return isPresetActive("renderer_test");
}

    private void applyPerformanceClear() {
        ConfigManager.get().weatherMode = "SUNNY";
        ConfigManager.get().weatherOverride = true;
        ConfigManager.get().rainIntensity = 0f;
        ConfigManager.get().particleAmount = 0.35f;
        ConfigManager.get().fogOverride = true;
        ConfigManager.get().fogDistance = 1.5f;
        ConfigManager.get().fogDensity = 0.5f;
    setRendererNeutral();
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
    setRendererMood(0.0f, 1.0f, 1.15f, 0.60f, 1.0f);
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
    setRendererMood(0.95f, 1.0f, 1.0f, 0.80f, 1.0f);
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

        y = addSearchAction(y, x, width, "Profiles · Import", "profile profiles import backup load file", "Import Profiles", "Import profile slots from backup file after confirmation.", IconType.PRESETS, this::confirmImportProfiles);

        y = addSearchAction(y, x, width, "Profiles · Clear All", "profile profiles clear all delete reset", "Clear All Profiles", "Clear every profile slot after confirmation.", IconType.ADVANCED, this::confirmClearAllProfiles);

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
                    selectedProfileIndex = slot;
                    ProfileManager.load(slot);
                } else {
                    selectedProfileIndex = slot;
                    ProfileManager.saveCurrentTo(slot);
                }
                rebuildWidgets();
            }));
            searchResultCount++;
            y += 84;

            int buttonW = (width - 12) / 3;
            widgets.add(new ActionButtonWidget(x, y, buttonW, "Save", "Overwrite this profile slot with current settings.", IconType.PRESETS, () -> {
                selectedProfileIndex = slot;
                ProfileManager.saveCurrentTo(slot);
                rebuildWidgets();
            }));
            searchResultCount++;

            widgets.add(new ActionButtonWidget(x + buttonW + 6, y, buttonW, "Rename", "Rename this profile slot.", IconType.PRESETS, () -> startRenamingProfile(slot)));
            searchResultCount++;

            widgets.add(new ActionButtonWidget(x + (buttonW + 6) * 2, y, buttonW, "Clear", "Clear this profile slot.", IconType.ADVANCED, () -> {
                confirmClearProfile(slot);
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


private String trimHeaderText(String text, int maxWidth) {
    if (text == null) {
        return "";
    }

    if (textRenderer.getWidth(text) <= maxWidth) {
        return text;
    }

    String result = text;
    while (result.length() > 3 && textRenderer.getWidth(result + "...") > maxWidth) {
        result = result.substring(0, result.length() - 1);
    }

    return result + "...";
}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    LayoutProfile currentLayout = LayoutProfile.create(width, height);
    if (lastScreenWidth != width || lastScreenHeight != height || !currentLayout.key().equals(lastLayoutKey)) {
        scrollOffset = 0;
        layoutProfile = currentLayout;
        rebuildWidgets();
    }

    renderBackdrop(context);

    Theme theme = ThemeManager.current();
    boolean modalOpen = isRenaming() || isConfirmingAction();
    int uiMouseX = modalOpen ? -100000 : mouseX;
    int uiMouseY = modalOpen ? -100000 : mouseY;

    UiRender.borderedRect(context, windowX, windowY, windowW, windowH, theme.background(), theme.border());
    UiRender.gradientHorizontal(context, windowX + 1, windowY + 1, windowW - 2, layout().topBarHeight(), theme.panel(), theme.panelAlt());
    UiRender.rect(context, windowX + 1, windowY + layout().topBarHeight(), windowW - 2, 1, theme.border());

    drawBranding(context, theme);
    drawSearchBar(context, theme);
    drawTopButtons(context, theme, uiMouseX, uiMouseY);
    drawSidebar(context, theme);
    drawContentHeader(context, theme);
    renderContentBackground(context, theme);

    context.enableScissor(sidebarLeft(), sidebarListTop() - 2, sidebarRight(), sidebarListTop() + sidebarViewportHeight());
    renderSidebarWidgets(context, uiMouseX, uiMouseY, delta);
    context.disableScissor();
    renderSidebarScrollIndicator(context, theme);

    int contentClipLeft = contentWidgetLeft();
    int contentClipRight = contentWidgetLeft() + contentWidgetWidth();
    context.enableScissor(contentClipLeft, windowY + layout().contentTopOffset() - 12, contentClipRight, sidebarPanelBottom() - contentPadding());
    renderContentWidgets(context, uiMouseX, uiMouseY, delta);
    context.disableScissor();

    renderScrollIndicator(context, theme);
    renderProfileRenameOverlay(context, theme);
    renderConfirmOverlay(context, theme, mouseX, mouseY);
    renderTooltip(context, uiMouseX, uiMouseY);
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
    int x = sidebarLeft();
    int y = sidebarPanelTop();
    int w = sidebarWidth();
    int h = sidebarPanelBottom() - y;

    UiRender.panel(context, x, y, w, h, theme.panel(), theme.border(), theme.accent());

    UiRender.text(context, textRenderer, isSearching() ? "Search" : "Navigation", x + 14, y + 12, theme.mutedText());
    UiRender.rect(context, x + 14, y + 29, w - 28, 1, theme.border());

    if (isSearching()) {
        UiRender.centeredText(context, textRenderer, "Direct results", x + w / 2, y + 54, theme.accent());
        UiRender.centeredText(context, textRenderer, "Categories hidden", x + w / 2, y + 74, theme.mutedText());
        UiRender.centeredText(context, textRenderer, "Clear search", x + w / 2, y + 94, theme.mutedText());

        UiRender.borderedRect(context, x + 16, y + 122, w - 32, 24, theme.accentSoft(), theme.border());
        UiRender.centeredText(context, textRenderer, searchResultCount + " result" + (searchResultCount == 1 ? "" : "s"), x + w / 2, y + 130, theme.text());
        return;
    }

        if (visibleCategories().isEmpty()) {
            UiRender.centeredText(context, textRenderer, "No category matches", x + w / 2, y + 64, theme.mutedText());
        }
}

    private void drawContentHeader(DrawContext context, Theme theme) {
    int x = contentLeft();
    int y = windowY + layout().topBarHeight() + 10;
    int w = contentWidth();

    UiRender.borderedRect(context, x, y, w, 38, theme.panel(), theme.border());

    if (isSearching()) {
        UiRender.borderedRect(context, x + 10, y + 8, 22, 22, theme.accentSoft(), theme.accent());
        IconRenderer.drawCentered(context, IconType.ADVANCED, x + 21, y + 19, 18);

        UiRender.text(context, textRenderer, "SEARCH MODE", x + 42, y + 7, theme.accent());
        UiRender.text(context, textRenderer, trimHeaderText(searchResultCount + " direct result" + (searchResultCount == 1 ? "" : "s") + " for " + searchQuery, w - 150), x + 42, y + 22, theme.text());

        int chipW = Math.min(82, Math.max(54, w / 4));
        UiRender.borderedRect(context, x + w - chipW - 10, y + 9, chipW, 18, theme.accentSoft(), theme.accent());
        UiRender.centeredText(context, textRenderer, "DIRECT", x + w - chipW / 2 - 10, y + 14, theme.text());
        return;
    }

    UiRender.borderedRect(context, x + 10, y + 8, 22, 22, theme.accentSoft(), theme.accent());
    IconRenderer.drawCentered(context, selected.icon, x + 21, y + 19, 18);

    UiRender.text(context, textRenderer, selected.title, x + 42, y + 8, theme.text());
    UiRender.text(context, textRenderer, trimHeaderText(selected.description, w - 58), x + 42, y + 22, theme.mutedText());
}

    private void renderContentBackground(DrawContext context, Theme theme) {
    int x = contentLeft();
    int y = windowY + layout().topBarHeight() + 56;
    int w = contentWidth();
    int h = Math.max(1, sidebarPanelBottom() - y);

    UiRender.panel(context, x, y, w, h, theme.panel(), theme.border(), theme.accent());

    if (isSearching()) {
        if (searchResultCount == 0) {
            UiRender.centeredText(context, textRenderer, "No direct settings found", x + w / 2, y + 112, theme.text());
            UiRender.centeredText(context, textRenderer, "Try clouds, profile, sunrise, rain, fog, fullbright, or gamma.", x + w / 2, y + 134, theme.mutedText());
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
        selectedProfileIndex = Math.max(0, Math.min(ProfileManager.PROFILE_COUNT - 1, slot));
        AtmosphereProfile profile = ProfileManager.profile(slot);
        renamingProfileIndex = slot;
        renamingThemeId = "";
        renameProfileText = profile.name == null || profile.name.isBlank() ? "Profile " + (slot + 1) : profile.name;
        searchFocused = false;
        NotificationUtil.show("Type a new name, Enter to save, Esc to cancel");
    }

    private void startRenamingTheme(String themeId) {
        CustomThemeData data = CustomThemeManager.get(themeId);
        if (data == null) {
            NotificationUtil.show("Built-in themes are read-only");
            return;
        }

        renamingProfileIndex = -1;
        renamingThemeId = data.id;
        String currentName = data.id.equals(themeStudioState.selectedThemeId()) ? themeStudioState.themeName() : data.displayName;
        renameProfileText = currentName == null || currentName.isBlank() ? "Custom Theme" : currentName;
        searchFocused = false;
        NotificationUtil.show("Type a new theme name, Enter to save, Esc to cancel");
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

    private void finishRenamingTheme() {
        if (!isRenamingTheme()) {
            return;
        }

        if (CustomThemeManager.isCustomTheme(renamingThemeId)) {
            themeStudioState.selectTheme(renamingThemeId);
            themeStudioState.setThemeName(renameProfileText);
            NotificationUtil.show("Renamed theme draft");
        } else {
            NotificationUtil.show("Theme could not be renamed");
        }

        renamingThemeId = "";
        renameProfileText = "";
        rebuildWidgets();
    }

    private void cancelRenamingProfile() {
        renamingProfileIndex = -1;
        renamingThemeId = "";
        renameProfileText = "";
        NotificationUtil.show("Rename cancelled");
        rebuildWidgets();
    }

    private boolean isRenamingProfile() {
        return renamingProfileIndex >= 0;
    }

    private boolean isRenamingTheme() {
        return renamingThemeId != null && !renamingThemeId.isBlank();
    }

    private boolean isRenaming() {
        return isRenamingProfile() || isRenamingTheme();
    }



private void renderConfirmOverlay(DrawContext context, Theme theme, int mouseX, int mouseY) {
        if (!isConfirmingAction()) {
            return;
        }

        int modalW = Math.min(420, windowW - 80);
        int modalH = 132;
        int x = windowX + windowW / 2 - modalW / 2;
        int y = windowY + windowH / 2 - modalH / 2;

        context.fill(0, 0, width, height, 0xB0000000);
        UiRender.borderedRect(context, x, y, modalW, modalH, theme.panel(), theme.accent());

        UiRender.text(context, textRenderer, confirmTitle, x + 16, y + 14, theme.text());
        UiRender.text(context, textRenderer, confirmMessage, x + 16, y + 34, theme.mutedText());
        UiRender.rect(context, x + 16, y + 58, modalW - 32, 1, theme.border());

        int buttonY = y + 78;
        int buttonW = (modalW - 44) / 2;
        int confirmX = x + 16;
        int cancelX = confirmX + buttonW + 12;

        boolean confirmHover = UiRender.hovered(mouseX, mouseY, confirmX, buttonY, buttonW, 32);
        boolean cancelHover = UiRender.hovered(mouseX, mouseY, cancelX, buttonY, buttonW, 32);

        int confirmFill = confirmHover ? theme.panel() : theme.panelAlt();
        int confirmBorder = confirmHover ? theme.accent() : theme.border();
        int cancelFill = cancelHover ? theme.panel() : theme.panelAlt();
        int cancelBorder = cancelHover ? theme.accent() : theme.border();

        UiRender.borderedRect(context, confirmX, buttonY, buttonW, 32, confirmFill, confirmBorder);
        UiRender.centeredText(context, textRenderer, "Confirm", confirmX + buttonW / 2, buttonY + 12, theme.text());

        UiRender.borderedRect(context, cancelX, buttonY, buttonW, 32, cancelFill, cancelBorder);
        UiRender.centeredText(context, textRenderer, "Cancel", cancelX + buttonW / 2, buttonY + 12, theme.text());

        if (confirmHover) {
            UiRender.centeredText(context, textRenderer, "Click to confirm", confirmX + buttonW / 2, buttonY + 36, theme.accent());
        } else if (cancelHover) {
            UiRender.centeredText(context, textRenderer, "Click to cancel", cancelX + buttonW / 2, buttonY + 36, theme.accent());
        } else {
            UiRender.centeredText(context, textRenderer, "Enter confirms · Esc cancels", x + modalW / 2, y + 116, theme.mutedText());
        }
    }

    private boolean handleConfirmClick(double mouseX, double mouseY) {
        if (!isConfirmingAction()) {
            return false;
        }

        int modalW = Math.min(420, windowW - 80);
        int modalH = 132;
        int x = windowX + windowW / 2 - modalW / 2;
        int y = windowY + windowH / 2 - modalH / 2;
        int buttonY = y + 78;
        int buttonW = (modalW - 44) / 2;
        int confirmX = x + 16;
        int cancelX = confirmX + buttonW + 12;

        if (UiRender.hovered(mouseX, mouseY, confirmX, buttonY, buttonW, 32)) {
            acceptConfirmation();
            return true;
        }

        if (UiRender.hovered(mouseX, mouseY, cancelX, buttonY, buttonW, 32)) {
            cancelConfirmation();
            return true;
        }

        return true;
    }

    private void renderProfileRenameOverlay(DrawContext context, Theme theme) {
        if (!isRenaming()) {
            return;
        }

        int modalW = Math.min(360, windowW - 80);
        int modalH = 116;
        int x = windowX + windowW / 2 - modalW / 2;
        int y = windowY + windowH / 2 - modalH / 2;

        context.fill(0, 0, width, height, 0xAA000000);
        UiRender.borderedRect(context, x, y, modalW, modalH, theme.panel(), theme.accent());

        String title = isRenamingTheme() ? "Rename Theme" : "Rename Profile " + (renamingProfileIndex + 1);
        int maxNameLength = isRenamingTheme() ? 28 : 24;

        UiRender.text(context, textRenderer, title, x + 16, y + 14, theme.text());
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

        UiRender.centeredText(context, textRenderer, "New name: " + renameProfileText.length() + "/" + maxNameLength, x + modalW / 2, y + 92, theme.mutedText());
    }


private void renderSidebarScrollIndicator(DrawContext context, Theme theme) {
    if (sidebarMaxScroll <= 0 || isSearching()) {
        return;
    }

    int trackX = sidebarRight() - 5;
    int trackY = sidebarListTop();
    int trackH = sidebarViewportHeight();
    int thumbH = Math.max(18, (int) (trackH * (trackH / (float) (trackH + sidebarMaxScroll))));
    int thumbY = trackY + (int) ((trackH - thumbH) * (sidebarScrollOffset / (float) sidebarMaxScroll));

    UiRender.rect(context, trackX, trackY, 2, trackH, theme.border());
    UiRender.rect(context, trackX, thumbY, 2, thumbH, theme.accent());
}

    private void renderScrollIndicator(DrawContext context, Theme theme) {
        if (maxScroll <= 0) {
            return;
        }

        int trackX = contentWidgetLeft() + contentWidgetWidth() + 4;
        int trackY = windowY + layout().contentTopOffset() - 10;
        int trackH = Math.max(24, sidebarPanelBottom() - contentPadding() - trackY);
        int thumbH = Math.max(24, trackH * trackH / (trackH + maxScroll));
        int thumbY = trackY + (trackH - thumbH) * scrollOffset / maxScroll;

        context.fill(trackX, trackY, trackX + 3, trackY + trackH, theme.panelAlt());
        context.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, theme.accent());
    }

    private void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        if (isRenaming() || isConfirmingAction()) {
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

        if (isConfirmingAction()) {
            return handleConfirmClick(click.x(), click.y());
        }

        if (isRenaming()) {
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
    if (!isSearching() && sidebarMaxScroll > 0 && isMouseOverSidebar(mouseX, mouseY)) {
        int oldOffset = sidebarScrollOffset;
        int newOffset = (int) Math.max(0, Math.min(sidebarMaxScroll, sidebarScrollOffset - verticalAmount * 18));

        if (newOffset != oldOffset) {
            sidebarScrollOffset = newOffset;
            rebuildWidgets();
        }

        return true;
    }

    if (maxScroll > 0 && UiRender.hovered(mouseX, mouseY, contentLeft(), windowY + layout().topBarHeight() + 56, contentWidth(), Math.max(1, sidebarPanelBottom() - (windowY + layout().topBarHeight() + 56)))) {
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

        if (isConfirmingAction()) {
            if (input.isEscape()) {
                cancelConfirmation();
                return true;
            }

            if (input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
                acceptConfirmation();
                return true;
            }

            return true;
        }

        if (isRenaming()) {
            if (input.isEscape()) {
                cancelRenamingProfile();
                return true;
            }

            if (input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
                if (isRenamingTheme()) {
                    finishRenamingTheme();
                } else {
                    finishRenamingProfile();
                }
                return true;
            }

            if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE && !renameProfileText.isEmpty()) {
                renameProfileText = renameProfileText.substring(0, renameProfileText.length() - 1);
                return true;
            }

            return true;
        }

        if (handleThemeStudioHexKey(input)) {
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
        if (handleThemeStudioHexChar(input)) {
            return true;
        }

        int maxRenameLength = isRenamingTheme() ? 28 : 24;
        if (isRenaming() && input.isValidChar() && renameProfileText.length() < maxRenameLength) {
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

    private boolean handleThemeStudioHexKey(KeyInput input) {
        if (selected != UiCategory.THEME_STUDIO || themeStudioState.focusedToken() == null) {
            return false;
        }

        if (input.isEscape()) {
            themeStudioState.clearHexFocus();
            rebuildWidgets();
            return true;
        }

        if (input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
            ThemeStudioState.HexResult result = themeStudioState.commitHex();
            if (result == ThemeStudioState.HexResult.INVALID) {
                NotificationUtil.show("Invalid hex color. Use #RRGGBB or #AARRGGBB");
            }
            rebuildWidgets();
            return true;
        }

        if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE) {
            themeStudioState.backspaceHex();
            rebuildWidgets();
            return true;
        }

        return false;
    }

    private boolean handleThemeStudioHexChar(CharInput input) {
        if (selected != UiCategory.THEME_STUDIO || themeStudioState.focusedToken() == null || !input.isValidChar()) {
            return false;
        }

        String typed = input.asString();
        if ("#".equals(typed)) {
            return true;
        }

        ThemeStudioState.HexResult result = themeStudioState.appendHexChar(typed.charAt(0));
        if (result == ThemeStudioState.HexResult.INVALID) {
            NotificationUtil.show("Invalid hex color. Use 0-9 and A-F");
        }
        rebuildWidgets();
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
