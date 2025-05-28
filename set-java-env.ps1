$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Write-Host "JAVA_HOME установлен на: $env:JAVA_HOME"
Write-Host "Java версия:"
java -version 