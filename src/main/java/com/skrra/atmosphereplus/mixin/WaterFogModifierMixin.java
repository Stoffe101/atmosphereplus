package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.visual.FogDebugState;
import com.skrra.atmosphereplus.visual.VisualSettings;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.WaterFogModifier;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaterFogModifier.class)
public abstract class WaterFogModifierMixin {
    @Inject(method = "applyStartEndModifier", at = @At("TAIL"), require = 0)
    private void atmosphereplus$modifyWaterFogData(FogData data, Camera camera, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci) {
        VisualSettings.applySubmersionFogOff(data, viewDistance);
        FogDebugState.recordModifier("WaterFogModifier", CameraSubmersionType.WATER, data);
    }
}
