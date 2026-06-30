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
    private final String description;
    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;

    public ToggleWidget(int x, int y, int width, String label, String description, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        super(x, y, width, 46);
        this.label = label;
        this.description = description;
        this.tooltip = description;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean hover = isHovered(mouseX, mouseY);
        boolean enabled = getter.get();

        UiRender.v2Card(context, x, y, width, height, hover, enabled);

        int switchW = 38;
        int switchH = 16;
        int switchX = x + width - switchW - 12;
        int switchY = y + 15;

        int textW = Math.max(40, switchX - x - 24);

        UiRender.text(context, textRenderer, trim(textRenderer, label, textW), x + 12, y + 7, theme.text());
        if (description != null && !description.isBlank()) {
            UiRender.text(context, textRenderer, trim(textRenderer, description, textW), x + 12, y + 21, theme.mutedText());
        }

        int fill = enabled ? UiRender.V2_ACCENT_SOFT() : UiRender.V2_PANEL_ALT();
        int border = enabled ? UiRender.V2_ACCENT() : UiRender.V2_BORDER();

        UiRender.borderedRect(context, switchX, switchY, switchW, switchH, fill, border);

        int knobSize = 10;
        int knobX = enabled ? switchX + switchW - knobSize - 3 : switchX + 3;
        int knobY = switchY + 3;

        context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFAFAFA);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            setter.accept(!getter.get());
            return true;
        }

        return false;
    }
}
