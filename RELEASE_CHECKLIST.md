# Atmosphere+ Release Checklist

Use this checklist when preparing a release.

## 1. Update Version

Update the version in:

- `gradle.properties`
- `src/main/resources/fabric.mod.json`
- `src/main/java/com/skrra/atmosphereplus/client/AtmospherePlusClient.java` while the version is still hardcoded there
- `CHANGELOG.md`
- `README.md` if release instructions changed

## 2. Build And Test Locally

```bash
./gradlew clean build
```

On Windows:

```powershell
.\gradlew.bat clean build
```

Test the changed areas in Minecraft:

- Open the Atmosphere+ menu.
- Apply presets.
- Test Mood Overlay / Color Grade.
- Test Preset Pack export/import if preset sharing changed.
- Test Open Preset Packs Folder.
- Test Nether/End behavior if the release changed dimension visuals.
- Test Biome Atmospheres if automation changed.

## 3. Commit And Push

```bash
git add .
git commit -m "chore(release): prepare vX.Y.Z"
git push
```

Do not create a local tag for the recommended flow.

## 4. Recommended GitHub Actions Release Flow

1. Open GitHub.
2. Go to Actions.
3. Choose Create Release.
4. Click Run workflow.
5. Enter the version, for example `0.4.0-beta.2`.
6. Optionally enter release notes.
7. Run the workflow.

The workflow validates the version, checks `gradle.properties`, verifies the tag and release do not already exist, builds with Java 21, creates tag `vX.Y.Z`, creates the GitHub release, and uploads `build/libs/atmosphereplus-X.Y.Z.jar`.

Alpha, beta, and rc versions are marked as pre-release automatically.

## 5. Manual Tag Fallback

Use this only if the manual Create Release workflow is unavailable.

```bash
git tag vX.Y.Z
git push origin vX.Y.Z
```

The tag workflow builds the jar and creates a GitHub release when the tag version matches `mod_version` in `gradle.properties`. It checks for an existing release first to avoid duplicate release creation.

## 6. Confirm Release

- Confirm GitHub Actions built successfully.
- Confirm the release uploaded the normal mod jar, not the sources jar.
- Confirm the release is marked pre-release for alpha, beta, or rc versions.
