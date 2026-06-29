package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.automation.BiomeAtmosphereConfig;
import com.skrra.atmosphereplus.automation.BiomeAtmosphereManager;
import com.skrra.atmosphereplus.automation.BiomeCategory;
import com.skrra.atmosphereplus.automation.CaveHandlingMode;
import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.environment.EnvironmentDetector;
import com.skrra.atmosphereplus.environment.EnvironmentSnapshot;
import com.skrra.atmosphereplus.presets.PresetLibraryManager;
import com.skrra.atmosphereplus.presets.PresetReference;
import com.skrra.atmosphereplus.transitions.TransitionManager;
import com.skrra.atmosphereplus.transitions.TransitionSpeed;
import com.skrra.atmosphereplus.ui.widgets.AtmosphereWidget;
import com.skrra.atmosphereplus.ui.widgets.BiomeMappingRowWidget;
import com.skrra.atmosphereplus.ui.widgets.ChoiceButtonWidget;
import com.skrra.atmosphereplus.ui.widgets.InfoCardWidget;
import com.skrra.atmosphereplus.ui.widgets.PresetRowWidget;
import com.skrra.atmosphereplus.ui.widgets.SectionLabelWidget;
import com.skrra.atmosphereplus.ui.widgets.ToggleWidget;

import java.util.ArrayList;
import java.util.List;

public final class BiomeAtmospheresPage {
    private BiomeAtmospheresPage() {
    }

    public interface Actions {
        void setEnabled(boolean value);

        void setPaused(boolean value);

        void setManualPause(boolean value);

        void setShowAutomationToasts(boolean value);

        void cycleTransitionSpeed();

        void cycleMinimumBiomeTime();

        void cycleCaveHandlingMode();

        void togglePresetPicker(BiomeCategory category);

        void selectPreset(BiomeCategory category, String presetId);

        void toggleCavePresetPicker();

        void selectCavePreset(String presetId);

        void toggleFavorite(String presetId);
    }

    public static int addWidgets(List<AtmosphereWidget> widgets, Actions actions, BiomeCategory pickerCategory, boolean cavePresetPickerOpen, int contentX, int contentY, int contentW) {
        BiomeAtmosphereConfig config = ConfigManager.get().biomeAtmospheres;
        int gap = 12;
        boolean twoColumn = contentW >= 760;
        int leftW = twoColumn ? (contentW - gap) / 2 : contentW;
        int rightW = twoColumn ? contentW - leftW - gap : contentW;
        int rightX = twoColumn ? contentX + leftW + gap : contentX;

        int leftY = contentY;
        int rightY = contentY;

        leftY = addControls(widgets, actions, config, cavePresetPickerOpen, contentX, leftY, leftW);
        if (twoColumn) {
            rightY = addMappings(widgets, actions, config, pickerCategory, rightX, rightY, rightW);
            return Math.max(leftY, rightY);
        }

        rightY = addMappings(widgets, actions, config, pickerCategory, rightX, leftY + 8, rightW);
        return rightY;
    }

    private static int addControls(List<AtmosphereWidget> widgets, Actions actions, BiomeAtmosphereConfig config, boolean cavePresetPickerOpen, int x, int y, int width) {
        widgets.add(new SectionLabelWidget(x, y, width, "Biome Atmospheres", "Client-side preset automation"));
        y += 30;

        widgets.add(new InfoCardWidget(
                x,
                y,
                width,
                76,
                "Automation: " + automationStatusTitle(config),
                automationStatusDescription(config),
                IconType.SKY
        ));
        y += 88;

        widgets.add(new ToggleWidget(x, y, width, "Enable Biome Atmospheres", "Opt in to client-side biome preset automation.", () -> config.enabled, actions::setEnabled));
        y += 46;

        widgets.add(new ChoiceButtonWidget(x, y, width, config.paused ? "Resume Automation" : "Pause Automation", config.paused ? "Continue automatic biome preset changes." : "Temporarily stop automatic biome preset changes.", IconType.SKY, () -> !config.paused, () -> actions.setPaused(!config.paused)));
        y += 46;

        widgets.add(new ToggleWidget(x, y, width, "Manual changes pause automation", "Pause automation when you manually change atmosphere settings.", () -> config.manualChangesPause, actions::setManualPause));
        y += 46;

        widgets.add(new ToggleWidget(x, y, width, "Show Automation Toasts", "Show brief Biome Atmospheres status notifications.", () -> config.showAutomationToasts, actions::setShowAutomationToasts));
        y += 46;

        widgets.add(new ChoiceButtonWidget(
                x,
                y,
                width,
                "Transition Speed: " + TransitionSpeed.parse(config.transitionSpeed).label(),
                "Ease-in-out speed used when applying mapped presets.",
                IconType.TIME,
                () -> TransitionSpeed.parse(config.transitionSpeed) != TransitionSpeed.INSTANT,
                actions::cycleTransitionSpeed
        ));
        y += 46;

        widgets.add(new ChoiceButtonWidget(
                x,
                y,
                width,
                "Minimum Biome Time: " + BiomeAtmosphereManager.minimumBiomeTimeLabel(config.minimumBiomeTimeMs),
                "Wait this long in a biome category before transitioning.",
                IconType.SKY,
                () -> config.minimumBiomeTimeMs > 0,
                actions::cycleMinimumBiomeTime
        ));
        y += 54;

        widgets.add(new SectionLabelWidget(x, y, width, "Cave Handling", "Underground automation behavior"));
        y += 30;

        CaveHandlingMode caveMode = CaveHandlingMode.parse(config.caveHandlingMode);
        widgets.add(new ChoiceButtonWidget(
                x,
                y,
                width,
                "Cave Handling: " + caveMode.label(),
                caveModeDescription(caveMode),
                IconType.FOG,
                () -> caveMode != CaveHandlingMode.IGNORE,
                actions::cycleCaveHandlingMode
        ));
        y += 46;

        if (caveMode == CaveHandlingMode.APPLY_CAVE_PRESET) {
            PresetReference cavePreset = PresetLibraryManager.reference(config.cavePresetId);
            widgets.add(new BiomeMappingRowWidget(
                    x,
                    y,
                    width,
                    "Cave Preset",
                    cavePreset == null ? "None / Disabled" : cavePreset.displayName(),
                    IconType.LIGHTING,
                    cavePresetPickerOpen,
                    actions::toggleCavePresetPicker
            ));
            y += 42;

            if (cavePresetPickerOpen) {
                y = addCavePresetPicker(widgets, actions, config.cavePresetId, x, y, width) + 8;
            }
        }

        widgets.add(new SectionLabelWidget(x, y, width, "Current Status", "Detected category and applied preset"));
        y += 30;

        EnvironmentSnapshot environment = EnvironmentDetector.current();
        widgets.add(new InfoCardWidget(
                x,
                y,
                width,
                62,
                "Environment: " + environment.type().label(),
                "Can See Sky: " + (environment.canSeeSky() ? "Yes" : "No") + " - Automation: " + BiomeAtmosphereManager.automationStateLabel(),
                IconType.SKY
        ));
        y += 74;

        PresetReference last = PresetLibraryManager.reference(config.lastAppliedPreset);
        String lastPreset = last == null ? "None" : last.displayName();
        PresetReference transitionTarget = PresetLibraryManager.reference(TransitionManager.targetPresetId());
        String status = TransitionManager.isTransitioning()
                ? "Transitioning to " + (transitionTarget == null ? "preset" : transitionTarget.displayName()) + " - " + TransitionManager.progressPercent() + "%"
                : "Last applied preset: " + lastPreset;
        widgets.add(new InfoCardWidget(
                x,
                y,
                width,
                68,
                "Biome: " + BiomeAtmosphereManager.currentCategoryLabel(),
                status,
                IconType.PRESETS
        ));

        return y + 80;
    }

    private static String caveModeDescription(CaveHandlingMode mode) {
        return switch (mode) {
            case KEEP_CURRENT -> "Pause biome changes while underground and keep the current atmosphere.";
            case APPLY_CAVE_PRESET -> "Transition to a chosen cave preset while underground.";
            case IGNORE -> "Continue biome automation everywhere.";
        };
    }

    private static String automationStatusTitle(BiomeAtmosphereConfig config) {
        if (!config.enabled) {
            return "Disabled";
        }
        if (config.paused) {
            return "Manually paused";
        }
        if (TransitionManager.isTransitioning()) {
            return "Transitioning";
        }
        return BiomeAtmosphereManager.automationStateLabel();
    }

    private static String automationStatusDescription(BiomeAtmosphereConfig config) {
        if (!config.enabled) {
            return "Enable automation to apply mapped presets as the environment changes.";
        }
        if (config.paused) {
            return "Manual pause is persisted. Use Resume Automation to continue.";
        }
        return "Current state: " + BiomeAtmosphereManager.automationStateLabel();
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

        boolean netherPicker = category == BiomeCategory.NETHER;
        boolean endPicker = category == BiomeCategory.END;

        List<PresetReference> favorites = PresetLibraryManager.favorites();
        if (!netherPicker && !endPicker && !favorites.isEmpty()) {
            widgets.add(new SectionLabelWidget(x, y, width, "Favorite Presets", "Starred"));
            y += 28;
            y = addPickerRows(widgets, actions, category, selectedPresetId, favorites, x, y, width);
            y += 6;
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
            List<PresetReference> custom = nonFavorite(PresetLibraryManager.customPresetsSorted());
            if (!custom.isEmpty()) {
                y = addPickerRows(widgets, actions, category, selectedPresetId, custom, x, y, width);
                y += 6;
            }
        }

        if (netherPicker) {
            List<PresetReference> nether = PresetLibraryManager.netherPresetsSorted();
            widgets.add(new SectionLabelWidget(x, y, width, "Nether Presets", "Dimension-tuned"));
            y += 28;
            return addPickerRows(widgets, actions, category, selectedPresetId, nether, x, y, width);
        }

        if (endPicker) {
            List<PresetReference> end = PresetLibraryManager.endPresetsSorted();
            widgets.add(new SectionLabelWidget(x, y, width, "End Presets", "Dimension-tuned"));
            y += 28;
            return addPickerRows(widgets, actions, category, selectedPresetId, end, x, y, width);
        }

        widgets.add(new SectionLabelWidget(x, y, width, "Prebuilt Presets", "Read-only"));
        y += 28;
        y = addPickerRows(widgets, actions, category, selectedPresetId, nonFavorite(PresetLibraryManager.builtInsSorted()), x, y, width);

        return y;
    }

    private static int addCavePresetPicker(List<AtmosphereWidget> widgets, Actions actions, String selectedPresetId, int x, int y, int width) {
        widgets.add(new ChoiceButtonWidget(
                x,
                y,
                width,
                "None / Disabled",
                "Keep the current atmosphere while underground.",
                IconType.ADVANCED,
                () -> selectedPresetId == null || selectedPresetId.isBlank() || PresetLibraryManager.reference(selectedPresetId) == null,
                () -> actions.selectCavePreset("")
        ));
        y += 42;

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
            List<PresetReference> custom = nonFavorite(PresetLibraryManager.customPresetsSorted());
            if (!custom.isEmpty()) {
                y = addCavePickerRows(widgets, actions, selectedPresetId, custom, x, y, width);
                y += 6;
            }
        }

        List<PresetReference> caveFriendly = PresetLibraryManager.caveFriendlyPresetsSorted();
        widgets.add(new SectionLabelWidget(x, y, width, "Cave-Friendly Presets", "Visibility-focused"));
        y += 28;
        y = addCavePickerRows(widgets, actions, selectedPresetId, caveFriendly, x, y, width);
        y += 6;

        widgets.add(new SectionLabelWidget(x, y, width, "Prebuilt Presets", "Read-only"));
        y += 28;
        return addCavePickerRows(widgets, actions, selectedPresetId, nonFavorite(excluding(PresetLibraryManager.builtInsSorted(), caveFriendly)), x, y, width);
    }

    private static int addCavePickerRows(List<AtmosphereWidget> widgets, Actions actions, String selectedPresetId, List<PresetReference> presets, int x, int y, int width) {
        for (PresetReference preset : presets) {
            widgets.add(new PresetRowWidget(
                    x,
                    y,
                    width,
                    preset.displayName(),
                    preset.description(),
                    preset.icon(),
                    () -> preset.id().equals(selectedPresetId),
                    () -> PresetLibraryManager.isFavorite(preset.id()),
                    () -> actions.selectCavePreset(preset.id()),
                    () -> actions.toggleFavorite(preset.id())
            ));
            y += 44;
        }
        return y;
    }

    private static int addPickerRows(List<AtmosphereWidget> widgets, Actions actions, BiomeCategory category, String selectedPresetId, List<PresetReference> presets, int x, int y, int width) {
        for (PresetReference preset : presets) {
            widgets.add(new PresetRowWidget(
                    x,
                    y,
                    width,
                    preset.displayName(),
                    preset.description(),
                    preset.icon(),
                    () -> preset.id().equals(selectedPresetId),
                    () -> PresetLibraryManager.isFavorite(preset.id()),
                    () -> actions.selectPreset(category, preset.id()),
                    () -> actions.toggleFavorite(preset.id())
            ));
            y += 44;
        }
        return y;
    }

    private static List<PresetReference> nonFavorite(List<PresetReference> presets) {
        List<PresetReference> result = new ArrayList<>();
        for (PresetReference preset : presets) {
            if (!PresetLibraryManager.isFavorite(preset.id())) {
                result.add(preset);
            }
        }
        return result;
    }

    private static List<PresetReference> excluding(List<PresetReference> presets, List<PresetReference> excluded) {
        List<PresetReference> result = new ArrayList<>();
        for (PresetReference preset : presets) {
            boolean skip = false;
            for (PresetReference excludedPreset : excluded) {
                if (preset.id().equals(excludedPreset.id())) {
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                result.add(preset);
            }
        }
        return result;
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
