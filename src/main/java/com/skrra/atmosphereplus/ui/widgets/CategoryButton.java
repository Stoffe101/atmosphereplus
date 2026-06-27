package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.IconRenderer;
import com.skrra.atmosphereplus.ui.UiCategory;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CategoryButton extends AtmosphereWidget {
    private final UiCategory category;
    private final Supplier<UiCategory> current;
    private final Consumer<UiCategory> onClick;

    public CategoryButton(int x, int y, int width, UiCategory category, Supplier<UiCategory> current, Consumer<UiCategory> onClick) {
        super(x, y, width, 24);
        this.category = category;
        this.current = current;
        this.onClick = onClick;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean active = current.get() == category;
        boolean hover = isHovered(mouseX, mouseY);

        int buttonX = x + 4;
        int buttonW = width - 8;
        int fill = active ? theme.accentSoft() : hover ? theme.panelAlt() : 0x00000000;
        int textColor = active || hover ? theme.text() : theme.mutedText();

        if (fill != 0) {
            context.fill(buttonX, y, buttonX + buttonW, y + height, fill);
            if (active) {
                context.fill(buttonX, y, buttonX + 2, y + height, theme.accent());
            }
        }

        int tileX = buttonX + 8;
        int tileY = y + 3;
        int tileSize = 18;
        int tileFill = active ? theme.accentSoft() : hover ? theme.panel() : theme.panelAlt();

        UiRender.borderedRect(context, tileX, tileY, tileSize, tileSize, tileFill, active ? theme.accent() : theme.border());
        IconRenderer.drawCentered(context, category.icon, tileX + tileSize / 2, tileY + tileSize / 2, 16);

        UiRender.text(context, textRenderer, category.title, buttonX + 34, y + 8, textColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            onClick.accept(category);
            return true;
        }
        return false;
    }
}
