package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.visual.VisualSettings;
import net.minecraft.client.render.SkyRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SkyRendering.class)
public abstract class SkyRenderingMixin {
    @ModifyArg(
            method = "renderCelestialBodies(Lnet/minecraft/client/util/math/MatrixStack;FFFLnet/minecraft/util/math/AxisRotation$Quaternion;FF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/SkyRendering;renderStars(FLnet/minecraft/client/util/math/MatrixStack;)V"
            ),
            index = 0,
            require = 0
    )
    private float atmosphereplus$adjustStarBrightness(float brightness) {
        return VisualSettings.adjustStarBrightness(brightness);
    }

    @ModifyArg(
            method = "renderCelestialBodies(Lnet/minecraft/client/util/math/MatrixStack;FFFLnet/minecraft/util/math/AxisRotation$Quaternion;FF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/SkyRendering;renderSun(FLnet/minecraft/client/util/math/MatrixStack;)V"
            ),
            index = 0,
            require = 0
    )
    private float atmosphereplus$adjustSunAlpha(float alpha) {
        return VisualSettings.adjustSunMoonAlpha(alpha);
    }

    /*
     * Moon visibility is temporarily disabled in alpha 16.1.
     *
     * The first alpha 16 target for renderMoon did not match the actual 1.21.11 callsite:
     *
     * Critical injection failure:
     * atmosphereplus$adjustMoonAlpha(F)F failed injection check, scanned 0 target(s).
     *
     * Keeping this disabled lets the client launch while we verify the exact Yarn signature/callsite.
     */

    @ModifyVariable(
            method = "renderTopSky(I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 0
    )
    private int atmosphereplus$adjustTopSkyColor(int color) {
        return VisualSettings.adjustSkyColor(color);
    }

    @ModifyVariable(
            method = "renderGlowingSky(Lnet/minecraft/client/util/math/MatrixStack;FI)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 0
    )
    private int atmosphereplus$adjustGlowingSkyColor(int color) {
        return VisualSettings.adjustSkyColor(color);
    }
}
