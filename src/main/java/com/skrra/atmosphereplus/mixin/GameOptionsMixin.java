package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.visual.VisualSettings;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @Inject(method = "write", at = @At("HEAD"))
    private void atmosphereplus$beforeOptionsWrite(CallbackInfo ci) {
        VisualSettings.setSavingGameOptions(true);
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void atmosphereplus$afterOptionsWrite(CallbackInfo ci) {
        VisualSettings.setSavingGameOptions(false);
    }
}
