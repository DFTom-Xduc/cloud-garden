param([switch]$CompileOnly, [switch]$Test, [int]$Port = 8080)

$ErrorActionPreference = 'Stop'

# Prefer the JDK executables directly; Oracle's javapath launcher can stall.
$javaCommand = 'java'
$javacCommand = 'javac'
$jdkCandidates = @()
if ($env:JAVA_HOME) { $jdkCandidates += $env:JAVA_HOME }
if ($env:ProgramFiles) {
    $installedJavaRoot = Join-Path $env:ProgramFiles 'Java'
    if (Test-Path -LiteralPath $installedJavaRoot) {
        $jdkCandidates += @(Get-ChildItem -LiteralPath $installedJavaRoot -Directory |
            Where-Object { $_.Name -like 'jdk*' } |
            Sort-Object LastWriteTime -Descending |
            ForEach-Object { $_.FullName })
    }
}
foreach ($jdkDirectory in $jdkCandidates) {
    $jdkJava = Join-Path $jdkDirectory 'bin\java.exe'
    $jdkJavac = Join-Path $jdkDirectory 'bin\javac.exe'
    if ((Test-Path -LiteralPath $jdkJava) -and (Test-Path -LiteralPath $jdkJavac)) {
        $javaCommand = $jdkJava
        $javacCommand = $jdkJavac
        break
    }
}

$outputDirectory = Join-Path $PSScriptRoot 'out'
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null

$sourceFiles = @(
    foreach ($sourceFolder in @('Entity', 'Controller', 'Service', 'Repository')) {
        Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot $sourceFolder) -Filter '*.java' -File |
            Where-Object { $_.Name -ne 'tempCodeRunnerFile.java' } |
            ForEach-Object { $_.FullName }
    }
)

if ($Test) {
    $sourceFiles += @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'tests') -Filter '*.java' -File | ForEach-Object { $_.FullName })
}

& $javacCommand -encoding UTF-8 -d $outputDirectory @sourceFiles
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$dataDirectory = Join-Path $outputDirectory 'Service\data'
New-Item -ItemType Directory -Force -Path $dataDirectory | Out-Null
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'Service\data\flowers.csv') -Destination $dataDirectory -Force

if ($Test) {
    Push-Location -LiteralPath $PSScriptRoot
    try {
        & $javaCommand '--enable-native-access=ALL-UNNAMED' -cp "$outputDirectory;$PSScriptRoot\lib\*" tests.WebIntegrationTest
        $testExitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    exit $testExitCode
} elseif (-not $CompileOnly) {
    & $javaCommand '--enable-native-access=ALL-UNNAMED' "-Dcloudgarden.root=$PSScriptRoot" -cp "$outputDirectory;$PSScriptRoot\lib\*" Controller.Main --port $Port
    exit $LASTEXITCODE
}
