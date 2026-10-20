param([switch]$SkipTests, [string]$IsccPath)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
Push-Location $projectRoot
try {
    if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin/jpackage.exe'))) {
        throw 'Configure JAVA_HOME para um JDK 21 com jpackage.'
    }
    if (-not $IsccPath) { $IsccPath = Join-Path $projectRoot '.build-tools/innosetup/ISCC.exe' }
    if (-not (Test-Path $IsccPath)) {
        $compiler = Get-Command ISCC.exe -ErrorAction SilentlyContinue
        if ($compiler) { $IsccPath = $compiler.Source } else { throw 'Informe -IsccPath com o caminho do compilador Inno Setup.' }
    }
    [xml]$pom = Get-Content -LiteralPath 'pom.xml'
    $releaseVersion = [string]$pom.project.version
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'create-icon.ps1')
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao gerar icone.' }
    $mavenArguments = @('-B', 'package')
    if ($SkipTests) { $mavenArguments += '-DskipTests' }
    & .\mvnw.cmd @mavenArguments
    if ($LASTEXITCODE -ne 0) { throw 'Build Maven falhou.' }
    $buildRoot = Join-Path $projectRoot ('target/windows-' + [Guid]::NewGuid().ToString('N'))
    $inputFolder = Join-Path $buildRoot 'input'
    New-Item -ItemType Directory -Force $inputFolder | Out-Null
    Copy-Item -LiteralPath "target/tarot-exe-$releaseVersion.jar" -Destination $inputFolder
    Copy-Item -LiteralPath 'target/package-input/lib' -Destination $inputFolder -Recurse
    Copy-Item -LiteralPath 'packaging/LEIA-ME.txt','packaging/THIRD-PARTY.md' -Destination $inputFolder
    $imageOutput = Join-Path $buildRoot 'image'
    $icon = Join-Path $PSScriptRoot 'app.ico'
    & (Join-Path $env:JAVA_HOME 'bin/jpackage.exe') --type app-image --name Tarot --app-version $releaseVersion `
        --vendor 'Tarot.exe' --description 'Tarot.exe - leituras locais' `
        --input $inputFolder --main-jar "tarot-exe-$releaseVersion.jar" --main-class com.tarotexe.Launcher `
        --icon $icon --dest $imageOutput --java-options '-Dfile.encoding=UTF-8' `
        --add-modules 'java.se,jdk.unsupported,jdk.crypto.ec,jdk.charsets'
    if ($LASTEXITCODE -ne 0) { throw 'jpackage falhou.' }
    $imageFolder = Join-Path $imageOutput 'Tarot'
    $releaseFolder = Join-Path $projectRoot 'dist'
    New-Item -ItemType Directory -Force $releaseFolder | Out-Null
    & $IsccPath "/DAppVersion=$releaseVersion" "/DImageDir=$imageFolder" "/DOutputDir=$releaseFolder" "/DIconFile=$icon" (Join-Path $PSScriptRoot 'Tarot.iss')
    if ($LASTEXITCODE -ne 0) { throw 'Compilacao do instalador falhou.' }
    $portable = Join-Path $releaseFolder "Tarot-Portable-$releaseVersion.zip"
    Compress-Archive -Path (Join-Path $imageFolder '*') -DestinationPath $portable -Force
    $manifest = @{
        version = $releaseVersion
        builtAt = [DateTime]::UtcNow.ToString('o')
        imageFolder = $imageFolder
        installer = (Join-Path $releaseFolder "Tarot-Setup-$releaseVersion.exe")
        portable = $portable
        jdk = $env:JAVA_HOME
    }
    $manifest | ConvertTo-Json | Set-Content -Encoding UTF8 (Join-Path $releaseFolder 'build-info.json')
    Get-FileHash -Algorithm SHA256 -LiteralPath $manifest.installer,$portable |
        Select-Object @{Name='File';Expression={Split-Path $_.Path -Leaf}},Hash |
        ConvertTo-Json | Set-Content -Encoding UTF8 (Join-Path $releaseFolder 'SHA256.json')
    Write-Output "Installer: $($manifest.installer)"
    Write-Output "Portable: $portable"
} finally { Pop-Location }
