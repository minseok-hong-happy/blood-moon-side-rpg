[CmdletBinding()]
param(
    [string]$JdkHome = "$env:LOCALAPPDATA\BloodMoonNightfall\toolchain\jdk-17",
    [string]$KeystorePath = "$env:USERPROFILE\.android\bloodmoon-nightfall-update.keystore",
    [string]$GodotExe = "$env:LOCALAPPDATA\BloodMoonNightfall\toolchain\godot-4.7.1\Godot_v4.7.1-stable_win64.exe"
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$safeBase = Join-Path $env:LOCALAPPDATA 'BloodMoonNightfall'
$stageRoot = Join-Path $safeBase 'release-stage'
$appBuildGradle = Join-Path $projectRoot 'app\build.gradle'
$versionMatch = [Regex]::Match(
    [IO.File]::ReadAllText($appBuildGradle),
    '(?m)^\s*versionName\s+"([^"]+)"\s*$')
if (-not $versionMatch.Success) {
    throw "Could not read versionName from $appBuildGradle"
}
$versionName = $versionMatch.Groups[1].Value
$apkFileName = "VAYLORN-v$versionName.apk"
$expectedSignerSha256 = '41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D'

if (-not $JdkHome -or -not (Test-Path -LiteralPath (Join-Path $JdkHome 'bin\java.exe'))) {
    throw 'Java 17 or newer was not found. Supply -JdkHome with a valid JDK path.'
}
if (-not (Test-Path -LiteralPath $KeystorePath)) {
    throw "The update-compatible signing keystore was not found: $KeystorePath"
}
if (-not (Test-Path -LiteralPath $GodotExe)) {
    throw "Godot 4.7.1 was not found. Supply -GodotExe with the official editor executable."
}

function Invoke-GodotAndWait {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$GodotArguments,
        [Parameter(Mandatory = $true)]
        [string]$FailureMessage
    )

    $escapedArguments = @($GodotArguments | ForEach-Object {
        if ($_ -match '[\s"]') {
            '"' + $_.Replace('"', '\"') + '"'
        } else {
            $_
        }
    })
    $process = Start-Process -FilePath $GodotExe `
        -ArgumentList $escapedArguments `
        -NoNewWindow `
        -Wait `
        -PassThru
    if ($process.ExitCode -ne 0) {
        throw "$FailureMessage`: exit $($process.ExitCode)"
    }
}

if (Test-Path -LiteralPath $stageRoot) {
    $resolvedStage = (Resolve-Path -LiteralPath $stageRoot).Path
    $resolvedBase = (Resolve-Path -LiteralPath $safeBase).Path
    if (-not $resolvedStage.StartsWith(
            $resolvedBase + [IO.Path]::DirectorySeparatorChar,
            [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to remove a staging path outside the safe build directory: $resolvedStage"
    }
    Remove-Item -LiteralPath $stageRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $stageRoot -Force | Out-Null

& robocopy $projectRoot $stageRoot /E /XD .git .gradle build dist tmp .godot /NFL /NDL /NJH /NJS /NP
if ($LASTEXITCODE -gt 7) {
    throw "Failed to copy the release staging tree: robocopy exit $LASTEXITCODE"
}

$env:JAVA_HOME = (Resolve-Path -LiteralPath $JdkHome).Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$previousErrorAction = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
$javaVersion = (& (Join-Path $env:JAVA_HOME 'bin\java.exe') -version 2>&1 | Out-String)
$javaExitCode = $LASTEXITCODE
$ErrorActionPreference = $previousErrorAction
if ($javaExitCode -ne 0) {
    throw "Java failed to start: exit $javaExitCode"
}
if ($javaVersion -notmatch 'version "(17|18|19|2[0-9])') {
    throw "Java 17 or newer is required. Current runtime: $javaVersion"
}

$env:ANDROID_KEYSTORE_FILE = (Resolve-Path -LiteralPath $KeystorePath).Path
$env:ANDROID_KEYSTORE_PASSWORD = 'android'
$env:ANDROID_KEY_ALIAS = 'androiddebugkey'
$env:ANDROID_KEY_PASSWORD = 'android'

$godotProject = Join-Path $stageRoot 'app\src\main\assets'
Invoke-GodotAndWait @('--headless', '--editor', '--path', $godotProject, '--quit') `
    'Godot asset import failed'

$importedCache = Join-Path $godotProject '.godot\imported'
$importedResources = @(Get-ChildItem -LiteralPath $importedCache -File -ErrorAction SilentlyContinue)
$importRemaps = @(Get-ChildItem -LiteralPath (Join-Path $godotProject 'art') `
    -File -Filter '*.import' -ErrorAction SilentlyContinue)
if ($importedResources.Count -lt 20 -or $importRemaps.Count -lt 10) {
    throw "Godot import did not produce the required runtime cache/remaps. Cache: $($importedResources.Count), remaps: $($importRemaps.Count)"
}

Invoke-GodotAndWait @('--headless', '--path', $godotProject,
    '--script', 'res://tests/run_tests.gd') 'Godot gameplay tests failed'
Invoke-GodotAndWait @('--headless', '--path', $godotProject, '--fixed-fps', '60',
    '--script', 'res://tests/integration_test.gd') 'Godot integration simulation failed'

# Godot resolves source media through the generated .import remap files. Shipping both the
# original PNG/WAV/TTF and the imported mobile resource would duplicate tens of megabytes.
$sourceMedia = @(
    Get-ChildItem -LiteralPath (Join-Path $godotProject 'art') -File -Filter '*.png'
    Get-ChildItem -LiteralPath (Join-Path $godotProject 'audio') -File -Filter '*.wav'
    Get-ChildItem -LiteralPath (Join-Path $godotProject 'fonts') -File -Filter '*.ttf'
)
$resolvedGodotProject = (Resolve-Path -LiteralPath $godotProject).Path
foreach ($mediaFile in $sourceMedia) {
    if (-not $mediaFile.FullName.StartsWith(
            $resolvedGodotProject + [IO.Path]::DirectorySeparatorChar,
            [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to remove staged media outside the Godot project: $($mediaFile.FullName)"
    }
    Remove-Item -LiteralPath $mediaFile.FullName -Force
}
$editorCache = Join-Path $godotProject '.godot\editor'
if (Test-Path -LiteralPath $editorCache) {
    $resolvedEditorCache = (Resolve-Path -LiteralPath $editorCache).Path
    if (-not $resolvedEditorCache.StartsWith(
            $resolvedGodotProject + [IO.Path]::DirectorySeparatorChar,
            [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to remove an editor cache outside the Godot project: $resolvedEditorCache"
    }
    Remove-Item -LiteralPath $resolvedEditorCache -Recurse -Force
}
$testAssets = Join-Path $godotProject 'tests'
if (Test-Path -LiteralPath $testAssets) {
    $resolvedTestAssets = (Resolve-Path -LiteralPath $testAssets).Path
    if (-not $resolvedTestAssets.StartsWith(
            $resolvedGodotProject + [IO.Path]::DirectorySeparatorChar,
            [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to remove staged tests outside the Godot project: $resolvedTestAssets"
    }
    Remove-Item -LiteralPath $resolvedTestAssets -Recurse -Force
}

Push-Location $stageRoot
try {
    & .\gradlew.bat clean testDebugUnitTest lintRelease assembleRelease --no-build-cache
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle verification or release build failed: exit $LASTEXITCODE"
    }
} finally {
    Pop-Location
}

$builtApk = Join-Path $stageRoot 'app\build\outputs\apk\release\app-release.apk'
if (-not (Test-Path -LiteralPath $builtApk)) {
    throw 'Gradle completed but the release APK was not produced.'
}

$jarTool = Join-Path $env:JAVA_HOME 'bin\jar.exe'
$apkEntries = @(& $jarTool tf $builtApk)
if ($LASTEXITCODE -ne 0) {
    throw 'Could not inspect the generated APK contents.'
}
$hasProject = @($apkEntries | Where-Object { $_ -eq 'assets/project.godot' }).Count -eq 1
$hasMainScript = @($apkEntries | Where-Object { $_ -eq 'assets/scripts/main.gd' }).Count -eq 1
$cacheEntryCount = @($apkEntries | Where-Object {
    $_ -match '^assets/\.godot/imported/.+\.(ctex|sample|fontdata)$'
}).Count
$remapEntryCount = @($apkEntries | Where-Object { $_ -match '^assets/.+\.import$' }).Count
$rawMediaCount = @($apkEntries | Where-Object {
    $_ -match '^assets/(art/.+\.png|audio/.+\.wav|fonts/.+\.ttf)$'
}).Count
if (-not $hasProject -or -not $hasMainScript -or $cacheEntryCount -lt 20 `
        -or $remapEntryCount -lt 20 -or $rawMediaCount -ne 0) {
    throw "APK asset gate failed. project=$hasProject, main=$hasMainScript, cache=$cacheEntryCount, remaps=$remapEntryCount, raw=$rawMediaCount"
}

$localProperties = Join-Path $stageRoot 'local.properties'
if (-not (Test-Path -LiteralPath $localProperties)) {
    throw 'local.properties is required to locate the Android SDK for signature verification.'
}
$sdkLine = Get-Content -LiteralPath $localProperties |
    Where-Object { $_ -match '^sdk\.dir=' } |
    Select-Object -First 1
if (-not $sdkLine) {
    throw 'sdk.dir is missing from local.properties.'
}
$sdkRoot = ($sdkLine -replace '^sdk\.dir=', '') -replace '\\\\', '\'
$buildToolsRoot = Join-Path $sdkRoot 'build-tools'
$buildTools = Get-ChildItem -LiteralPath $buildToolsRoot -Directory |
    Sort-Object { [version]$_.Name } -Descending |
    Select-Object -First 1
if (-not $buildTools) {
    throw "Android build-tools were not found under: $buildToolsRoot"
}
$apkSigner = Join-Path $buildTools.FullName 'apksigner.bat'
if (-not (Test-Path -LiteralPath $apkSigner)) {
    throw "apksigner was not found: $apkSigner"
}
$signatureReport = (& $apkSigner verify --verbose --print-certs $builtApk 2>&1 | Out-String)
if ($LASTEXITCODE -ne 0) {
    throw "APK signature verification failed: $signatureReport"
}
$digestMatch = [Regex]::Match($signatureReport,
    'certificate SHA-256 digest:\s*([A-Fa-f0-9:]+)')
if (-not $digestMatch.Success) {
    throw "The signer SHA-256 digest was not present in the verification report: $signatureReport"
}
$actualSignerSha256 = $digestMatch.Groups[1].Value.Replace(':', '').ToUpperInvariant()
if ($actualSignerSha256 -ne $expectedSignerSha256) {
    throw "Unexpected APK signer. Expected $expectedSignerSha256 but found $actualSignerSha256"
}

$dist = Join-Path $projectRoot 'dist'
New-Item -ItemType Directory -Path $dist -Force | Out-Null
$destination = Join-Path $dist $apkFileName
Copy-Item -LiteralPath $builtApk -Destination $destination -Force

$mapping = Join-Path $stageRoot 'app\build\outputs\mapping\release\mapping.txt'
if (Test-Path -LiteralPath $mapping) {
    Copy-Item -LiteralPath $mapping -Destination (Join-Path $dist "mapping-v$versionName.txt") -Force
}

$hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $destination).Hash
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$checksumPath = Join-Path $dist 'SHA256.txt'
$checksumLines = @()
if (Test-Path -LiteralPath $checksumPath) {
    $checksumLines = @(Get-Content -LiteralPath $checksumPath | Where-Object {
        $_ -notmatch "\s+$([Regex]::Escape($apkFileName))$"
    })
}
$checksumLines += "$hash  $apkFileName"
[IO.File]::WriteAllLines($checksumPath, $checksumLines, $utf8NoBom)

$releaseMetadataPaths = @(
    (Join-Path $dist "RELEASE_NOTES-v$versionName.md"),
    (Join-Path $dist "QA-v$versionName.md")
)
foreach ($metadataPath in $releaseMetadataPaths) {
    if (Test-Path -LiteralPath $metadataPath) {
        $metadata = [IO.File]::ReadAllText($metadataPath)
        $metadata = [Regex]::Replace(
            $metadata,
            '(?m)^- (?:APK )?SHA-256: `[A-Fa-f0-9]{64}`$',
            "- APK SHA-256: ``$hash``")
        [IO.File]::WriteAllText($metadataPath, $metadata, $utf8NoBom)
    }
}

$desktopPath = [Environment]::GetFolderPath('Desktop')
$desktopDestination = $null
if ($desktopPath -and (Test-Path -LiteralPath $desktopPath)) {
    $desktopDestination = Join-Path $desktopPath $apkFileName
    Copy-Item -LiteralPath $destination -Destination $desktopDestination -Force
}

Write-Host "APK: $destination"
if ($desktopDestination) {
    Write-Host "Desktop copy: $desktopDestination"
}
Write-Host "Signer SHA-256: $actualSignerSha256"
Write-Host "File SHA-256: $hash"
