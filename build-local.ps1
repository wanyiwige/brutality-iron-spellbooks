param([string[]]$Tasks = @('build'))
$ErrorActionPreference = 'Stop'
$taskGradleRoot = 'C:\Users\WANYIWIGE\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8'
$taskJdkRoot = 'C:\Program Files\Microsoft\jdk-17.0.15.6-hotspot'
$env:JAVA_HOME = $taskJdkRoot
$taskClasspath = (Get-ChildItem -LiteralPath "$taskGradleRoot\lib" -Filter '*.jar' | Select-Object -ExpandProperty FullName) -join ';'
$taskArguments = @(
    '--add-opens=java.base/java.util=ALL-UNNAMED',
    '--add-opens=java.base/java.lang=ALL-UNNAMED',
    '--add-opens=java.base/java.lang.invoke=ALL-UNNAMED',
    '--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED',
    '--add-opens=java.base/java.nio.charset=ALL-UNNAMED',
    '--add-opens=java.base/java.net=ALL-UNNAMED',
    '--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED',
    '-Dfile.encoding=UTF-8', '-Duser.country=CN', '-Duser.language=en', '-Duser.variant',
    "-javaagent:$taskGradleRoot\lib\agents\gradle-instrumentation-agent-8.8.jar",
    '-cp', $taskClasspath, 'org.gradle.launcher.Main', '--offline', '--no-daemon'
) + $Tasks
Push-Location $PSScriptRoot
try {
    & "$taskJdkRoot\bin\java.exe" $taskArguments
    if ($LASTEXITCODE -ne 0) { throw "Gradle exited with code $LASTEXITCODE" }
} finally { Pop-Location }
