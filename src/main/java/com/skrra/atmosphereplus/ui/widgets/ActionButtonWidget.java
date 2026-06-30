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
        super(x, y, width, UiRender.V2_BUTTON_HEIGHT);
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

        UiRender.v2Card(context, x, y, width, height, hover, false);

        int tileX = x + 9;
        int tileY = y + 6;
        int tileSize = UiRender.V2_ICON_BOX;
        UiRender.v2IconBox(context, tileX, tileY, tileSize, hover);
        IconRenderer.drawCentered(context, icon, tileX + tileSize / 2, tileY + tileSize / 2, 15);

        int textX = x + 40;
        int textW = width - 50;
        UiRender.text(context, textRenderer, trim(textRenderer, label, textW), textX, y + 6, theme.text());

        if (description != null && !description.isBlank()) {
            UiRender.text(context, textRenderer, trim(textRenderer, description, textW), textX, y + 19, hover ? UiRender.V2_TEXT() : theme.mutedText());
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
