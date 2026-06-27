package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ToggleWidget extends AtmosphereWidget {
    private final String label;
    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;

    public ToggleWidget(int x, int y, int width, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        super(x, y, width, 38);
        this.label = label;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean enabled = getter.get();
        boolean hover = isHovered(mouseX, mouseY);

        int cardFill = hover ? theme.panelAlt() : theme.panel();
        UiRender.card(context, x, y, width, height, cardFill, theme.border());

        UiRender.text(context, textRenderer, label, x + 12, y + 10, theme.text());
        UiRender.text(context, textRenderer, enabled ? "Enabled" : "Disabled", x + 12, y + 23, enabled ? theme.accent() : theme.mutedText());

        int switchW = 44;
        int switchH = 16;
        int sx = x + width - switchW - 12;
        int sy = y + 11;

        int fill = enabled ? theme.accent() : theme.panelAlt();
        UiRender.borderedRect(context, sx, sy, switchW, switchH, fill, theme.border());

        int knobX = enabled ? sx + switchW - 14 : sx + 3;
        context.fill(knobX, sy + 3, knobX + 10, sy + 13, 0xFFFFFFFF);
        context.fill(knobX + 1, sy + 4, knobX + 9, sy + 12, enabled ? theme.accentSoft() : 0x22000000);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            setter.accept(!getter.get());
            return true;
        }
        return false;
    }
}
