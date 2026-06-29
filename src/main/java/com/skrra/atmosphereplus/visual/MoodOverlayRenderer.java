package com.skrra.atmosphereplus.visual;

import com.skrra.atmosphereplus.compat.CompatibilityUtil;
import com.skrra.atmosphereplus.config.AtmosphereConfig;
import com.skrra.atmosphereplus.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class MoodOverlayRenderer {
    private MoodOverlayRenderer() {
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (context == null || client == null || client.world == null || client.player == null || client.currentScreen != null) {
            return;
        }

        AtmosphereConfig config = ConfigManager.get();
        if (!config.moodOverlayEnabled) {
            return;
        }

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        float shaderScale = CompatibilityUtil.isIrisLoaded() ? 0.75F : 1.0F;
        float strength = clamp(config.moodOverlayStrength, 0.0F, 1.0F) * shaderScale;
        float saturation = clamp(config.moodSaturation, 0.0F, 2.0F);
        float brightness = clamp(config.moodBrightness, 0.5F, 1.5F);
        float contrast = clamp(config.moodContrast, 0.5F, 1.5F);

        int tint = argb(Math.round(strength * (0.20F + Math.max(0.0F, saturation - 1.0F) * 0.10F) * 255.0F),
                Math.round(clamp(config.moodOverlayRed, 0.0F, 1.0F) * 255.0F),
                Math.round(clamp(config.moodOverlayGreen, 0.0F, 1.0F) * 255.0F),
                Math.round(clamp(config.moodOverlayBlue, 0.0F, 1.0F) * 255.0F));
        if ((tint >>> 24) > 0) {
            context.fill(0, 0, width, height, tint);
        }

        if (brightness > 1.001F) {
            context.fill(0, 0, width, height, argb(Math.round((brightness - 1.0F) * 0.22F * 255.0F * shaderScale), 255, 245, 225));
        } else if (brightness < 0.999F) {
            context.fill(0, 0, width, height, argb(Math.round((1.0F - brightness) * 0.34F * 255.0F * shaderScale), 0, 0, 0));
        }

        if (contrast > 1.001F) {
            context.fill(0, 0, width, height, argb(Math.round((contrast - 1.0F) * 0.18F * 255.0F * shaderScale), 0, 0, 0));
        } else if (contrast < 0.999F) {
            context.fill(0, 0, width, height, argb(Math.round((1.0F - contrast) * 0.12F * 255.0F * shaderScale), 128, 128, 128));
        }

        if (saturation < 0.999F) {
            context.fill(0, 0, width, height, argb(Math.round((1.0F - saturation) * 0.18F * 255.0F * shaderScale), 128, 128, 128));
        }

        renderVignette(context, width, height, clamp(config.moodVignetteStrength, 0.0F, 1.0F) * shaderScale);
    }

    private static void renderVignette(DrawContext context, int width, int height, float strength) {
        if (strength <= 0.001F) {
            return;
        }

        int shortSide = Math.min(width, height);
        int bandX = Math.min(width / 2, Math.max(24, Math.round(shortSide * 0.16F)));
        int bandY = Math.min(height / 2, Math.max(24, Math.round(shortSide * 0.14F)));
        int maxAlpha = Math.round(strength * 118.0F);

        drawEdgeStrips(context, width, height, bandX, maxAlpha, true);
        drawEdgeStrips(context, width, height, bandY, maxAlpha, false);
    }

    private static void drawEdgeStrips(DrawContext context, int width, int height, int band, int maxAlpha, boolean horizontal) {
        if (band <= 0 || maxAlpha <= 0) {
            return;
        }

        int steps = Math.max(12, Math.min(96, band));
        for (int i = 0; i < steps; i++) {
            int start = Math.round(i * band / (float) steps);
            int end = Math.round((i + 1) * band / (float) steps);
            if (end <= start) {
                continue;
            }

            float edgeDistance = 1.0F - ((i + 0.5F) / steps);
            float eased = edgeDistance * edgeDistance * (3.0F - 2.0F * edgeDistance);
            int alpha = Math.round(maxAlpha * eased);
            if (alpha <= 0) {
                continue;
            }

            int color = argb(alpha, 0, 0, 0);
            if (horizontal) {
                context.fill(start, 0, end, height, color);
                context.fill(width - end, 0, width - start, height, color);
            } else {
                context.fill(0, start, width, end, color);
                context.fill(0, height - end, width, height - start, color);
            }
        }
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return (clamp(alpha, 0, 255) << 24)
                | (clamp(red, 0, 255) << 16)
                | (clamp(green, 0, 255) << 8)
                | clamp(blue, 0, 255);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }
}
