package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.weather.WeatherVisuals;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public abstract class SoundManagerMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;", at = @At("HEAD"), cancellable = true)
    private void atmosphereplus$blockThunderSound(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        if (WeatherVisuals.shouldBlockThunderSound(sound)) {
            cir.setReturnValue(SoundSystem.PlayResult.NOT_STARTED);
        }
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V", at = @At("HEAD"), cancellable = true)
    private void atmosphereplus$blockDelayedThunderSound(SoundInstance sound, int delay, CallbackInfo ci) {
        if (WeatherVisuals.shouldBlockThunderSound(sound)) {
            ci.cancel();
        }
    }
}
