# Atmosphere+

Atmosphere+ is a client-side Fabric visual control suite for Minecraft.

It lets you change the atmosphere of the game locally without changing the server.

Version 0.5.0-beta.1 introduces the UI v2 redesign with a premium dark neon dashboard style, refreshed cards/buttons/icons, improved Theme Studio layout, and better high-GUI-scale responsiveness.

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
- UI v2 responsive dashboard
- Sodium/Iris-aware tools
- Reset Renderer
- Vanilla Safe Mode
- Sodium/Iris Safe Mode

## UI v2

Atmosphere+ 0.5.0-beta.1 refreshes the mod menu with a darker dashboard shell, stronger sidebar navigation, cleaner search, compact settings cards, refreshed icon boxes, and polished preview panels.

Theme Studio is the reference page for the new design: important actions stay near the top, the live preview remains useful without taking over constrained layouts, and high GUI scale users should see less excessive scrolling.

## Renderer controls

Known useful renderer features:

- Cloud height
- Star brightness

Shader packs may override sky/cloud renderer hooks. This is expected with many Iris shader packs. Biome Atmospheres uses the same client-side visual controls, so shader limitations still apply.

## Required

- Minecraft 1.21.11
- Fabric Loader 0.18.4+
- Fabric API
- Java 21

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

Duplicate imported preset names are cleaned up with readable suffixes like `Imported`, `Imported 2`, and `Imported 3`.

## Screenshot checklist

Recommended screenshots for the gallery:

- Main menu / Home page
- Presets page
- Preset Packs Import/Export UI
- Nether preset before/after
- End preset before/after
- Mood Overlay controls
- Biome Atmospheres page

## Client-side

Atmosphere+ is client-side. It changes what you see, not the server's actual time/weather.
Biome Atmospheres is also client-side, disabled by default, and applies mapped presets based on detected biome categories.
