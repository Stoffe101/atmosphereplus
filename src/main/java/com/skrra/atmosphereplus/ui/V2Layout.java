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

    public static ThemeStudioSpec themeStudio(int x, int width, int height) {
        int gap = width < 560 ? V2DesignTokens.ROW_GAP : V2DesignTokens.COLUMN_GAP;
        int minEditor = 500;
        int minPreview = V2DesignTokens.PREVIEW_MIN_WIDTH;
        boolean canUseSidePreview = width >= minEditor + minPreview + gap && height >= 300;

        if (canUseSidePreview) {
            int previewW = Math.min(V2DesignTokens.PREVIEW_MAX_WIDTH, Math.max(minPreview, width * 30 / 100));
            int editorW = width - gap - previewW;
            if (editorW < minEditor) {
                editorW = minEditor;
                previewW = width - gap - editorW;
            }
            Mode mode = width >= 980 && height >= 380 ? Mode.WIDE : Mode.STANDARD;
            int actionColumns = editorW >= 760 ? 5 : editorW >= 560 ? 3 : 2;
            return new ThemeStudioSpec(mode, true, mode != Mode.WIDE, x, editorW, x + editorW + gap, previewW, 2, actionColumns, gap);
        }

        Mode mode = width < 500 || height < 300 ? Mode.ULTRA_COMPACT : Mode.COMPACT;
        int columns = width >= 430 ? 2 : 1;
        int actionColumns = width >= 520 ? 3 : width >= 340 ? 2 : 1;
        return new ThemeStudioSpec(mode, false, true, x, width, x, width, columns, actionColumns, gap);
    }
}
