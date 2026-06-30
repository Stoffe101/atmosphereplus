package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Supplier;

public class TimePresetButtonWidget extends AtmosphereWidget {
    private final String label;
    private final String timeLabel;
    private final Supplier<Boolean> activeSupplier;
    private final Runnable action;

    public TimePresetButtonWidget(int x, int y, int width, String label, String timeLabel, Runnable action) {
        this(x, y, width, label, timeLabel, () -> false, action);
    }

    public TimePresetButtonWidget(int x, int y, int width, String label, String timeLabel, Supplier<Boolean> activeSupplier, Runnable action) {
        super(x, y, width, 36);
        this.label = label;
        this.timeLabel = timeLabel;
        this.tooltip = "Set visual time to " + label + ".";
        this.activeSupplier = activeSupplier;
        this.action = action;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean hover = isHovered(mouseX, mouseY);
        boolean active = activeSupplier.get();

        UiRender.v2Card(context, x, y, width, height, hover, active);

        UiRender.text(context, textRenderer, label, x + 10, y + 8, active ? theme.text() : theme.text());
        UiRender.text(context, textRenderer, timeLabel, x + 10, y + 22, active ? theme.accent() : theme.mutedText());

        int dotX = x + width - 17;
        int dotY = y + 13;

        if (active) {
            context.fill(dotX - 2, dotY - 2, dotX + 8, dotY + 8, UiRender.V2_ACCENT_SOFT());
            context.fill(dotX, dotY, dotX + 6, dotY + 6, UiRender.V2_ACCENT());
        } else {
            context.fill(dotX, dotY, dotX + 6, dotY + 6, hover ? UiRender.V2_ACCENT() : UiRender.V2_BORDER_SOFT());
        }
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
