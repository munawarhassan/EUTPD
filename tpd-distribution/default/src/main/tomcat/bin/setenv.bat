rem
rem Note: If running ${application.title} as a Service, settings in this file have no
rem effect. See ${site.deploy.url.latest}docs/increase-memory.html
rem

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

set JAVA_HOME=${jdk.home}

rem
rem Native DLLs can be placed in %${app.home.property}%\lib\native, where they will also be included in the
rem library path used by the JVM. By placing DLLs in %${app.home.property}%, they can be preserved across ${application.title} upgrades.
rem
rem NOTE: You must choose the DLL architecture, x86 or x64, based on the JVM you'll be running, _not_ based on Windows.
rem
set JVM_LIBRARY_PATH=%CATALINA_HOME%\lib\native;%${app.home.property}%\lib\native

rem
rem Occasionally, you set some specific JVM arguments.  You can use this variable
rem below to do that.
rem
set JVM_SUPPORT_RECOMMENDED_ARGS=

rem
rem The following 2 settings control the minimum and maximum given to the ${application.title} Java virtual machine.
rem In larger ${application.title} instances, the maximum amount will need to be increased.
rem
rem recommended for elasticsearch
set JVM_MINIMUM_MEMORY=1g
set JVM_MAXIMUM_MEMORY=4g

rem
rem File encoding passed into the ${application.title} Java virtual machine
rem
set JVM_FILE_ENCODING=UTF-8

rem
rem The following are the required arguments needed for ${application.title}.
rem
set JVM_REQUIRED_ARGS=-Djava.awt.headless=true -Dfile.encoding=%JVM_FILE_ENCODING% -Dmail.mime.decodeparameters=true -Dorg.apache.catalina.connector.Response.ENFORCE_ENCODING_IN_GET_WRITER=false

rem -----------------------------------------------------------------------------------
rem  JMX
rem
rem  JMX is enabled by selecting an authentication method value for JMX_REMOTE_AUTH and then configuring related the
rem  variables.
rem
rem  See http://docs.oracle.com/javase/7/docs/technotes/guides/management/agent.html for more information on JMX
rem  configuration in general.
rem -----------------------------------------------------------------------------------

rem
rem  Set the authentication to use for remote JMX access. Anything other than "password" or "ssl" will cause remote JMX
rem  access to be disabled.
rem
set JMX_REMOTE_AUTH=

rem
rem  The port for remote JMX support if enabled
rem
set JMX_REMOTE_PORT=3333

rem
rem  If `hostname -i` returns a local address then JMX-RMI communication may fail because the address returned by JMX for
rem  the RMI-JMX stub will not resolve for non-local clients. To fix this you will need to explicitly specify the
rem  IP address / host name of this server that is reachable / resolvable by JMX clients. e.g.
rem  RMI_SERVER_HOSTNAME="-Djava.rmi.server.hostname=non.local.name.of.my.server"
rem
rem set RMI_SERVER_HOSTNAME="-Djava.rmi.server.hostname="

rem -----------------------------------------------------------------------------------
rem  JMX username/password support
rem -----------------------------------------------------------------------------------

rem
rem  The full path to the JMX username/password file used to authenticate remote JMX clients
rem
rem set JMX_PASSWORD_FILE=

rem -----------------------------------------------------------------------------------
rem  JMX SSL support
rem -----------------------------------------------------------------------------------

rem
rem  The full path to the Java keystore which must contain ${application.title}'s key pair used for SSL authentication for JMX
rem
rem set JAVA_KEYSTORE=

rem
rem  The password for JAVA_KEYSTORE
rem
rem set JAVA_KEYSTORE_PASSWORD=

rem
rem  The full path to the Java truststore which must contain the client certificates accepted by ${application.title} for SSL authentication
rem  of JMX
rem
rem set JAVA_TRUSTSTORE=

rem
rem  The password for JAVA_TRUSTSTORE
rem
rem set JAVA_TRUSTSTORE_PASSWORD=

rem --------------------------------------------------------------------------
rem
rem In general don't make changes below here
rem
rem --------------------------------------------------------------------------

set _PRG_DIR=%~dp0

rem Checks if the program directory has a space in it (will cause issues)
set _marker="x%_PRG_DIR%"
set _marker=%_marker: =%
if %_marker% == "x%_PRG_DIR%" goto APPHOMECHECK
echo.
echo -------------------------------------------------------------------------------
echo   ${application.title} directory "%_PRG_DIR%" contains spaces.
echo   Please change to a location without spaces and try again.
echo -------------------------------------------------------------------------------

:APPHOMECHECK
set ${app.home.property}_MINUSD=
if "x%${app.home.property}%x" == "xx" goto NOAPPHOME

rem Remove any trailing backslash in ${app.home.property}
if %${app.home.property}:~-1%==\ SET ${app.home.property}=%${app.home.property}:~0,-1%

rem Checks if the ${app.home.property} has a space in it (can cause issues)
set _marker="x%${app.home.property}%"
set _marker=%_marker: =%
if %_marker% == "x%${app.home.property}%" goto APPHOME
echo.
echo -------------------------------------------------------------------------------
echo   ${app.home.property} "%${app.home.property}%" contains spaces.
echo   Please change to a location without spaces if this causes problems.
echo -------------------------------------------------------------------------------

:APPHOME
set ${app.home.property}_MINUSD=-Dapp.home="%${app.home.property}%"
goto :CONFIGURE_JAVA_OPTS

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
exit /b 1

:CONFIGURE_JAVA_OPTS
if "x%JVM_LIBRARY_PATH%x" == "xx" goto SET_JMX_OPTS
rem If a native library path has been specified, add it to the required arguments
set JVM_LIBRARY_PATH_MINUSD=-Djava.library.path="%JVM_LIBRARY_PATH%"
set JVM_REQUIRED_ARGS=%JVM_REQUIRED_ARGS% %JVM_LIBRARY_PATH_MINUSD%

:SET_JMX_OPTS
if "%JMX_REMOTE_AUTH%" == "password" goto JMXPASSWORDAUTH
if "%JMX_REMOTE_AUTH%" == "ssl" goto JMXSSLAUTH
goto :SET_JAVA_OPTS

:JMXPASSWORDAUTH
set JMX_OPTS=-Dcom.sun.management.jmxremote.port=%JMX_REMOTE_PORT% %RMI_SERVER_HOSTNAME% -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.password.file=%JMX_PASSWORD_FILE%
goto :SET_JAVA_OPTS

:JMXSSLAUTH
set JMX_OPTS=-Dcom.sun.management.jmxremote.port=%JMX_REMOTE_PORT% %RMI_SERVER_HOSTNAME% -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl.need.client.auth=true -Djavax.net.ssl.keyStore=%JAVA_KEYSTORE% -Djavax.net.ssl.keyStorePassword=%JAVA_KEYSTORE_PASSWORD% -Djavax.net.ssl.trustStore=%JAVA_TRUSTSTORE% -Djavax.net.ssl.trustStorePassword=%JAVA_TRUSTSTORE_PASSWORD%
goto :SET_JAVA_OPTS

:SET_JAVA_OPTS
set GC_OPTIONS=-XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly

set JAVA_OPTS=%GC_OPTIONS% -Xms%JVM_MINIMUM_MEMORY% -Xmx%JVM_MAXIMUM_MEMORY% %JMX_OPTS% %JAVA_OPTS% %JVM_REQUIRED_ARGS% %JVM_SUPPORT_RECOMMENDED_ARGS% %${app.home.property}_MINUSD%

set JDK_JAVA_OPTIONS=${jdk.jdkoptions}

rem Checks if the JAVA_HOME has a space in it (can cause issues)
set _marker="x%JAVA_HOME%"
set _marker=%_marker: =%
if %_marker% == "x%JAVA_HOME%" goto RUN_JAVA
echo.
echo -------------------------------------------------------------------------------
echo   JAVA_HOME "%JAVA_HOME%" contains spaces.
echo   Please change to a location without spaces if this causes problems.
echo -------------------------------------------------------------------------------

:RUN_JAVA
rem Check that JAVA_HOME is valid
if exist "%JAVA_HOME%\bin\java.exe" goto JAVA_OK
echo.
echo -------------------------------------------------------------------------------
echo   JAVA_HOME "%JAVA_HOME%" does not point to a valid version of Java.
echo -------------------------------------------------------------------------------


:JAVA_OK
echo.
echo Using ${app.home.property}:      "%${app.home.property}%"
:END
