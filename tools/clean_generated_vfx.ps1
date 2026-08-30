[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Source,
    [Parameter(Mandatory = $true)][string]$Destination,
    [int]$PreserveCenterRadius = 0
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
Add-Type -AssemblyName System.Drawing

$sourceBitmap = [System.Drawing.Bitmap]::new((Resolve-Path -LiteralPath $Source).Path)
try {
    $width = $sourceBitmap.Width
    $height = $sourceBitmap.Height
    $pixelCount = $width * $height
    $pixels = New-Object 'System.Drawing.Color[]' $pixelCount
    $neutral = New-Object 'System.Boolean[]' $pixelCount

    # Generated concept art can arrive with a visible checkerboard despite a
    # transparency request. Cache RGB values once, then flood-fill only the
    # near-neutral, bright component connected to the image border. This keeps
    # the white-hot core and antialiased highlight pixels inside the effect.
    for ($y = 0; $y -lt $height; $y++) {
        for ($x = 0; $x -lt $width; $x++) {
            $index = $y * $width + $x
            $pixel = $sourceBitmap.GetPixel($x, $y)
            $pixels[$index] = $pixel
            $maximum = [Math]::Max($pixel.R, [Math]::Max($pixel.G, $pixel.B))
            $minimum = [Math]::Min($pixel.R, [Math]::Min($pixel.G, $pixel.B))
            $neutral[$index] = ($minimum -ge 220 -and ($maximum - $minimum) -le 22)
        }
    }

    $background = New-Object 'System.Boolean[]' $pixelCount
    $queue = New-Object 'System.Int32[]' $pixelCount
    $head = 0
    $tail = 0
    $enqueue = {
        param([int]$index)
        if ($index -ge 0 -and $index -lt $pixelCount -and $neutral[$index] -and -not $background[$index]) {
            $background[$index] = $true
            $queue[$tail] = $index
            $script:tail++
        }
    }
    for ($x = 0; $x -lt $width; $x++) {
        & $enqueue $x
        & $enqueue (($height - 1) * $width + $x)
    }
    for ($y = 1; $y -lt ($height - 1); $y++) {
        & $enqueue ($y * $width)
        & $enqueue ($y * $width + $width - 1)
    }
    while ($head -lt $tail) {
        $current = $queue[$head]
        $head++
        $x = $current % $width
        $y = [int]($current / $width)
        if ($x -gt 0) { & $enqueue ($current - 1) }
        if ($x -lt ($width - 1)) { & $enqueue ($current + 1) }
        if ($y -gt 0) { & $enqueue ($current - $width) }
        if ($y -lt ($height - 1)) { & $enqueue ($current + $width) }
    }

    $outputBitmap = [System.Drawing.Bitmap]::new(
        $width, $height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        $centerX = $width / 2.0
        $centerY = $height / 2.0
        for ($y = 0; $y -lt $height; $y++) {
            for ($x = 0; $x -lt $width; $x++) {
                $index = $y * $width + $x
                $pixel = $pixels[$index]
                $alpha = 255
                if ($background[$index]) {
                    $alpha = 0
                } elseif ($neutral[$index]) {
                    $nearArtwork = $false
                    for ($dy = -3; $dy -le 3 -and -not $nearArtwork; $dy++) {
                        for ($dx = -3; $dx -le 3; $dx++) {
                            $nx = $x + $dx
                            $ny = $y + $dy
                            if ($nx -ge 0 -and $nx -lt $width -and $ny -ge 0 -and $ny -lt $height) {
                                $neighborIndex = $ny * $width + $nx
                                if (-not $neutral[$neighborIndex] -and -not $background[$neighborIndex]) {
                                    $nearArtwork = $true
                                    break
                                }
                            }
                        }
                    }
                    $centerDistance = [Math]::Sqrt(
                        [Math]::Pow($x - $centerX, 2) + [Math]::Pow($y - $centerY, 2))
                    if (-not $nearArtwork -and $centerDistance -gt $PreserveCenterRadius) {
                        $alpha = 0
                    }
                }
                $outputBitmap.SetPixel($x, $y,
                    [System.Drawing.Color]::FromArgb($alpha, $pixel.R, $pixel.G, $pixel.B))
            }
        }
        $destinationPath = [IO.Path]::GetFullPath($Destination)
        $destinationDirectory = Split-Path -Parent $destinationPath
        New-Item -ItemType Directory -Path $destinationDirectory -Force | Out-Null
        $outputBitmap.Save($destinationPath, [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $outputBitmap.Dispose()
    }
} finally {
    $sourceBitmap.Dispose()
}

Write-Host "Cleaned transparent VFX: $Destination"
