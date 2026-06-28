package com.skrra.atmosphereplus.compat;

import net.fabricmc.loader.api.FabricLoader;

public final class CompatibilityUtil {
    private CompatibilityUtil() {
    }

    public static boolean isSodiumLoaded() {
        return isLoaded("sodium");
    }

    public static boolean isIrisLoaded() {
        return isLoaded("iris");
    }

    public static boolean isModMenuLoaded() {
        return isLoaded("modmenu");
    }

    public static boolean hasShaderPipelineRisk() {
        return isIrisLoaded();
    }

    public static String renderCompatibilitySummary() {
        String sodium = isSodiumLoaded() ? "Sodium detected" : "Sodium not detected";
        String iris = isIrisLoaded() ? "Iris detected" : "Iris not detected";
        String modMenu = isModMenuLoaded() ? "Mod Menu detected" : "Mod Menu not detected";
        return sodium + " · " + iris + " · " + modMenu;
    }

    public static String sodiumStatus() {
        return isSodiumLoaded()
                ? "Detected. Vanilla-compatible visual hooks should still work."
                : "Not detected.";
    }

    public static String irisStatus() {
        return isIrisLoaded()
                ? "Detected. Shader packs may override fog/sky/cloud visuals."
                : "Not detected.";
    }

    public static String modMenuStatus() {
        return isModMenuLoaded()
                ? "Detected. Config screen entrypoint should be available."
                : "Not detected.";
    }

    private static boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
