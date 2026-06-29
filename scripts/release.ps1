param(
    [Parameter(Mandatory = $true)]
    [string] $Version
)

$ErrorActionPreference = "Stop"

Write-Host "Atmosphere+ release preparation: v$Version"
Write-Host ""
Write-Host "Before tagging, make sure this version is updated in:"
Write-Host "- gradle.properties"
Write-Host "- src/main/resources/fabric.mod.json"
Write-Host "- src/main/java/com/skrra/atmosphereplus/client/AtmospherePlusClient.java"
Write-Host "- CHANGELOG.md"
Write-Host "- README.md if release notes changed"
Write-Host ""

$versionLine = Select-String -Path "gradle.properties" -Pattern "^mod_version=" | Select-Object -First 1
if ($versionLine) {
    $currentVersion = $versionLine.Line.Substring("mod_version=".Length)
    if ($currentVersion -ne $Version) {
        Write-Warning "gradle.properties currently has mod_version=$currentVersion, not $Version."
    }
}

Write-Host "Running local build..."
& ".\gradlew.bat" clean build
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Build passed."
Write-Host ""
Write-Host "Suggested next commands:"
Write-Host "git add ."
Write-Host "git commit -m `"chore(release): prepare v$Version`""
Write-Host "git tag v$Version"
Write-Host "git push"
Write-Host "git push origin v$Version"
Write-Host ""
Write-Host "This helper does not tag, push, or create a release automatically."
