package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class SectionLabelWidget extends AtmosphereWidget {
    private final String label;
    private final String description;

    public SectionLabelWidget(int x, int y, int width, String label, String description) {
        super(x, y, width, 22);
        this.label = label;
        this.description = description;
        this.tooltip = description;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();

        int labelMaxWidth = Math.min(width / 2, Math.max(80, width - 80));
        String safeLabel = trim(textRenderer, label, labelMaxWidth);
        UiRender.text(context, textRenderer, safeLabel, x, y + 2, theme.text());

        int descX = x + textRenderer.getWidth(safeLabel) + 20;
        int descW = Math.max(0, x + width - descX);

        if (description != null && !description.isBlank() && descW > 24) {
            UiRender.text(context, textRenderer, trim(textRenderer, description, descW), descX, y + 2, theme.mutedText());
        }

        int accentW = Math.min(width, Math.max(70, textRenderer.getWidth(safeLabel) + 34));
        UiRender.v2Rule(context, x, y + 18, width, accentW);
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
