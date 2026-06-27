package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.client.AtmospherePlusClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class IconRenderer {
    private static final int TEXTURE_SIZE = 64;

    private IconRenderer() {
    }

    public static void draw(DrawContext context, IconType icon, int x, int y, int size) {
        Identifier texture = Identifier.of(
                AtmospherePlusClient.MOD_ID,
                "textures/gui/icons/" + icon.textureName + ".png"
        );

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                0.0F,
                0.0F,
                size,
                size,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );
    }

    public static void drawCentered(DrawContext context, IconType icon, int centerX, int centerY, int size) {
        draw(context, icon, centerX - size / 2, centerY - size / 2, size);
    }
}