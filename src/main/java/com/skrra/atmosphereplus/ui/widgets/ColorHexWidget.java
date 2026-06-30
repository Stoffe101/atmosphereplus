package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Supplier;

public class ColorHexWidget extends AtmosphereWidget {
    private final String label;
    private final Supplier<Integer> colorSupplier;
    private final Supplier<String> hexSupplier;
    private final Supplier<Boolean> focusedSupplier;
    private final Runnable focusAction;

    public ColorHexWidget(int x, int y, int width, String label, Supplier<Integer> colorSupplier, Supplier<String> hexSupplier, Supplier<Boolean> focusedSupplier, Runnable focusAction) {
        super(x, y, width, 34);
        this.label = label;
        this.colorSupplier = colorSupplier;
        this.hexSupplier = hexSupplier;
        this.focusedSupplier = focusedSupplier;
        this.focusAction = focusAction;
        this.tooltip = "Click the hex field, then type #RRGGBB or #AARRGGBB.";
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean focused = focusedSupplier.get();
        boolean hover = isHovered(mouseX, mouseY);
        int border = focused ? UiRender.V2_ACCENT() : hover ? UiRender.V2_BORDER_SOFT() : UiRender.V2_BORDER();

        UiRender.v2Card(context, x, y, width, height, hover, focused);

        int swatchSize = 18;
        int swatchX = x + 9;
        int swatchY = y + 8;
        UiRender.borderedRect(context, swatchX, swatchY, swatchSize, swatchSize, colorSupplier.get(), border);

        int hexW = Math.min(92, Math.max(70, width / 3));
        int hexX = x + width - hexW - 8;
        int labelW = Math.max(20, hexX - (x + 36) - 8);
        UiRender.text(context, textRenderer, trim(textRenderer, label, labelW), x + 36, y + 12, theme.text());

        UiRender.borderedRect(context, hexX, y + 7, hexW, 20, focused ? UiRender.V2_ACCENT_SOFT() : UiRender.V2_PANEL_ALT(), focused ? UiRender.V2_ACCENT() : UiRender.V2_BORDER());
        UiRender.centeredText(context, textRenderer, trim(textRenderer, hexSupplier.get(), hexW - 8), hexX + hexW / 2, y + 13, focused ? theme.text() : theme.mutedText());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            focusAction.run();
            return true;
        }

        return false;
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
