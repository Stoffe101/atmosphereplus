package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.IconRenderer;
import com.skrra.atmosphereplus.ui.IconType;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Supplier;

public class PresetCardWidget extends AtmosphereWidget {
    private final String title;
    private final String description;
    private final IconType icon;
    private final Supplier<Boolean> activeSupplier;
    private final Runnable action;

    public PresetCardWidget(int x, int y, int width, String title, String description, IconType icon, Runnable action) {
        this(x, y, width, title, description, icon, () -> false, action);
    }

    public PresetCardWidget(int x, int y, int width, String title, String description, IconType icon, Supplier<Boolean> activeSupplier, Runnable action) {
        super(x, y, width, 48);
        this.title = title;
        this.description = description;
        this.tooltip = description;
        this.icon = icon;
        this.activeSupplier = activeSupplier;
        this.action = action;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean hover = isHovered(mouseX, mouseY);
        boolean active = activeSupplier.get();

        UiRender.v2Card(context, x, y, width, height, hover, active);

        int tileSize = 20;
        int tileX = x + 9;
        int tileY = y + 14;

        UiRender.v2IconBox(context, tileX, tileY, tileSize, active || hover);
        IconRenderer.drawCentered(context, icon, tileX + tileSize / 2, tileY + tileSize / 2, 15);

        int textX = x + 38;
        int textW = width - 48;

        if (active && textW > 72) {
            int chipW = 36;
            UiRender.borderedRect(context, x + width - chipW - 8, y + 7, chipW, 13, UiRender.V2_ACCENT_SOFT(), UiRender.V2_ACCENT());
            UiRender.centeredText(context, textRenderer, "ON", x + width - chipW / 2 - 8, y + 10, theme.text());
            textW -= chipW + 8;
        }

        UiRender.text(context, textRenderer, trim(textRenderer, title, textW), textX, y + 8, theme.text());

        if (description != null && !description.isBlank()) {
            UiRender.text(context, textRenderer, trim(textRenderer, description, width - 48), textX, y + 24, active ? theme.accent() : theme.mutedText());
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
