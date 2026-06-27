package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.visual.VisualSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleOption.class)
public abstract class SimpleOptionMixin<T> {
    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private void atmosphereplus$overrideVisualOptions(CallbackInfoReturnable<T> cir) {
        if (VisualSettings.isSavingGameOptions()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.options == null) {
            return;
        }

        Object self = this;
        Object value = cir.getReturnValue();

        if (self == client.options.getGamma()) {
            if (!(value instanceof Double vanillaGamma)) {
                return;
            }

            @SuppressWarnings("unchecked")
            T adjusted = (T) Double.valueOf(VisualSettings.applyGammaOption(vanillaGamma));
            cir.setReturnValue(adjusted);
            return;
        }

        if (self == client.options.getCloudRenderMode()) {
            if (!(value instanceof CloudRenderMode vanillaMode)) {
                return;
            }

            @SuppressWarnings("unchecked")
            T adjusted = (T) VisualSettings.applyCloudMode(vanillaMode);
            cir.setReturnValue(adjusted);
        }
    }
}
