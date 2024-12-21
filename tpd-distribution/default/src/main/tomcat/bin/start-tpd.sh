#!/usr/bin/env bash

function fgstartmsg {
    if [ "$PRGRUNMODE" == "true" ] ; then
        echo -e "If you do not see a 'Server startup' message within 3 minutes, please see the troubleshooting guide at:\n\n${site.deploy.url.latest}docs/troubleshooting-installation.html\n\n"
    fi
}

# resolve links - $0 may be a softlink - stolen from catalina.sh
PRG="$0"
while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/"$link"
    fi
done
PRGDIR=`dirname "$PRG"`

PRGRUNMODE=false
if [ "$1" = "-fg" ] || [ "$1" = "run" ]  ; then
	shift
	PRGRUNMODE=true
else
	echo "To run ${application.title} in the foreground, start the server with start-${application.name}.sh -fg"
fi

. `dirname $0`/user.sh #readin the username

if [ -z "$TPD_USER" ] || [ $(id -un) == "$TPD_USER" ]; then
    echo -e "Starting ${application.title} as current user\n"

    fgstartmsg
    if [ "$PRGRUNMODE" == "true" ] ; then
        $PRGDIR/catalina.sh run $@
        if [ $? -ne 0 ]; then
		exit 1
	fi
    else
        $PRGDIR/startup.sh $@
        if [ $? -ne 0 ]; then
		exit 1
	    fi
    fi
elif [ $UID -ne 0 ]; then
    echo ${application.title} has been installed to run as $TPD_USER. Use "sudo -u $TPD_USER $0" to enable
    echo starting the server as that user.
    exit 1
else
    echo -e "Starting ${application.title} as dedicated user $TPD_USER \n"
    fgstartmsg

    if [ -x "/sbin/runuser" ]; then
        sucmd="/sbin/runuser"
    else
        sucmd="su"
    fi

    if [ "$PRGRUNMODE" == "true" ] ; then
        $sucmd $TPD_USER -c "$PRGDIR/catalina.sh run $@"
    else
        $sucmd $TPD_USER -c "$PRGDIR/startup.sh $@"
    fi
fi

source $PRGDIR/../conf/${app.configfile}
echo -e "\nSuccess! You can now use ${application.title} at the following address:\n\nhttp://localhost:${app_httpport}/${app_context}\n"
echo -e "If you cannot access ${application.title} at the above location within 3 minutes, or encounter any other issues starting or stopping ${application.title}, please see the troubleshooting guide at:\n\n${site.deploy.url.latest}docs/troubleshooting-installation.html\n"
