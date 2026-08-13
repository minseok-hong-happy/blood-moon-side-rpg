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
	[string]$PortraitDisplaySize = "360x780",
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

function Dismiss-SystemWaitDialog {
    param([Parameter(Mandatory = $true)][string]$Serial)
    $remoteHierarchy = '/data/local/tmp/vaylorn-window.xml'
    Invoke-Adb -Arguments @('-s', $Serial, 'shell', 'uiautomator', 'dump',
        $remoteHierarchy) -AllowFailure | Out-Null
    $hierarchy = @(Invoke-Adb -Arguments @('-s', $Serial, 'shell', 'cat',
        $remoteHierarchy) -AllowFailure) -join "`n"
    foreach ($nodeMatch in [Regex]::Matches($hierarchy, '<node[^>]+>')) {
        $node = $nodeMatch.Value
        if ($node -notmatch 'text="(?:Wait|GOT IT)"') {
            continue
        }
        $bounds = [Regex]::Match($node,
            'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
        if (-not $bounds.Success) {
            continue
        }
        $tapX = [int](([int]$bounds.Groups[1].Value + [int]$bounds.Groups[3].Value) / 2)
        $tapY = [int](([int]$bounds.Groups[2].Value + [int]$bounds.Groups[4].Value) / 2)
        Invoke-Adb -Arguments @('-s', $Serial, 'shell', 'input', 'tap',
            [string]$tapX, [string]$tapY) | Out-Null
        Start-Sleep -Seconds 3
        return $true
    }
    return $false
}

$startedEmulator = $false
$displayOverridden = $false
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
        '-no-snapshot-save', '-skin', $PortraitDisplaySize,
        '-memory', '2048', '-cores', '2'
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
		# Use a modern tall portrait aspect. This catches the black-bottom regression that
		# a legacy 16:9 emulator cannot expose.
		Invoke-Adb -Arguments @('-s', $serial, 'shell', 'wm', 'size', $PortraitDisplaySize) | Out-Null
		$displayOverridden = $true
	}

	Write-Host "Runtime gate device: $serial"
	# Software-only API 24 boot occasionally reports a system-process ANR while it finishes
	# background setup. Hide OS diagnostic dialogs so they cannot cover the game screenshot;
	# the game process is still checked independently below for every fatal/ANR signature.
	Invoke-Adb -Arguments @('-s', $serial, 'shell', 'settings', 'put', 'global',
		'hide_error_dialogs', '1') -AllowFailure | Out-Null
	Invoke-Adb -Arguments @('-s', $serial, 'shell', 'settings', 'put', 'secure',
		'immersive_mode_confirmations', 'confirmed') -AllowFailure | Out-Null
	Invoke-Adb -Arguments @('-s', $serial, 'install', '-r', $resolvedApk) | Write-Host
	# A deterministic fresh profile prevents an old offline-reward modal from dimming the
	# visual artifact and makes startup behavior independent of earlier QA runs.
	Invoke-Adb -Arguments @('-s', $serial, 'shell', 'pm', 'clear',
		'com.example.bloodmoonnightfall') | Out-Null
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
	$layoutMatch = [Regex]::Match(($postLog -join "`n"),
		'VAYLORN_UI_LAYOUT_READY height=([0-9.]+) bottom=([0-9.]+) ground=([0-9.]+)')
	if (-not $layoutMatch.Success) {
		throw 'Tall-screen layout marker was not reported by the game scene.'
	}
	$layoutHeight = [double]$layoutMatch.Groups[1].Value
	$layoutBottom = [double]$layoutMatch.Groups[2].Value
	$layoutGround = [double]$layoutMatch.Groups[3].Value
	if ($layoutHeight -lt 1500.0 -or
			[Math]::Abs(($layoutBottom + 354.0) - $layoutHeight) -gt 1.0 -or
			$layoutGround -ge $layoutBottom) {
		throw "Tall-screen layout contract failed: height=$layoutHeight bottom=$layoutBottom ground=$layoutGround"
	}
	for ($dialogAttempt = 0; $dialogAttempt -lt 3; $dialogAttempt++) {
		if (-not (Dismiss-SystemWaitDialog -Serial $serial)) {
			break
		}
	}

	$remoteScreenshot = '/data/local/tmp/vaylorn-runtime-smoke.png'
	Invoke-Adb -Arguments @('-s', $serial, 'shell', 'screencap', '-p', $remoteScreenshot) | Out-Null
	Invoke-Adb -Arguments @('-s', $serial, 'pull', $remoteScreenshot,
		(Join-Path $resolvedArtifacts 'first-frame.png')) | Out-Null
	Invoke-Adb -Arguments @('-s', $serial, 'shell', 'rm', $remoteScreenshot) -AllowFailure | Out-Null
	$screenshotPath = Join-Path $resolvedArtifacts 'first-frame.png'
	if (-not (Test-Path -LiteralPath $screenshotPath) -or
			(Get-Item -LiteralPath $screenshotPath).Length -lt 10000) {
		throw 'Runtime gate could not capture a valid tall-screen frame.'
	}
	Add-Type -AssemblyName System.Drawing
	$bitmap = [Drawing.Bitmap]::new($screenshotPath)
	try {
		if (($bitmap.Height / [double]$bitmap.Width) -lt 1.9) {
			throw "Runtime screenshot is not tall portrait: $($bitmap.Width)x$($bitmap.Height)"
		}
		# The marker above proves the logical 720x1560 layout; these pixels prove the
		# physical bottom edge is visibly painted rather than left as an empty black band.
		$sampleCount = 0
		$visibleSampleCount = 0
		$bottomStart = [Math]::Floor($bitmap.Height * 0.84)
		for ($y = $bottomStart; $y -lt $bitmap.Height; $y += 4) {
			for ($x = 0; $x -lt $bitmap.Width; $x += 4) {
				$pixel = $bitmap.GetPixel($x, $y)
				$sampleCount++
				if ([Math]::Max($pixel.R, [Math]::Max($pixel.G, $pixel.B)) -ge 22) {
					$visibleSampleCount++
				}
			}
		}
		if ($sampleCount -eq 0 -or ($visibleSampleCount / [double]$sampleCount) -lt 0.03) {
			throw 'Tall-screen bottom-area visual gate detected an empty black band.'
		}
	} finally {
		$bitmap.Dispose()
	}

	Write-Host 'ANDROID_RUNTIME_SMOKE_PASS'
} finally {
	if ($displayOverridden -and $serial) {
		Invoke-Adb -Arguments @('-s', $serial, 'shell', 'wm', 'size', 'reset') -AllowFailure | Out-Null
	}
	if ($startedEmulator -and $serial) {
        Invoke-Adb -Arguments @('-s', $serial, 'emu', 'kill') -AllowFailure | Out-Null
    }
}
