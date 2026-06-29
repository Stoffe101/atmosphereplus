package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.widgets.AtmosphereWidget;
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

    public static int addWidgets(List<AtmosphereWidget> widgets, int contentX, int contentY, int contentW) {
        if (contentW >= 680) {
            return addWideWidgets(widgets, contentX, contentY, contentW);
        }

        return addStackedWidgets(widgets, contentX, contentY, contentW);
    }

    private static int addWideWidgets(List<AtmosphereWidget> widgets, int contentX, int contentY, int contentW) {
        int gap = 12;
        int leftW = Math.max(260, (contentW - gap) * 58 / 100);
        int rightW = Math.max(220, contentW - gap - leftW);

        if (leftW + gap + rightW > contentW) {
            rightW = Math.max(200, contentW - gap - leftW);
        }

        int leftY = contentY;
        int rightY = contentY;

        leftY = addCurrentThemeSection(widgets, contentX, leftY, leftW);
        leftY = addSimpleModeSection(widgets, contentX, leftY, leftW);
        leftY = addAdvancedModeSection(widgets, contentX, leftY, leftW);

        int rightX = contentX + leftW + gap;
        rightY = addLivePreviewSection(widgets, rightX, rightY, rightW);
        rightY = addThemeActionsSection(widgets, rightX, rightY, rightW);

        return Math.max(leftY, rightY);
    }

    private static int addStackedWidgets(List<AtmosphereWidget> widgets, int contentX, int contentY, int contentW) {
        int y = contentY;
        y = addCurrentThemeSection(widgets, contentX, y, contentW);
        y = addSimpleModeSection(widgets, contentX, y, contentW);
        y = addAdvancedModeSection(widgets, contentX, y, contentW);
        y = addLivePreviewSection(widgets, contentX, y, contentW);
        y = addThemeActionsSection(widgets, contentX, y, contentW);
        return y;
    }

    private static int addCurrentThemeSection(List<AtmosphereWidget> widgets, int x, int y, int w) {
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
        return y + 58 + SECTION_GAP;
    }

    private static int addSimpleModeSection(List<AtmosphereWidget> widgets, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Simple Mode", "Guided theme creation"));
        y += 28;
        widgets.add(new InfoCardWidget(
                x,
                y,
                w,
                72,
                "Guided controls placeholder",
                "Phase 1 reserves space for simple color and style presets without exposing editing controls yet.",
                IconType.PRESETS
        ));
        return y + 72 + SECTION_GAP;
    }

    private static int addAdvancedModeSection(List<AtmosphereWidget> widgets, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Advanced Mode", "Detailed theme controls"));
        y += 28;
        widgets.add(new InfoCardWidget(
                x,
                y,
                w,
                72,
                "Advanced editor placeholder",
                "Future phases can add token-level colors, import tools, and validation here while keeping browsing in Themes.",
                IconType.ADVANCED
        ));
        return y + 72 + SECTION_GAP;
    }

    private static int addLivePreviewSection(List<AtmosphereWidget> widgets, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Live Preview", "Theme sample area"));
        y += 28;
        widgets.add(new ThemePreviewWidget(x, y, w, 142));
        return y + 142 + SECTION_GAP;
    }

    private static int addThemeActionsSection(List<AtmosphereWidget> widgets, int x, int y, int w) {
        widgets.add(new SectionLabelWidget(x, y, w, "Theme Actions", "Create and manage themes later"));
        y += 28;
        widgets.add(new InfoCardWidget(
                x,
                y,
                w,
                88,
                "Actions placeholder",
                "Create, duplicate, import, export, and delete actions are intentionally disabled until editing support exists.",
                IconType.THEMES
        ));
        return y + 88 + SECTION_GAP;
    }

    private static final class ThemePreviewWidget extends AtmosphereWidget {
        private ThemePreviewWidget(int x, int y, int width, int height) {
            super(x, y, width, height);
            this.tooltip = "Preview of the currently selected theme.";
        }

        @Override
        public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
            Theme theme = ThemeManager.current();

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
