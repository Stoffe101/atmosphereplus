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
    }

    public static int addWidgets(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int contentX, int contentY, int contentW) {
        if (contentW >= 680) {
            return addWideWidgets(widgets, state, actions, contentX, contentY, contentW);
        }

        return addStackedWidgets(widgets, state, actions, contentX, contentY, contentW);
    }

    private static int addWideWidgets(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int contentX, int contentY, int contentW) {
        int gap = 12;
        int leftW = Math.max(260, (contentW - gap) * 58 / 100);
        int rightW = Math.max(220, contentW - gap - leftW);

        if (leftW + gap + rightW > contentW) {
            rightW = Math.max(200, contentW - gap - leftW);
        }

        int leftY = contentY;
        int rightY = contentY;

        leftY = addCurrentThemeSection(widgets, state, actions, contentX, leftY, leftW);
        leftY = addSimpleModeSection(widgets, state, actions, contentX, leftY, leftW);
        leftY = addAdvancedModeSection(widgets, state, contentX, leftY, leftW);

        int rightX = contentX + leftW + gap;
        rightY = addLivePreviewSection(widgets, state, rightX, rightY, rightW);
        rightY = addThemeActionsSection(widgets, state, actions, rightX, rightY, rightW);

        return Math.max(leftY, rightY);
    }

    private static int addStackedWidgets(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int contentX, int contentY, int contentW) {
        int y = contentY;
        y = addCurrentThemeSection(widgets, state, actions, contentX, y, contentW);
        y = addSimpleModeSection(widgets, state, actions, contentX, y, contentW);
        y = addAdvancedModeSection(widgets, state, contentX, y, contentW);
        y = addLivePreviewSection(widgets, state, contentX, y, contentW);
        y = addThemeActionsSection(widgets, state, actions, contentX, y, contentW);
        return y;
    }

    private static int addCurrentThemeSection(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        Theme theme = ThemeManager.current();
        widgets.add(new SectionLabelWidget(x, y, w, "Current Theme", "Selected interface theme"));
        y += 28;
        widgets.add(new InfoCardWidget(
                x,
                y,
                w,
                58,
                theme.displayName(),
                "Active theme id: " + ConfigManager.get().theme + ". Editing tools will arrive in a later Theme Studio phase.",
                IconType.THEMES
        ));

        y += 66;
        Theme selected = ThemeManager.byId(state.selectedThemeId());
        String label = selected == null ? "Select current theme" : "Apply " + selected.displayName();
        widgets.add(new ActionButtonWidget(
                x,
                y,
                w,
                label,
                "Apply the selected built-in or custom theme to Atmosphere+.",
                IconType.THEMES,
                () -> actions.applyTheme(state.selectedThemeId())
        ));

        return y + 44 + SECTION_GAP;
    }

    private static int addSimpleModeSection(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Simple Mode", "Custom theme library"));
        y += 28;

        if (!CustomThemeManager.hasCustomThemes()) {
            widgets.add(new InfoCardWidget(
                    x,
                    y,
                    w,
                    76,
                    "No custom themes yet",
                    "Create a theme or duplicate the current one to start building your custom library.",
                    IconType.THEMES
            ));
            return y + 76 + SECTION_GAP;
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

    private static int addAdvancedModeSection(List<AtmosphereWidget> widgets, ThemeStudioState state, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Advanced Mode", "Detailed theme metadata"));
        y += 28;
        Theme selected = ThemeManager.byId(state.selectedThemeId());
        boolean custom = CustomThemeManager.isCustomTheme(state.selectedThemeId());
        String title = selected == null ? "No theme selected" : selected.displayName();
        String description = custom
                ? "This custom theme is editable. Color controls are still reserved for the next phase."
                : "Built-in themes are read-only. Duplicate one to create an editable custom copy.";

        widgets.add(new InfoCardWidget(
                x,
                y,
                w,
                72,
                title,
                description,
                IconType.ADVANCED
        ));
        return y + 72 + SECTION_GAP;
    }

    private static int addLivePreviewSection(List<AtmosphereWidget> widgets, ThemeStudioState state, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Live Preview", "Theme sample area"));
        y += 28;
        widgets.add(new ThemePreviewWidget(x, y, w, 142, state));
        return y + 142 + SECTION_GAP;
    }

    private static int addThemeActionsSection(List<AtmosphereWidget> widgets, ThemeStudioState state, Actions actions, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Theme Actions", "Create and manage themes"));
        y += 28;

        int columns = w >= 430 ? 2 : 1;
        int buttonW = (w - CARD_GAP * (columns - 1)) / columns;
        int index = 0;

        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Create Theme", "Create a new custom theme from the active theme.", IconType.THEMES, actions::createTheme));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Duplicate", "Duplicate the selected built-in or custom theme.", IconType.PRESETS, () -> actions.duplicateTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Rename", "Rename the selected custom theme.", IconType.PRESETS, () -> actions.renameTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Save Theme", "Persist the selected custom theme to disk.", IconType.THEMES, () -> actions.saveTheme(state.selectedThemeId())));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Reload", "Reload custom themes from config/atmosphereplus-themes.json.", IconType.ADVANCED, actions::reloadThemes));
        index++;
        widgets.add(new ActionButtonWidget(actionX(x, buttonW, columns, index), actionY(y, index, columns), buttonW, "Delete", "Delete the selected custom theme after confirmation.", IconType.ADVANCED, () -> actions.deleteTheme(state.selectedThemeId())));
        index++;

        return y + ((index + columns - 1) / columns) * 42 + SECTION_GAP;
    }

    private static int actionX(int x, int buttonW, int columns, int index) {
        return x + (index % columns) * (buttonW + CARD_GAP);
    }

    private static int actionY(int y, int index, int columns) {
        return y + (index / columns) * 42;
    }

    private static final class ThemePreviewWidget extends AtmosphereWidget {
        private final ThemeStudioState state;

        private ThemePreviewWidget(int x, int y, int width, int height, ThemeStudioState state) {
            super(x, y, width, height);
            this.state = state;
            this.tooltip = "Preview of the currently selected theme.";
        }

        @Override
        public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
            Theme theme = ThemeManager.byId(state.selectedThemeId());
            if (theme == null) {
                theme = ThemeManager.current();
            }

            UiRender.card(context, x, y, width, height, theme.panel(), theme.border());
            UiRender.gradientHorizontal(context, x + 8, y + 8, width - 16, 24, theme.panelAlt(), theme.accentSoft());
            UiRender.text(context, textRenderer, trim(textRenderer, "Preview: " + theme.displayName(), width - 28), x + 14, y + 15, theme.text());

            int sampleY = y + 44;
            UiRender.borderedRect(context, x + 12, sampleY, width - 24, 38, theme.panelAlt(), theme.border());
            UiRender.text(context, textRenderer, "Sample panel", x + 22, sampleY + 8, theme.text());
            UiRender.text(context, textRenderer, trim(textRenderer, "Muted text and borders follow the active theme.", width - 48), x + 22, sampleY + 22, theme.mutedText());

            int buttonY = sampleY + 50;
            int buttonW = Math.min(116, Math.max(76, (width - 34) / 2));
            UiRender.borderedRect(context, x + 12, buttonY, buttonW, 24, theme.accentSoft(), theme.accent());
            UiRender.centeredText(context, textRenderer, "Primary", x + 12 + buttonW / 2, buttonY + 8, theme.text());

            int secondaryX = x + 22 + buttonW;
            UiRender.borderedRect(context, secondaryX, buttonY, buttonW, 24, theme.panelAlt(), theme.border());
            UiRender.centeredText(context, textRenderer, "Secondary", secondaryX + buttonW / 2, buttonY + 8, theme.mutedText());

            int swatchSize = 12;
            int swatchY = y + height - 20;
            int swatchX = x + width - 12 - swatchSize;
            int[] colors = {theme.accent(), theme.accentSoft(), theme.border(), theme.text()};
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
