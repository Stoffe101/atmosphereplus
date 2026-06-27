package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.visual.VisualSettings;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.nio.ByteBuffer;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @ModifyArgs(
            method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/fog/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"
            )
    )
    private void atmosphereplus$modifyFogDistances(Args args) {
        if (!VisualSettings.isFogOverrideEnabled()) {
            return;
        }

        float environmentalStart = args.get(3);
        float environmentalEnd = args.get(4);
        float renderDistanceStart = args.get(5);
        float renderDistanceEnd = args.get(6);
        float skyEnd = args.get(7);
        float cloudEnd = args.get(8);

        float newEnvironmentalStart = VisualSettings.adjustFogStart(environmentalStart);
        float newEnvironmentalEnd = VisualSettings.adjustFogEnd(environmentalEnd, newEnvironmentalStart);
        float newRenderDistanceStart = VisualSettings.adjustFogStart(renderDistanceStart);
        float newRenderDistanceEnd = VisualSettings.adjustFogEnd(renderDistanceEnd, newRenderDistanceStart);
        float newSkyEnd = VisualSettings.adjustFogEnd(skyEnd, 0.0F);
        float newCloudEnd = VisualSettings.adjustFogEnd(cloudEnd, 0.0F);

        args.set(3, newEnvironmentalStart);
        args.set(4, newEnvironmentalEnd);
        args.set(5, newRenderDistanceStart);
        args.set(6, newRenderDistanceEnd);
        args.set(7, newSkyEnd);
        args.set(8, newCloudEnd);
    }
}
