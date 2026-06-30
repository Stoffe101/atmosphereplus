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
        super(x, y, width, 20);
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
        int buttonY = y + 1;
        int buttonW = width - 8;
        int buttonH = height - 2;
        int textColor = active || hover ? theme.text() : theme.mutedText();

        if (active || hover) {
            UiRender.v2Card(context, buttonX, buttonY, buttonW, buttonH, hover, active);
        }

        int tileX = buttonX + 8;
        int tileY = y + 2;
        int tileSize = 16;

        UiRender.v2IconBox(context, tileX, tileY, tileSize, active || hover);
        IconRenderer.drawCentered(context, category.icon, tileX + tileSize / 2, tileY + tileSize / 2, 14);

        if (buttonW >= 92) {
            UiRender.text(context, textRenderer, trim(textRenderer, category.title, buttonW - 40), buttonX + 32, y + 6, textColor);
        }
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
            onClick.accept(category);
            return true;
        }
        return false;
    }
}
