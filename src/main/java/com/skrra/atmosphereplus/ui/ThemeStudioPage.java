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
    private static final int SECTION_GAP = 10;
    private static final int CARD_GAP = 8;

    private ThemeStudioPage() {
    }

    public interface Actions {
        void createTheme();

        void duplicateTheme(String sourceId);

        void renameTheme(String themeId);

        void deleteTheme(String themeId);

        void saveTheme(String themeId);

        void reloadThemes();

        void applyTheme(String themeId);

        void selectTheme(String themeId);

        void revertTheme();

        void resetTheme();

        void toggleAdvancedMode();
    }

    public static int addWidgets(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int contentX, int contentY, int contentW) {
        if (contentW >= 680) {
            return addWideWidgets(widgets, state, actions, contentX, contentY, contentW);
        }

        return addStackedWidgets(widgets, state, actions, contentX, contentY, contentW);
    }

    private static int addWideWidgets(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int contentX, int contentY, int contentW) {
        int gap = 12;
        int leftW = Math.max(300, (contentW - gap) * 60 / 100);
        int rightW = contentW - gap - leftW;

        int leftY = contentY;
        int rightY = contentY;

        leftY = addCurrentThemeSection(widgets, state, actions, contentX, leftY, leftW);
        leftY = addLibrarySection(widgets, state, actions, contentX, leftY, leftW);
        leftY = addEditorSection(widgets, state, actions, contentX, leftY, leftW);

        int rightX = contentX + leftW + gap;
        rightY = addLivePreviewSection(widgets, state, rightX, rightY, rightW);
        rightY = addThemeActionsSection(widgets, state, actions, rightX, rightY, rightW);

        return Math.max(leftY, rightY);
    }

    private static int addStackedWidgets(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int contentX, int contentY, int contentW) {
        int y = contentY;
        y = addCurrentThemeSection(widgets, state, actions, contentX, y, contentW);
        y = addLibrarySection(widgets, state, actions, contentX, y, contentW);
        y = addEditorSection(widgets, state, actions, contentX, y, contentW);
        y = addLivePreviewSection(widgets, state, contentX, y, contentW);
        y = addThemeActionsSection(widgets, state, actions, contentX, y, contentW);
        return y;
    }

    private static int addCurrentThemeSection(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        Theme current = ThemeManager.current();
        Theme selected = ThemeManager.byId(state.selectedThemeId());
        widgets.add(new SectionLabelWidget(x, y, w, "Current Theme", state.dirty() ? "Unsaved edits" : "Selected interface theme"));
        y += 28;

        String selectedText = selected == null ? "No selection" : "Selected: " + selected.displayName();
        if (state.dirty()) {
            selectedText += " (unsaved)";
        }

        widgets.add(new InfoCardWidget(
                x,
                y,
                w,
                58,
                current.displayName(),
                selectedText + ". Active id: " + ConfigManager.get().theme,
                IconType.THEMES
        ));

        y += 66;
        widgets.add(new ActionButtonWidget(
                x,
                y,
                w,
                selected == null ? "Apply Theme" : "Apply " + selected.displayName(),
                "Apply changes only after saving; preview updates immediately while editing.",
                IconType.THEMES,
                () -> actions.applyTheme(state.selectedThemeId())
        ));

        return y + 44 + SECTION_GAP;
    }

    private static int addLibrarySection(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Custom Themes", "Editable library"));
        y += 28;

        if (!CustomThemeManager.hasCustomThemes()) {
            widgets.add(new InfoCardWidget(
                    x,
                    y,
                    w,
                    74,
                    "No custom themes yet",
                    "Create a theme from the active look or duplicate a built-in theme to unlock editing.",
                    IconType.THEMES
            ));
            return y + 74 + SECTION_GAP;
        }

        int columns = w >= 520 ? 2 : 1;
        int cardW = (w - CARD_GAP * (columns - 1)) / columns;
        int index = 0;

        for (CustomThemeData data : CustomThemeManager.all().values()) {
            int cardX = x + (index % columns) * (cardW + CARD_GAP);
            int cardY = y + (index / columns) * 46;
            widgets.add(new ChoiceButtonWidget(
                    cardX,
                    cardY,
                    cardW,
                    data.displayName,
                    data.id.equals(ConfigManager.get().theme) ? "Applied custom theme" : "Custom theme",
                    IconType.THEMES,
                    () -> data.id.equals(state.selectedThemeId()),
                    () -> actions.selectTheme(data.id)
            ));
            index++;
        }

        return y + ((index + columns - 1) / columns) * 46 + SECTION_GAP;
    }

    private static int addEditorSection(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, state.advancedMode() ? "Advanced Mode" : "Simple Mode", "Custom theme editor"));
        y += 28;

        if (!state.selectedIsCustom()) {
            widgets.add(new InfoCardWidget(
                    x,
                    y,
                    w,
                    76,
                    "Read-only theme",
                    "Built-in themes cannot be edited. Duplicate this theme to create a custom editable copy.",
                    IconType.ADVANCED
            ));
            return y + 76 + SECTION_GAP;
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
        y += 42;

        widgets.add(new ActionButtonWidget(
                x,
                y,
                w,
                state.advancedMode() ? "Advanced: On" : "Advanced: Off",
                state.advancedMode() ? "Showing all editable color tokens." : "Simple mode edits only the accent family.",
                IconType.ADVANCED,
                actions::toggleAdvancedMode
        ));
        y += 46;

        ThemeStudioState.Token[] tokens = state.advancedMode()
                ? ThemeStudioState.Token.values()
                : new ThemeStudioState.Token[]{ThemeStudioState.Token.ACCENT};

        for (ThemeStudioState.Token token : tokens) {
            y = addColorControl(widgets, state, x, y, w, token);
        }

        return y + SECTION_GAP;
    }

    private static int addColorControl(List<AtmosphereWidget> widgets, ThemeStudioState state, int x, int y, int w, ThemeStudioState.Token token) {
        widgets.add(new ColorHexWidget(
                x,
                y,
                w,
                token.label,
                () -> state.color(token),
                () -> state.hexInput(token),
                () -> state.isHexFocused(token),
                () -> state.focusHex(token)
        ));
        y += 38;

        int columns = w >= 520 ? 3 : 1;
        int sliderW = (w - CARD_GAP * (columns - 1)) / columns;
        widgets.add(new SliderWidget(sliderX(x, sliderW, columns, 0), y + sliderY(0, columns), sliderW, "Red", null, 0f, 255f, () -> (float) red(state.color(token)), value -> state.setColor(token, replaceRed(state.color(token), Math.round(value))), value -> String.valueOf(Math.round(value))));
        widgets.add(new SliderWidget(sliderX(x, sliderW, columns, 1), y + sliderY(1, columns), sliderW, "Green", null, 0f, 255f, () -> (float) green(state.color(token)), value -> state.setColor(token, replaceGreen(state.color(token), Math.round(value))), value -> String.valueOf(Math.round(value))));
        widgets.add(new SliderWidget(sliderX(x, sliderW, columns, 2), y + sliderY(2, columns), sliderW, "Blue", null, 0f, 255f, () -> (float) blue(state.color(token)), value -> state.setColor(token, replaceBlue(state.color(token), Math.round(value))), value -> String.valueOf(Math.round(value))));

        return y + (columns == 1 ? 148 : 54) + SECTION_GAP;
    }

    private static int addLivePreviewSection(List<AtmosphereWidget> widgets, ThemeStudioState state, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Live Preview", state.dirty() ? "Unsaved preview" : "Saved preview"));
        y += 28;
        widgets.add(new ThemePreviewWidget(x, y, w, 184, state));
        return y + 184 + SECTION_GAP;
    }

    private static int addThemeActionsSection(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Theme Actions", "Create, save and manage"));
        y += 28;

        int columns = w >= 430 ? 2 : 1;
        int buttonW = (w - CARD_GAP * (columns - 1)) / columns;
        int index = 0;

        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Create Theme", "Create a custom theme from the active theme.", IconType.THEMES, actions::createTheme));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Duplicate", "Duplicate the selected theme.", IconType.PRESETS, () -> actions.duplicateTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, state.dirty() ? "Save Changes" : "Save Theme", "Persist custom theme edits to disk.", IconType.THEMES, () -> actions.saveTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Revert Changes", "Discard unsaved edits and reload the saved custom theme.", IconType.ADVANCED, actions::revertTheme));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Reset Theme", "Reset this custom draft to the default Midnight colors.", IconType.FOG, actions::resetTheme));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Delete", "Delete the selected custom theme after confirmation.", IconType.ADVANCED, () -> actions.deleteTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Reload", "Reload custom themes from disk.", IconType.ADVANCED, actions::reloadThemes));
        index++;

        return y + ((index + columns - 1) / columns) * 42 + SECTION_GAP;
    }

    private static int sliderX(int x, int sliderW, int columns, int index) {
        return x + (index % columns) * (sliderW + CARD_GAP);
    }

    private static int sliderY(int index, int columns) {
        return (index / columns) * 50;
    }

    private static int actionX(int x, int buttonW, int columns, int index) {
        return x + (index % columns) * (buttonW + CARD_GAP);
    }

    private static int actionY(int y, int index, int columns) {
        return y + (index / columns) * 42;
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

            int sampleY = y + 44;
            UiRender.borderedRect(context, x + 12, sampleY, width - 24, 48, theme.panel(), theme.border());
            UiRender.text(context, textRenderer, "Panel card", x + 22, sampleY + 8, theme.text());
            UiRender.text(context, textRenderer, trim(textRenderer, "Muted text, borders and panels update immediately.", width - 48), x + 22, sampleY + 24, theme.mutedText());

            int buttonY = sampleY + 60;
            int buttonW = Math.min(116, Math.max(76, (width - 34) / 2));
            UiRender.borderedRect(context, x + 12, buttonY, buttonW, 24, theme.accentSoft(), theme.accent());
            UiRender.centeredText(context, textRenderer, "Selected", x + 12 + buttonW / 2, buttonY + 8, theme.text());

            int secondaryX = x + 22 + buttonW;
            UiRender.borderedRect(context, secondaryX, buttonY, buttonW, 24, theme.panelAlt(), theme.border());
            UiRender.centeredText(context, textRenderer, "Button", secondaryX + buttonW / 2, buttonY + 8, theme.mutedText());

            int toggleY = buttonY + 40;
            UiRender.borderedRect(context, x + 12, toggleY, width - 24, 24, theme.panelAlt(), theme.border());
            UiRender.rect(context, x + 22, toggleY + 11, width - 84, 3, theme.border());
            UiRender.gradientHorizontal(context, x + 22, toggleY + 11, Math.max(12, (width - 84) * 2 / 3), 3, theme.accentSoft(), theme.accent());
            UiRender.borderedRect(context, x + width - 54, toggleY + 5, 32, 14, theme.accentSoft(), theme.accent());
            context.fill(x + width - 32, toggleY + 8, x + width - 22, toggleY + 18, theme.text());

            int swatchSize = 12;
            int swatchY = y + height - 20;
            int swatchX = x + width - 12 - swatchSize;
            int[] colors = {theme.accent(), theme.accentSoft(), theme.border(), theme.text(), theme.mutedText()};
            for (int i = colors.length - 1; i >= 0; i--) {
                UiRender.borderedRect(context, swatchX, swatchY, swatchSize, swatchSize, colors[i], theme.border());
                swatchX -= swatchSize + 4;
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
