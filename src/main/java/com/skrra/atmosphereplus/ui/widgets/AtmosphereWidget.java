package com.skrra.atmosphereplus.ui.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class AtmosphereWidget {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected String tooltip;
    // Widgets pinned outside the scrollable viewport (e.g. a sticky preview header) opt in via
    // lockToViewport(). AtmosphereScreen renders/click-tests these in a separate, unclipped pass
    // and derives the scrollable viewport's top bound from their combined bounds, instead of any
    // page hand-rolling its own overlay/click-blocking band.
    protected boolean scrollLocked = false;

    protected AtmosphereWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isScrollLocked() {
        return scrollLocked;
    }

    public AtmosphereWidget lockToViewport() {
        this.scrollLocked = true;
        return this;
    }

    public int bottom() {
        return y + height;
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
