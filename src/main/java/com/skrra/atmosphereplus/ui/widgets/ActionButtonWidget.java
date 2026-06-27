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
        super(x, y, width, 38);
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

        UiRender.card(context, x, y, width, height, hover ? theme.accentSoft() : theme.panel(), hover ? theme.accent() : theme.border());

        UiRender.borderedRect(context, x + 9, y + 8, 22, 22, hover ? theme.accentSoft() : theme.panelAlt(), hover ? theme.accent() : theme.border());
        IconRenderer.drawCentered(context, icon, x + 20, y + 19, 16);

        UiRender.text(context, textRenderer, label, x + 40, y + 8, theme.text());
        UiRender.text(context, textRenderer, trim(textRenderer, description, width - 52), x + 40, y + 22, hover ? theme.accent() : theme.mutedText());
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
