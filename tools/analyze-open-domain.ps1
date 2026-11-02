param([string]$Recording)
$ErrorActionPreference='Stop'
$taskProject=Split-Path $PSScriptRoot -Parent
$taskWorkspace=Split-Path (Split-Path $taskProject -Parent) -Parent
if(-not $Recording){
    $taskNewest=Get-ChildItem -LiteralPath (Join-Path $taskWorkspace 'config/inner_dimension/performance') -Filter '*.jfr' -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if(-not $taskNewest){throw 'No open-domain recording has been saved yet'}
    $Recording=$taskNewest.FullName
}
$taskRecording=(Resolve-Path -LiteralPath $Recording).Path
$taskClasspath=[IO.File]::ReadAllText((Join-Path $taskProject 'build/classpath.txt'))
$taskGson=($taskClasspath -split ';' | Where-Object {$_ -match '[/\\]gson-[0-9].*\.jar$'} | Select-Object -First 1)
if(-not $taskGson){throw 'Gson runtime missing from prepared classpath'}
$taskArgs=@('-cp',$taskGson,(Join-Path $PSScriptRoot 'OpenDomainJfrReport.java'),$taskRecording) | ForEach-Object {'"'+($_ -replace '\\','/')+'"'}
$taskArgsFile=Join-Path $taskProject 'out/jfr-report.args'
[IO.File]::WriteAllLines($taskArgsFile,[string[]]$taskArgs,[Text.UTF8Encoding]::new($false))
& java ('@'+$taskArgsFile)
if($LASTEXITCODE -ne 0){throw 'JFR report generation failed'}
