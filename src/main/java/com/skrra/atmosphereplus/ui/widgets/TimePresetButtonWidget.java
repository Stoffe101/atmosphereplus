package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class TimePresetButtonWidget extends AtmosphereWidget {
    private final String label;
    private final String timeLabel;
    private final Runnable action;

    public TimePresetButtonWidget(int x, int y, int width, String label, String timeLabel, Runnable action) {
        super(x, y, width, 34);
        this.label = label;
        this.timeLabel = timeLabel;
        this.tooltip = "Set visual time to " + label + ".";
        this.action = action;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean hover = isHovered(mouseX, mouseY);

        UiRender.card(context, x, y, width, height, hover ? theme.panelAlt() : theme.panel(), hover ? theme.accent() : theme.border());
        UiRender.text(context, textRenderer, label, x + 10, y + 8, theme.text());
        UiRender.text(context, textRenderer, timeLabel, x + 10, y + 21, theme.mutedText());

        int dotX = x + width - 16;
        int dotY = y + 13;
        context.fill(dotX, dotY, dotX + 6, dotY + 6, hover ? theme.accent() : theme.accentSoft());
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
