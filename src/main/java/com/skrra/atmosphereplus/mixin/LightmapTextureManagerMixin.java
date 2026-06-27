package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.visual.VisualSettings;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {
    @Inject(method = "getBrightness(FI)F", at = @At("RETURN"), cancellable = true)
    private static void atmosphereplus$modifyBrightness(float ambientLight, int lightLevel, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(VisualSettings.applyBrightness(cir.getReturnValue()));
    }
}
