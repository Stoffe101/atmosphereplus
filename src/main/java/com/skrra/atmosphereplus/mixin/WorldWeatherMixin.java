package com.skrra.atmosphereplus.mixin;

import com.skrra.atmosphereplus.weather.WeatherVisuals;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldWeatherMixin {
    @Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
    private void atmosphereplus$overrideRainGradient(float tickProgress, CallbackInfoReturnable<Float> cir) {
        float rainGradient = WeatherVisuals.rainGradient();

        if (!Float.isNaN(rainGradient)) {
            cir.setReturnValue(rainGradient);
        }
    }

    @Inject(method = "getThunderGradient", at = @At("HEAD"), cancellable = true)
    private void atmosphereplus$overrideThunderGradient(float tickProgress, CallbackInfoReturnable<Float> cir) {
        float thunderGradient = WeatherVisuals.thunderGradient();

        if (!Float.isNaN(thunderGradient)) {
            cir.setReturnValue(thunderGradient);
        }
    }

    @Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
    private void atmosphereplus$overrideIsRaining(CallbackInfoReturnable<Boolean> cir) {
        if (WeatherVisuals.isOverridingWeather()) {
            cir.setReturnValue(WeatherVisuals.shouldVisuallyRain());
        }
    }

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    private void atmosphereplus$overrideIsThundering(CallbackInfoReturnable<Boolean> cir) {
        if (WeatherVisuals.isOverridingWeather()) {
            cir.setReturnValue(WeatherVisuals.shouldVisuallyThunder());
        }
    }

    @Inject(method = "hasRain", at = @At("HEAD"), cancellable = true)
    private void atmosphereplus$overrideHasRain(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (WeatherVisuals.isOverridingWeather()) {
            cir.setReturnValue(WeatherVisuals.shouldForceVisiblePrecipitation());
        }
    }

    @Inject(method = "getPrecipitation", at = @At("HEAD"), cancellable = true)
    private void atmosphereplus$overridePrecipitation(BlockPos pos, CallbackInfoReturnable<Biome.Precipitation> cir) {
        if (WeatherVisuals.isOverridingWeather()) {
            cir.setReturnValue(WeatherVisuals.precipitation(cir.getReturnValue()));
        }
    }
}
