# Atmosphere+

**Atmosphere+** is a client-side Fabric visual control suite for Minecraft.

It changes what *you* see: weather, time, fog, lighting, particles, clouds, profiles, presets, biome-based preset automation, and a few renderer controls that work best without shader packs.

## Target

- Minecraft `1.21.11`
- Fabric Loader `0.18.4+`
- Java `21`
- Environment: client-side

## Features

### Visual controls

- Client-side weather visuals
- Client-side time visuals
- Fog distance and density
- Fullbright and gamma
- Particle amount
- Cloud mode: Server / Off / Fast / Fancy

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

Biome Atmospheres does not require server installation. It applies mapped presets only when the detected biome category changes.

### Transition Engine

- Reusable transition system for presets and automation
- Smooth ease-in-out interpolation for numeric atmosphere values
- Interruptible transitions that continue from the current interpolated state
- Shortest-path visual time interpolation across midnight
- Built for Biome Atmospheres now and future timeline/weather automation later

### Compatibility tools

- Sodium/Iris detection
- Shader Safe Reset
- Reset Renderer
- Vanilla Safe Mode
- Sodium/Iris Safe Mode

## Install

1. Install Fabric Loader for Minecraft `1.21.11`.
2. Put the Atmosphere+ jar in your `mods` folder.
3. Optional but recommended:
   - Sodium
   - Iris
   - Mod Menu
4. Start the game.
5. Open Atmosphere+ with the keybind or through Mod Menu.

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
