@echo off
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

rem ---------------------------------------------------------------------------
rem NT Service Install/Uninstall script
rem
rem Options
rem install                Install the service using tomcat9 as service name.
rem                        Service is installed using default settings.
rem remove                 Remove the service from the System.
rem
rem name        (optional) If the second argument is present it is considered
rem                        to be new service name
rem ---------------------------------------------------------------------------

setlocal
rem
rem The JRE_HOME or JAVA_HOME environment variable is must be defined correctly
rem This environment variable is needed to run this program
rem

rem recommended for elasticsearch
set JVM_MINIMUM_MEMORY=1024
set JVM_MAXIMUM_MEMORY=4096

rem
rem One way to set the ${app.home.property} path is here via this variable.  Simply uncomment it and set a valid path like
rem C:\${application.name}\home. You can of course set it outside in the command terminal; that will also work.
rem
rem WARNING: DO NOT wrap the ${app.home.property} value in quotes when setting it here, even if it contains spaces.
rem
rem set ${app.home.property}=

rem
rem configure the pre-configuration by environment (development,integration,staging,qa,production), production is default
rem
rem set ${app.profile.property}=production

if "x%${app.home.property}%x" == "xx" goto NOAPPHOME

rem Remove any trailing backslash in ${app.home.property}
if %${app.home.property}:~-1%==\ SET ${app.home.property}=%${app.home.property}:~0,-1%

rem Checks if the ${app.home.property} has a space in it (can cause issues)
set _marker="x%${app.home.property}%"
set _marker=%_marker: =%
if %_marker% == "x%${app.home.property}%" goto configureService
echo.
echo -------------------------------------------------------------------------------
echo   ${app.home.property} "%${app.home.property}%" contains spaces.
echo   Please change to a location without spaces if this causes problems.
echo -------------------------------------------------------------------------------
pause
goto end


:NOAPPHOME
echo.
echo -------------------------------------------------------------------------------
echo   ${application.title} doesn't know where to store its data. Please configure the ${app.home.property}
echo   environment variable with the directory where ${application.title} should store its data.
echo   Ensure that the path to ${app.home.property} does not contain spaces. ${app.home.property} may
echo   be configured in setenv.bat, if preferred, rather than exporting it as an
echo   environment variable.
echo -------------------------------------------------------------------------------
pause
goto end

:configureService:

set "SCRIPT_DIR=%~dp0"
set "SELF=%~dp0%service.bat"
rem Guess CATALINA_HOME if not defined
set "CURRENT_DIR=%cd%"
if not "%CATALINA_HOME%" == "" goto gotHome
set "CATALINA_HOME=%cd%"
if exist "%CATALINA_HOME%\bin\tomcat9.exe" goto okHome
rem CD to the upper dir
cd ..
set "CATALINA_HOME=%cd%"
:gotHome
if exist "%CATALINA_HOME%\bin\tomcat9.exe" goto okHome
echo The tomcat9.exe was not found...
echo The CATALINA_HOME environment variable is not defined correctly.
echo This environment variable is needed to run this program
goto end
:okHome

set JAVA_HOME=%CATALINA_HOME%\java\${jdk.name}

rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotJdkHome
if not "%JRE_HOME%" == "" goto gotJreHome
echo Neither the JAVA_HOME nor the JRE_HOME environment variable is defined
echo Service will try to guess them from the registry.
goto okJavaHome
:gotJreHome
if not exist "%JRE_HOME%\bin\java.exe" goto noJavaHome
if not exist "%JRE_HOME%\bin\javaw.exe" goto noJavaHome
goto okJavaHome
:gotJdkHome
if not exist "%JAVA_HOME%\bin\javac.exe" goto noJavaHome
rem Java 9 has a different directory structure
if exist "%JAVA_HOME%\jre\bin\java.exe" goto preJava9Layout
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if not "%JRE_HOME%" == "" goto okJavaHome
set "JRE_HOME=%JAVA_HOME%"
goto okJavaHome
:preJava9Layout
if not "%JRE_HOME%" == "" goto okJavaHome
set "JRE_HOME=%JAVA_HOME%\jre"
goto okJavaHome
:noJavaHome
echo The JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
echo NB: JAVA_HOME should point to a JDK not a JRE
goto end
:okJavaHome
if not "%CATALINA_BASE%" == "" goto gotBase
set "CATALINA_BASE=%CATALINA_HOME%"
:gotBase

rem Java 9 no longer supports the java.endorsed.dirs
rem system property. Only try to use it if
rem JAVA_ENDORSED_DIRS was explicitly set
rem or CATALINA_HOME/endorsed exists.
set ENDORSED_PROP=ignore.endorsed.dirs
if "%JAVA_ENDORSED_DIRS%" == "" goto noEndorsedVar
set ENDORSED_PROP=java.endorsed.dirs
goto doneEndorsed
:noEndorsedVar
if not exist "%CATALINA_HOME%\endorsed" goto doneEndorsed
set ENDORSED_PROP=java.endorsed.dirs
:doneEndorsed

set "EXECUTABLE=%CATALINA_HOME%\bin\tomcat9.exe"

rem Set default Service name
set SERVICE_NAME=${application.service.name}
set DISPLAYNAME=${application.title} %SERVICE_NAME%

if "x%1x" == "xx" goto displayUsage
set SERVICE_CMD=%1
shift
if "x%1x" == "xx" goto checkServiceCmd
:checkUser
if "x%1x" == "x/userx" goto runAsUser
if "x%1x" == "x--userx" goto runAsUser
set SERVICE_NAME=%1
set DISPLAYNAME=${application.title} %1
shift
if "x%1x" == "xx" goto checkServiceCmd
goto checkUser
:runAsUser
shift
if "x%1x" == "xx" goto displayUsage
set SERVICE_USER=%1
shift
runas /env /savecred /user:%SERVICE_USER% "%COMSPEC% /K \"%SELF%\" %SERVICE_CMD% %SERVICE_NAME%"
goto end
:checkServiceCmd
if /i %SERVICE_CMD% == install goto doInstall
if /i %SERVICE_CMD% == remove goto doRemove
if /i %SERVICE_CMD% == uninstall goto doRemove
echo Unknown parameter "%SERVICE_CMD%"
:displayUsage
echo.
echo Usage: service.bat install/remove [service_name] [/user username]
goto end

:doRemove
rem Remove the service
echo Removing the service '%SERVICE_NAME%' ...
echo Using CATALINA_BASE:    "%CATALINA_BASE%"

"%EXECUTABLE%" //DS//%SERVICE_NAME% ^
    --LogPath "%CATALINA_BASE%\logs"
if not errorlevel 1 goto removed
echo Failed removing '%SERVICE_NAME%' service
goto end
:removed
echo The service '%SERVICE_NAME%' has been removed
goto end

:doInstall
rem Install the service
echo Installing the service '%SERVICE_NAME%' ...
echo Using CATALINA_HOME:    "%CATALINA_HOME%"
echo Using CATALINA_BASE:    "%CATALINA_BASE%"
echo Using JAVA_HOME:        "%JAVA_HOME%"
echo Using JRE_HOME:         "%JRE_HOME%"

rem Try to use the server jvm
set "JVM=%JRE_HOME%\bin\server\jvm.dll"
if exist "%JVM%" goto foundJvm
rem Try to use the client jvm
set "JVM=%JRE_HOME%\bin\client\jvm.dll"
if exist "%JVM%" goto foundJvm
echo Warning: Neither 'server' nor 'client' jvm.dll was found at JRE_HOME.
set JVM=auto
:foundJvm
echo Using JVM:              "%JVM%"

set "CLASSPATH=%CATALINA_HOME%\bin\${application.name}.tomcat.bootstrap.jar;%CATALINA_HOME%\bin\bootstrap.jar;%CATALINA_BASE%\bin\tomcat-juli.jar"
if not "%CATALINA_HOME%" == "%CATALINA_BASE%" set "CLASSPATH=%CLASSPATH%;%CATALINA_HOME%\bin\tomcat-juli.jar"


REM Note the ticks around the value of java.library.path; they are necessary to prevent the semicolon that is the path
REM separator from prematurely splitting the values, since the Tomcat service binary uses it as a key/value separator. -->


"%EXECUTABLE%" //IS//%SERVICE_NAME% ^
    --Description "${application.title} Server - http://localhost:${app.http.port}/${app.context}" ^
    --DisplayName "%DISPLAYNAME%" ^
    --Install "%EXECUTABLE%" ^
    --LogPath "%CATALINA_BASE%\logs" ^
    --StdOutput auto ^
    --StdError auto ^
    --Classpath "%CLASSPATH%" ^
    --Jvm "%JVM%" ^
    --StartMode jvm ^
    --StopMode jvm ^
    --StartPath "%CATALINA_HOME%" ^
    --StopPath "%CATALINA_HOME%" ^
    --StartClass com.pmi.tpd.catalina.startup.Bootstrap ^
    --StopClass com.pmi.tpd.catalina.startup.Bootstrap ^
    --StartParams start ^
    --StopParams stop ^
    --JvmOptions "-XX:+UseConcMarkSweepGC;-XX:CMSInitiatingOccupancyFraction=75;-XX:+UseCMSInitiatingOccupancyOnly;-Dcatalina.home=%CATALINA_HOME%;-Dcatalina.base=%CATALINA_BASE%;-D%ENDORSED_PROP%=%CATALINA_HOME%\endorsed;-Dapp.home=%${app.home.property}%;-Dspring.profiles.active=%${app.profile.property}%;-Djava.io.tmpdir=%CATALINA_BASE%\temp;-Dmail.mime.decodeparameters=true;-Dfile.encoding=UTF-8;-Djava.library.path='%CATALINA_HOME%\lib\native;%${app.home.property}%\lib\native';-Dorg.apache.catalina.connector.Response.ENFORCE_ENCODING_IN_GET_WRITER=false;-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager;-Djava.util.logging.config.file=%CATALINA_BASE%\conf\logging.properties" ^
    --JvmOptions9 "${tomcat.srv.jdkoptions};--add-opens=java.base/java.lang=ALL-UNNAMED;--add-opens=java.base/java.io=ALL-UNNAMED;--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED" ^
    --JvmMs "%JVM_MINIMUM_MEMORY%" ^
    --JvmMx "%JVM_MAXIMUM_MEMORY%" --Startup auto
if not errorlevel 1 goto installed
echo Failed installing '%SERVICE_NAME%' service
goto end
:installed
echo The service '%SERVICE_NAME%' has been installed.

:end
cd "%CURRENT_DIR%"
