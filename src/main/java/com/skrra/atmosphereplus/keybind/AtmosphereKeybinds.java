package com.skrra.atmosphereplus.keybind;

import com.skrra.atmosphereplus.client.AtmospherePlusClient;
import com.skrra.atmosphereplus.ui.AtmosphereScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class AtmosphereKeybinds {
    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of(AtmospherePlusClient.MOD_ID, "main"));

    private static KeyBinding openMenuKey;

    private AtmosphereKeybinds() {}

    public static void register() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.atmosphereplus.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                MinecraftClient minecraft = MinecraftClient.getInstance();

                if (minecraft.currentScreen instanceof AtmosphereScreen) {
                    minecraft.setScreen(null);
                } else if (minecraft.currentScreen == null) {
                    minecraft.setScreen(new AtmosphereScreen());
                }
            }
        });
    }

    public static boolean matchesOpenMenu(KeyInput input) {
        return openMenuKey != null && openMenuKey.matchesKey(input);
    }

    public static boolean matchesOpenMenuMouse(Click click) {
        return openMenuKey != null && openMenuKey.matchesMouse(click);
    }

    public static KeyBinding getOpenMenuKey() {
        return openMenuKey;
    }
}
