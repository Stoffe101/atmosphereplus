# Atmosphere+ Release Checklist

Use this checklist when preparing a local release.

## 1. Update Version

Update the version in:

- `gradle.properties`
- `src/main/resources/fabric.mod.json` if needed
- `src/main/java/com/skrra/atmosphereplus/client/AtmospherePlusClient.java` while the version is still hardcoded there
- `README.md` and `CHANGELOG.md` if needed

## 2. Build Locally

```bash
./gradlew clean build
```

On Windows:

```powershell
.\gradlew.bat clean build
```

## 3. Test In Minecraft

- Open the Atmosphere+ menu.
- Apply presets.
- Test Mood Overlay.
- Test Biome Atmospheres.
- Test Nether/End behavior if the release changed dimension visuals.

## 4. Commit

```bash
git add .
git commit -m "chore(release): prepare vX.Y.Z"
```

## 5. Tag

```bash
git tag vX.Y.Z
```

## 6. Push

```bash
git push
git push origin vX.Y.Z
```

## 7. Confirm Release

- Confirm GitHub Actions built successfully.
- Confirm the tag workflow created a GitHub release.
- Confirm the release uploaded the normal mod jar, not the sources jar.
