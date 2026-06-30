# Atmosphere+

Atmosphere+ is a client-side Fabric visual control suite for Minecraft.

It lets you change the atmosphere of the game locally without changing the server.

## Features

- Visual weather override
- Visual time override
- Fog controls
- Fullbright/gamma
- Particle amount
- Cloud mode controls
- Quick menu
- Prebuilt Presets
- My Presets
- Preset Packs for JSON import/export
- Open Preset Packs Folder action
- Nether Presets
- End Presets
- Mood Overlay / Color Grade
- Client-side Biome Atmospheres automation
- Save/load profiles
- Profile import/export
- Responsive UI
- Sodium/Iris-aware tools
- Reset Renderer
- Vanilla Safe Mode
- Sodium/Iris Safe Mode

## Renderer controls

Known useful renderer features:

- Cloud height
- Star brightness

Shader packs may override sky/cloud renderer hooks. This is expected with many Iris shader packs. Biome Atmospheres uses the same client-side visual controls, so shader limitations still apply.

## Required

- Fabric Loader
- Fabric API

## Optional companion mods

- Sodium
- Iris
- Mod Menu

## Dimension Presets

Atmosphere+ includes Nether and End-focused presets such as Nether Clear, Lava Bloom, Basalt Ash, Soul Haze, Nether Horror, End Clear, Chorus Dream, Dragon Night, and Celestial Void.

These presets focus on supported client-side controls such as fog distance, fog density, gamma/fullbright, particles, lava/submersion fog, Low Fire, renderer mood values, and the Mood Overlay / Color Grade system. Vanilla Nether/End rendering and shader packs may still override some sky, weather, or fog behavior.

## Mood Overlay / Color Grade

Atmosphere+ includes a safe gameplay-only color grading overlay for tint, brightness, contrast, saturation, and vignette-style edge darkening. It helps Nether and End presets feel more distinct without requiring shader packs.

This is not a full shader replacement. Iris shader packs may still override or visually compete with some effects, and the overlay can be disabled or reset.

## Preset Packs

Preset Packs let players share groups of Atmosphere+ presets as JSON files. Export selected presets from the Presets page, then share the generated file from:

```text
config/atmosphereplus-preset-packs/
```

To import a pack, place the `.json` file in that folder, open Presets, choose Import Preset Pack, preview the contents, and import. Imported presets appear under My Presets. Broken or unsupported packs are rejected safely.

The Presets page can open the Preset Packs folder for you, and import/export messages now clearly report successful exports, missing packs, invalid packs, broken JSON, unsupported versions, empty packs, and renamed duplicate preset names.

## Client-side

Atmosphere+ is client-side. It changes what you see, not the server's actual time/weather.
Biome Atmospheres is also client-side, disabled by default, and applies mapped presets based on detected biome categories.
