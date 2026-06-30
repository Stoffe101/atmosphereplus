package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.themes.CustomThemeData;
import com.skrra.atmosphereplus.themes.CustomThemeManager;
import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.widgets.ActionButtonWidget;
import com.skrra.atmosphereplus.ui.widgets.AtmosphereWidget;
import com.skrra.atmosphereplus.ui.widgets.ChoiceButtonWidget;
import com.skrra.atmosphereplus.ui.widgets.InfoCardWidget;
import com.skrra.atmosphereplus.ui.widgets.SectionLabelWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.function.Supplier;

public final class ThemeStudioPage {
    private static final int GAP = V2DesignTokens.SECTION_GAP;
    private static final int SMALL_GAP = V2DesignTokens.ROW_GAP;

    private ThemeStudioPage() {
    }

    public interface Actions {
        void createTheme();

        void duplicateTheme(String sourceId);

        void renameTheme(String themeId);

        void deleteTheme(String themeId);

        void exportTheme(String themeId);

        void importTheme();

        void saveTheme(String themeId);

        void reloadThemes();

        void applyTheme(String themeId);

        void selectTheme(String themeId);

        void revertTheme();

        void resetTheme();

        void toggleAdvancedMode();

        void expandSection(ThemeStudioState.EditorSection section);

        void focusThemeSearch();

        void clearThemeSearch();
    }

    public static int addWidgets(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int contentX, int contentY, int contentW, int viewportY, int viewportBottom) {
        int viewportHeight = Math.max(0, viewportBottom - viewportY);
        V2Layout.ThemeStudioSpec spec = V2Layout.themeStudio(contentX, contentW, viewportHeight);

        if (spec.sidePreview()) {
            // Separate, non-overlapping columns: nothing can hide behind the preview here,
            // so no sticky-overlay click guard is needed for this layout.
            state.clearStickyOverlay();

            int y = contentY;
            widgets.add(new StudioTabsWidget(contentX, y, contentW, actions::focusThemeSearch));
            y += V2DesignTokens.TAB_TO_CONTENT_GAP;

            y = addToolbar(widgets, state, actions, spec.editorX(), y, spec.editorW(), spec.compactDensity());
            int bodyTop = y;

            int leftY = addSelectedThemeCard(widgets, state, actions, spec.editorX(), bodyTop, spec.editorW(), spec.compactDensity());
            leftY = addEditorSection(widgets, state, actions, spec.editorX(), leftY, spec.editorW(), spec.columns(), spec.compactDensity());
            leftY = addLibrarySection(widgets, state, actions, spec.editorX(), leftY, spec.editorW(), spec.compactDensity());

            int previewCardH = previewHeight(spec.previewW());
            addLivePreviewSection(widgets, state, spec.previewX(), viewportY, spec.previewW(), previewCardH, false);

            return leftY + GAP;
        }

        // Compact/stacked layout: the live preview becomes a sticky band pinned to the top of
        // the viewport so it never scrolls out of view, while everything else scrolls beneath it.
        int previewCardH = compactPreviewHeight(contentW);
        int previewBandH = previewCardH + 24;
        // Block clicks/tooltips only inside the overlay's exact drawn rectangle (no extra
        // padding folded in) so visible controls right below/beside it stay interactive.
        state.setStickyOverlay(contentX, viewportY, contentW, previewBandH);

        int y = contentY + previewBandH + GAP;
        widgets.add(new StudioTabsWidget(contentX, y, contentW, actions::focusThemeSearch));
        y += V2DesignTokens.TAB_TO_CONTENT_GAP;

        y = addToolbar(widgets, state, actions, contentX, y, contentW, spec.compactDensity());
        y = addSelectedThemeCard(widgets, state, actions, contentX, y, contentW, spec.compactDensity());
        y = addEditorSection(widgets, state, actions, contentX, y, contentW, spec.columns(), spec.compactDensity());
        y = addLibrarySection(widgets, state, actions, contentX, y, contentW, spec.compactDensity());
        int finalY = y + GAP;

        // Added last so it paints over anything that has scrolled underneath it.
        addLivePreviewSection(widgets, state, contentX, viewportY, contentW, previewCardH, true);

        return finalY;
    }

    private record ToolbarItem(String label, String tooltip, IconType icon, Supplier<Boolean> active, Runnable action) {
    }

    private static int addToolbar(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w, boolean compact) {
        ToolbarItem[] items = {
                new ToolbarItem("Create", "Create a new custom theme.", IconType.THEMES, null, actions::createTheme),
                new ToolbarItem("Duplicate", "Duplicate the selected theme into a new custom theme.", IconType.PRESETS, null, () -> actions.duplicateTheme(state.selectedThemeId())),
                new ToolbarItem("Save", "Save changes to the selected theme.", IconType.THEMES, null, () -> actions.saveTheme(state.selectedThemeId())),
                new ToolbarItem("Reset", "Reset the draft to its base theme.", IconType.FOG, null, actions::resetTheme),
                new ToolbarItem(state.advancedMode() ? "Simple Mode" : "Advanced Mode", state.advancedMode() ? "Return to core color cards." : "Show grouped color controls.", IconType.ADVANCED, state::advancedMode, actions::toggleAdvancedMode),
                new ToolbarItem("Revert", "Discard unsaved edits.", IconType.PRESETS, null, actions::revertTheme),
                new ToolbarItem("Delete", "Delete the selected custom theme.", IconType.ADVANCED, null, () -> actions.deleteTheme(state.selectedThemeId())),
                new ToolbarItem("Export", "Export the selected custom theme.", IconType.THEMES, null, () -> actions.exportTheme(state.selectedThemeId())),
                new ToolbarItem("Import", "Import a theme from config.", IconType.PRESETS, null, actions::importTheme),
                new ToolbarItem("Library", "Reload custom themes from disk.", IconType.PRESETS, null, actions::reloadThemes),
        };

        int columns = Math.max(2, Math.min(items.length, w / 112));
        int buttonW = (w - SMALL_GAP * (columns - 1)) / columns;
        int rowH = compact ? 26 : 28;
        int rowStep = rowH + V2DesignTokens.TOOLBAR_ROW_GAP;

        for (int i = 0; i < items.length; i++) {
            ToolbarItem item = items[i];
            int bx = actionX(x, buttonW, columns, i);
            int by = y + (i / columns) * rowStep;
            widgets.add(new ToolbarButtonWidget(bx, by, buttonW, rowH, item.label(), item.tooltip(), item.icon(), item.active(), item.action()));
        }

        int rows = (items.length + columns - 1) / columns;
        return y + rows * rowStep - V2DesignTokens.TOOLBAR_ROW_GAP + GAP;
    }

    private static int addSelectedThemeCard(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w, boolean compact) {
        Theme selected = ThemeManager.byId(state.selectedThemeId());
        widgets.add(new SelectedThemeWidget(
                x,
                y,
                w,
                compact ? 42 : 50,
                selected == null ? "Custom Theme" : selected.displayName(),
                state.selectedIsCustom() ? (state.dirty() ? "Unsaved draft" : "Custom theme") : "Built-in theme",
                state.selectedThemeId().equals(ConfigManager.get().theme),
                () -> actions.renameTheme(state.selectedThemeId())
        ));
        return y + (compact ? 42 : 50) + GAP;
    }

    private static int addLibrarySection(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w, boolean compact) {
        Theme selected = ThemeManager.byId(state.selectedThemeId());
        widgets.add(new SectionLabelWidget(x, y, w, "Custom Themes", state.dirty() ? "Draft active" : "Library"));
        y += 28;

        if (!CustomThemeManager.hasCustomThemes()) {
            widgets.add(new InfoCardWidget(
                    x,
                    y,
                    w,
                    compact ? 54 : 62,
                    "No custom themes yet",
                    "Create a custom theme from the active look or duplicate the selected theme.",
                    IconType.THEMES
            ));
            return y + (compact ? 54 : 62) + GAP;
        }

        widgets.add(new ActionButtonWidget(
                x,
                y,
                w,
                state.themeSearch().isBlank() ? (state.themeSearchFocused() ? "Search: typing..." : "Search Custom Themes") : "Search: " + state.themeSearch(),
                state.themeSearchFocused() ? "Type to filter custom themes. Enter keeps results." : "Click to filter custom themes by name.",
                IconType.THEMES,
                actions::focusThemeSearch
        ));
        y += compact ? 38 : 42;

        if (!state.themeSearch().isBlank()) {
            widgets.add(new ActionButtonWidget(
                    x,
                    y,
                    w,
                    "Clear Search",
                    "Show all custom themes.",
                    IconType.ADVANCED,
                    actions::clearThemeSearch
            ));
            y += compact ? 38 : 42;
        }

        int columns = w >= 520 && !compact ? 2 : 1;
        int cardW = (w - SMALL_GAP * (columns - 1)) / columns;
        int index = 0;

        for (CustomThemeData data : CustomThemeManager.all().values()) {
            if (!state.matchesThemeSearch(data)) {
                continue;
            }

            int cardX = x + (index % columns) * (cardW + SMALL_GAP);
            int cardY = y + (index / columns) * (compact ? 38 : 42);
            widgets.add(new ChoiceButtonWidget(
                    cardX,
                    cardY,
                    cardW,
                    data.displayName,
                    data.id.equals(ConfigManager.get().theme) ? "Applied" : "Custom theme",
                    IconType.THEMES,
                    () -> data.id.equals(state.selectedThemeId()),
                    () -> actions.selectTheme(data.id)
            ));
            index++;
        }

        if (index == 0) {
            widgets.add(new InfoCardWidget(
                    x,
                    y,
                    w,
                    58,
                    "No matching themes",
                    "Try a different custom theme search.",
                    IconType.THEMES
            ));
            return y + 58 + GAP;
        }

        int bottom = y + ((index + columns - 1) / columns) * (compact ? 38 : 42) + GAP;
        if (selected != null && !state.selectedIsCustom()) {
            widgets.add(new InfoCardWidget(
                    x,
                    bottom,
                    w,
                    54,
                    "Read-only selection",
                    "Duplicate " + selected.displayName() + " to edit it in Theme Studio.",
                    IconType.ADVANCED
            ));
            bottom += 54 + GAP;
        }

        return bottom;
    }

    private static int addEditorSection(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w, int columns, boolean compact) {
        widgets.add(new SectionLabelWidget(x, y, w, "Theme Editor", state.advancedMode() ? "Advanced color groups" : "Core colors"));
        y += 28;

        if (!state.selectedIsCustom()) {
            widgets.add(new InfoCardWidget(
                    x,
                    y,
                    w,
                    compact ? 54 : 64,
                    "Editor locked",
                    "Built-in themes stay read-only. Create or duplicate a theme to edit colors.",
                    IconType.ADVANCED
            ));
            return y + (compact ? 54 : 64) + GAP;
        }

        widgets.add(new ActionButtonWidget(
                x,
                y,
                w,
                "Theme Name: " + state.themeName(),
                "Click to rename this custom theme.",
                IconType.PRESETS,
                () -> actions.renameTheme(state.selectedThemeId())
        ));
        y += compact ? 38 : 42;

        if (!state.advancedMode()) {
            ThemeStudioState.Token[] tokens = {
                    ThemeStudioState.Token.ACCENT,
                    ThemeStudioState.Token.PANEL,
                    ThemeStudioState.Token.BACKGROUND,
                    ThemeStudioState.Token.BORDER,
                    ThemeStudioState.Token.TEXT,
                    ThemeStudioState.Token.MUTED_TEXT
            };
            return addColorCardGrid(widgets, state, x, y, w, columns, compact, tokens) + GAP;
        }

        for (ThemeStudioState.EditorSection section : ThemeStudioState.EditorSection.values()) {
            boolean expanded = section == state.expandedSection();
            widgets.add(new ActionButtonWidget(
                    x,
                    y,
                    w,
                    (expanded ? "- " : "+ ") + section.label,
                    expanded ? section.description : "Open " + section.label.toLowerCase() + " controls.",
                    expanded ? IconType.THEMES : IconType.PRESETS,
                    () -> actions.expandSection(section)
            ));
            y += compact ? 38 : 40;

            if (expanded) {
                y = addColorCardGrid(widgets, state, x, y, w, columns, compact, section.tokens);
            }
        }

        return y + GAP;
    }

    private static int addColorCardGrid(List<AtmosphereWidget> widgets, ThemeStudioState state, int x, int y, int w, int columns, boolean compact, ThemeStudioState.Token[] tokens) {
        columns = Math.max(1, Math.min(columns, w >= 430 ? 2 : 1));
        int cardH = compact ? V2DesignTokens.SETTING_CARD_HEIGHT_COMPACT : V2DesignTokens.SETTING_CARD_HEIGHT;
        int cardW = (w - SMALL_GAP * (columns - 1)) / columns;
        for (int i = 0; i < tokens.length; i++) {
            int cardX = x + (i % columns) * (cardW + SMALL_GAP);
            int cardY = y + (i / columns) * (cardH + SMALL_GAP);
            widgets.add(new ThemeColorCardWidget(cardX, cardY, cardW, cardH, state, tokens[i], compact));
        }
        return y + ((tokens.length + columns - 1) / columns) * (cardH + SMALL_GAP);
    }

    private static void addLivePreviewSection(List<AtmosphereWidget> widgets, ThemeStudioState state, int x, int y, int w, int previewCardH, boolean compact) {
        widgets.add(new SectionLabelWidget(x, y, w, compact ? "Live Preview (sticky)" : "Live Preview", state.dirty() ? "Unsaved Changes" : "Current preview"));
        y += 24;
        widgets.add(new ThemePreviewWidget(x, y, w, previewCardH, state, compact));
    }

    private static int previewHeight(int width) {
        if (width >= 430) {
            return 188;
        }
        if (width >= 320) {
            return 200;
        }
        return 214;
    }

    private static int compactPreviewHeight(int width) {
        return width >= 360 ? 100 : 112;
    }

    private static int actionX(int x, int buttonW, int columns, int index) {
        return x + (index % columns) * (buttonW + SMALL_GAP);
    }

    private static int red(int color) {
        return (color >>> 16) & 255;
    }

    private static int green(int color) {
        return (color >>> 8) & 255;
    }

    private static int blue(int color) {
        return color & 255;
    }

    private static int replaceRed(int color, int value) {
        return (color & 0xFF00FFFF) | ((value & 255) << 16);
    }

    private static int replaceGreen(int color, int value) {
        return (color & 0xFFFF00FF) | ((value & 255) << 8);
    }

    private static int replaceBlue(int color, int value) {
        return (color & 0xFFFFFF00) | (value & 255);
    }

    private static String trim(TextRenderer renderer, String text, int maxWidth) {
        if (text == null || maxWidth <= 0) {
            return "";
        }
        if (renderer.getWidth(text) <= maxWidth) {
            return text;
        }
        String result = text;
        while (result.length() > 3 && renderer.getWidth(result + "...") > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result + "...";
    }

    private static final class StudioTabsWidget extends AtmosphereWidget {
        private final Runnable customThemesAction;

        private StudioTabsWidget(int x, int y, int width, Runnable customThemesAction) {
            super(x, y, width, 30);
            this.customThemesAction = customThemesAction;
            this.tooltip = "Theme Studio editor and custom theme library.";
        }

        @Override
        public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
            int tabW = Math.min(132, Math.max(98, width / 4));
            UiRender.v2Rule(context, x, y + height - 1, width, tabW);
            UiRender.borderedRect(context, x, y, tabW, height, UiRender.V2_CARD, UiRender.V2_BORDER);
            UiRender.centeredText(context, textRenderer, "Theme Studio", x + tabW / 2, y + 10, UiRender.V2_TEXT);
            UiRender.text(context, textRenderer, "Custom Themes", x + tabW + 24, y + 10, UiRender.V2_MUTED);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && UiRender.hovered(mouseX, mouseY, x + Math.min(132, Math.max(98, width / 4)) + 12, y, 132, height)) {
                customThemesAction.run();
                return true;
            }
            return false;
        }
    }

    private static final class ToolbarButtonWidget extends AtmosphereWidget {
        private final String label;
        private final IconType icon;
        private final Supplier<Boolean> activeSupplier;
        private final Runnable action;

        private ToolbarButtonWidget(int x, int y, int width, int height, String label, String tooltip, IconType icon, Supplier<Boolean> activeSupplier, Runnable action) {
            super(x, y, width, height);
            this.label = label;
            this.icon = icon;
            this.activeSupplier = activeSupplier;
            this.action = action;
            this.tooltip = tooltip;
        }

        @Override
        public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
            Theme theme = ThemeManager.current();
            boolean hover = isHovered(mouseX, mouseY);
            boolean active = activeSupplier != null && activeSupplier.get();
            UiRender.v2Card(context, x, y, width, height, hover, active);

            int iconSize = Math.min(14, height - 10);
            int iconX = x + 7;
            int iconY = y + (height - iconSize) / 2;
            UiRender.v2IconBox(context, iconX, iconY, iconSize, active || hover);
            IconRenderer.drawCentered(context, icon, iconX + iconSize / 2, iconY + iconSize / 2, Math.max(9, iconSize - 3));

            int textX = iconX + iconSize + 7;
            int textW = Math.max(0, x + width - textX - 7);
            if (textW > 6) {
                UiRender.text(context, textRenderer, trim(textRenderer, label, textW), textX, y + (height - 8) / 2, active ? UiRender.V2_ACCENT : theme.text());
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && isHovered(mouseX, mouseY)) {
                action.run();
                return true;
            }
            return false;
        }
    }

    private static final class SelectedThemeWidget extends AtmosphereWidget {
        private final String name;
        private final String status;
        private final boolean applied;
        private final Runnable renameAction;

        private SelectedThemeWidget(int x, int y, int width, int height, String name, String status, boolean applied, Runnable renameAction) {
            super(x, y, width, height);
            this.name = name;
            this.status = status;
            this.applied = applied;
            this.renameAction = renameAction;
            this.tooltip = "Selected theme. Click to rename custom themes.";
        }

        @Override
        public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
            boolean hover = isHovered(mouseX, mouseY);
            UiRender.v2Card(context, x, y, width, height, hover, applied);
            int iconSize = height <= 42 ? 20 : 24;
            int iconX = x + 10;
            int iconY = y + (height - iconSize) / 2;
            UiRender.v2IconBox(context, iconX, iconY, iconSize, true);
            IconRenderer.drawCentered(context, IconType.THEMES, iconX + iconSize / 2, iconY + iconSize / 2, Math.max(13, iconSize - 5));

            int chipW = applied ? 52 : 0;
            int textX = iconX + iconSize + 10;
            int textW = width - (textX - x) - 16 - chipW;
            UiRender.text(context, textRenderer, trim(textRenderer, name, textW), textX, y + 10, UiRender.V2_TEXT);
            UiRender.text(context, textRenderer, trim(textRenderer, status, textW), textX, y + height - 18, applied ? UiRender.V2_ACCENT : UiRender.V2_MUTED);

            if (applied && width > 150) {
                int chipX = x + width - chipW - 10;
                UiRender.borderedRect(context, chipX, y + (height - 18) / 2, chipW, 18, UiRender.V2_ACCENT_SOFT, UiRender.V2_ACCENT_PURPLE);
                UiRender.centeredText(context, textRenderer, "Applied", chipX + chipW / 2, y + (height - 18) / 2 + 5, UiRender.V2_TEXT);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && isHovered(mouseX, mouseY)) {
                renameAction.run();
                return true;
            }
            return false;
        }
    }

    private static final class ThemeColorCardWidget extends AtmosphereWidget {
        private static final int[] CHANNEL_LOW = {0xFF5A1E30, 0xFF17422A, 0xFF172D58};
        private static final int[] CHANNEL_HIGH = {0xFFFF5E8B, 0xFF6BFFB8, 0xFF6D88FF};
        private static final String[] CHANNEL_TAG = {"R", "G", "B"};

        private final ThemeStudioState state;
        private final ThemeStudioState.Token token;
        private final boolean compact;
        private int draggingChannel = -1;

        private ThemeColorCardWidget(int x, int y, int width, int height, ThemeStudioState state, ThemeStudioState.Token token, boolean compact) {
            super(x, y, width, height);
            this.state = state;
            this.token = token;
            this.compact = compact;
            this.tooltip = "Click the hex field to type a color, or drag a red, green, or blue slider.";
        }

        private static final int TAG_COLUMN_W = 14;
        private static final int VALUE_COLUMN_W = 26;

        private int pad() {
            return compact ? 8 : 10;
        }

        private int headerH() {
            return compact ? 24 : 28;
        }

        private int channelRowH() {
            return compact ? 16 : 18;
        }

        private int channelGap() {
            return compact ? 4 : 5;
        }

        private int channelsTop() {
            return y + pad() + headerH() + (compact ? 5 : 7);
        }

        private int channelRowY(int channel) {
            return channelsTop() + channel * (channelRowH() + channelGap());
        }

        private int trackX() {
            return x + pad() + TAG_COLUMN_W;
        }

        private int trackW() {
            return Math.max(24, width - pad() * 2 - TAG_COLUMN_W - VALUE_COLUMN_W);
        }

        private int hexW() {
            return Math.min(94, Math.max(68, width / 3));
        }

        private int hexX() {
            return x + width - hexW() - pad();
        }

        private int hexH() {
            return 18;
        }

        private int hexY() {
            int swatch = compact ? 16 : 20;
            int swatchCenterY = y + pad() + swatch / 2;
            return swatchCenterY - hexH() / 2;
        }

        @Override
        public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
            boolean focused = state.isHexFocused(token);
            boolean hover = isHovered(mouseX, mouseY);
            int color = state.color(token);
            UiRender.v2Card(context, x, y, width, height, hover, focused);

            int pad = pad();
            int swatch = compact ? 16 : 20;
            int swatchX = x + pad;
            int swatchY = y + pad;
            int swatchCenterY = swatchY + swatch / 2;
            UiRender.borderedRect(context, swatchX, swatchY, swatch, swatch, color, focused ? UiRender.V2_ACCENT : UiRender.V2_BORDER);

            int hexY = hexY();
            int labelX = swatchX + swatch + 8;
            int labelW = Math.max(20, hexX() - labelX - 6);
            UiRender.text(context, textRenderer, trim(textRenderer, token.label, labelW), labelX, swatchCenterY - 4, UiRender.V2_TEXT);

            UiRender.borderedRect(context, hexX(), hexY, hexW(), hexH(), focused ? UiRender.V2_ACCENT_SOFT : UiRender.V2_PANEL_ALT, focused ? UiRender.V2_ACCENT : UiRender.V2_BORDER);
            UiRender.centeredText(context, textRenderer, trim(textRenderer, state.hexInput(token), hexW() - 8), hexX() + hexW() / 2, hexY + 5, focused ? UiRender.V2_TEXT : UiRender.V2_MUTED);

            int[] values = {red(color), green(color), blue(color)};
            int trackX = trackX();
            int trackW = trackW();
            for (int channel = 0; channel < 3; channel++) {
                drawChannelSlider(context, textRenderer, channelRowY(channel), channelRowH(), CHANNEL_TAG[channel], values[channel], trackX, trackW, CHANNEL_LOW[channel], CHANNEL_HIGH[channel]);
            }
        }

        private void drawChannelSlider(DrawContext context, TextRenderer textRenderer, int rowY, int rowH, String tag, int value, int trackX, int trackW, int low, int high) {
            int midY = rowY + rowH / 2;

            UiRender.text(context, textRenderer, tag, x + pad(), midY - 4, high);

            int barY = midY - 1;
            UiRender.rect(context, trackX, barY, trackW, 3, UiRender.V2_PANEL_ALT);
            int filled = Math.max(2, trackW * value / 255);
            UiRender.gradientHorizontal(context, trackX, barY, filled, 3, low, high);

            int knobX = trackX + filled - 1;
            int knobH = rowH - 4;
            context.fill(knobX - 3, midY - knobH / 2, knobX + 3, midY + knobH / 2, 0xFFFAFAFA);
            context.fill(knobX - 2, midY - knobH / 2 + 1, knobX + 2, midY + knobH / 2 - 1, high);

            String valueText = String.valueOf(value);
            int valueX = trackX + trackW + VALUE_COLUMN_W - textRenderer.getWidth(valueText) - 2;
            UiRender.text(context, textRenderer, valueText, valueX, midY - 4, UiRender.V2_MUTED);
        }

        private int channelAt(double mouseX, double mouseY) {
            int trackX = trackX();
            int trackW = trackW();
            for (int channel = 0; channel < 3; channel++) {
                int rowY = channelRowY(channel);
                if (UiRender.hovered(mouseX, mouseY, trackX - 5, rowY - 3, trackW + 10, channelRowH() + 6)) {
                    return channel;
                }
            }
            return -1;
        }

        private void applyChannel(int channel, double mouseX) {
            int trackX = trackX();
            int trackW = trackW();
            int value = Math.max(0, Math.min(255, (int) Math.round((mouseX - trackX) * 255.0D / Math.max(1, trackW))));
            int color = state.color(token);
            switch (channel) {
                case 0 -> state.setColor(token, replaceRed(color, value));
                case 1 -> state.setColor(token, replaceGreen(color, value));
                default -> state.setColor(token, replaceBlue(color, value));
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 || !isHovered(mouseX, mouseY)) {
                return false;
            }

            if (UiRender.hovered(mouseX, mouseY, hexX(), hexY(), hexW(), hexH())) {
                state.focusHex(token);
                return true;
            }

            int channel = channelAt(mouseX, mouseY);
            if (channel >= 0) {
                draggingChannel = channel;
                applyChannel(channel, mouseX);
                state.focusHex(token);
                return true;
            }

            state.focusHex(token);
            return true;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (button == 0 && draggingChannel >= 0) {
                applyChannel(draggingChannel, mouseX);
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (button == 0 && draggingChannel >= 0) {
                draggingChannel = -1;
                return true;
            }
            return false;
        }
    }

    private static final class ThemePreviewWidget extends AtmosphereWidget {
        private final ThemeStudioState state;
        private final boolean compact;

        private ThemePreviewWidget(int x, int y, int width, int height, ThemeStudioState state, boolean compact) {
            super(x, y, width, height);
            this.state = state;
            this.compact = compact;
            this.tooltip = "Live preview of the selected theme draft.";
        }

        @Override
        public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
            Theme theme = state.previewTheme();

            UiRender.v2Card(context, x, y, width, height, false, false);
            if (compact) {
                renderCompact(context, textRenderer, theme);
                return;
            }

            UiRender.gradientHorizontal(context, x + 8, y + 8, width - 16, 24, theme.panel(), theme.panelAlt());
            UiRender.text(context, textRenderer, trim(textRenderer, "Preview: " + theme.displayName(), width - 28), x + 14, y + 15, theme.text());

            int sampleY = y + 42;
            UiRender.borderedRect(context, x + 12, sampleY, width - 24, 42, UiRender.V2_CARD, theme.border());
            UiRender.text(context, textRenderer, "Panel card", x + 22, sampleY + 8, theme.text());
            UiRender.text(context, textRenderer, trim(textRenderer, "Muted copy and border sample.", width - 48), x + 22, sampleY + 24, theme.mutedText());

            int buttonY = sampleY + 52;
            int buttonW = Math.min(116, Math.max(76, (width - 34) / 2));
            UiRender.borderedRect(context, x + 12, buttonY, buttonW, 24, UiRender.V2_ACCENT_SOFT, UiRender.V2_ACCENT);
            UiRender.centeredText(context, textRenderer, "Selected", x + 12 + buttonW / 2, buttonY + 8, theme.text());

            int secondaryX = x + 22 + buttonW;
            UiRender.borderedRect(context, secondaryX, buttonY, buttonW, 24, UiRender.V2_PANEL_ALT, UiRender.V2_BORDER);
            UiRender.centeredText(context, textRenderer, "Button", secondaryX + buttonW / 2, buttonY + 8, theme.mutedText());

            int nextY = drawPreviewControls(context, theme, buttonY + 38);

            int[] colors = {theme.accent(), theme.accentSoft(), theme.border(), theme.text(), theme.mutedText()};
            drawSwatches(context, theme, colors, nextY + 8);
        }

        private void renderCompact(DrawContext context, TextRenderer textRenderer, Theme theme) {
            int innerX = x + 10;
            int innerW = width - 20;
            UiRender.gradientHorizontal(context, innerX, y + 8, innerW, 18, theme.panel(), theme.panelAlt());
            UiRender.text(context, textRenderer, trim(textRenderer, "Preview: " + theme.displayName(), innerW - 12), innerX + 6, y + 13, theme.text());

            int sampleY = y + 34;
            if (innerW < 300) {
                UiRender.borderedRect(context, innerX, sampleY, innerW, 24, UiRender.V2_CARD, theme.border());
                UiRender.text(context, textRenderer, trim(textRenderer, "Panel card", innerW - 16), innerX + 8, sampleY + 8, theme.text());

                int buttonW = (innerW - 8) / 2;
                int buttonY = sampleY + 32;
                UiRender.borderedRect(context, innerX, buttonY, buttonW, 24, UiRender.V2_ACCENT_SOFT, UiRender.V2_ACCENT);
                UiRender.centeredText(context, textRenderer, "Selected", innerX + buttonW / 2, buttonY + 8, theme.text());
                UiRender.borderedRect(context, innerX + buttonW + 8, buttonY, buttonW, 24, UiRender.V2_PANEL_ALT, UiRender.V2_BORDER);
                UiRender.centeredText(context, textRenderer, "Button", innerX + buttonW + 8 + buttonW / 2, buttonY + 8, theme.mutedText());

                // Only draw the accent strip where it actually has clearance below the button
                // row, otherwise it visually collides with it on short cards.
                int stripY = buttonY + 24 + 6;
                if (stripY + 4 <= y + height - 4) {
                    UiRender.gradientHorizontal(context, innerX, stripY, innerW, 4, theme.accentSoft(), theme.accent());
                }
                return;
            }

            int buttonW = Math.min(92, Math.max(58, (innerW - 12) / 3));
            int panelW = Math.max(64, innerW - buttonW * 2 - 20);
            UiRender.borderedRect(context, innerX, sampleY, panelW, 28, UiRender.V2_CARD, theme.border());
            UiRender.text(context, textRenderer, trim(textRenderer, "Panel", panelW - 16), innerX + 8, sampleY + 6, theme.text());
            UiRender.text(context, textRenderer, trim(textRenderer, "Muted sample", panelW - 16), innerX + 8, sampleY + 18, theme.mutedText());

            int selectedX = innerX + panelW + 8;
            UiRender.borderedRect(context, selectedX, sampleY, buttonW, 28, UiRender.V2_ACCENT_SOFT, UiRender.V2_ACCENT);
            UiRender.centeredText(context, textRenderer, "Selected", selectedX + buttonW / 2, sampleY + 10, theme.text());

            int normalX = selectedX + buttonW + 8;
            UiRender.borderedRect(context, normalX, sampleY, buttonW, 28, UiRender.V2_PANEL_ALT, UiRender.V2_BORDER);
            UiRender.centeredText(context, textRenderer, "Button", normalX + buttonW / 2, sampleY + 10, theme.mutedText());

            int stripY = y + height - 18;
            UiRender.gradientHorizontal(context, innerX, stripY, Math.max(20, innerW - 76), 4, theme.accentSoft(), theme.accent());
            UiRender.borderedRect(context, x + width - 54, stripY - 6, 38, 16, theme.accentSoft(), theme.accent());
            context.fill(x + width - 28, stripY - 3, x + width - 18, stripY + 7, theme.text());
        }

        private int drawPreviewControls(DrawContext context, Theme theme, int topY) {
            int innerX = x + 14;
            int innerW = width - 28;
            int sliderMinW = 92;
            int toggleW = 36;
            int gap = 12;

            if (innerW >= sliderMinW + toggleW + gap) {
                int toggleX = innerX + innerW - toggleW;
                int sliderW = Math.max(24, toggleX - innerX - gap);
                drawSlider(context, theme, innerX, topY + 8, sliderW);
                drawToggle(context, theme, toggleX, topY + 2, toggleW);
                return topY + 24;
            }

            drawSlider(context, theme, innerX, topY + 6, innerW);
            drawToggle(context, theme, innerX, topY + 22, toggleW);
            return topY + 40;
        }

        private void drawSlider(DrawContext context, Theme theme, int sliderX, int sliderY, int sliderW) {
            UiRender.rect(context, sliderX, sliderY + 6, sliderW, 3, theme.border());
            UiRender.gradientHorizontal(context, sliderX, sliderY + 6, Math.max(8, sliderW * 2 / 3), 3, theme.accentSoft(), theme.accent());
        }

        private void drawToggle(DrawContext context, Theme theme, int toggleX, int toggleY, int toggleW) {
            UiRender.borderedRect(context, toggleX, toggleY, toggleW, 16, theme.accentSoft(), theme.accent());
            int knobSize = 10;
            int knobX = toggleX + toggleW - knobSize - 3;
            context.fill(knobX, toggleY + 3, knobX + knobSize, toggleY + 13, theme.text());
        }

        private void drawSwatches(DrawContext context, Theme theme, int[] colors, int startY) {
            int swatchSize = width >= 320 ? 12 : 10;
            int gap = 8;
            int innerX = x + 14;
            int innerRight = x + width - 14;
            int swatchX = innerX;
            int swatchY = startY;

            for (int color : colors) {
                if (swatchX + swatchSize > innerRight) {
                    swatchX = innerX;
                    swatchY += swatchSize + gap;
                }

                if (swatchY + swatchSize <= y + height - 10) {
                    UiRender.borderedRect(context, swatchX, swatchY, swatchSize, swatchSize, color, theme.border());
                }

                swatchX += swatchSize + gap;
            }
        }

        private String trim(TextRenderer renderer, String text, int maxWidth) {
            if (text == null || maxWidth <= 0) {
                return "";
            }

            if (renderer.getWidth(text) <= maxWidth) {
                return text;
            }

            String result = text;
            while (result.length() > 3 && renderer.getWidth(result + "...") > maxWidth) {
                result = result.substring(0, result.length() - 1);
            }

            return result + "...";
        }
    }
}
