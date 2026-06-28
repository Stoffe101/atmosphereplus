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
            case TINY -> 6;
            case COMPACT -> 8;
            default -> 12;
        };
    }

    public int maxWindowWidth() {
        return switch (mode) {
            case TINY -> Math.max(1, scaledWidth - outerMargin() * 2);
            case COMPACT -> Math.min(900, scaledWidth - outerMargin() * 2);
            case NORMAL -> Math.min(960, scaledWidth - outerMargin() * 2);
            case WIDE -> Math.min(1040, scaledWidth - outerMargin() * 2);
            case ULTRAWIDE -> Math.min(1180, scaledWidth - outerMargin() * 2);
        };
    }

    public int maxWindowHeight() {
        return switch (mode) {
            case TINY -> Math.max(1, scaledHeight - outerMargin() * 2);
            case COMPACT -> Math.min(520, scaledHeight - outerMargin() * 2);
            case NORMAL -> Math.min(560, scaledHeight - outerMargin() * 2);
            default -> Math.min(600, scaledHeight - outerMargin() * 2);
        };
    }

    public int topBarHeight() {
        return mode == Mode.TINY ? 44 : 48;
    }

    public int sidebarWidth() {
        return switch (mode) {
            case TINY -> 104;
            case COMPACT -> 142;
            case NORMAL -> 164;
            default -> 174;
        };
    }

    public int contentGap() {
        return switch (mode) {
            case TINY -> 8;
            case COMPACT -> 12;
            default -> 18;
        };
    }

    public int contentPadding() {
        return switch (mode) {
            case TINY -> 6;
            case COMPACT -> 10;
            default -> 14;
        };
    }

    public int sidebarHeaderHeight() {
        return switch (mode) {
            case TINY -> 52;
            case COMPACT -> 58;
            default -> 72;
        };
    }

    public int sidebarStep(int categoryCount, int availableHeight) {
        if (categoryCount <= 0) {
            return 24;
        }

        int ideal = switch (mode) {
            case TINY -> 21;
            case COMPACT -> 22;
            default -> 25;
        };

        int fitted = availableHeight / categoryCount;
        return Math.max(21, Math.min(ideal, fitted));
    }

    public int contentTopOffset() {
        return switch (mode) {
            case TINY -> 112;
            case COMPACT -> 120;
            default -> 126;
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
