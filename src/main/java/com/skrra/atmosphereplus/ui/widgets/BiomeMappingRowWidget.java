package com.skrra.atmosphereplus.ui.widgets;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.IconRenderer;
import com.skrra.atmosphereplus.ui.IconType;
import com.skrra.atmosphereplus.ui.UiRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class BiomeMappingRowWidget extends AtmosphereWidget {
    private final String biomeName;
    private final String presetName;
    private final IconType icon;
    private final boolean pickerOpen;
    private final Runnable action;

    public BiomeMappingRowWidget(int x, int y, int width, String biomeName, String presetName, IconType icon, boolean pickerOpen, Runnable action) {
        super(x, y, width, 34);
        this.biomeName = biomeName;
        this.presetName = presetName;
        this.icon = icon;
        this.pickerOpen = pickerOpen;
        this.action = action;
        this.tooltip = "Select preset for " + biomeName;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.current();
        boolean hover = isHovered(mouseX, mouseY);
        UiRender.v2Card(context, x, y, width, height, hover, pickerOpen);

        int tileSize = 18;
        int tileX = x + 8;
        int tileY = y + 8;
        UiRender.v2IconBox(context, tileX, tileY, tileSize, pickerOpen || hover);
        IconRenderer.drawCentered(context, icon, tileX + tileSize / 2, tileY + tileSize / 2, 13);

        int buttonW = Math.min(Math.max(102, width / 3), Math.max(96, width - 118));
        int buttonX = x + width - buttonW - 8;
        int buttonY = y + 6;
        int buttonH = 22;
        boolean buttonHover = UiRender.hovered(mouseX, mouseY, buttonX, buttonY, buttonW, buttonH);
        int buttonFill = pickerOpen ? UiRender.V2_ACCENT_SOFT : buttonHover ? UiRender.V2_CARD_HOVER : UiRender.V2_PANEL_ALT;
        int buttonBorder = pickerOpen || buttonHover ? UiRender.V2_ACCENT : UiRender.V2_BORDER;
        UiRender.borderedRect(context, buttonX, buttonY, buttonW, buttonH, buttonFill, buttonBorder);

        int nameW = Math.max(24, buttonX - (tileX + tileSize + 8) - 6);
        UiRender.text(context, textRenderer, trim(textRenderer, biomeName, nameW), tileX + tileSize + 8, y + 8, theme.text());

        String pickerLabel = trim(textRenderer, presetName + " v", buttonW - 12);
        UiRender.centeredText(context, textRenderer, pickerLabel, buttonX + buttonW / 2, buttonY + 7, pickerOpen ? theme.text() : theme.mutedText());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        int buttonW = Math.min(Math.max(102, width / 3), Math.max(96, width - 118));
        int buttonX = x + width - buttonW - 8;
        if (UiRender.hovered(mouseX, mouseY, buttonX, y + 6, buttonW, 22)) {
            action.run();
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
