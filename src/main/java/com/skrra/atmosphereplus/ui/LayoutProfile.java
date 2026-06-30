package com.skrra.atmosphereplus.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public final class LayoutProfile {
    public enum Mode {
        TINY,
        COMPACT,
        NORMAL,
        WIDE,
        ULTRAWIDE
    }

    public final Mode mode;
    public final int scaledWidth;
    public final int scaledHeight;
    public final int framebufferWidth;
    public final int framebufferHeight;
    public final double guiScale;

    private LayoutProfile(Mode mode, int scaledWidth, int scaledHeight, int framebufferWidth, int framebufferHeight, double guiScale) {
        this.mode = mode;
        this.scaledWidth = scaledWidth;
        this.scaledHeight = scaledHeight;
        this.framebufferWidth = framebufferWidth;
        this.framebufferHeight = framebufferHeight;
        this.guiScale = guiScale;
    }

    public static LayoutProfile create(int scaledWidth, int scaledHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        int framebufferWidth = scaledWidth;
        int framebufferHeight = scaledHeight;
        double guiScale = 1.0D;

        if (client != null && client.getWindow() != null) {
            Window window = client.getWindow();
            framebufferWidth = window.getFramebufferWidth();
            framebufferHeight = window.getFramebufferHeight();
            guiScale = window.getScaleFactor();
        }

        Mode mode;
        if (scaledWidth <= 740 || scaledHeight <= 430) {
            mode = Mode.TINY;
        } else if (scaledWidth <= 920 || scaledHeight <= 520) {
            mode = Mode.COMPACT;
        } else if (scaledWidth <= 1280) {
            mode = Mode.NORMAL;
        } else if (scaledWidth >= 1900 || framebufferWidth >= 3000) {
            mode = Mode.ULTRAWIDE;
        } else {
            mode = Mode.WIDE;
        }

        return new LayoutProfile(mode, scaledWidth, scaledHeight, framebufferWidth, framebufferHeight, guiScale);
    }

    public String key() {
        return mode.name() + ":" + scaledWidth + "x" + scaledHeight + ":" + framebufferWidth + "x" + framebufferHeight + ":" + Math.round(guiScale * 100.0D);
    }

    public int outerMargin() {
        return switch (mode) {
            case TINY -> 4;
            case COMPACT -> V2DesignTokens.WINDOW_MARGIN;
            default -> 10;
        };
    }

    public int maxWindowWidth() {
        return switch (mode) {
            case TINY -> Math.max(1, scaledWidth - outerMargin() * 2);
            case COMPACT -> Math.min(920, scaledWidth - outerMargin() * 2);
            case NORMAL -> Math.min(1120, scaledWidth - outerMargin() * 2);
            case WIDE -> Math.min(1400, scaledWidth - outerMargin() * 2);
            case ULTRAWIDE -> Math.min(1680, scaledWidth - outerMargin() * 2);
        };
    }

    public int maxWindowHeight() {
        return switch (mode) {
            case TINY -> Math.max(1, scaledHeight - outerMargin() * 2);
            case COMPACT -> Math.min(560, scaledHeight - outerMargin() * 2);
            case NORMAL -> Math.min(620, scaledHeight - outerMargin() * 2);
            default -> Math.min(720, scaledHeight - outerMargin() * 2);
        };
    }

    public int topBarHeight() {
        return mode == Mode.TINY ? V2DesignTokens.TOP_BAR_HEIGHT_COMPACT : V2DesignTokens.TOP_BAR_HEIGHT;
    }

    public int sidebarWidth() {
        return switch (mode) {
            case TINY -> V2DesignTokens.SIDEBAR_WIDTH_ULTRA_COMPACT;
            case COMPACT -> V2DesignTokens.SIDEBAR_WIDTH_COMPACT;
            case NORMAL -> 160;
            default -> V2DesignTokens.SIDEBAR_WIDTH;
        };
    }

    public int contentGap() {
        return switch (mode) {
            case TINY -> 6;
            case COMPACT -> 8;
            default -> V2DesignTokens.COLUMN_GAP;
        };
    }

    public int contentPadding() {
        return switch (mode) {
            case TINY -> V2DesignTokens.CARD_PADDING_COMPACT;
            case COMPACT -> 10;
            default -> V2DesignTokens.PAGE_MARGIN;
        };
    }

    public int sidebarHeaderHeight() {
        return switch (mode) {
            case TINY -> 44;
            case COMPACT -> 56;
            default -> 68;
        };
    }

    public int sidebarStep(int categoryCount, int availableHeight) {
        if (categoryCount <= 0) {
            return 24;
        }

        int ideal = switch (mode) {
            case TINY -> 21;
            case COMPACT -> V2DesignTokens.NAV_ROW_HEIGHT_COMPACT;
            default -> V2DesignTokens.NAV_ROW_HEIGHT;
        };

        int fitted = availableHeight / categoryCount;
        return Math.max(21, Math.min(ideal, fitted));
    }

    public int contentTopOffset() {
        return switch (mode) {
            case TINY -> 112;
            case COMPACT -> 112;
            default -> 116;
        };
    }

    public int sectionGap() {
        return mode == Mode.TINY ? 7 : 10;
    }

    public int quickActionColumns(int contentWidth) {
        return columns(contentWidth, switch (mode) {
            case TINY, COMPACT -> 1;
            case NORMAL -> 2;
            default -> 3;
        }, mode == Mode.TINY ? 180 : 210);
    }

    public int quickProfileColumns(int contentWidth) {
        return columns(contentWidth, switch (mode) {
            case TINY -> 1;
            case COMPACT -> 2;
            case NORMAL -> 3;
            default -> 5;
        }, mode == Mode.TINY ? 160 : 150);
    }

    public int quickPresetColumns(int contentWidth) {
        return columns(contentWidth, switch (mode) {
            case TINY -> 1;
            case COMPACT -> 2;
            default -> 3;
        }, mode == Mode.TINY ? 180 : 210);
    }

    public int manageColumns(int contentWidth) {
        return columns(contentWidth, mode == Mode.TINY ? 1 : 2, 210);
    }

    private int columns(int contentWidth, int preferred, int minCardWidth) {
        int possible = Math.max(1, contentWidth / Math.max(1, minCardWidth));
        return Math.max(1, Math.min(preferred, possible));
    }
}
