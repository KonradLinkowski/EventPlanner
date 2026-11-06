@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.4
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0'; $script='%__MVNW_ARG0_NAME__%'; icm -ScriptBlock ([Scriptblock]::Create((Get-Content -Raw '%~f0'))) -NoNewScope}"`) DO @(
  IF "%%A"=="MVN_CMD" (set __MVNW_CMD__=%%B) ELSE IF "%%A"=="MVN_ERROR" (set __MVNW_ERROR__=%%B)
)
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE%
@SET __MVNW_PSMODULEP_SAVE=
@SET __MVNW_ARG0_NAME__=
@SET MVNW_USERNAME=
@SET MVNW_PASSWORD=
@IF NOT "%__MVNW_CMD__%"=="" (%__MVNW_CMD__% %*)
@echo Cannot start maven from wrapper >&2 && exit /b 1
@GOTO :EOF
@REM End batch / begin powershell hybrid chimera script
:init
$ErrorActionPreference = "Stop"
if (($PSVersionTable.PSVersion.Major) -lt 5) {
    Write-Error "PowerShell 5.0 or later is required"
    return
}

$MAVEN_PROJECTBASEDIR = if ($env:MAVEN_BASEDIR) { $env:MAVEN_BASEDIR } else { $scriptDir }
$distributionUrl = $null
$wrapperUrl = $null
Get-Content -Raw "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties" | ConvertFrom-StringData | ForEach-Object {
    if ($_.distributionUrl) { $distributionUrl = $_.distributionUrl.Trim() }
    if ($_.wrapperUrl) { $wrapperUrl = $_.wrapperUrl.Trim() }
}

if (-not $env:JAVA_HOME) {
    Write-Error "JAVA_HOME is not set."
    return
}

$wrapperJarPath = "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
if (-not (Test-Path -Path $wrapperJarPath)) {
    Write-Output "Couldn't find $wrapperJarPath, downloading it ..."
    if ($env:MVNW_REPOURL) {
        $wrapperUrl = "$env:MVNW_REPOURL/org/apache/maven/wrapper/maven-wrapper/3.3.4/maven-wrapper-3.3.4.jar"
    }
    Write-Output "Downloading from: $wrapperUrl"
    $webclient = New-Object System.Net.WebClient
    if ($env:MVNW_USERNAME -and $env:MVNW_PASSWORD) {
        $webclient.Credentials = New-Object System.Net.NetworkCredential($env:MVNW_USERNAME, $env:MVNW_PASSWORD)
    }
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
    $webclient.DownloadFile($wrapperUrl, $wrapperJarPath) | Out-Null
    Write-Output "Finished downloading $wrapperJarPath"
}

$javaExe = Join-Path -Path $env:JAVA_HOME -ChildPath "bin\java.exe"
$MVN_CMD = "`"$javaExe`" $env:MAVEN_OPTS -classpath `"$wrapperJarPath`" `"-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR`" org.apache.maven.wrapper.MavenWrapperMain"
Write-Output "MVN_CMD=$MVN_CMD"
