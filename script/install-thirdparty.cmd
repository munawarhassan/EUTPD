@ECHO off

SET dir=%~dp0

SET ROOT_PATH=%dir%..
SET M2_HOME=~\.m2\repository
SET cache_path=%dir%..\.cache

SET tomcat_version=9.0.85
SET tomcat_groupId=org.apache.tomcat
SET tomcat_artifactId=apache-tomcat

SET jdk_groupId=net.adoptium
SET jdk_artifactId=jdk-hotspot
SET jdk_version=11.0.21_9
SET jdk_version_enc=11.0.21+9


call :tomcat_install %tomcat_version% "" zip
call :tomcat_install %tomcat_version% windows-x64 zip

call :openjdk_hotspot_install "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-%jdk_version_enc%/OpenJDK11U-jdk_aarch64_linux_hotspot_%jdk_version%.tar.gz" %jdk_version% "linux-x64" "tar.gz"
call :openjdk_hotspot_install "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-%jdk_version_enc%/OpenJDK11U-jdk_x64_mac_hotspot_%jdk_version%.tar.gz" %jdk_version% "mac-x64" "tar.gz"
call :openjdk_hotspot_install "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-%jdk_version_enc%/OpenJDK11U-jdk_x64_windows_hotspot_%jdk_version%.zip" %jdk_version% "windows-x64" "zip"


goto :eof

:mvn_install_file
SETLOCAL
SET _file=%~1
SET _basename=%~n1%~x1
SET _group=%~2
SET _artifact=%~3
SET _version=%~4
SET _classifier_raw=%~5
SET _classifier=%~5
SET _extension=%~6

if not "%_classifier%" == "" SET _classifier="-Dclassifier=%_classifier%"
if %_extension% == "" SET _extension=jar 
CALL mvn -N -e install:install-file -Dfile=%_file% -DgroupId=%_group% -DartifactId=%_artifact% -Dversion=%_version% %_classifier% -Dpackaging=%_extension%
ECHO Installing file %_basename%

ENDLOCAL
goto :eof

:tomcat_install  
SETLOCAL
SET _version=%~1
SET _groupId=%tomcat_groupId%
SET _artifactId=%tomcat_artifactId%
SET _classifier_raw=%~2
SET _classifier=%~2
SET _packaging=%~3
SET _tomcat_major_version=9

if not "%_classifier%" == "" SET _classifier=-%_classifier%

ECHO Installing Tomcat artifact %_artifactId%-%_version%%_classifier%.%_packaging%... 

SET _file_name=%_artifactId%-%_version%%_classifier%.%_packaging%
SET _file_path=%cache_path%\%_file_name%
SET _download_path=https://dlcdn.apache.org/tomcat/tomcat-%_tomcat_major_version%/v%_version%/bin/%_file_name%


if EXIST %_file_path% ( 
  ECHO ^>^>^> %_artifactId%-%_version%%_classifier%.%_packaging% already exists in cache
) ELSE (
  curl.exe --ssl-no-revoke -L --output %_file_path% --url %_download_path%
)


CALL :mvn_install_file "%_file_path%" "%_groupId%" "%_artifactId%" "%_version%" "%_classifier_raw%" "%_packaging%"
ECHO ^>^>^> Add the following to the %_groupId%:%_artifactId% pom.xml
ECHO.
ECHO ^<dependency^>
ECHO   ^<groupId^>%_groupId%^</groupId^>
ECHO   ^<artifactId^>%_artifactId%^</artifactId^>
ECHO   ^<version^>%_version%^</version^>
ECHO   ^<classifier^>%_classifier_raw%^</classifier^>
ECHO ^</dependency^>
ENDLOCAL
goto :eof


:openjdk_hotspot_install
SETLOCAL
SET _download_url=%~1
SET _version=%~2
SET _classifier_raw=%~3
SET _classifier=%~3
SET _packaging=%~4
SET _groupId=%jdk_groupId%
SET _artifactId=%jdk_artifactId%
SET _file_name=%~n1%~x1

if not "%_classifier%" == "" SET _classifier=-%_classifier%

SET _file_path=%cache_path%\%_file_name%

if EXIST %_file_path% ( 
  ECHO ^>^>^> %_artifactId%-%_version%%_classifier%.%_packaging% already exists in cache
) ELSE (
  ECHO ^>^>^> Downloading JDK %_download_url%...
  curl.exe --ssl-no-revoke -L --output %_file_path% --url %_download_url%
)

CALL :mvn_install_file "%_file_path%" "%_groupId%" "%_artifactId%" "%_version%" "%_classifier_raw%" "%_packaging%"
ECHO Add the following to the %_groupId%:%_artifactId% pom.xml
ECHO.
ECHO ^<dependency^>
ECHO   ^<groupId^>%_groupId%^</groupId^>
ECHO   ^<artifactId^>%_artifactId%^</artifactId^>
ECHO   ^<version^>%_version%^</version^>
ECHO   ^<classifier^>%_classifier_raw%^</classifier^>
ECHO ^</dependency^>

ENDLOCAL
goto :eof

