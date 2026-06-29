package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.automation.BiomeAtmosphereConfig;
import com.skrra.atmosphereplus.automation.BiomeAtmosphereManager;
import com.skrra.atmosphereplus.automation.BiomeCategory;
import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.presets.PresetLibraryManager;
import com.skrra.atmosphereplus.presets.PresetReference;
import com.skrra.atmosphereplus.ui.widgets.AtmosphereWidget;
import com.skrra.atmosphereplus.ui.widgets.BiomeMappingRowWidget;
import com.skrra.atmosphereplus.ui.widgets.ChoiceButtonWidget;
import com.skrra.atmosphereplus.ui.widgets.InfoCardWidget;
import com.skrra.atmosphereplus.ui.widgets.SectionLabelWidget;
import com.skrra.atmosphereplus.ui.widgets.ToggleWidget;

import java.util.List;

public final class BiomeAtmospheresPage {
    private BiomeAtmospheresPage() {
    }

    public interface Actions {
        void setEnabled(boolean value);

        void setPaused(boolean value);

        void setManualPause(boolean value);

        void cycleTransition();

        void togglePresetPicker(BiomeCategory category);

        void selectPreset(BiomeCategory category, String presetId);
    }

    public static int addWidgets(List<AtmosphereWidget> widgets, Actions actions, BiomeCategory pickerCategory, int contentX, int contentY, int contentW) {
        BiomeAtmosphereConfig config = ConfigManager.get().biomeAtmospheres;
        int gap = 12;
        boolean twoColumn = contentW >= 760;
        int leftW = twoColumn ? (contentW - gap) / 2 : contentW;
        int rightW = twoColumn ? contentW - leftW - gap : contentW;
        int rightX = twoColumn ? contentX + leftW + gap : contentX;

        int leftY = contentY;
        int rightY = contentY;

        leftY = addControls(widgets, actions, config, contentX, leftY, leftW);
        if (twoColumn) {
            rightY = addMappings(widgets, actions, config, pickerCategory, rightX, rightY, rightW);
            return Math.max(leftY, rightY);
        }

        rightY = addMappings(widgets, actions, config, pickerCategory, rightX, leftY + 8, rightW);
        return rightY;
    }

    private static int addControls(List<AtmosphereWidget> widgets, Actions actions, BiomeAtmosphereConfig config, int x, int y, int width) {
        widgets.add(new SectionLabelWidget(x, y, width, "Biome Atmospheres", "Client-side preset automation"));
        y += 30;

        widgets.add(new InfoCardWidget(
                x,
                y,
                width,
                76,
                config.enabled ? (config.paused ? "Automation paused" : "Automation ready") : "Automation disabled",
                "Disabled by default. When enabled, Atmosphere+ applies mapped presets only when the detected biome category changes.",
                IconType.SKY
        ));
        y += 88;

        widgets.add(new ToggleWidget(x, y, width, "Enable Biome Atmospheres", "Opt in to client-side biome preset automation.", () -> config.enabled, actions::setEnabled));
        y += 46;

        widgets.add(new ChoiceButtonWidget(x, y, width, config.paused ? "Resume Automation" : "Pause Automation", config.paused ? "Continue automatic biome preset changes." : "Temporarily stop automatic biome preset changes.", IconType.SKY, () -> !config.paused, () -> actions.setPaused(!config.paused)));
        y += 46;

        widgets.add(new ToggleWidget(x, y, width, "Manual changes pause automation", "Pause automation when you manually change atmosphere settings.", () -> config.manualChangesPause, actions::setManualPause));
        y += 46;

        widgets.add(new ChoiceButtonWidget(
                x,
                y,
                width,
                "Transition: " + BiomeAtmosphereManager.transitionLabel(config.transitionDurationMs),
                "Stored for smooth transitions; current preset application is instant.",
                IconType.TIME,
                () -> config.transitionDurationMs > 0,
                actions::cycleTransition
        ));
        y += 54;

        widgets.add(new SectionLabelWidget(x, y, width, "Current Status", "Detected category and applied preset"));
        y += 30;

        PresetReference last = PresetLibraryManager.reference(config.lastAppliedPreset);
        String lastPreset = last == null ? "None" : last.displayName();
        widgets.add(new InfoCardWidget(
                x,
                y,
                width,
                68,
                "Biome: " + BiomeAtmosphereManager.currentCategoryLabel(),
                "Last applied preset: " + lastPreset,
                IconType.PRESETS
        ));

        return y + 80;
    }

    private static int addMappings(List<AtmosphereWidget> widgets, Actions actions, BiomeAtmosphereConfig config, BiomeCategory pickerCategory, int x, int y, int width) {
        widgets.add(new SectionLabelWidget(x, y, width, "Biome Mapping", "Category to preset"));
        y += 30;

        int columns = width >= 560 ? 2 : 1;
        int gap = 10;
        int columnW = (width - gap * (columns - 1)) / columns;
        int[] columnY = new int[columns];
        for (int i = 0; i < columns; i++) {
            columnY[i] = y;
        }
        int index = 0;

        for (BiomeCategory category : BiomeCategory.values()) {
            String presetId = config.mappings == null ? "" : config.mappings.getOrDefault(category.name(), "");
            PresetReference preset = PresetLibraryManager.reference(presetId);
            String selectedPreset = preset == null ? "None / Disabled" : preset.displayName();
            int col = index % columns;
            int rowX = x + col * (columnW + gap);
            int rowY = columnY[col];
            boolean open = category == pickerCategory;

            widgets.add(new BiomeMappingRowWidget(
                    rowX,
                    rowY,
                    columnW,
                    category.label(),
                    selectedPreset,
                    iconForCategory(category),
                    open,
                    () -> actions.togglePresetPicker(category)
            ));
            columnY[col] += 42;

            if (open) {
                columnY[col] = addPresetPicker(widgets, actions, category, presetId, rowX, columnY[col], columnW);
                columnY[col] += 8;
            }
            index++;
        }

        y = Math.max(columnY[0], columns > 1 ? columnY[1] : columnY[0]) + 4;

        if (PresetLibraryManager.allReferences().isEmpty()) {
            widgets.add(new InfoCardWidget(
                    x,
                    y,
                    width,
                    60,
                    "No presets available",
                    "Create or restore presets before enabling Biome Atmospheres.",
                    IconType.PRESETS
            ));
            y += 72;
        }

        return y;
    }

    private static int addPresetPicker(List<AtmosphereWidget> widgets, Actions actions, BiomeCategory category, String selectedPresetId, int x, int y, int width) {
        widgets.add(new ChoiceButtonWidget(
                x,
                y,
                width,
                "None / Disabled",
                "Do nothing for this biome category.",
                IconType.ADVANCED,
                () -> selectedPresetId == null || selectedPresetId.isBlank() || PresetLibraryManager.reference(selectedPresetId) == null,
                () -> actions.selectPreset(category, "")
        ));
        y += 42;

        widgets.add(new SectionLabelWidget(x, y, width, "Prebuilt Presets", "Read-only"));
        y += 28;
        for (PresetReference preset : PresetLibraryManager.builtIns()) {
            widgets.add(new ChoiceButtonWidget(
                    x,
                    y,
                    width,
                    preset.displayName(),
                    preset.description(),
                    preset.icon(),
                    () -> preset.id().equals(selectedPresetId),
                    () -> actions.selectPreset(category, preset.id())
            ));
            y += 42;
        }

        widgets.add(new SectionLabelWidget(x, y, width, "My Presets", "Saved custom presets"));
        y += 28;
        if (!PresetLibraryManager.hasCustomPresets()) {
            widgets.add(new InfoCardWidget(
                    x,
                    y,
                    width,
                    48,
                    "No custom presets",
                    "Save a custom preset from the Presets page.",
                    IconType.PRESETS
            ));
            y += 58;
        } else {
            for (PresetReference preset : PresetLibraryManager.customPresets()) {
                widgets.add(new ChoiceButtonWidget(
                        x,
                        y,
                        width,
                        preset.displayName(),
                        preset.description(),
                        preset.icon(),
                        () -> preset.id().equals(selectedPresetId),
                        () -> actions.selectPreset(category, preset.id())
                ));
                y += 42;
            }
        }

        return y;
    }

    private static IconType iconForCategory(BiomeCategory category) {
        return switch (category) {
            case DESERT, OCEAN, PLAINS, MOUNTAIN -> IconType.SKY;
            case FOREST, SWAMP, SNOW, CAVE -> IconType.FOG;
            case NETHER -> IconType.LIGHTING;
            case END -> IconType.TIME;
        };
    }
}
