package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.IconRenderer;
import com.skrra.atmosphereplus.ui.IconType;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class ActionButtonWidget extends AtmosphereWidget {
    private final String label;
    private final String description;
    private final IconType icon;
    private final Runnable action;

    public ActionButtonWidget(int x, int y, int width, String label, String description, IconType icon, Runnable action) {
        super(x, y, width, 34);
        this.label = label;
        this.description = description;
        this.icon = icon;
        this.tooltip = description;
        this.action = action;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean hover = isHovered(mouseX, mouseY);

        int fill = hover ? theme.panelAlt() : theme.panel();
        int border = hover ? theme.accentSoft() : theme.border();
        UiRender.card(context, x, y, width, height, fill, border);

        int tileX = x + 9;
        int tileY = y + 7;
        int tileSize = 20;
        UiRender.borderedRect(context, tileX, tileY, tileSize, tileSize, theme.panelAlt(), hover ? theme.accent() : theme.border());
        IconRenderer.drawCentered(context, icon, tileX + tileSize / 2, tileY + tileSize / 2, 15);

        int textX = x + 38;
        int textW = width - 48;
        UiRender.text(context, textRenderer, trim(textRenderer, label, textW), textX, y + 6, theme.text());

        if (description != null && !description.isBlank()) {
            UiRender.text(context, textRenderer, trim(textRenderer, description, textW), textX, y + 19, hover ? theme.text() : theme.mutedText());
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            action.run();
            return true;
        }

        return false;
    }
}
