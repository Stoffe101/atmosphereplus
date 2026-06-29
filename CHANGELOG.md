# Atmosphere+ Changelog

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

- Transition duration is stored in config, but preset application is instant in this phase.
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
