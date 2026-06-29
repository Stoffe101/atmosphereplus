package com.skrra.atmosphereplus.transitions;

import com.skrra.atmosphereplus.config.AtmosphereConfig;
import com.skrra.atmosphereplus.config.AtmosphereProfile;
import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.presets.PresetLibraryManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public final class TransitionManager {
    private static final int TICK_MS = 50;
    private static TransitionState state = null;

    private TransitionManager() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> update());
    }

    public static boolean transitionTo(String targetPresetId) {
        return transitionTo(targetPresetId, TransitionSpeed.NORMAL);
    }

    public static boolean transitionTo(String targetPresetId, TransitionSpeed speed) {
        AtmosphereProfile target = PresetLibraryManager.snapshotForPreset(targetPresetId);
        if (target == null) {
            return false;
        }

        return transitionTo(new TransitionRequest(targetPresetId, target, speed == null ? TransitionSpeed.NORMAL : speed));
    }

    public static boolean transitionTo(TransitionRequest request) {
        if (request == null || request.target() == null) {
            return false;
        }

        TransitionSpeed speed = request.speed() == null ? TransitionSpeed.NORMAL : request.speed();
        AtmosphereProfile start = captureCurrent();

        if (speed.durationMs() <= 0) {
            cancelTransition();
            TransitionInterpolator.apply(ConfigManager.get(), start, request.target(), 1.0f, true);
            ConfigManager.get().activePreset = request.targetPresetId() == null ? "" : request.targetPresetId();
            ConfigManager.save();
            return true;
        }

        state = new TransitionState(request.targetPresetId(), start, request.target(), speed.durationMs());
        ConfigManager.get().activePreset = request.targetPresetId() == null ? "" : request.targetPresetId();
        return true;
    }

    public static void cancelTransition() {
        state = null;
    }

    public static boolean isTransitioning() {
        return state != null;
    }

    public static void update() {
        if (state == null || MinecraftClient.getInstance() == null) {
            return;
        }

        state.advance(TICK_MS);
        float eased = TransitionInterpolator.easeInOut(state.progress());
        boolean complete = state.complete();
        AtmosphereConfig config = ConfigManager.get();
        TransitionInterpolator.apply(config, state.start(), state.target(), eased, complete);
        config.activePreset = state.targetPresetId() == null ? "" : state.targetPresetId();

        if (complete) {
            state = null;
            ConfigManager.save();
        }
    }

    private static AtmosphereProfile captureCurrent() {
        AtmosphereProfile profile = new AtmosphereProfile("Transition Start");
        profile.capture(ConfigManager.get());
        return profile;
    }
}
