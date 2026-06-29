# Atmosphere+ Changelog

## 0.3.0-beta.9

### Fixed

- Fog Off now targets Minecraft 1.21.11's `AtmosphericFogModifier` and pushes both fog start and end distances outward.
- Lava/Submersion Fog Off now targets `LavaFogModifier`, `WaterFogModifier`, and `PowderSnowFogModifier` directly.
- Nether and End fog changes should be more visible because dimension fog is adjusted at the active `FogData` modifier path instead of only at the final fog buffer write.

### Added

- Fog Debug action showing current dimension, camera submersion, active fog modifier, fog toggles, and latest fog ranges.

### Notes

- Low Fire remains separate from fog and only changes the first-person fire overlay.
- Shader packs may still replace or post-process Minecraft fog after Atmosphere+ adjusts vanilla fog data.

## 0.3.0-beta.8

### Added

- Lava/Submersion Fog Off control for lava, water, and powder snow fog.
- Low Fire option that lowers the first-person fire overlay without removing it.
- Search entries for lava fog, submersion fog, water fog, and low fire.

### Changed

- Nether and End presets now use stronger supported visual values so mapped dimension presets are more visibly distinct.
- Fog rendering now also applies Atmosphere+ brightness/renderer mood to fog color where Minecraft exposes the fog path.

### Notes

- Vanilla dimensions and shader packs can still limit sky, weather, and some fog behavior. Atmosphere+ now applies supported fog, gamma/fullbright, particle, and renderer controls, but shader packs may override the final image.

## 0.3.0-beta.7

### Fixed

- Nether and End mappings now force a transition when the mapped preset is not actually active.
- Nether and End category changes bypass Overworld biome dwell timing.
- Cave Handling no longer masks dimension preset application.
- Biome Atmospheres status wording is clearer for disabled, active, paused, underground, dwell, and transition states.

### Added

- Fog Mode controls: Server Fog, Custom Fog, and Fog Off.
- Search entries for fog off, disable fog, custom fog, server fog, and reset fog.

## 0.3.0-beta.6

### Added

- Biome Atmospheres automation toast feedback.
- Show Automation Toasts setting for Biome Atmospheres.

### Fixed

- Nether and End dimension detection now use Minecraft world registry keys.
- Cave Handling no longer overrides Nether or End mappings.

### Changed

- Biome Atmospheres notifications are debounced to avoid toast spam.

## 0.3.0-beta.5

### Added

- Reusable EnvironmentDetector for future automation systems.
- Cave Handling controls for Biome Atmospheres.
- Underground pause mode that keeps the current atmosphere while caving.
- Optional Cave Preset mode that transitions to a selected preset underground.
- Compact environment status in Biome Atmospheres.

### Changed

- Biome Atmospheres now uses environment context instead of Y-level-only cave handling.
- Preset picker star buttons are smaller and cleaner.

## 0.3.0-beta.4

### Added

- Transition status feedback in Biome Atmospheres.
- Favorite presets for both Prebuilt Presets and My Presets.
- Favorite Presets section on the Presets page and in preset pickers.
- Profile loading options: Apply Instantly and Transition To Profile.

### Changed

- Visual time transitions now wrap the shortest direction across midnight.
- Preset picker ordering is now Favorites, My Presets, then Prebuilt Presets.
- Preset rows now use separate star buttons so favoriting does not apply a preset.

## 0.3.0-beta.3

### Added

- Reusable Transition Engine for preset and automation changes.
- Smooth ease-in-out interpolation for numeric atmosphere values.
- Interruptible transitions that retarget from the current interpolated state.
- Transition Speed options: Instant, Fast, Normal, Slow.
- Minimum Biome Time options to avoid rapid biome-border switching.

### Changed

- Biome Atmospheres now transitions through the shared Transition Engine instead of applying presets instantly.
- Manual preset switching now uses the default smooth transition path.

## 0.3.0-beta.2

### Changed

- Polished Biome Atmospheres mapping UI with compact biome rows.
- Replaced whole-card preset cycling with an explicit preset picker button.
- Preset picker now groups None / Disabled, Prebuilt Presets, and My Presets.
- Reduced empty space in the biome mapping list.

## 0.3.0-beta.1

### Added

- Preset Library cleanup with separate Prebuilt Presets and My Presets sections.
- Persistent custom presets in `config/atmosphereplus-presets.json`.
- New Biome Atmospheres page below Presets.
- Client-side biome category preset automation.
- Default biome mappings for plains/default, forest, desert, snow, swamp, ocean, mountain, cave, Nether, and End.
- Biome Atmospheres search entries.

### Changed

- Presets page is cleaner and easier to scan.
- Built-in presets are treated as read-only Prebuilt Presets.
- Biome Atmospheres is disabled by default and can be paused/resumed.

### Notes

- Transition duration from this phase was superseded by Transition Speed in `0.3.0-beta.3`.
- Shader limitations still apply because shader packs may override sky, fog, cloud, and lighting hooks.

## 0.1.0-alpha.18

### Added

- Release candidate cleanup pass.
- Release-ready Gradle files:
  - `build.gradle`
  - `settings.gradle`
  - `gradle.properties`
- Install guide.
- Troubleshooting guide.
- Screenshots checklist.
- Cleaner README.
- Cleaner Modrinth description.
- Cleaner release checklist.

### Changed

- Final text pass across tabs.
- Shader warning wording is clearer.
- Safe Mode presets are clearer.
- Renderer notes are clearer.
- Docs/changelog clutter removed from the packaged ZIP.
- `fabric.mod.json` metadata polished.

### Kept

- Cloud height.
- Star brightness.
- Safe modes.
- Profiles.
- Presets.
- Sodium/Iris compatibility tools.

### Removed / disabled

- Cloud distance remains removed for stability.
