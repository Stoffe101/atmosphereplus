package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.visual.VisualSettings;
import net.minecraft.client.render.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CloudRenderer.class)
public abstract class CloudRendererMixin {
    @ModifyVariable(
            method = "renderClouds(ILnet/minecraft/client/option/CloudRenderMode;FLnet/minecraft/util/math/Vec3d;JF)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 0
    )
    private int atmosphereplus$adjustCloudColor(int color) {
        return VisualSettings.adjustCloudColor(color);
    }

    @ModifyVariable(
            method = "renderClouds(ILnet/minecraft/client/option/CloudRenderMode;FLnet/minecraft/util/math/Vec3d;JF)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 0
    )
    private float atmosphereplus$adjustCloudHeight(float cloudHeight) {
        return VisualSettings.adjustCloudHeight(cloudHeight);
    }
}
