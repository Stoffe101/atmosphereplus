# Atmosphere+ Troubleshooting

## Shaders override the visuals

Iris shader packs may override:

- cloud height
- star brightness
- fog appearance
- lava/water/submersion fog
- sky brightness
- lightmap/fullbright

Biome Atmospheres applies presets through the same visual controls, so shader packs may still override the final look.

Try:

- Advanced → Reset Renderer
- Advanced → Shader Safe Reset
- Presets → Sodium/Iris Safe Mode

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

## Biome Atmospheres is not changing visuals

Check:

- Biome Atmospheres is enabled.
- Automation is not paused.
- Cave Handling may be pausing automation while you are underground.
- The current biome category has a mapped preset.
- The mapped preset still exists in Prebuilt Presets or My Presets.
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
