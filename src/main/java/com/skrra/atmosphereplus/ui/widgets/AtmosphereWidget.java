package com.skrra.atmosphereplus.ui.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class AtmosphereWidget {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected String tooltip;

    protected AtmosphereWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta);

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean isHoveredPublic(double mouseX, double mouseY) {
        return isHovered(mouseX, mouseY);
    }

    public String getTooltip() {
        return tooltip;
    }

    protected boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
