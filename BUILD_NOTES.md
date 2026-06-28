# Build Notes

Alpha 18 includes release-candidate Gradle files.

## Important versions

- Minecraft: 1.21.11
- Java: 21
- Fabric Loader: 0.18.4
- Fabric Loom: 1.14-SNAPSHOT
- Mod Menu: 17.0.0-beta.1

## Build

```powershell
.\gradlew clean build
```

## Mod Menu dependency

Mod Menu is optional for players, but the source project needs it to compile the Mod Menu integration class.
