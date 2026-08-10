[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$ApkPath,
    [string]$AdbPath = "$env:LOCALAPPDATA\BloodMoonNightfall\toolchain\android-sdk\platform-tools\adb.exe",
    [string]$EmulatorPath = "$env:LOCALAPPDATA\BloodMoonNightfall\toolchain\android-emulator-27.3.10\emulator\emulator.exe",
    [string]$AndroidSdkRoot = "$env:LOCALAPPDATA\BloodMoonNightfall\toolchain\android-sdk",
    [string]$AvdHome = "$env:LOCALAPPDATA\BloodMoonNightfall\runtime-smoke\avd",
    [string]$AvdName = "Vaylorn_X64_API24",
    [string]$RequiredAbi = "x86_64",
    [string]$EntropySeederPath = "$env:LOCALAPPDATA\BloodMoonNightfall\toolchain\android-entropy-seed-x86_64",
    [int]$BootTimeoutSeconds = 420,
    [int]$ReadyTimeoutSeconds = 600,
    [string]$ArtifactDirectory = ""
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$resolvedApk = (Resolve-Path -LiteralPath $ApkPath).Path
if (-not (Test-Path -LiteralPath $AdbPath)) {
    throw "Android runtime gate requires adb: $AdbPath"
}
if (-not $ArtifactDirectory) {
    $ArtifactDirectory = Join-Path (Split-Path -Parent $resolvedApk) 'runtime-smoke'
}
New-Item -ItemType Directory -Path $ArtifactDirectory -Force | Out-Null
$resolvedArtifacts = (Resolve-Path -LiteralPath $ArtifactDirectory).Path

function Invoke-Adb {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments,
        [switch]$AllowFailure
    )
    # Windows PowerShell promotes native stderr to ErrorRecord. adb writes benign connection
    # messages there, so capture it without letting ErrorActionPreference abort the gate.
    $previousErrorAction = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $output = @(& $AdbPath @Arguments 2>&1)
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
    }
    if (-not $AllowFailure -and $exitCode -ne 0) {
        throw "adb $($Arguments -join ' ') failed with exit $exitCode`n$($output -join [Environment]::NewLine)"
    }
    return $output
}

function Get-OnlineSerials {
    $lines = @(Invoke-Adb -Arguments @('devices'))
    $serials = @()
    foreach ($line in $lines) {
        if ($line -match '^([^\s]+)\s+device$') {
            $serials += $Matches[1]
        }
    }
    return $serials
}

function Get-DeviceAbi {
    param([Parameter(Mandatory = $true)][string]$Serial)
    $result = @(Invoke-Adb -Arguments @('-s', $Serial, 'shell', 'getprop',
        'ro.product.cpu.abi') -AllowFailure)
    return ($result -join '').Trim()
}

function Find-CompatibleSerial {
    foreach ($candidate in @(Get-OnlineSerials)) {
        if ((Get-DeviceAbi -Serial $candidate) -eq $RequiredAbi) {
            return $candidate
        }
    }
    return $null
}

function Write-Utf8Log {
    param([string]$Path, [object[]]$Lines)
    $utf8NoBom = New-Object Text.UTF8Encoding($false)
    [IO.File]::WriteAllLines($Path, @($Lines | ForEach-Object { [string]$_ }), $utf8NoBom)
}

$startedEmulator = $false
$serial = Find-CompatibleSerial
if (-not $serial) {
    if (-not (Test-Path -LiteralPath $EmulatorPath)) {
        throw "No compatible Android device is online and the verified emulator is missing: $EmulatorPath"
    }
    $avdDirectory = Join-Path $AvdHome "$AvdName.avd"
    if (-not (Test-Path -LiteralPath $avdDirectory)) {
        throw "No Android device is online and the runtime-smoke AVD is missing: $avdDirectory"
    }
    $env:ANDROID_HOME = $AndroidSdkRoot
    $env:ANDROID_SDK_ROOT = $AndroidSdkRoot
    $env:ANDROID_AVD_HOME = $AvdHome
    $arguments = @(
        '-avd', $AvdName, '-engine', 'qemu2', '-accel', 'off',
        '-feature', 'GLESDynamicVersion', '-no-window', '-no-audio',
        '-no-boot-anim', '-gpu', 'angle_indirect', '-no-snapshot',
        '-no-snapshot-save', '-memory', '1024', '-cores', '2'
    )
    Start-Process -FilePath $EmulatorPath -ArgumentList $arguments -WindowStyle Hidden | Out-Null
    $startedEmulator = $true
}

try {
    $deadline = [DateTime]::UtcNow.AddSeconds($BootTimeoutSeconds)
    while (-not $serial -and [DateTime]::UtcNow -lt $deadline) {
        Start-Sleep -Seconds 2
        $serial = Find-CompatibleSerial
    }
    if (-not $serial) {
        throw "Android device did not become online within $BootTimeoutSeconds seconds."
    }

    $packageManagerReady = $false
    while (-not $packageManagerReady -and [DateTime]::UtcNow -lt $deadline) {
        $pm = @(Invoke-Adb -Arguments @('-s', $serial, 'shell', 'pm', 'path', 'android') `
            -AllowFailure)
        $packageManagerReady = ($pm -join "`n") -match 'package:'
        if (-not $packageManagerReady) {
            Start-Sleep -Seconds 2
        }
    }
    if (-not $packageManagerReady) {
        throw 'Android package manager did not become ready before the boot deadline.'
    }

    $deviceAbi = Get-DeviceAbi -Serial $serial
    if ($deviceAbi -ne $RequiredAbi) {
        throw "Runtime gate requires ABI $RequiredAbi but device $serial reports $deviceAbi."
    }

    $isEmulator = $serial -like 'emulator-*'
    if ($isEmulator) {
        $surfaceReport = @(Invoke-Adb -Arguments @('-s', $serial, 'shell', 'dumpsys',
            'SurfaceFlinger') -AllowFailure)
        if (($surfaceReport -join "`n") -notmatch 'GLES:.*OpenGL ES 3\.') {
            throw 'Runtime gate emulator does not expose the OpenGL ES 3 context required by Godot 4.'
        }
        if (-not (Test-Path -LiteralPath $EntropySeederPath)) {
            throw "The deterministic emulator entropy seeder is missing: $EntropySeederPath"
        }
        Invoke-Adb -Arguments @('-s', $serial, 'root') -AllowFailure | Out-Null
        $rootDeadline = [DateTime]::UtcNow.AddSeconds(60)
        while ([DateTime]::UtcNow -lt $rootDeadline) {
            Start-Sleep -Seconds 1
            if ($serial -in @(Get-OnlineSerials)) {
                break
            }
        }
        if ($serial -notin @(Get-OnlineSerials)) {
            throw 'Emulator did not reconnect after enabling root for entropy setup.'
        }
        Invoke-Adb -Arguments @('-s', $serial, 'push', $EntropySeederPath,
            '/data/local/tmp/vaylorn-entropy-seed') | Out-Null
        Invoke-Adb -Arguments @('-s', $serial, 'shell', 'chmod', '700',
            '/data/local/tmp/vaylorn-entropy-seed') | Out-Null
        $seedResult = @(Invoke-Adb -Arguments @('-s', $serial, 'shell',
            '/data/local/tmp/vaylorn-entropy-seed'))
        if (($seedResult -join "`n") -notmatch 'ANDROID_EMULATOR_ENTROPY_READY') {
            throw "Emulator entropy setup failed.`n$($seedResult -join [Environment]::NewLine)"
        }
    }

    Write-Host "Runtime gate device: $serial"
    Invoke-Adb -Arguments @('-s', $serial, 'install', '-r', $resolvedApk) | Write-Host
    Invoke-Adb -Arguments @('-s', $serial, 'logcat', '-c') | Out-Null
    Invoke-Adb -Arguments @('-s', $serial, 'shell', 'am', 'force-stop',
        'com.example.bloodmoonnightfall') | Out-Null
    $startOutput = @(Invoke-Adb -Arguments @('-s', $serial, 'shell', 'am', 'start', '-W',
        '-n', 'com.example.bloodmoonnightfall/.MainActivity'))
    Write-Utf8Log -Path (Join-Path $resolvedArtifacts 'activity-start.txt') -Lines $startOutput

    $ready = $false
    $readyDeadline = [DateTime]::UtcNow.AddSeconds($ReadyTimeoutSeconds)
    $nextEntropySeed = [DateTime]::UtcNow
    while ([DateTime]::UtcNow -lt $readyDeadline) {
        $briefLog = @(Invoke-Adb -Arguments @('-s', $serial, 'logcat', '-d', '-v', 'brief',
            'VaylornBoot:I', 'Godot:V', 'AndroidRuntime:E', 'libc:F', 'DEBUG:F', '*:S') `
            -AllowFailure)
        if (($briefLog -join "`n") -match
                'VaylornBoot.*GAME_READY|VAYLORN_GAME_READY') {
            $ready = $true
            break
        }
        $gamePid = @(Invoke-Adb -Arguments @('-s', $serial, 'shell', 'pidof',
            'com.example.bloodmoonnightfall') -AllowFailure)
        if (-not ($gamePid -join '').Trim()) {
            break
        }
        if ($isEmulator -and [DateTime]::UtcNow -ge $nextEntropySeed) {
            Invoke-Adb -Arguments @('-s', $serial, 'shell',
                '/data/local/tmp/vaylorn-entropy-seed') -AllowFailure | Out-Null
            $nextEntropySeed = [DateTime]::UtcNow.AddSeconds(20)
        }
        Start-Sleep -Seconds 2
    }

    $fullLog = @(Invoke-Adb -Arguments @('-s', $serial, 'logcat', '-d', '-v', 'threadtime') `
        -AllowFailure)
    Write-Utf8Log -Path (Join-Path $resolvedArtifacts 'logcat.txt') -Lines $fullLog
    if (-not $ready) {
        $fatal = @($fullLog | Where-Object {
            $_ -match 'VaylornBoot|AndroidRuntime|FATAL EXCEPTION|Fatal signal|JNI DETECTED ERROR|bloodmoonnightfall|Godot'
        })
        throw "Game scene never reported GAME_READY.`n$($fatal -join [Environment]::NewLine)"
    }

    Start-Sleep -Seconds 15
    $pidAfter = @(Invoke-Adb -Arguments @('-s', $serial, 'shell', 'pidof',
        'com.example.bloodmoonnightfall') -AllowFailure)
    if (-not ($pidAfter -join '').Trim()) {
        throw 'Game process died after reporting GAME_READY.'
    }

    $postLog = @(Invoke-Adb -Arguments @('-s', $serial, 'logcat', '-d', '-v', 'threadtime') `
        -AllowFailure)
    Write-Utf8Log -Path (Join-Path $resolvedArtifacts 'logcat.txt') -Lines $postLog
    if (($postLog -join "`n") -match
            'Process: com\.example\.bloodmoonnightfall|>>> com\.example\.bloodmoonnightfall <<<|JNI DETECTED ERROR') {
        throw 'Java/JNI/native crash was recorded for the game process.'
    }

    Write-Host 'ANDROID_RUNTIME_SMOKE_PASS'
} finally {
    if ($startedEmulator -and $serial) {
        Invoke-Adb -Arguments @('-s', $serial, 'emu', 'kill') -AllowFailure | Out-Null
    }
}
