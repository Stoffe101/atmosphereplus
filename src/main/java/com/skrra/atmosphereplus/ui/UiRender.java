package com.skrra.atmosphereplus.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class UiRender {
    public static final int V2_BACKGROUND = 0xF20A0F1F;
    public static final int V2_BACKGROUND_DEEP = 0xF8050914;
    public static final int V2_PANEL = 0xCC11192E;
    public static final int V2_PANEL_ALT = 0xD0182542;
    public static final int V2_CARD = 0xC7142038;
    public static final int V2_CARD_HOVER = 0xE01C2B4C;
    public static final int V2_SELECTED = 0xD52A3F8F;
    public static final int V2_BORDER = 0xAA33466F;
    public static final int V2_BORDER_SOFT = 0x774E67A2;
    public static final int V2_ACCENT = 0xFF6D88FF;
    public static final int V2_ACCENT_PURPLE = 0xFFE76DFF;
    public static final int V2_ACCENT_SOFT = 0x773A52C9;
    public static final int V2_TEXT = 0xFFEFF4FF;
    public static final int V2_MUTED = 0xFF9CA9C8;
    public static final int V2_DANGER = 0xFFFF6B8A;
    public static final int V2_WARNING = 0xFFFFD166;
    public static final int V2_SUCCESS = 0xFF6BFFB8;

    public static final int V2_GAP = 10;
    public static final int V2_SMALL_GAP = 8;
    public static final int V2_PAD = 12;
    public static final int V2_ROW_HEIGHT = 38;
    public static final int V2_BUTTON_HEIGHT = 34;
    public static final int V2_SLIDER_HEIGHT = 44;
    public static final int V2_TAB_HEIGHT = 28;
    public static final int V2_ICON_BOX = 22;

    private UiRender() {
    }

    public static void rect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }

    public static void borderedRect(DrawContext ctx, int x, int y, int w, int h, int fill, int border) {
        ctx.fill(x, y, x + w, y + h, fill);
        border(ctx, x, y, w, h, border);
    }

    public static void v2Window(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x + 2, y + 2, x + w + 2, y + h + 2, 0x55000000);
        ctx.fill(x, y, x + w, y + h, V2_BACKGROUND);
        gradientHorizontal(ctx, x + 1, y + 1, w - 2, 1, V2_ACCENT, V2_ACCENT_PURPLE);
        border(ctx, x, y, w, h, V2_BORDER);
    }

    public static void v2Panel(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x + 1, y + 2, x + w + 1, y + h + 2, 0x33000000);
        gradientHorizontal(ctx, x, y, w, h, V2_PANEL, V2_BACKGROUND_DEEP);
        ctx.fill(x, y, x + w, y + 1, 0x33FFFFFF);
        border(ctx, x, y, w, h, V2_BORDER);
    }

    public static void v2Card(DrawContext ctx, int x, int y, int w, int h, boolean hovered, boolean selected) {
        int fill = selected ? V2_SELECTED : hovered ? V2_CARD_HOVER : V2_CARD;
        int border = selected ? V2_ACCENT : hovered ? V2_BORDER_SOFT : V2_BORDER;
        ctx.fill(x + 1, y + 2, x + w + 1, y + h + 2, 0x28000000);
        gradientHorizontal(ctx, x, y, w, h, fill, selected ? 0xDD1D2B5A : fill);
        ctx.fill(x, y, x + w, y + 1, 0x2FFFFFFF);
        border(ctx, x, y, w, h, border);
        if (selected) {
            gradientHorizontal(ctx, x, y, 3, h, V2_ACCENT, V2_ACCENT_PURPLE);
        }
    }

    public static void v2IconBox(DrawContext ctx, int x, int y, int size, boolean active) {
        borderedRect(ctx, x, y, size, size, active ? V2_ACCENT_SOFT : V2_PANEL_ALT, active ? V2_ACCENT : V2_BORDER);
        if (active) {
            ctx.fill(x + size - 2, y + 2, x + size - 1, y + size - 2, V2_ACCENT_PURPLE);
        }
    }

    public static void v2Rule(DrawContext ctx, int x, int y, int w, int accentWidth) {
        rect(ctx, x, y, w, 1, V2_BORDER);
        gradientHorizontal(ctx, x, y, Math.min(w, Math.max(24, accentWidth)), 1, V2_ACCENT, V2_ACCENT_PURPLE);
    }

    public static void panel(DrawContext ctx, int x, int y, int w, int h, int fill, int border, int accent) {
        ctx.fill(x + 2, y + 2, x + w + 2, y + h + 2, 0x33000000);
        ctx.fill(x, y, x + w, y + h, fill);
        ctx.fill(x, y, x + w, y + 1, 0x33FFFFFF);
        ctx.fill(x, y, x + 3, y + h, accent);
        border(ctx, x, y, w, h, border);
    }

    public static void card(DrawContext ctx, int x, int y, int w, int h, int fill, int border) {
        ctx.fill(x + 1, y + 2, x + w + 1, y + h + 2, 0x22000000);
        ctx.fill(x, y, x + w, y + h, fill);
        ctx.fill(x, y, x + w, y + 1, 0x22FFFFFF);
        border(ctx, x, y, w, h, border);
    }

    public static void border(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    public static void gradientHorizontal(DrawContext ctx, int x, int y, int w, int h, int leftColor, int rightColor) {
        int slices = Math.max(1, Math.min(w, 32));
        for (int i = 0; i < slices; i++) {
            float p = slices <= 1 ? 0f : (float) i / (float) (slices - 1);
            int color = lerpColor(leftColor, rightColor, p);
            int sx = x + i * w / slices;
            int ex = x + (i + 1) * w / slices;
            ctx.fill(sx, y, ex, y + h, color);
        }
    }

    public static void text(DrawContext ctx, TextRenderer renderer, String text, int x, int y, int color) {
        ctx.drawTextWithShadow(renderer, Text.literal(text), x, y, color);
    }

    public static void centeredText(DrawContext ctx, TextRenderer renderer, String text, int centerX, int y, int color) {
        ctx.drawCenteredTextWithShadow(renderer, Text.literal(text), centerX, y, color);
    }

    public static boolean hovered(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static int lerpColor(int from, int to, float progress) {
        progress = Math.max(0f, Math.min(1f, progress));

        int a = lerp((from >>> 24) & 255, (to >>> 24) & 255, progress);
        int r = lerp((from >>> 16) & 255, (to >>> 16) & 255, progress);
        int g = lerp((from >>> 8) & 255, (to >>> 8) & 255, progress);
        int b = lerp(from & 255, to & 255, progress);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerp(int a, int b, float progress) {
        return (int) (a + (b - a) * progress);
    }
}
