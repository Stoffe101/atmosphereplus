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
        super(x, y, width, 42);
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
        int border = active ? theme.accent() : hover ? theme.accent() : theme.border();

        UiRender.card(context, x, y, width, height, fill, border);

        if (active) {
            context.fill(x, y, x + 3, y + height, theme.accent());
        }

        UiRender.borderedRect(context, x + 9, y + 10, 22, 22, active ? theme.accentSoft() : theme.panelAlt(), border);
        IconRenderer.drawCentered(context, icon, x + 20, y + 21, 17);

        UiRender.text(context, textRenderer, label, x + 40, y + 9, theme.text());
        UiRender.text(context, textRenderer, trim(textRenderer, description, width - 54), x + 40, y + 24, active ? theme.accent() : theme.mutedText());
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
