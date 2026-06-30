package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.IconRenderer;
import com.skrra.atmosphereplus.ui.IconType;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Supplier;

public class PresetRowWidget extends AtmosphereWidget {
    private final String title;
    private final String description;
    private final IconType icon;
    private final Supplier<Boolean> activeSupplier;
    private final Supplier<Boolean> favoriteSupplier;
    private final Runnable applyAction;
    private final Runnable favoriteAction;

    public PresetRowWidget(
            int x,
            int y,
            int width,
            String title,
            String description,
            IconType icon,
            Supplier<Boolean> activeSupplier,
            Supplier<Boolean> favoriteSupplier,
            Runnable applyAction,
            Runnable favoriteAction
    ) {
        super(x, y, width, 38);
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.activeSupplier = activeSupplier;
        this.favoriteSupplier = favoriteSupplier;
        this.applyAction = applyAction;
        this.favoriteAction = favoriteAction;
        this.tooltip = description;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean active = activeSupplier.get();
        boolean favorite = favoriteSupplier.get();
        boolean hover = isHovered(mouseX, mouseY);
        boolean starHover = UiRender.hovered(mouseX, mouseY, starX(), y + 9, 20, 20);

        UiRender.v2Card(context, x, y, width, height, hover, active);

        int starFill = starHover ? UiRender.V2_CARD_HOVER() : UiRender.V2_PANEL_ALT();
        UiRender.borderedRect(context, starX(), y + 9, 20, 20, starFill, favorite ? UiRender.V2_ACCENT_PURPLE() : UiRender.V2_BORDER());
        UiRender.centeredText(context, textRenderer, favorite ? "\u2605" : "\u2606", starX() + 10, y + 15, favorite ? theme.accent() : theme.mutedText());

        int tileSize = 18;
        int tileX = x + 36;
        int tileY = y + 10;
        UiRender.v2IconBox(context, tileX, tileY, tileSize, active || hover);
        IconRenderer.drawCentered(context, icon, tileX + tileSize / 2, tileY + tileSize / 2, 13);

        int textX = tileX + tileSize + 8;
        int textW = Math.max(16, width - (textX - x) - 10);
        UiRender.text(context, textRenderer, trim(textRenderer, title, textW), textX, y + 6, theme.text());
        UiRender.text(context, textRenderer, trim(textRenderer, description, textW), textX, y + 20, active ? theme.accent() : theme.mutedText());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isHovered(mouseX, mouseY)) {
            return false;
        }

        if (UiRender.hovered(mouseX, mouseY, starX(), y + 9, 20, 20)) {
            favoriteAction.run();
            return true;
        }

        applyAction.run();
        return true;
    }

    private int starX() {
        return x + 8;
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
