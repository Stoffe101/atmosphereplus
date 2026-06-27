package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.UiRender;
import com.skrra.atmosphereplus.util.MathUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderWidget extends AtmosphereWidget {
    private final String label;
    private final float min;
    private final float max;
    private final Supplier<Float> getter;
    private final Consumer<Float> setter;
    private boolean dragging = false;

    public SliderWidget(int x, int y, int width, String label, float min, float max, Supplier<Float> getter, Consumer<Float> setter) {
        super(x, y, width, 46);
        this.label = label;
        this.min = min;
        this.max = max;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean hover = isHovered(mouseX, mouseY);
        float value = getter.get();
        float percent = (value - min) / (max - min);
        percent = MathUtil.clamp(percent, 0f, 1f);

        UiRender.card(context, x, y, width, height, hover ? theme.panelAlt() : theme.panel(), theme.border());

        UiRender.text(context, textRenderer, label, x + 12, y + 9, theme.text());
        UiRender.text(context, textRenderer, Math.round(value * 100f) + "%", x + width - 46, y + 9, theme.mutedText());

        int barX = x + 12;
        int barY = y + 31;
        int barW = width - 24;

        context.fill(barX, barY, barX + barW, barY + 4, theme.panelAlt());
        UiRender.gradientHorizontal(context, barX, barY, (int) (barW * percent), 4, theme.accentSoft(), theme.accent());

        int knobX = barX + (int) (barW * percent);
        context.fill(knobX - 4, barY - 4, knobX + 4, barY + 8, 0xFFFAFAFA);
        context.fill(knobX - 2, barY - 2, knobX + 2, barY + 6, theme.accent());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            dragging = true;
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            return true;
        }
        return false;
    }

    private void updateValue(double mouseX) {
        float percent = (float) ((mouseX - (x + 12)) / (width - 24));
        percent = MathUtil.clamp(percent, 0f, 1f);
        setter.accept(min + (max - min) * percent);
    }
}
