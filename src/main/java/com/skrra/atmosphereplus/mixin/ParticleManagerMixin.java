package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.config.ConfigManager;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void atmosphereplus$limitParticles(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
        float amount = ConfigManager.get().particleAmount;

        if (amount <= 0.0F) {
            cir.setReturnValue(null);
            return;
        }

        if (amount < 1.0F && ThreadLocalRandom.current().nextFloat() > amount) {
            cir.setReturnValue(null);
        }
    }
}
