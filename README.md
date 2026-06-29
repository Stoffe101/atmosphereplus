# Atmosphere+

**Atmosphere+** is a client-side Fabric visual control suite for Minecraft.

It changes what *you* see: weather, time, fog, lighting, particles, clouds, profiles, presets, biome-based preset automation, and a few renderer controls that work best without shader packs.

## Target

- Minecraft `1.21.11`
- Fabric Loader `0.18.4+`
- Fabric API
- Java `21`
- Environment: client-side

## Features

### Visual controls

- Client-side weather visuals
- Client-side time visuals
- Fog distance and density
- Fog modes: Default / Server Fog, Custom Fog, Fog Off
- Lava/Submersion Fog Off for lava, water, and powder snow fog
- Fog Debug status for the active dimension, submersion type, and Minecraft fog modifier
- Fullbright and gamma
- Particle amount
- Low Fire first-person overlay option
- Cloud mode: Server / Off / Fast / Fancy
- Mood Overlay / Color Grade: gameplay-only tint, brightness, contrast, saturation, and vignette

### Renderer controls

Known useful renderer controls:

- Cloud height
- Star brightness

Removed for stability:

- Cloud distance

Shader packs may override renderer hooks. See `KNOWN_WORKING_SHADER_LIMITED.md`.

### Presets and profiles

- Quick menu
- Prebuilt Presets
- My Presets
- Nether Presets
- End Presets
- Cinematic presets
- Safe mode presets
- Optional Biome Atmospheres automation
- Save/load profiles
- Rename profiles
- Import/export profiles

### Biome Atmospheres

- Client-side biome category detection
- Optional preset automation by biome
- Disabled by default
- Default mappings for plains, desert, snow, Nether, and End atmospheres
- Uses both Prebuilt Presets and My Presets
- Smooth, interruptible atmosphere transitions
- Transition speed controls: Instant, Fast, Normal, Slow
- Minimum biome dwell time to avoid rapid border switching
- Transition status feedback while automation is moving toward a preset
- Favorite presets for quick access
- EnvironmentDetector-powered surface, underground, cave, Nether, and End awareness
- Cave Handling options for pausing automation underground or applying a dedicated cave preset
- Reliable Nether and End dimension detection
- Optional Biome Atmospheres automation toasts
- Nether and End mappings apply supported fog, gamma/fullbright, particle, and renderer controls; vanilla dimensions and shader packs can still limit sky or weather visuals
- Minecraft 1.21.11 fog hooks target `AtmosphericFogModifier`, `LavaFogModifier`, `WaterFogModifier`, and `PowderSnowFogModifier`
- Context-aware preset pickers keep Nether, End, cave, and Overworld choices focused

Biome Atmospheres does not require server installation. It applies mapped presets only when the detected biome category changes.

### Mood Overlay / Color Grade

- Gameplay-only overlay that skips Atmosphere+ menus and normal Minecraft screens where practical
- Adds shader-like tint, brightness, contrast, saturation, and vignette without requiring shader packs
- Included in presets, custom presets, profiles, and smooth transitions
- Strengthened Nether and End presets use color grading so dimension presets remain visually distinct even when sky/time/weather controls are limited
- Can be disabled or reset from the visual controls/search
- Vignette uses adaptive edge darkening for normal and ultrawide displays, and Vignette Strength `0` disables vignette rendering completely

This is a safe overlay fallback, not a full shader replacement. Iris shader packs may still override, cover, or visually compete with the final look.

### Transition Engine

- Reusable transition system for presets and automation
- Smooth ease-in-out interpolation for numeric atmosphere values
- Interruptible transitions that continue from the current interpolated state
- Shortest-path visual time interpolation across midnight
- Built for Biome Atmospheres now and future timeline/weather automation later

### Environment Detection

- Reusable EnvironmentDetector for automation systems
- Uses sky visibility, overhead enclosure, depth, and dimension context
- Helps Biome Atmospheres avoid disruptive preset changes while mining or caving

### Compatibility tools

- Sodium/Iris detection
- Shader Safe Reset
- Reset Renderer
- Vanilla Safe Mode
- Sodium/Iris Safe Mode

## Install

1. Install Fabric Loader for Minecraft `1.21.11`.
2. Install Fabric API.
3. Put the Atmosphere+ jar in your `mods` folder.
4. Optional but recommended:
   - Sodium
   - Iris
   - Mod Menu
5. Start the game.
6. Open Atmosphere+ with the keybind or through Mod Menu.

## Development build

Use Java 21.

```powershell
.\gradlew build
```

For dev testing companion mods:

```text
run/mods
```

For shader packs:

```text
run/shaderpacks
```

Then:

```powershell
.\gradlew runClient
```

## Troubleshooting

### Visuals look weird with shaders

Shader packs may override sky/cloud/fog/lightmap rendering. Biome Atmospheres uses the same client-side visual controls, so shader limitations still apply.

Try:

- Advanced → Reset Renderer
- Advanced → Shader Safe Reset
- Presets → Sodium/Iris Safe Mode

### Clouds look wrong

Cloud distance was removed for stability. Use cloud mode and cloud height instead.

### Old config acts strange

Delete:

```text
config/atmosphereplus.json
```

or in a Gradle dev run:

```text
run/config/atmosphereplus.json
```

## License

MIT
