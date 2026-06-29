package com.skrra.atmosphereplus.visual;

import com.skrra.atmosphereplus.config.ConfigManager;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public final class FogDebugState {
    private static String lastModifier = "Unknown";
    private static String lastSubmersion = "Unknown";
    private static float lastEnvironmentalStart = 0.0F;
    private static float lastEnvironmentalEnd = 0.0F;
    private static float lastRenderDistanceStart = 0.0F;
    private static float lastRenderDistanceEnd = 0.0F;
    private static long lastUpdatedAt = 0L;

    private FogDebugState() {
    }

    public static void recordModifier(String modifier, CameraSubmersionType submersionType, FogData data) {
        lastModifier = modifier == null || modifier.isBlank() ? "Unknown" : modifier;
        lastSubmersion = submersionType == null ? "Unknown" : submersionType.name();
        if (data != null) {
            lastEnvironmentalStart = data.environmentalStart;
            lastEnvironmentalEnd = data.environmentalEnd;
            lastRenderDistanceStart = data.renderDistanceStart;
            lastRenderDistanceEnd = data.renderDistanceEnd;
        }
        lastUpdatedAt = System.currentTimeMillis();
    }

    public static String summary() {
        return "Fog Debug: dimension=" + currentDimension()
                + ", submersion=" + lastSubmersion
                + ", modifier=" + lastModifier
                + ", fogOff=" + yesNo(VisualSettings.isFogOffEnabled())
                + ", submersionOff=" + yesNo(ConfigManager.get().submersionFogOff)
                + ", env=" + range(lastEnvironmentalStart, lastEnvironmentalEnd)
                + ", render=" + range(lastRenderDistanceStart, lastRenderDistanceEnd)
                + ", age=" + ageLabel();
    }

    private static String currentDimension() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return "Unknown";
        }

        RegistryKey<World> key = client.world.getRegistryKey();
        if (World.NETHER.equals(key)) {
            return "Nether";
        }
        if (World.END.equals(key)) {
            return "End";
        }
        if (World.OVERWORLD.equals(key)) {
            return "Overworld";
        }
        return key.getValue().toString();
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }

    private static String range(float start, float end) {
        return Math.round(start) + "-" + Math.round(end);
    }

    private static String ageLabel() {
        if (lastUpdatedAt <= 0L) {
            return "never";
        }

        long ageMs = Math.max(0L, System.currentTimeMillis() - lastUpdatedAt);
        if (ageMs < 1000L) {
            return "now";
        }
        return (ageMs / 1000L) + "s";
    }
}
