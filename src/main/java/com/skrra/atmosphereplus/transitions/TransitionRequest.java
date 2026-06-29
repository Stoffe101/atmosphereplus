package com.skrra.atmosphereplus.transitions;

import com.skrra.atmosphereplus.config.AtmosphereProfile;

public record TransitionRequest(
        String targetPresetId,
        AtmosphereProfile target,
        TransitionSpeed speed
) {
}
