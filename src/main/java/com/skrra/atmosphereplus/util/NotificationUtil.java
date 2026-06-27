package com.skrra.atmosphereplus.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class NotificationUtil {
    private NotificationUtil() {
    }

    public static void show(String message) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("Atmosphere+ · " + message), true);
        }
    }
}
