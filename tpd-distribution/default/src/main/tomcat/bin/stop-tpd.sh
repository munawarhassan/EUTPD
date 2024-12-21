#!/usr/bin/env bash

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

. `dirname $0`/user.sh #readin the username
if [ -z "$TPD_USER" ] || [ $(id -un) == "$TPD_USER" ]; then
    echo -e "Stopping ${application.title} as the current user \n\n"
    $PRGDIR/shutdown.sh 60 -force $@
    if [ $? -ne 0 ]; then
        exit 1
    fi
elif [ $UID -ne 0 ]; then
    echo ${application.title} has been installed to run as $TPD_USER. Use "sudo -u $TPD_USER $0" to enable
    echo stopping the server as that user.
    exit 1
else
    echo -e "Stopping ${application.title} as dedicated user $TPD_USER\n\n"
    if [ -x "/sbin/runuser" ]; then
        sucmd="/sbin/runuser"
    else
        sucmd="su"
    fi
    $sucmd -m $TPD_USER -c "$PRGDIR/shutdown.sh 60 -force $@"
fi
    

source $PRGDIR/../conf/${app.configfile}
echo Stopped ${application.title} at http://localhost:${app_httpport}/${app_context} 
