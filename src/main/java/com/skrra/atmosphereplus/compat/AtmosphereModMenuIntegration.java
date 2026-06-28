package com.skrra.atmosphereplus.compat;

import com.skrra.atmosphereplus.ui.AtmosphereScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class AtmosphereModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new AtmosphereScreen();
    }
}
