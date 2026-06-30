package com.skrra.atmosphereplus.ui;

public final class V2Layout {
    public enum Mode {
        WIDE,
        STANDARD,
        COMPACT,
        ULTRA_COMPACT
    }

    public record ThemeStudioSpec(
            Mode mode,
            boolean sidePreview,
            boolean compactDensity,
            int editorX,
            int editorW,
            int previewX,
            int previewW,
            int columns,
            int actionColumns,
            int gap
    ) {
    }

    private V2Layout() {
    }

    public static Mode modeFor(int scaledWidth, int scaledHeight, int contentWidth, int contentHeight) {
        if (contentWidth < 480 || contentHeight < 280 || scaledWidth <= 740 || scaledHeight <= 430) {
            return Mode.ULTRA_COMPACT;
        }
        if (contentWidth < 700 || contentHeight < 360) {
            return Mode.COMPACT;
        }
        if (contentWidth < 960) {
            return Mode.STANDARD;
        }
        return Mode.WIDE;
    }

    // Minimum usable inspector width. Below this the side layout would crush the preview and
    // 3x2 action grid, so we fall back to the compact reserved-band layout instead.
    private static final int INSPECTOR_MIN_WIDTH = 200;
    private static final int INSPECTOR_MAX_WIDTH = 320;
    // Minimum editor width that still reads as an editor (2-col color cards stay usable, or
    // gracefully drop to 1 col just below this). Tuned so 1920x1080 at GUI scale 2 and 3 — both
    // a 960-px virtual canvas, ~716 px of content — qualify for the side inspector.
    private static final int MIN_EDITOR_WIDTH = 470;

    public static ThemeStudioSpec themeStudio(int x, int width, int height) {
        int gap = width < 560 ? V2DesignTokens.ROW_GAP : V2DesignTokens.COLUMN_GAP;
        boolean canUseSidePreview = width >= MIN_EDITOR_WIDTH + INSPECTOR_MIN_WIDTH + gap && height >= 300;

        if (canUseSidePreview) {
            int previewW = clamp(width * 28 / 100, INSPECTOR_MIN_WIDTH, INSPECTOR_MAX_WIDTH);
            int editorW = width - gap - previewW;
            if (editorW < MIN_EDITOR_WIDTH) {
                editorW = MIN_EDITOR_WIDTH;
                previewW = width - gap - editorW;
            }
            // Only commit to the side layout if the inspector ends up genuinely usable; otherwise
            // drop through to the compact reserved-band layout rather than crushing it.
            if (previewW >= INSPECTOR_MIN_WIDTH && editorW >= MIN_EDITOR_WIDTH) {
                Mode mode = width >= 980 && height >= 380 ? Mode.WIDE : Mode.STANDARD;
                int actionColumns = editorW >= 760 ? 5 : editorW >= 560 ? 3 : 2;
                return new ThemeStudioSpec(mode, true, mode != Mode.WIDE, x, editorW, x + editorW + gap, previewW, 2, actionColumns, gap);
            }
        }

        Mode mode = width < 500 || height < 300 ? Mode.ULTRA_COMPACT : Mode.COMPACT;
        int columns = width >= 430 ? 2 : 1;
        int actionColumns = width >= 520 ? 3 : width >= 340 ? 2 : 1;
        return new ThemeStudioSpec(mode, false, true, x, width, x, width, columns, actionColumns, gap);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
