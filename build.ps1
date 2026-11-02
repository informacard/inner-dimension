param([string]$DependencyClasspath = $env:MOD_COMPILE_CLASSPATH)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (-not $DependencyClasspath) {
    $jars = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'libs') -Filter '*.jar' -File -ErrorAction SilentlyContinue)
    if (-not $jars) { throw 'Put intermediary Minecraft, Fabric and mod dependency jars in libs/ or set MOD_COMPILE_CLASSPATH.' }
    $DependencyClasspath = ($jars.FullName -join [IO.Path]::PathSeparator)
}
$metadata = Get-Content -Raw resources/fabric.mod.json | ConvertFrom-Json
$stage = Join-Path $PSScriptRoot ('build/stage-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $stage | Out-Null
$sources = @(Get-ChildItem src -Recurse -Filter '*.java' -File | ForEach-Object FullName)
$arguments = @('--release','21','-proc:none','-encoding','UTF-8','-cp',$DependencyClasspath,'-d',$stage) + $sources
$argFile = Join-Path $PSScriptRoot 'build/javac.args'
$arguments | ForEach-Object { '"' + ($_ -replace '\\','/') + '"' } | Set-Content -Encoding utf8 $argFile
& javac "@$argFile"
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
Copy-Item -Recurse -Force resources/* $stage
$out = Join-Path $PSScriptRoot ('build/' + $metadata.id + '-' + $metadata.version + '.jar')
& jar cf $out -C $stage .
if ($LASTEXITCODE -ne 0) { throw 'Packaging failed' }
Write-Host "Built $out"
