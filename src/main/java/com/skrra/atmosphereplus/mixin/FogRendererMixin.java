package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.visual.VisualSettings;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.nio.ByteBuffer;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Unique
    private CameraSubmersionType atmosphereplus$currentSubmersionType = CameraSubmersionType.NONE;

    @Inject(
            method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
            at = @At("HEAD"),
            require = 0
    )
    private void atmosphereplus$captureSubmersion(Camera camera, int viewDistance, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world, CallbackInfoReturnable<Vector4f> cir) {
        atmosphereplus$currentSubmersionType = camera == null ? CameraSubmersionType.NONE : camera.getSubmersionType();
    }

    @ModifyArgs(
            method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/fog/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"
            ),
            require = 0
    )
    private void atmosphereplus$modifyFogDistances(Args args) {
        Vector4f fogColor = args.get(2);
        Vector4f adjustedFogColor = VisualSettings.adjustFogColor(fogColor);
        if (adjustedFogColor != fogColor) {
            args.set(2, adjustedFogColor);
        }

        boolean fogOff = VisualSettings.isFogOffEnabled();
        boolean fogOverride = VisualSettings.isFogOverrideEnabled();
        boolean submersionFogOff = VisualSettings.isSubmersionFogOffEnabled() && atmosphereplus$isSubmersionFog(atmosphereplus$currentSubmersionType);
        if (!fogOverride && !submersionFogOff) {
            return;
        }

        float environmentalStart = args.get(3);
        float environmentalEnd = args.get(4);
        float renderDistanceStart = args.get(5);
        float renderDistanceEnd = args.get(6);
        float skyEnd = args.get(7);
        float cloudEnd = args.get(8);

        float newEnvironmentalStart = fogOverride ? VisualSettings.adjustFogStart(environmentalStart) : environmentalStart;
        float newEnvironmentalEnd = fogOverride ? VisualSettings.adjustFogEnd(environmentalEnd, newEnvironmentalStart) : environmentalEnd;
        float newRenderDistanceStart = fogOverride ? VisualSettings.adjustFogStart(renderDistanceStart) : renderDistanceStart;
        float newRenderDistanceEnd = fogOverride ? VisualSettings.adjustFogEnd(renderDistanceEnd, newRenderDistanceStart) : renderDistanceEnd;
        float newSkyEnd = fogOverride ? VisualSettings.adjustFogEnd(skyEnd, 0.0F) : skyEnd;
        float newCloudEnd = fogOverride ? VisualSettings.adjustFogEnd(cloudEnd, 0.0F) : cloudEnd;

        if (submersionFogOff) {
            newEnvironmentalStart = VisualSettings.disabledSubmersionFogStart(newEnvironmentalStart);
            newEnvironmentalEnd = VisualSettings.disabledSubmersionFogEnd(newEnvironmentalEnd, newEnvironmentalStart);
            newRenderDistanceStart = VisualSettings.disabledSubmersionFogStart(newRenderDistanceStart);
            newRenderDistanceEnd = VisualSettings.disabledSubmersionFogEnd(newRenderDistanceEnd, newRenderDistanceStart);
        } else if (fogOff) {
            float viewDistance = Math.max(renderDistanceEnd, environmentalEnd);
            newEnvironmentalStart = VisualSettings.disabledWorldFogStart(newEnvironmentalStart, viewDistance);
            newEnvironmentalEnd = VisualSettings.disabledWorldFogEnd(newEnvironmentalEnd, newEnvironmentalStart, viewDistance);
            newRenderDistanceStart = VisualSettings.disabledWorldFogStart(newRenderDistanceStart, viewDistance);
            newRenderDistanceEnd = VisualSettings.disabledWorldFogEnd(newRenderDistanceEnd, newRenderDistanceStart, viewDistance);
            newSkyEnd = VisualSettings.disabledWorldFogEnd(newSkyEnd, 0.0F, viewDistance);
            newCloudEnd = VisualSettings.disabledWorldFogEnd(newCloudEnd, 0.0F, viewDistance);
        }

        args.set(3, newEnvironmentalStart);
        args.set(4, newEnvironmentalEnd);
        args.set(5, newRenderDistanceStart);
        args.set(6, newRenderDistanceEnd);
        args.set(7, newSkyEnd);
        args.set(8, newCloudEnd);
    }

    @Unique
    private boolean atmosphereplus$isSubmersionFog(CameraSubmersionType type) {
        return type == CameraSubmersionType.LAVA
                || type == CameraSubmersionType.WATER
                || type == CameraSubmersionType.POWDER_SNOW;
    }
}
