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
        super(x, y, width, 72);
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

        int fill = active ? theme.accentSoft() : hover ? theme.panelAlt() : theme.panel();
        int border = active ? theme.accent() : hover ? theme.accent() : theme.border();

        UiRender.card(context, x, y, width, height, fill, border);

        if (active) {
            context.fill(x, y, x + 3, y + height, theme.accent());
            UiRender.borderedRect(context, x + width - 54, y + 10, 42, 15, theme.accentSoft(), theme.accent());
            UiRender.centeredText(context, textRenderer, "ACTIVE", x + width - 33, y + 14, theme.text());
        }

        UiRender.borderedRect(context, x + 12, y + 13, 26, 26, active ? theme.accentSoft() : theme.panelAlt(), border);
        IconRenderer.drawCentered(context, icon, x + 25, y + 26, 20);

        UiRender.text(context, textRenderer, title, x + 48, y + 14, theme.text());
        UiRender.text(context, textRenderer, trim(textRenderer, description, width - 72), x + 48, y + 30, active ? theme.accent() : theme.mutedText());

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
