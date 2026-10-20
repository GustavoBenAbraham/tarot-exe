$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$projectRoot = Split-Path $PSScriptRoot -Parent
$frames = [System.Collections.Generic.List[byte[]]]::new()
$sizes = @(16, 32, 48, 256)
foreach ($size in $sizes) {
    $bitmap = New-Object System.Drawing.Bitmap $size, $size
    $graphics = [Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.Clear([Drawing.Color]::Transparent)
    $graphics.ScaleTransform(($size / 256.0), ($size / 256.0))
    $ink = New-Object Drawing.SolidBrush ([Drawing.ColorTranslator]::FromHtml('#203649'))
    $paper = New-Object Drawing.SolidBrush ([Drawing.ColorTranslator]::FromHtml('#f1e6cc'))
    $wine = New-Object Drawing.SolidBrush ([Drawing.ColorTranslator]::FromHtml('#643e46'))
    $gold = New-Object Drawing.SolidBrush ([Drawing.ColorTranslator]::FromHtml('#d9b66e'))
    $outline = New-Object Drawing.Pen ([Drawing.ColorTranslator]::FromHtml('#d9b66e')), 5
    $graphics.FillRectangle($wine, 28, 42, 162, 196)
    $graphics.DrawRectangle($outline, 28, 42, 162, 196)
    $graphics.FillRectangle($paper, 57, 20, 174, 208)
    $graphics.FillRectangle($ink, 68, 31, 152, 186)
    $graphics.DrawRectangle($outline, 68, 31, 152, 186)
    $graphics.FillEllipse($paper, 88, 51, 62, 62)
    $graphics.FillEllipse($ink, 107, 42, 62, 62)
    $points = [Drawing.PointF[]]::new(8)
    for ($point = 0; $point -lt 8; $point++) {
        $radius = if ($point % 2 -eq 0) { 42 } else { 13 }
        $angle = -[Math]::PI / 2 + $point * [Math]::PI / 4
        $points[$point] = New-Object Drawing.PointF (154 + [Math]::Cos($angle) * $radius), (145 + [Math]::Sin($angle) * $radius)
    }
    $graphics.FillPolygon($gold, $points)
    $graphics.DrawLine($outline, 91, 199, 195, 199)
    $memory = New-Object IO.MemoryStream
    $bitmap.Save($memory, [Drawing.Imaging.ImageFormat]::Png)
    $frames.Add($memory.ToArray())
    if ($size -eq 256) { [IO.File]::WriteAllBytes((Join-Path $projectRoot 'src/main/resources/app-icon.png'), $memory.ToArray()) }
    $memory.Dispose(); $outline.Dispose(); $ink.Dispose(); $paper.Dispose(); $wine.Dispose(); $gold.Dispose(); $graphics.Dispose(); $bitmap.Dispose()
}
$iconPath = Join-Path $PSScriptRoot 'app.ico'
$stream = [IO.File]::Create($iconPath)
$writer = New-Object IO.BinaryWriter $stream
try {
    $writer.Write([uint16]0); $writer.Write([uint16]1); $writer.Write([uint16]$sizes.Count)
    $offset = 6 + 16 * $sizes.Count
    for ($index = 0; $index -lt $sizes.Count; $index++) {
        $dimension = if ($sizes[$index] -eq 256) { 0 } else { $sizes[$index] }
        $writer.Write([byte]$dimension); $writer.Write([byte]$dimension); $writer.Write([byte]0); $writer.Write([byte]0)
        $writer.Write([uint16]1); $writer.Write([uint16]32); $writer.Write([uint32]$frames[$index].Length); $writer.Write([uint32]$offset)
        $offset += $frames[$index].Length
    }
    foreach ($frame in $frames) { $writer.Write([byte[]]$frame) }
} finally { $writer.Dispose(); $stream.Dispose() }
Write-Output "Icon created: $iconPath"
