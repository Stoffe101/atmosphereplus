package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.themes.CustomThemeData;
import com.skrra.atmosphereplus.themes.CustomThemeManager;
import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.widgets.ActionButtonWidget;
import com.skrra.atmosphereplus.ui.widgets.AtmosphereWidget;
import com.skrra.atmosphereplus.ui.widgets.ChoiceButtonWidget;
import com.skrra.atmosphereplus.ui.widgets.ColorHexWidget;
import com.skrra.atmosphereplus.ui.widgets.InfoCardWidget;
import com.skrra.atmosphereplus.ui.widgets.SectionLabelWidget;
import com.skrra.atmosphereplus.ui.widgets.SliderWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public final class ThemeStudioPage {
    private static final int GAP = 10;
    private static final int SMALL_GAP = 8;

    private enum LayoutMode {
        WIDE,
        MEDIUM,
        NARROW
    }

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

    public static int addWidgets(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int contentX, int contentY, int contentW) {
        LayoutMode mode = layoutMode(contentW);

        return switch (mode) {
            case WIDE -> addTwoColumnWidgets(widgets, state, actions, contentX, contentY, contentW, 58);
            case MEDIUM -> addTwoColumnWidgets(widgets, state, actions, contentX, contentY, contentW, 50);
            case NARROW -> addStackedWidgets(widgets, state, actions, contentX, contentY, contentW);
        };
    }

    private static LayoutMode layoutMode(int width) {
        if (width >= 820) {
            return LayoutMode.WIDE;
        }
        if (width >= 620) {
            return LayoutMode.MEDIUM;
        }
        return LayoutMode.NARROW;
    }

    private static int addTwoColumnWidgets(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w, int leftPercent) {
        int leftW = Math.max(280, (w - GAP) * leftPercent / 100);
        int rightW = w - GAP - leftW;

        int leftY = y;
        int rightY = y;

        leftY = addLibrarySection(widgets, state, actions, x, leftY, leftW);
        leftY = addEditorSection(widgets, state, actions, x, leftY, leftW);

        int rightX = x + leftW + GAP;
        rightY = addLivePreviewSection(widgets, state, rightX, rightY, rightW);
        rightY = addPrimaryActions(widgets, state, actions, rightX, rightY, rightW);
        rightY = addSecondaryActions(widgets, state, actions, rightX, rightY, rightW);

        return Math.max(leftY, rightY);
    }

    private static int addStackedWidgets(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        y = addLibrarySection(widgets, state, actions, x, y, w);
        y = addLivePreviewSection(widgets, state, x, y, w);
        y = addEditorSection(widgets, state, actions, x, y, w);
        y = addPrimaryActions(widgets, state, actions, x, y, w);
        y = addSecondaryActions(widgets, state, actions, x, y, w);
        return y;
    }

    private static int addLibrarySection(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        Theme selected = ThemeManager.byId(state.selectedThemeId());
        widgets.add(new SectionLabelWidget(x, y, w, "Theme Studio", state.dirty() ? "Unsaved Changes" : "Custom themes"));
        y += 28;

        if (!CustomThemeManager.hasCustomThemes()) {
            widgets.add(new InfoCardWidget(
                    x,
                    y,
                    w,
                    72,
                    "No custom themes yet",
                    "Create a custom theme from the active look or duplicate the selected theme.",
                    IconType.THEMES
            ));
            return y + 72 + GAP;
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
        y += 42;

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
            y += 42;
        }

        int columns = w >= 520 ? 2 : 1;
        int cardW = (w - SMALL_GAP * (columns - 1)) / columns;
        int index = 0;

        for (CustomThemeData data : CustomThemeManager.all().values()) {
            if (!state.matchesThemeSearch(data)) {
                continue;
            }

            int cardX = x + (index % columns) * (cardW + SMALL_GAP);
            int cardY = y + (index / columns) * 42;
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

        int bottom = y + ((index + columns - 1) / columns) * 42 + GAP;
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

    private static int addEditorSection(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, state.advancedMode() ? "Advanced Mode" : "Simple Mode", state.advancedMode() ? "One group expanded" : "Name and accent"));
        y += 28;

        if (!state.selectedIsCustom()) {
            widgets.add(new InfoCardWidget(
                    x,
                    y,
                    w,
                    68,
                    "Editor locked",
                    "Built-in themes stay read-only. Create or duplicate a theme to edit colors.",
                    IconType.ADVANCED
            ));
            return y + 68 + GAP;
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
        y += 44;

        if (!state.advancedMode()) {
            y = addColorControl(widgets, state, x, y, w, ThemeStudioState.Token.ACCENT, true);
            y = addInlineSaveReset(widgets, state, actions, x, y, w);
            return y + GAP;
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
            y += 40;

            if (expanded) {
                for (ThemeStudioState.Token token : section.tokens) {
                    y = addColorControl(widgets, state, x, y, w, token, false);
                }
            }
        }

        y = addInlineSaveReset(widgets, state, actions, x, y, w);
        return y + GAP;
    }

    private static int addInlineSaveReset(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        int columns = w >= 430 ? 2 : 1;
        int buttonW = (w - SMALL_GAP * (columns - 1)) / columns;

        widgets.add(new ActionButtonWidget(x, y, buttonW, state.dirty() ? "Save Changes" : "Save Theme", "Persist custom theme edits.", IconType.THEMES, () -> actions.saveTheme(state.selectedThemeId())));
        widgets.add(new ActionButtonWidget(x + (columns > 1 ? buttonW + SMALL_GAP : 0), y + (columns > 1 ? 0 : 40), buttonW, "Reset Theme", "Reset this custom draft to Midnight colors.", IconType.FOG, actions::resetTheme));

        return y + (columns > 1 ? 40 : 80) + GAP;
    }

    private static int addColorControl(List<AtmosphereWidget> widgets, ThemeStudioState state, int x, int y, int w, ThemeStudioState.Token token, boolean compact) {
        widgets.add(new ColorHexWidget(
                x,
                y,
                w,
                compact ? "Accent Color" : token.label,
                () -> state.color(token),
                () -> state.hexInput(token),
                () -> state.isHexFocused(token),
                () -> state.focusHex(token)
        ));
        y += 38;

        int columns = w >= 560 ? 3 : w >= 390 ? 2 : 1;
        int sliderW = (w - SMALL_GAP * (columns - 1)) / columns;
        widgets.add(new SliderWidget(sliderX(x, sliderW, columns, 0), y + sliderY(0, columns), sliderW, "Red", null, 0f, 255f, () -> (float) red(state.color(token)), value -> state.setColor(token, replaceRed(state.color(token), Math.round(value))), value -> String.valueOf(Math.round(value))));
        widgets.add(new SliderWidget(sliderX(x, sliderW, columns, 1), y + sliderY(1, columns), sliderW, "Green", null, 0f, 255f, () -> (float) green(state.color(token)), value -> state.setColor(token, replaceGreen(state.color(token), Math.round(value))), value -> String.valueOf(Math.round(value))));
        widgets.add(new SliderWidget(sliderX(x, sliderW, columns, 2), y + sliderY(2, columns), sliderW, "Blue", null, 0f, 255f, () -> (float) blue(state.color(token)), value -> state.setColor(token, replaceBlue(state.color(token), Math.round(value))), value -> String.valueOf(Math.round(value))));

        return y + ((3 + columns - 1) / columns) * 50 + (compact ? 6 : GAP);
    }

    private static int addLivePreviewSection(List<AtmosphereWidget> widgets, ThemeStudioState state, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Live Preview", state.dirty() ? "Unsaved Changes" : "Current preview"));
        y += 28;
        int previewH = previewHeight(w);
        widgets.add(new ThemePreviewWidget(x, y, w, previewH, state));
        return y + previewH + GAP;
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

    private static int addPrimaryActions(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Mode", "Simple or advanced"));
        y += 28;

        int columns = w >= 430 ? 2 : 1;
        int buttonW = (w - SMALL_GAP * (columns - 1)) / columns;

        widgets.add(new ActionButtonWidget(x, y, buttonW, state.advancedMode() ? "Simple Mode" : "Advanced Mode", state.advancedMode() ? "Return to the compact editor." : "Show grouped color controls.", IconType.ADVANCED, actions::toggleAdvancedMode));
        widgets.add(new ActionButtonWidget(x + (columns > 1 ? buttonW + SMALL_GAP : 0), y + (columns > 1 ? 0 : 40), buttonW, "Revert", "Discard unsaved edits.", IconType.PRESETS, actions::revertTheme));

        return y + (columns > 1 ? 40 : 80) + GAP;
    }

    private static int addSecondaryActions(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Manage", "Theme library"));
        y += 28;

        int columns = w >= 430 ? 2 : 1;
        int buttonW = (w - SMALL_GAP * (columns - 1)) / columns;
        int index = 0;

        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Create", "Create a custom theme from the active theme.", IconType.THEMES, actions::createTheme));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Duplicate", "Duplicate the selected theme.", IconType.PRESETS, () -> actions.duplicateTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Delete", "Delete the selected custom theme.", IconType.ADVANCED, () -> actions.deleteTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Export Theme", "Export selected custom theme.", IconType.THEMES, () -> actions.exportTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Import Theme", "Import theme from config.", IconType.PRESETS, actions::importTheme));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Reload", "Reload custom themes from disk.", IconType.ADVANCED, actions::reloadThemes));
        index++;

        return y + ((index + columns - 1) / columns) * 40 + GAP;
    }

    private static int sliderX(int x, int sliderW, int columns, int index) {
        return x + (index % columns) * (sliderW + SMALL_GAP);
    }

    private static int sliderY(int index, int columns) {
        return (index / columns) * 50;
    }

    private static int actionX(int x, int buttonW, int columns, int index) {
        return x + (index % columns) * (buttonW + SMALL_GAP);
    }

    private static int actionY(int y, int index, int columns) {
        return y + (index / columns) * 40;
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

    private static final class ThemePreviewWidget extends AtmosphereWidget {
        private final ThemeStudioState state;

        private ThemePreviewWidget(int x, int y, int width, int height, ThemeStudioState state) {
            super(x, y, width, height);
            this.state = state;
            this.tooltip = "Live preview of the selected theme draft.";
        }

        @Override
        public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
            Theme theme = state.previewTheme();

            UiRender.card(context, x, y, width, height, theme.background(), theme.border());
            UiRender.gradientHorizontal(context, x + 8, y + 8, width - 16, 24, theme.panel(), theme.panelAlt());
            UiRender.text(context, textRenderer, trim(textRenderer, "Preview: " + theme.displayName(), width - 28), x + 14, y + 15, theme.text());

            int sampleY = y + 42;
            UiRender.borderedRect(context, x + 12, sampleY, width - 24, 42, theme.panel(), theme.border());
            UiRender.text(context, textRenderer, "Panel card", x + 22, sampleY + 8, theme.text());
            UiRender.text(context, textRenderer, trim(textRenderer, "Muted copy and border sample.", width - 48), x + 22, sampleY + 24, theme.mutedText());

            int buttonY = sampleY + 52;
            int buttonW = Math.min(116, Math.max(76, (width - 34) / 2));
            UiRender.borderedRect(context, x + 12, buttonY, buttonW, 24, theme.accentSoft(), theme.accent());
            UiRender.centeredText(context, textRenderer, "Selected", x + 12 + buttonW / 2, buttonY + 8, theme.text());

            int secondaryX = x + 22 + buttonW;
            UiRender.borderedRect(context, secondaryX, buttonY, buttonW, 24, theme.panelAlt(), theme.border());
            UiRender.centeredText(context, textRenderer, "Button", secondaryX + buttonW / 2, buttonY + 8, theme.mutedText());

            int nextY = drawPreviewControls(context, theme, buttonY + 38);

            int[] colors = {theme.accent(), theme.accentSoft(), theme.border(), theme.text(), theme.mutedText()};
            drawSwatches(context, theme, colors, nextY + 8);
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
