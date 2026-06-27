package com.skrra.atmosphereplus.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class UiRender {
    private UiRender() {
    }

    public static void rect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }

    public static void borderedRect(DrawContext ctx, int x, int y, int w, int h, int fill, int border) {
        ctx.fill(x, y, x + w, y + h, fill);
        border(ctx, x, y, w, h, border);
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
