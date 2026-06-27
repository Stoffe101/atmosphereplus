package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.UiRender;
import com.skrra.atmosphereplus.util.MathUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SliderWidget extends AtmosphereWidget {
    private final String label;
    private final String description;
    private final float min;
    private final float max;
    private final Supplier<Float> getter;
    private final Consumer<Float> setter;
    private final Function<Float, String> formatter;
    private boolean dragging = false;

    public SliderWidget(int x, int y, int width, String label, float min, float max, Supplier<Float> getter, Consumer<Float> setter) {
        this(x, y, width, label, null, min, max, getter, setter, null);
    }

    public SliderWidget(int x, int y, int width, String label, String description, float min, float max, Supplier<Float> getter, Consumer<Float> setter, Function<Float, String> formatter) {
        super(x, y, width, 52);
        this.label = label;
        this.description = description;
        this.tooltip = description;
        this.min = min;
        this.max = max;
        this.getter = getter;
        this.setter = setter;
        this.formatter = formatter;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean hover = isHovered(mouseX, mouseY);
        float value = getter.get();
        float percent = (value - min) / (max - min);
        percent = MathUtil.clamp(percent, 0f, 1f);

        UiRender.card(context, x, y, width, height, hover ? theme.panelAlt() : theme.panel(), theme.border());

        String valueText = format(value);
        int valueWidth = textRenderer.getWidth(valueText);
        int valueX = x + width - 12 - valueWidth;
        int labelMaxWidth = Math.max(40, valueX - (x + 12) - 10);

        UiRender.text(context, textRenderer, trim(textRenderer, label, labelMaxWidth), x + 12, y + 8, theme.text());
        UiRender.text(context, textRenderer, valueText, valueX, y + 8, theme.mutedText());

        if (description != null && !description.isEmpty()) {
            UiRender.text(context, textRenderer, trim(textRenderer, description, width - 24), x + 12, y + 21, theme.mutedText());
        }

        int barX = x + 12;
        int barY = y + 38;
        int barW = width - 24;

        context.fill(barX, barY, barX + barW, barY + 4, theme.panelAlt());
        UiRender.gradientHorizontal(context, barX, barY, (int) (barW * percent), 4, theme.accentSoft(), theme.accent());

        int knobX = barX + (int) (barW * percent);
        context.fill(knobX - 4, barY - 4, knobX + 4, barY + 8, 0xFFFAFAFA);
        context.fill(knobX - 2, barY - 2, knobX + 2, barY + 6, theme.accent());
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

    private String format(float value) {
        if (formatter != null) {
            return formatter.apply(value);
        }

        if (max > 10f) {
            return String.valueOf(Math.round(value));
        }

        return Math.round(value * 100f) + "%";
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
