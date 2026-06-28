# Atmosphere+

**Atmosphere+** is a client-side Fabric visual control suite for Minecraft.

It changes what *you* see: weather, time, fog, lighting, particles, clouds, profiles, presets, and a few renderer controls that work best without shader packs.

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
- Cinematic presets
- Safe mode presets
- Save/load profiles
- Rename profiles
- Import/export profiles

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

Shader packs may override sky/cloud/fog/lightmap rendering.

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
