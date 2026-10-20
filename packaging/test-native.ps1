param([string]$Executable)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
if (-not $Executable) {
    $buildInfo = Get-Content (Join-Path $projectRoot 'dist/build-info.json') -Raw | ConvertFrom-Json
    $Executable = Join-Path $buildInfo.imageFolder 'Tarot.exe'
}
$Executable = (Resolve-Path -LiteralPath $Executable).Path
$existing = @(Get-Process Tarot -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Id)
$previousPath = $env:Path
$previousJavaHome = $env:JAVA_HOME
$window = $null
try {
    $env:Path = "$env:SystemRoot\System32;$env:SystemRoot"
    Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue
    $launcher = Start-Process -FilePath $Executable -WindowStyle Hidden -PassThru
    $limit = (Get-Date).AddSeconds(35)
    do {
        Start-Sleep -Milliseconds 300
        # jpackage may fork another process with the same executable path.
        $window = Get-Process Tarot -ErrorAction SilentlyContinue | Where-Object {
            $_.Id -notin $existing -and $_.Path -eq $Executable -and $_.MainWindowTitle -eq 'Tarot.exe - Windows Retro Edition'
        } | Select-Object -First 1
    } until ($window -or (Get-Date) -gt $limit)
    if (-not $window) { throw 'A janela do executavel nativo nao abriu.' }
    $jvm = $window.Modules | Where-Object ModuleName -eq 'jvm.dll' | Select-Object -First 1 -ExpandProperty FileName
    $expectedRuntime = Join-Path (Split-Path $Executable -Parent) 'runtime'
    if (-not $jvm -or -not $jvm.StartsWith($expectedRuntime, [StringComparison]::OrdinalIgnoreCase)) {
        throw 'O aplicativo nao usou o runtime incluido.'
    }
    if (-not $window.Responding) { throw 'A janela nao esta respondendo.' }
    $report = [pscustomobject]@{ Executable = $Executable; Title = $window.MainWindowTitle; Responding = $window.Responding; JVM = $jvm; JavaHomeRemoved = $true; PathWithoutJava = $true }
    $report | ConvertTo-Json | Set-Content -Encoding UTF8 (Join-Path $projectRoot 'target/native-smoke.json')
    $report | ConvertTo-Json
} finally {
    if ($window -and -not $window.HasExited) { $window.CloseMainWindow() | Out-Null; $window.WaitForExit(10000) | Out-Null }
    $env:Path = $previousPath; $env:JAVA_HOME = $previousJavaHome
}
