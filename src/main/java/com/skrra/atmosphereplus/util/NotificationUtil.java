package com.skrra.atmosphereplus.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class NotificationUtil {
    private NotificationUtil() {
    }

    public static void applied(String label) {
        show("Applied " + label);
    }

    public static void toggled(String label, boolean enabled) {
        show(label + (enabled ? " enabled" : " disabled"));
    }

    public static void show(String message) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("✦ Atmosphere+ · " + message), true);
        }
    }
}
