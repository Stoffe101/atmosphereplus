package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.visual.VisualSettings;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
    @ModifyConstant(method = "renderFireOverlay", constant = @Constant(floatValue = -0.3F))
    private static float atmosphereplus$lowerFireOverlay(float original) {
        return VisualSettings.isLowFireEnabled() ? -0.62F : original;
    }
}
