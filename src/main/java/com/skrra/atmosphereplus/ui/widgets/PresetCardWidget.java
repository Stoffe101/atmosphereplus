package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.IconRenderer;
import com.skrra.atmosphereplus.ui.IconType;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class PresetCardWidget extends AtmosphereWidget {
    private final String title;
    private final String description;
    private final IconType icon;
    private final Runnable action;

    public PresetCardWidget(int x, int y, int width, String title, String description, IconType icon, Runnable action) {
        super(x, y, width, 72);
        this.title = title;
        this.description = description;
        this.tooltip = description;
        this.icon = icon;
        this.action = action;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean hover = isHovered(mouseX, mouseY);

        UiRender.card(context, x, y, width, height, hover ? theme.panelAlt() : theme.panel(), hover ? theme.accent() : theme.border());

        UiRender.borderedRect(context, x + 12, y + 13, 26, 26, theme.accentSoft(), theme.accent());
        IconRenderer.drawCentered(context, icon, x + 25, y + 26, 20);

        UiRender.text(context, textRenderer, title, x + 48, y + 14, theme.text());
        UiRender.text(context, textRenderer, trim(textRenderer, description, width - 62), x + 48, y + 30, theme.mutedText());

        UiRender.rect(context, x + 12, y + 56, width - 24, 3, theme.accentSoft());
        UiRender.rect(context, x + 12, y + 56, (width - 24) / 2, 3, theme.accent());
    }

    private String trim(TextRenderer renderer, String text, int maxWidth) {
        if (renderer.getWidth(text) <= maxWidth) {
            return text;
        }

        String result = text;
        while (result.length() > 3 && renderer.getWidth(result + "...") > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }

        return result + "...";
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
