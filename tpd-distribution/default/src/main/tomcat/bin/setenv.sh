#!/usr/bin/env bash

export JAVA_HOME="${jdk.home}"


#
# configure the pre-configuration by environment (development,integration,staging,qa,production), production is default
#
# export ${app.profile.property}=production

#
# One way to set the ${app.home.property} path is here via this variable.  Simply uncomment it and set a valid path like
# /${application.name}/home.  You can of course set it outside in the command terminal; that will also work.
#
# export ${app.home.property}=

if [ "x$${app.home.property}" = "x" ]; then
    export ${app.home.property}=
fi

if [ "x$JAVA_HOME" = "x" ]; then
    export JAVA_HOME=
fi

#
# Native libraries, such as the Tomcat native library, can be placed here for use by ${application.title}. Alternatively, native
# libraries can also be placed in $${app.home.property}/lib/native, where they will also be included in the library path used
# by the JVM. By placing libraries in $${app.home.property}, they can be preserved across ${application.title} upgrades.
#
# NOTE: You must choose the library architecture, x86 or x64, based on the JVM you'll be running, _not_ based on the OS.
#
JVM_LIBRARY_PATH="$CATALINA_HOME/lib/native:$${app.home.property}/lib/native"

#
# Occasionally, you set some specific JVM arguments.  You can use this variable below to do that.
#
JVM_SUPPORT_RECOMMENDED_ARGS=""

#
# The following 2 settings control the minimum and maximum given to the ${application.title} Java virtual machine.
# In larger ${application.title} instances, the maximum amount will need to be increased.
#
# recommended for elasticsearch
JVM_MINIMUM_MEMORY="1g"
JVM_MAXIMUM_MEMORY="4g"

#
# File encoding passed into the ${application.title} Java virtual machine
#
JVM_FILE_ENCODING="UTF-8"

#
# The following are the required arguments needed for ${application.title}.
#
JVM_REQUIRED_ARGS="-Djava.awt.headless=true -Dfile.encoding=$JVM_FILE_ENCODING -Dmail.mime.decodeparameters=true -Dorg.apache.catalina.connector.Response.ENFORCE_ENCODING_IN_GET_WRITER=false"

#
# Add the JAVA 9 specific start-up parameters required by Tomcat for ${application.title}.
#
JDK_JAVA_OPTIONS="${jdk.jdkoptions}"

#
# Uncommenting the following will set the umask for the ${application.title} application. If can be used to override
# the default settings of the ${application.title} user is they are not sufficiently secure.
#
# umask 0027

#-----------------------------------------------------------------------------------
# JMX
#
# JMX is enabled by selecting an authentication method value for JMX_REMOTE_AUTH and then configuring related the
# variables.
#
# See http://docs.oracle.com/javase/7/docs/technotes/guides/management/agent.html for more information on JMX
# configuration in general.
#-----------------------------------------------------------------------------------

#
# Set the authentication to use for remote JMX access. Anything other than "password" or "ssl" will cause remote JMX
# access to be disabled.
#
JMX_REMOTE_AUTH=

#
# The port for remote JMX support if enabled
#
JMX_REMOTE_PORT=3333

#
# If `hostname -i` returns a local address then JMX-RMI communication may fail because the address returned by JMX for
# the RMI-JMX stub will not resolve for non-local clients. To fix this you will need to explicitly specify the
# IP address / host name of this server that is reachable / resolvable by JMX clients. e.g.
# RMI_SERVER_HOSTNAME="-Djava.rmi.server.hostname=non.local.name.of.my.server"
#
#RMI_SERVER_HOSTNAME="-Djava.rmi.server.hostname="

#-----------------------------------------------------------------------------------
# JMX username/password support
#-----------------------------------------------------------------------------------

#
# The full path to the JMX username/password file used to authenticate remote JMX clients
#
#JMX_PASSWORD_FILE=

#-----------------------------------------------------------------------------------
# JMX SSL support
#-----------------------------------------------------------------------------------

#
# The full path to the Java keystore which must contain ${application.title}'s key pair used for SSL authentication for JMX
#
#JAVA_KEYSTORE=

#
# The password for JAVA_KEYSTORE
#
#JAVA_KEYSTORE_PASSWORD=

#
# The full path to the Java truststore which must contain the client certificates accepted by ${application.title} for SSL authentication
# of JMX
#
#JAVA_TRUSTSTORE=

#
# The password for JAVA_TRUSTSTORE
#
#JAVA_TRUSTSTORE_PASSWORD=

#-----------------------------------------------------------------------------------
#
# In general don't make changes below here
#
#-----------------------------------------------------------------------------------

PRGDIR=`dirname "$0"`

if [ -z "$${app.home.property}" ]; then
    echo ""
    echo "-------------------------------------------------------------------------------"
    echo "  ${application.title} doesn't know where to store its data. Please configure the ${app.home.property}"
    echo "  environment variable with the directory where ${application.title} should store its data."
    echo "  Ensure that the path to ${app.home.property} does not contain spaces. ${app.home.property} may"
    echo "  be configured in setenv.sh, if preferred, rather than exporting it as an"
    echo "  environment variable."
    echo "-------------------------------------------------------------------------------"
    exit 1
fi

echo $${app.home.property} | grep -q " "
if [ $? -eq 0 ]; then
    echo ""
    echo "-------------------------------------------------------------------------------"
    echo "  ${app.home.property} \"$${app.home.property}\" contains spaces."
    echo "  Using a directory with spaces is likely to cause unexpected behaviour and is"
    echo "  not supported. Please use a directory which does not contain spaces."
    echo "-------------------------------------------------------------------------------"
    exit 1
fi

UMASK=`umask`
UMASK_SYMBOLIC=`umask -S`
if echo $UMASK | grep -qv '0[2367]7$'; then
    FORCE_EXIT=false
    echo ""
    echo "-------------------------------------------------------------------------------"
    echo "${application.title} is being run with a umask that contains potentially unsafe settings."
    echo "The following issues were found with the mask \"$UMASK_SYMBOLIC\" ($UMASK):"
    if echo $UMASK | grep -qv '7$'; then
        echo " - access is allowed to 'others'. It is recommended that 'others' be denied"
        echo "   all access for security reasons."
    fi
    if echo $UMASK | grep -qv '[2367][0-9]$'; then
        echo " - write access is allowed to 'group'. It is recommend that 'group' be"
        echo "   denied write access. Read access to a restricted group is recommended"
        echo "   to allow access to the logs."
    fi
    if echo $UMASK | grep -qv '0[0-9][0-9]$'; then
        echo " - full access has been denied to 'user'. ${application.title} cannot be run without full"
        echo "   access being allowed."
        FORCE_EXIT=true
    fi
    echo ""
    echo "The recommended umask for ${application.title} is \"u=,g=w,o=rwx\" (0027) and can be"
    echo "configured in setenv.sh"
    echo "-------------------------------------------------------------------------------"
    if [ "x${FORCE_EXIT}" = "xtrue" ]; then
        exit 1;
    fi
fi

if [ "x$JMX_REMOTE_AUTH" = "xpassword" ]; then
    number='^[0-9]+$'
    if ! [[ $JMX_REMOTE_PORT =~ $number ]]; then
        echo ""
        echo "-------------------------------------------------------------------------------"
        echo "  Remote JMX is enabled.                                                       "
        echo "                                                                               "
        echo "  You must specify a valid port number. This is done by specifying             "
        echo "  JMX_REMOTE_PORT in setenv.sh.                                                "
        echo "-------------------------------------------------------------------------------"
        exit 1
    fi

    if [ -z "$JMX_PASSWORD_FILE" ] || [ ! -f "$JMX_PASSWORD_FILE" ]; then
        echo ""
        echo "-----------------------------------------------------------------------------------"
        echo "  Remote JMX with username/password authentication is enabled.                     "
        echo "                                                                                   "
        echo "  You must specify a valid path to the password file used by ${application.title}. "
        echo "  This is done by specifying JMX_PASSWORD_FILE in setenv.sh.                       "
        echo "-----------------------------------------------------------------------------------"
        exit 1
    fi

    JMX_OPTS="-Dcom.sun.management.jmxremote.port=$JMX_REMOTE_PORT $RMI_SERVER_HOSTNAME -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.password.file=$JMX_PASSWORD_FILE"

elif [ "x$JMX_REMOTE_AUTH" = "xssl" ]; then
    number='^[0-9]+$'
    if ! [[ $JMX_REMOTE_PORT =~ $number ]]; then
        echo ""
        echo "-------------------------------------------------------------------------------"
        echo "  Remote JMX is enabled.                                                       "
        echo "                                                                               "
        echo "  You must specify a valid port number. This is done by specifying             "
        echo "  JMX_REMOTE_PORT in setenv.sh.                                                "
        echo "-------------------------------------------------------------------------------"
        exit 1
    fi

    if [ -z "$JAVA_KEYSTORE" ] || [ ! -f "$JAVA_KEYSTORE" ]; then
        echo ""
        echo "-------------------------------------------------------------------------------"
        echo "  Remote JMX with SSL authentication is enabled.                               "
        echo "                                                                               "
        echo "  You must specify a valid path to the keystore used by ${application.title}.  "
        echo "  This is done by specifying JAVA_KEYSTORE in setenv.sh.                       "
        echo "-------------------------------------------------------------------------------"
        exit 1
    fi

    if [ -z "$JAVA_KEYSTORE_PASSWORD" ]; then
        echo ""
        echo "-------------------------------------------------------------------------------"
        echo "  Remote JMX with SSL authentication is enabled.                               "
        echo "                                                                               "
        echo "  You must specify a password to the keystore used by ${application.title}.    "
        echo "  This is done by specifying JAVA_KEYSTORE_PASSWORD in setenv.sh.              "
        echo "-------------------------------------------------------------------------------"
        exit 1
    fi

    if [ -z "$JAVA_TRUSTSTORE" ] || [ ! -f "$JAVA_TRUSTSTORE" ]; then
        echo ""
        echo "-------------------------------------------------------------------------------"
        echo "  Remote JMX with SSL authentication is enabled.                               "
        echo "                                                                               "
        echo "  You must specify a valid path to the keystore used by ${application.title}.        "
        echo "  This is done by specifying JAVA_TRUSTSTORE in setenv.sh.                     "
        echo "-------------------------------------------------------------------------------"
        exit 1
    fi

    if [ -z "$JAVA_TRUSTSTORE_PASSWORD" ]; then
        echo ""
        echo "-------------------------------------------------------------------------------"
        echo "  Remote JMX with SSL authentication enabled.                                  "
        echo "                                                                               "
        echo "  You must specify a password to the truststore used by ${application.title}.        "
        echo "  This is done by specifying JAVA_TRUSTSTORE_PASSWORD in setenv.sh.            "
        echo "-------------------------------------------------------------------------------"
        exit 1
    fi

    JMX_OPTS="-Dcom.sun.management.jmxremote.port=$JMX_REMOTE_PORT $RMI_SERVER_HOSTNAME -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl.need.client.auth=true -Djavax.net.ssl.keyStore=${JAVA_KEYSTORE} -Djavax.net.ssl.keyStorePassword=${JAVA_KEYSTORE_PASSWORD} -Djavax.net.ssl.trustStore=${JAVA_TRUSTSTORE} -Djavax.net.ssl.trustStorePassword=${JAVA_TRUSTSTORE_PASSWORD}"
fi

${app.home.property}_MINUSD=-Dapp.home=$${app.home.property}

if [ "x$JVM_LIBRARY_PATH" != "x" ]; then
    JVM_LIBRARY_PATH_MINUSD=-Djava.library.path=$JVM_LIBRARY_PATH
    JVM_REQUIRED_ARGS="$JVM_REQUIRED_ARGS $JVM_LIBRARY_PATH_MINUSD"
fi

GC_OPTIONS="-XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly"

JAVA_OPTS="$GC_OPTIONS -Xms$JVM_MINIMUM_MEMORY -Xmx$JVM_MAXIMUM_MEMORY $JMX_OPTS $JAVA_OPTS $JVM_REQUIRED_ARGS $JVM_SUPPORT_RECOMMENDED_ARGS $${app.home.property}_MINUSD"

export JAVA_OPTS

if [ "x$${app.home.property}_MINUSD" != "x" ]; then
    echo "Using ${app.home.property}:      $${app.home.property}"
fi

# set the location of the pid file
if [ -z "$CATALINA_PID" ] ; then
    if [ -n "$CATALINA_BASE" ] ; then
        CATALINA_PID="$CATALINA_BASE"/work/catalina.pid
    elif [ -n "$CATALINA_HOME" ] ; then
        CATALINA_PID="$CATALINA_HOME"/work/catalina.pid
    fi
fi
export CATALINA_PID
