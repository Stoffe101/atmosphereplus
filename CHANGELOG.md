# Atmosphere+ Changelog

## 0.4.0-beta.1

### Added

- Preset Packs for sharing groups of Atmosphere+ presets as JSON files.
- Preset pack export flow on the Presets page with pack name, author, description, and preset selection.
- Preset pack import flow with file listing, preview, validation warnings, and safe import into My Presets.
- Preset pack folder workflow at `config/atmosphereplus-preset-packs/`.
- Lightweight preset tags in exported/imported pack data for future filtering.
- Search entries for preset packs, import preset, export preset, share presets, preset JSON, backup presets, Nether pack, End pack, and cinematic pack.

### Changed

- Imported preset IDs and display names are made unique automatically instead of overwriting existing presets.
- Exported packs contain full preset snapshots, including Mood Overlay, fog, gamma, particles, renderer, submersion fog, and Low Fire values.

### Notes

- Broken, empty, unsupported, or invalid preset pack JSON files are rejected safely before custom presets are modified.
- Imported presets appear under My Presets.

## 0.3.0-beta.13

### Added

- GitHub Actions build workflow for pushes and pull requests to `master`.
- Tag-based GitHub release workflow for `v*` version tags.
- Release workflow validation that tag versions match `gradle.properties` `mod_version`.
- Workflow artifact upload for the normal mod jar.
- Local release checklist and optional Windows PowerShell release helper.

### Notes

- Pre-release GitHub releases are marked automatically when the tag contains `alpha`, `beta`, or `rc`.
- Release automation uploads the normal `atmosphereplus-<version>.jar`, not the sources jar.

## 0.3.0-beta.12

### Fixed

- Fixed Mood Overlay vignette side-panel artifacts caused by broad rectangular gradient fills.
- Vignette rendering now uses eased edge strips based on the current scaled window size, improving ultrawide behavior.
- Setting Vignette Strength to `0` fully skips vignette rendering.

### Changed

- End presets are more vibrant, colorful, purple, and cosmic while staying playable.
- Void Purple, End Clear, Chorus Dream, Dragon Night, and Celestial Void now use stronger fantasy color-grade tuning.
- Reduced heavy vignette values on Nether/End presets so color and fog carry more of the mood.

## 0.3.0-beta.11

### Added

- Mood Overlay / Color Grade system for gameplay-only tint, brightness, contrast, saturation, and vignette-style edge darkening.
- Mood Overlay values are saved in config, profiles, presets, custom presets, and transition snapshots.
- Search entries for mood overlay, color grade, tint, brightness, contrast, saturation, vignette, Nether tint, and End tint.

### Changed

- Nether presets now use color grading to feel more distinct: Dark Crimson, Nether Clear, Lava Bloom, Basalt Ash, Soul Haze, and Nether Horror.
- End presets now use color grading to feel more distinct: Void Purple, End Clear, Chorus Dream, Dragon Night, and Celestial Void.
- Presets page now separates Nether Presets and End Presets instead of a single Dimension Presets section.
- Biome Atmospheres preset pickers are context-aware: Nether mappings show My Presets and Nether Presets, End mappings show My Presets and End Presets, and cave preset selection prioritizes cave-friendly presets.

### Notes

- Mood Overlay uses a safe fullscreen gameplay overlay rather than a fragile Minecraft post-processing shader pipeline.
- The overlay is intentionally skipped while menus/screens are open, but it is still an approximation rather than true shader color grading.
- Shader packs can alter, cover, or visually compete with the overlay and other renderer effects.

## 0.3.0-beta.10

### Added

- Dimension Presets section for Nether and End-focused atmosphere presets.
- New Nether presets: Nether Clear, Lava Bloom, Basalt Ash, Soul Haze, and Nether Horror.
- New End presets: End Clear, Chorus Dream, Dragon Night, and Celestial Void.
- Search entries for the new dimension presets and Dimension Presets section.

### Changed

- Preset page and Biome Atmospheres pickers now order presets as Favorites, My Presets, Dimension Presets, then Prebuilt Presets.
- Dark Crimson and Void Purple are now grouped as Dimension Presets while remaining the default Nether and End mappings.
- Fabric API is now declared as a required dependency in `fabric.mod.json`.
- Optional renderer polish hooks use softer injection requirements where practical.

### Notes

- Fog Off still uses inferred distance/density state internally; an explicit persisted fogOff flag is noted for a future migration.
- Nether/End visuals remain limited by vanilla dimension rendering and shader packs, but the new presets focus on supported fog, gamma/fullbright, particle, submersion fog, low-fire, and renderer mood controls.

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
