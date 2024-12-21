${at.hack}echo off
set _PRG_DIR=%~dp0

echo Stopping ${application.title}
echo .
call "%_PRG_DIR%\shutdown.bat" %1 %2 %3 %4 %5 %6 %7 %8 %9
echo .
set APP_CONTEXT=
set APP_HTTPPORT=

FOR /F "eol=# tokens=1,2 delims==" %%a in (%_PRG_DIR%..\conf\${app.configfile}) DO (
    if %%a==app_context set APP_CONTEXT=%%b
    if %%a==app_httpport set APP_HTTPPORT=%%b
)
echo Stopped ${application.title} at http://localhost:%APP_HTTPPORT%/%APP_CONTEXT%

