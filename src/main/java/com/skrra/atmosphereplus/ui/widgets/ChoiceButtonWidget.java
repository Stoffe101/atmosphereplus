package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.IconRenderer;
import com.skrra.atmosphereplus.ui.IconType;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Supplier;

public class ChoiceButtonWidget extends AtmosphereWidget {
    private final String label;
    private final String description;
    private final IconType icon;
    private final Supplier<Boolean> activeSupplier;
    private final Runnable action;

    public ChoiceButtonWidget(int x, int y, int width, String label, String description, IconType icon, Supplier<Boolean> activeSupplier, Runnable action) {
        super(x, y, width, 38);
        this.label = label;
        this.description = description;
        this.icon = icon;
        this.tooltip = description;
        this.activeSupplier = activeSupplier;
        this.action = action;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean active = activeSupplier.get();
        boolean hover = isHovered(mouseX, mouseY);

        int fill = active ? theme.accentSoft() : hover ? theme.panelAlt() : theme.panel();
        int border = active ? theme.accent() : hover ? theme.accentSoft() : theme.border();

        UiRender.card(context, x, y, width, height, fill, border);

        if (active) {
            context.fill(x, y + 4, x + 2, y + height - 4, theme.accent());
        }

        int tileX = x + 9;
        int tileY = y + 8;
        int tileSize = 20;

        UiRender.borderedRect(context, tileX, tileY, tileSize, tileSize, active ? theme.accentSoft() : theme.panelAlt(), active ? theme.accent() : theme.border());
        IconRenderer.drawCentered(context, icon, tileX + tileSize / 2, tileY + tileSize / 2, 15);

        int textX = x + 38;
        int textW = width - 48;
        UiRender.text(context, textRenderer, trim(textRenderer, label, textW), textX, y + 6, theme.text());

        if (description != null && !description.isBlank()) {
            UiRender.text(context, textRenderer, trim(textRenderer, description, textW), textX, y + 20, active ? theme.accent() : theme.mutedText());
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
