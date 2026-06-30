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

        int y = contentY;
        widgets.add(new StudioTabsWidget(contentX, y, contentW, actions::focusThemeSearch));
        y += V2DesignTokens.TOP_BAR_HEIGHT_COMPACT;

        y = addTopActionRow(widgets, state, actions, contentX, y, spec.sidePreview() ? spec.editorW() : contentW, spec.actionColumns(), spec.compactDensity());
        int bodyTop = y;

        int leftY = addSelectedThemeCard(widgets, state, actions, spec.editorX(), bodyTop, spec.editorW(), spec.compactDensity());
        leftY = addEditorSection(widgets, state, actions, spec.editorX(), leftY, spec.editorW(), spec.columns(), spec.compactDensity());

        if (spec.sidePreview()) {
            leftY = addPrimaryActions(widgets, state, actions, spec.editorX(), leftY, spec.editorW(), spec.compactDensity());
            leftY = addLibrarySection(widgets, state, actions, spec.editorX(), leftY, spec.editorW(), spec.compactDensity());
            leftY = addSecondaryActions(widgets, state, actions, spec.editorX(), leftY, spec.editorW(), spec.compactDensity());
            int previewY = addLivePreviewSection(widgets, state, spec.previewX(), bodyTop, spec.previewW(), spec.compactDensity());
            return Math.max(leftY, previewY) + GAP;
        }

        leftY = addCompactLivePreviewSection(widgets, state, contentX, leftY, contentW);
        leftY = addPrimaryActions(widgets, state, actions, spec.editorX(), leftY, spec.editorW(), spec.compactDensity());
        leftY = addLibrarySection(widgets, state, actions, spec.editorX(), leftY, spec.editorW(), spec.compactDensity());
        leftY = addSecondaryActions(widgets, state, actions, spec.editorX(), leftY, spec.editorW(), spec.compactDensity());
        return leftY + GAP;
    }

    private static int addTopActionRow(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w, int columns, boolean compact) {
        int buttonW = (w - SMALL_GAP * (columns - 1)) / columns;
        int index = 0;

        int rowH = compact ? 36 : 42;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns, rowH), buttonW, "Create", "New theme", IconType.THEMES, actions::createTheme));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns, rowH), buttonW, "Duplicate", "Copy theme", IconType.PRESETS, () -> actions.duplicateTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns, rowH), buttonW, state.dirty() ? "Save" : "Save", "Save changes", IconType.THEMES, () -> actions.saveTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns, rowH), buttonW, "Reset", "Reset draft", IconType.FOG, actions::resetTheme));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns, rowH), buttonW, "Library", "Reload themes", IconType.PRESETS, actions::reloadThemes));
        index++;

        return y + ((index + columns - 1) / columns) * rowH + GAP;
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
            y = addColorCardGrid(widgets, state, x, y, w, columns, compact, tokens);
            widgets.add(new ActionButtonWidget(x, y, w, "Advanced Options", "Tweaks for all theme tokens.", IconType.ADVANCED, actions::toggleAdvancedMode));
            return y + (compact ? 38 : 42) + GAP;
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

    private static int addPrimaryActions(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w, boolean compact) {
        widgets.add(new SectionLabelWidget(x, y, w, "Mode", "Editor controls"));
        y += 28;

        int columns = w >= 430 ? 2 : 1;
        int buttonW = (w - SMALL_GAP * (columns - 1)) / columns;

        widgets.add(new ActionButtonWidget(x, y, buttonW, state.advancedMode() ? "Simple Mode" : "Advanced Mode", state.advancedMode() ? "Return to core color cards." : "Show grouped color controls.", IconType.ADVANCED, actions::toggleAdvancedMode));
        widgets.add(new ActionButtonWidget(x + (columns > 1 ? buttonW + SMALL_GAP : 0), y + (columns > 1 ? 0 : 40), buttonW, "Revert", "Discard unsaved edits.", IconType.PRESETS, actions::revertTheme));

        return y + (columns > 1 ? (compact ? 36 : 40) : (compact ? 72 : 80)) + GAP;
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

    private static int addLivePreviewSection(List<AtmosphereWidget> widgets, ThemeStudioState state, int x, int y, int w, boolean compact) {
        widgets.add(new SectionLabelWidget(x, y, w, "Live Preview", state.dirty() ? "Unsaved Changes" : "Current preview"));
        y += 28;
        int previewH = compact ? compactPreviewHeight(w) + 58 : previewHeight(w);
        widgets.add(new ThemePreviewWidget(x, y, w, previewH, state, compact));
        return y + previewH + GAP;
    }

    private static int addCompactLivePreviewSection(List<AtmosphereWidget> widgets, ThemeStudioState state, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Compact Preview", state.dirty() ? "Unsaved Changes" : "Current preview"));
        y += 28;
        int previewH = compactPreviewHeight(w);
        widgets.add(new ThemePreviewWidget(x, y, w, previewH, state, true));
        return y + previewH + SMALL_GAP;
    }

    private static int previewHeight(int width) {
        if (width >= 430) {
            return 176;
        }
        if (width >= 320) {
            return 188;
        }
        return 202;
    }

    private static int compactPreviewHeight(int width) {
        return width >= 360 ? 92 : 104;
    }

    private static int addSecondaryActions(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w, boolean compact) {
        widgets.add(new SectionLabelWidget(x, y, w, "Library Tools", "Import and cleanup"));
        y += 28;

        int columns = w >= 430 ? 2 : 1;
        int buttonW = (w - SMALL_GAP * (columns - 1)) / columns;
        int index = 0;

        int rowH = compact ? 36 : 40;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns, rowH), buttonW, "Delete", "Delete the selected custom theme.", IconType.ADVANCED, () -> actions.deleteTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns, rowH), buttonW, "Export Theme", "Export selected custom theme.", IconType.THEMES, () -> actions.exportTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns, rowH), buttonW, "Import Theme", "Import theme from config.", IconType.PRESETS, actions::importTheme));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns, rowH), buttonW, "Reload", "Reload custom themes from disk.", IconType.ADVANCED, actions::reloadThemes));
        index++;

        return y + ((index + columns - 1) / columns) * rowH + GAP;
    }

    private static int actionX(int x, int buttonW, int columns, int index) {
        return x + (index % columns) * (buttonW + SMALL_GAP);
    }

    private static int actionY(int y, int index, int columns, int rowH) {
        return y + (index / columns) * rowH;
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
        private final ThemeStudioState state;
        private final ThemeStudioState.Token token;
        private final boolean compact;

        private ThemeColorCardWidget(int x, int y, int width, int height, ThemeStudioState state, ThemeStudioState.Token token, boolean compact) {
            super(x, y, width, height);
            this.state = state;
            this.token = token;
            this.compact = compact;
            this.tooltip = "Click the hex field to type a color, or click a red, green, or blue strip.";
        }

        @Override
        public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
            boolean focused = state.isHexFocused(token);
            boolean hover = isHovered(mouseX, mouseY);
            int color = state.color(token);
            UiRender.v2Card(context, x, y, width, height, hover, focused);

            int pad = compact ? 8 : 10;
            int swatch = compact ? 18 : 22;
            UiRender.borderedRect(context, x + pad, y + pad + 18, swatch, swatch, color, focused ? UiRender.V2_ACCENT : UiRender.V2_BORDER);

            UiRender.text(context, textRenderer, trim(textRenderer, token.label, width - pad * 2 - 18), x + pad, y + pad, UiRender.V2_TEXT);
            int hexW = Math.min(94, Math.max(68, width / 3));
            int hexX = x + width - hexW - pad;
            UiRender.borderedRect(context, hexX, y + pad + 16, hexW, 20, focused ? UiRender.V2_ACCENT_SOFT : UiRender.V2_PANEL_ALT, focused ? UiRender.V2_ACCENT : UiRender.V2_BORDER);
            UiRender.centeredText(context, textRenderer, trim(textRenderer, state.hexInput(token), hexW - 8), hexX + hexW / 2, y + pad + 22, focused ? UiRender.V2_TEXT : UiRender.V2_MUTED);

            int stripX = x + pad;
            int stripY = y + height - (compact ? 24 : 30);
            int stripW = width - pad * 2;
            drawChannel(context, stripX, stripY, stripW, red(color), 0xFF5A1E30, 0xFFFF5E8B);
            drawChannel(context, stripX, stripY + 7, stripW, green(color), 0xFF17422A, 0xFF6BFFB8);
            drawChannel(context, stripX, stripY + 14, stripW, blue(color), 0xFF172D58, 0xFF6D88FF);
        }

        private void drawChannel(DrawContext context, int sx, int sy, int sw, int value, int low, int high) {
            UiRender.rect(context, sx, sy, sw, 3, UiRender.V2_PANEL_ALT);
            UiRender.gradientHorizontal(context, sx, sy, Math.max(1, sw * value / 255), 3, low, high);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 || !isHovered(mouseX, mouseY)) {
                return false;
            }

            int pad = compact ? 8 : 10;
            int stripY = y + height - (compact ? 24 : 30);
            int stripW = width - pad * 2;
            int stripX = x + pad;
            int channel = -1;
            if (UiRender.hovered(mouseX, mouseY, stripX, stripY - 3, stripW, 8)) {
                channel = 0;
            } else if (UiRender.hovered(mouseX, mouseY, stripX, stripY + 4, stripW, 8)) {
                channel = 1;
            } else if (UiRender.hovered(mouseX, mouseY, stripX, stripY + 11, stripW, 8)) {
                channel = 2;
            }

            if (channel >= 0) {
                int value = Math.max(0, Math.min(255, (int) ((mouseX - stripX) * 255.0D / Math.max(1, stripW))));
                int color = state.color(token);
                if (channel == 0) {
                    state.setColor(token, replaceRed(color, value));
                } else if (channel == 1) {
                    state.setColor(token, replaceGreen(color, value));
                } else {
                    state.setColor(token, replaceBlue(color, value));
                }
            }

            state.focusHex(token);
            return true;
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

                UiRender.gradientHorizontal(context, innerX, y + height - 14, innerW, 4, theme.accentSoft(), theme.accent());
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
