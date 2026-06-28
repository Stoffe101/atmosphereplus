# Atmosphere+

**Atmosphere+** is a client-side Fabric visual control suite for Minecraft.

It lets you change the *look and mood* of the game without changing the server:

- Visual weather
- Visual time
- Fog distance/density
- Fullbright/gamma
- Particle amount
- Cloud mode
- Presets
- Profiles
- Quick menu
- Sodium/Iris-aware compatibility tools

## Current target

- Minecraft: `1.21.11`
- Fabric Loader: `0.18.4+`
- Java: `21`
- Environment: client-side only

## Recommended companion mods

- Sodium
- Iris
- Mod Menu
- Fabric API, if your loader setup requires it

## Main features

### Quick menu

The Quick tab is designed for fast in-game use:

- Last Profile
- Last Preset
- Fullbright
- Clear Weather
- Server Visuals
- Reset All
- Profiles
- Presets

The UI uses responsive layout profiles based on Minecraft's scaled UI size, physical framebuffer size and GUI scale.

### Presets

Built-in moods include:

- Golden Hour
- Midnight Calm
- Cozy Rain
- Thunder Night
- Deep Fog
- Bright Caves
- Misty Morning
- Starlit Night
- Storm Front
- Moonlit Fog
- Screenshot Clear
- Shader Friendly
- Performance Clear
- Soft Mist
- Cloudless Clear
- Fancy Clouds

### Profiles

Profiles allow you to save, load, rename, export and import custom atmosphere setups.

Profile backup path:

```text
config/atmosphereplus-profiles-backup.json
```

## Shader notes

Atmosphere+ works well with Sodium/Iris for normal visual state changes.

Iris shader packs may override some visual paths:

- Fog
- Clouds
- Sky colors
- Lightmap/fullbright
- Sun/moon/stars

Use:

```text
Advanced → Compatibility Status
Advanced → Shader Safe Reset
Advanced → Shader Friendly Preset
```

if shader visuals look strange.

## Development run setup

For Gradle dev testing, put companion mods here:

```text
run/mods
```

For shader packs:

```text
run/shaderpacks
```

Then run:

```powershell
.\gradlew runClient
```

## Known limitations

Cloud distance override is disabled for now because forcing it through the option value caused tiny/local cloud rendering issues in Minecraft 1.21.11.

True cloud opacity/height and direct sky/star/sun/moon renderer hooks are intentionally not enabled yet because they are higher-risk with Sodium/Iris/Iris shader packs.

## License

MIT
