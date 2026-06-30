package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.IconRenderer;
import com.skrra.atmosphereplus.ui.IconType;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class InfoCardWidget extends AtmosphereWidget {
    private final String title;
    private final String description;
    private final IconType icon;

    public InfoCardWidget(int x, int y, int width, int height, String title, String description, IconType icon) {
        super(x, y, width, height);
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.tooltip = description;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();

        UiRender.v2Card(context, x, y, width, height, false, false);

        int tileSize = UiRender.V2_ICON_BOX;
        int tileX = x + 9;
        int tileY = y + 8;
        UiRender.v2IconBox(context, tileX, tileY, tileSize, false);
        IconRenderer.drawCentered(context, icon, tileX + tileSize / 2, tileY + tileSize / 2, 15);

        int textX = x + 40;
        int textW = Math.max(0, width - 50);
        UiRender.text(context, textRenderer, trim(textRenderer, title, textW), textX, y + 7, theme.text());

        if (description == null || description.isBlank() || textW <= 0) {
            return;
        }

        int lineY = y + 21;
        int bottom = y + height - 6;
        String remaining = description;

        while (!remaining.isBlank() && lineY + 8 <= bottom) {
            String line = nextLine(textRenderer, remaining, textW);
            UiRender.text(context, textRenderer, line, textX, lineY, theme.mutedText());
            remaining = remaining.substring(Math.min(remaining.length(), line.length())).trim();
            lineY += 11;
        }
    }

    private String nextLine(TextRenderer renderer, String text, int maxWidth) {
        if (renderer.getWidth(text) <= maxWidth) {
            return text;
        }

        int end = text.length();
        while (end > 3 && renderer.getWidth(text.substring(0, end) + "...") > maxWidth) {
            end--;
        }

        int breakAt = text.lastIndexOf(' ', end);
        if (breakAt > 12) {
            return text.substring(0, breakAt);
        }

        return text.substring(0, Math.max(1, end)) + "...";
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
