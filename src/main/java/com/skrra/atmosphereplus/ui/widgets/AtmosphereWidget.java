package com.skrra.atmosphereplus.ui.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class AtmosphereWidget {
    // Layout region a content widget belongs to. AtmosphereScreen renders and hit-tests each
    // region in its own pass/scissor, so a page never has to hand-roll an overlay or a broad
    // click-blocking band: it just tags its widgets and the screen does the rest.
    public enum Region {
        // Normal scrollable editor content: clipped to the editor's X range and to the scroll
        // viewport top, and subject to the scroll-top hit guard.
        CONTENT,
        // Fixed full-width band pinned to the top of the content area (compact-mode sticky
        // preview + primary actions). Rendered unclipped and defines where the scroll viewport
        // begins, so scrolled CONTENT is cut off beneath it instead of ghosting through.
        STICKY_BAND,
        // Fixed right-hand inspector column (side layout). Rendered in its own X range, never
        // scrolls, and never contributes to the scroll viewport top.
        INSPECTOR
    }

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected String tooltip;
    protected Region region = Region.CONTENT;

    protected AtmosphereWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Region region() {
        return region;
    }

    public AtmosphereWidget region(Region region) {
        this.region = region;
        return this;
    }

    // A fixed region is anything that does not scroll with the editor and so must never be
    // suppressed by the editor's scroll-top hit guard.
    public boolean isFixedRegion() {
        return region != Region.CONTENT;
    }

    public boolean isStickyBand() {
        return region == Region.STICKY_BAND;
    }

    public int left() {
        return x;
    }

    public int right() {
        return x + width;
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
