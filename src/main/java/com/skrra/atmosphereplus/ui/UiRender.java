package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class UiRender {
    // V2 chrome colors are derived live from the active Atmosphere+ theme (built-in or custom)
    // instead of being fixed constants, so theme presets meaningfully change the dashboard's
    // mood rather than only tinting a handful of text labels. Semantic colors (danger/warning/
    // success) stay fixed on purpose — they signal state, not branding.
    public static int V2_BACKGROUND() {
        return currentTheme().background();
    }

    public static int V2_BACKGROUND_DEEP() {
        return darken(currentTheme().background(), 0.35f, 0xF8);
    }

    public static int V2_PANEL() {
        return currentTheme().panel();
    }

    public static int V2_PANEL_ALT() {
        return currentTheme().panelAlt();
    }

    public static int V2_CARD() {
        return withAlpha(lerpColor(currentTheme().panel(), currentTheme().panelAlt(), 0.5f), 0xC7);
    }

    public static int V2_CARD_HOVER() {
        return withAlpha(lighten(currentTheme().panelAlt(), 0.18f), 0xE0);
    }

    public static int V2_SELECTED() {
        return withAlpha(lerpColor(currentTheme().panel(), currentTheme().accent(), 0.45f), 0xD5);
    }

    public static int V2_BORDER() {
        return currentTheme().border();
    }

    public static int V2_BORDER_SOFT() {
        return withAlpha(currentTheme().border(), 0x77);
    }

    public static int V2_ACCENT() {
        return currentTheme().accent();
    }

    public static int V2_ACCENT_PURPLE() {
        return rotateHue(currentTheme().accent(), 45f);
    }

    public static int V2_ACCENT_SOFT() {
        return currentTheme().accentSoft();
    }

    public static int V2_TEXT() {
        return currentTheme().text();
    }

    public static int V2_MUTED() {
        return currentTheme().mutedText();
    }

    public static final int V2_DANGER = V2DesignTokens.DANGER;
    public static final int V2_WARNING = V2DesignTokens.WARNING;
    public static final int V2_SUCCESS = V2DesignTokens.SUCCESS;

    public static final int V2_GAP = V2DesignTokens.SECTION_GAP;
    public static final int V2_SMALL_GAP = V2DesignTokens.ROW_GAP;
    public static final int V2_PAD = V2DesignTokens.CARD_PADDING;
    public static final int V2_ROW_HEIGHT = V2DesignTokens.ACTION_CARD_HEIGHT;
    public static final int V2_BUTTON_HEIGHT = V2DesignTokens.BUTTON_HEIGHT;
    public static final int V2_SLIDER_HEIGHT = V2DesignTokens.SLIDER_HEIGHT;
    public static final int V2_TAB_HEIGHT = 28;
    public static final int V2_ICON_BOX = V2DesignTokens.ICON_BOX_SIZE;

    private UiRender() {
    }

    private static Theme currentTheme() {
        return ThemeManager.current();
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
        ctx.fill(x, y, x + w, y + h, V2_BACKGROUND());
        gradientHorizontal(ctx, x + 1, y + 1, w - 2, 1, V2_ACCENT(), V2_ACCENT_PURPLE());
        border(ctx, x, y, w, h, V2_BORDER());
    }

    public static void v2Panel(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x + 1, y + 2, x + w + 1, y + h + 2, 0x33000000);
        gradientHorizontal(ctx, x, y, w, h, V2_PANEL(), V2_BACKGROUND_DEEP());
        ctx.fill(x, y, x + w, y + 1, 0x33FFFFFF);
        border(ctx, x, y, w, h, V2_BORDER());
    }

    public static void v2Card(DrawContext ctx, int x, int y, int w, int h, boolean hovered, boolean selected) {
        // Idle cards carry no outline at all — fill contrast and a faint shadow/highlight are
        // enough to read as a card. A border only appears once a card means something (hover =
        // soft border, selected = strong accent border + left rail), so the page doesn't read
        // as a wall of identical boxes.
        int fill = selected ? V2_SELECTED() : hovered ? V2_CARD_HOVER() : V2_CARD();
        ctx.fill(x + 1, y + 2, x + w + 1, y + h + 2, 0x1E000000);
        gradientHorizontal(ctx, x, y, w, h, fill, selected ? withAlpha(lerpColor(currentTheme().panel(), currentTheme().accent(), 0.25f), 0xDD) : fill);
        ctx.fill(x, y, x + w, y + 1, 0x22FFFFFF);
        if (selected) {
            border(ctx, x, y, w, h, V2_ACCENT());
            gradientHorizontal(ctx, x, y, 3, h, V2_ACCENT(), V2_ACCENT_PURPLE());
        } else if (hovered) {
            border(ctx, x, y, w, h, V2_BORDER_SOFT());
        }
    }

    public static void v2IconBox(DrawContext ctx, int x, int y, int size, boolean active) {
        if (active) {
            borderedRect(ctx, x, y, size, size, V2_ACCENT_SOFT(), V2_ACCENT());
            ctx.fill(x + size - 2, y + 2, x + size - 1, y + size - 2, V2_ACCENT_PURPLE());
        } else {
            rect(ctx, x, y, size, size, V2_PANEL_ALT());
        }
    }

    public static void v2Rule(DrawContext ctx, int x, int y, int w, int accentWidth) {
        rect(ctx, x, y, w, 1, V2_BORDER());
        gradientHorizontal(ctx, x, y, Math.min(w, Math.max(24, accentWidth)), 1, V2_ACCENT(), V2_ACCENT_PURPLE());
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

    private static int withAlpha(int color, int alpha) {
        return ((alpha & 255) << 24) | (color & 0x00FFFFFF);
    }

    private static int darken(int color, float amount, int alpha) {
        int black = withAlpha(0, 255);
        return withAlpha(lerpColor(color, black, amount), alpha);
    }

    private static int lighten(int color, float amount) {
        int white = withAlpha(0xFFFFFF, 255);
        return lerpColor(color, white, amount);
    }

    private static int rotateHue(int color, float degrees) {
        int a = (color >>> 24) & 255;
        float r = ((color >>> 16) & 255) / 255f;
        float g = ((color >>> 8) & 255) / 255f;
        float b = (color & 255) / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h;
        if (delta < 1e-6f) {
            h = 0f;
        } else if (max == r) {
            h = ((g - b) / delta) % 6f;
        } else if (max == g) {
            h = (b - r) / delta + 2f;
        } else {
            h = (r - g) / delta + 4f;
        }
        h *= 60f;
        if (h < 0f) {
            h += 360f;
        }

        float s = max <= 1e-6f ? 0f : delta / max;
        float v = max;

        h = (h + degrees) % 360f;
        if (h < 0f) {
            h += 360f;
        }

        float c = v * s;
        float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
        float m = v - c;
        float rr;
        float gg;
        float bb;
        if (h < 60f) {
            rr = c;
            gg = x;
            bb = 0f;
        } else if (h < 120f) {
            rr = x;
            gg = c;
            bb = 0f;
        } else if (h < 180f) {
            rr = 0f;
            gg = c;
            bb = x;
        } else if (h < 240f) {
            rr = 0f;
            gg = x;
            bb = c;
        } else if (h < 300f) {
            rr = x;
            gg = 0f;
            bb = c;
        } else {
            rr = c;
            gg = 0f;
            bb = x;
        }

        int red = clamp255(Math.round((rr + m) * 255f));
        int green = clamp255(Math.round((gg + m) * 255f));
        int blue = clamp255(Math.round((bb + m) * 255f));

        return (a << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int clamp255(int value) {
        return Math.max(0, Math.min(255, value));
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
