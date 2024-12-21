${at.hack}echo off
set _PRG_DIR=%~dp0

rem Checks if the program directory has a space in it (will cause issues)
set _marker="x%_PRG_DIR%"
set _marker=%_marker: =%
if %_marker% == "x%_PRG_DIR%" goto NOSPACES
echo.
echo -------------------------------------------------------------------------------
echo   ${application.title} directory "%_PRG_DIR%" contains spaces.
echo   Please change to a location without spaces and try again.
echo -------------------------------------------------------------------------------

:NOSPACES
set _PRGRUNMODE=false
if "%1" == "/fg" set _PRGRUNMODE=true
if "%1" == "run" set _PRGRUNMODE=true

if "%_PRGRUNMODE%" == "true" goto EXECSTART
	echo To run ${application.title} in the foreground, start the server with start-${application.name}.bat /fg
	echo .

:EXECSTART
	echo.
	if "%_PRGRUNMODE%" == "true" goto EXECRUNMODE
	call "%_PRG_DIR%\startup.bat"  %1 %2 %3 %4 %5 %6 %7 %8 %9
	goto END

:EXECRUNMODE
    echo.
    echo If you do not see a 'Server startup' message within 3 minutes, please see the troubleshooting guide at:
    echo.
    echo ${site.deploy.url.latest}docs/troubleshooting-installation.html
    echo.
    echo.
	call "%_PRG_DIR%\catalina.bat"  run %2 %3 %4 %5 %6 %7 %8 %9
	goto END

:END

rem Exit if there was a problem above
if ERRORLEVEL 1 exit /b 1

echo .
set APP_CONTEXT=
set APP_HTTPPORT=

FOR /F "usebackq eol=# tokens=1,2 delims==" %%a in (%_PRG_DIR%..\conf\${app.configfile}) DO (
    if %%a==app_context set APP_CONTEXT=%%b
    if %%a==app_httpport set APP_HTTPPORT=%%b
)
echo.
echo Started ${application.title} at:
echo http://localhost:%APP_HTTPPORT%/%APP_CONTEXT%
echo.
echo If you cannot access ${application.title} at the above location within 3 minutes, or encounter any other issues starting or stopping ${application.title},
echo please see the troubleshooting guide at:
echo.
echo ${site.deploy.url.latest}docs/troubleshooting-installation.html
