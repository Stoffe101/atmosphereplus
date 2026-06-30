# Atmosphere+ Troubleshooting

## Shaders override the visuals

Iris shader packs may override:

- cloud height
- star brightness
- fog appearance
- lava/water/submersion fog
- sky brightness
- lightmap/fullbright
- mood overlay/color grade appearance

Biome Atmospheres applies presets through the same visual controls, so shader packs may still override the final look.

Try:

- Advanced → Reset Renderer
- Advanced → Shader Safe Reset
- Presets → Sodium/Iris Safe Mode
- Search for "Reset Mood Overlay" if a color grade looks too strong

## Mood Overlay looks too strong

Use Search -> Reset Mood Overlay, or open the visual controls and disable Mood Overlay / Color Grade. The overlay is gameplay-only where practical and is skipped while screens are open, but it is a lightweight overlay rather than a full post-processing shader.

Set Vignette Strength to `0` to fully disable vignette edge darkening. The vignette uses the current screen dimensions and avoids fixed 16:9 assumptions, including on ultrawide displays.

Shader packs may change how strong the final tint, brightness, contrast, saturation, or vignette appears.

## UI feels crowded at high GUI scale

Atmosphere+ UI v2 uses compact dashboard layouts at high GUI scale. If a page still feels cramped, use the page scrollbar first; Theme Studio keeps the preview compact on constrained layouts so Create, Duplicate, Save, Reset, and library tools stay reachable.

For the most room, Minecraft GUI scale 2 gives the UI more breathing space than GUI scale 3 at 1920x1080.

## Cloud distance is missing

Cloud distance was removed because it caused broken/tiny/local clouds.

Kept renderer features:

- Cloud height
- Star brightness

## Profile or config feels broken

Delete the config file and restart:

```text
config/atmosphereplus.json
```

For Gradle dev runs:

```text
run/config/atmosphereplus.json
```

## Preset Pack import failed

Preset Packs are JSON files stored in:

```text
config/atmosphereplus-preset-packs/
```

If import fails, check that the file is valid JSON, uses a supported `formatVersion`, and contains at least one preset with a `displayName` and `snapshot`. Broken or unsupported packs are rejected before existing custom presets are changed.

Imported presets appear under My Presets. Duplicate IDs and display names are renamed automatically.

If you import the same pack repeatedly, Atmosphere+ uses clean duplicate names such as `Imported`, `Imported 2`, and `Imported 3` instead of stacking duplicate words.

Use Presets -> Open Preset Packs Folder to open the folder. If the OS cannot open it, Atmosphere+ copies the path so you can paste it into your file manager.

## Biome Atmospheres is not changing visuals

Check:

- Biome Atmospheres is enabled.
- Automation is not paused.
- Cave Handling may be pausing automation while you are underground.
- The current biome category has a mapped preset.
- The mapped preset still exists in Nether Presets, End Presets, Prebuilt Presets, or My Presets.
- If you expect popup feedback, Show Automation Toasts is enabled.
- Shader packs may be overriding the visible result.

## Nether or End mappings do not apply

Make sure the Nether or End category has a mapped preset. Atmosphere+ detects these dimensions directly before cave handling, so Cave Handling should not block Nether or End mappings.

If the category changes but the scene still looks similar, open Fog -> Fog Debug. In normal Nether/End air, the active modifier should usually be `AtmosphericFogModifier`. Atmosphere+ targets that modifier in Minecraft 1.21.11, but vanilla Nether/End sky and shader packs can still limit weather, time, sky, and some fog visuals even when the preset is applied.

## Lava or water fog is still visible

Enable Fog -> Lava/Submersion Fog Off. This targets `LavaFogModifier`, `WaterFogModifier`, and `PowderSnowFogModifier` and pushes the submersion fog range outward. Use Fog -> Fog Debug while submerged to confirm which modifier is active. Shader packs or future Minecraft renderer changes may still override part of this effect.

## Fire overlay covers too much of the screen

Enable Particles -> Low Fire. It lowers the first-person fire overlay while keeping the fire warning visible.

## Biome Atmospheres changes presets while mining

Use Biome Atmospheres -> Cave Handling:

- Keep Current / Pause Automation keeps the active atmosphere underground.
- Apply Cave Preset transitions to your selected cave preset underground.
- Ignore Cave Handling keeps biome automation active everywhere.

## Mod Menu does not show Atmosphere+

Make sure Mod Menu is installed. For source builds, the Gradle file includes:

```gradle
modImplementation "com.terraformersmc:modmenu:${project.modmenu_version}"
```

## Crash on launch after renderer changes

Disable/remove Atmosphere+ and send the crash log. Renderer mixins can be sensitive to Minecraft/Yarn changes.
