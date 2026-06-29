package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.visual.FogDebugState;
import com.skrra.atmosphereplus.visual.VisualSettings;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.AtmosphericFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AtmosphericFogModifier.class)
public abstract class AtmosphericFogModifierMixin {
    @Inject(method = "applyStartEndModifier", at = @At("TAIL"), require = 0)
    private void atmosphereplus$modifyAtmosphericFogData(FogData data, Camera camera, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci) {
        VisualSettings.applyAtmosphericFogOff(data, viewDistance);
        FogDebugState.recordModifier("AtmosphericFogModifier", camera == null ? null : camera.getSubmersionType(), data);
    }
}
