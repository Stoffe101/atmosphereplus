package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.config.ConfigManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.Properties.class)
public abstract class ClientWorldPropertiesMixin {
    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void atmosphereplus$overrideVisualTime(CallbackInfoReturnable<Long> cir) {
        if (ConfigManager.get().timeOverride) {
            cir.setReturnValue((long) ConfigManager.get().visualTime);
        }
    }
}
