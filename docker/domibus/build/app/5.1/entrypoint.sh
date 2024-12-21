#!/bin/bash

set -e

# Script to configure liquibase and dashboard.
# Intended to be run before domibus entrypoint...
# ENTRYPOINT ["/entrypoint.sh"]"

function get_path() {
  local path=$1
  local file_name=$(basename ${path})
  local len=$((${#path} - ${#file_name}))
  echo "${path:0:$len}"
}

function install_testconfiguration() {
  local keystore_path=$(get_path $KEYSTORE_LOCATION)
  local truststore_path=$(get_path $TRUSTSTORE_LOCATION)
  pushd ${DOMIBUS_INSTALL}
  # create directory used to store the keystore used by domibus
  mkdir -p ${keystore_path} || true
  # create directory used to store the trustore used by domibus
  mkdir -p ${truststore_path} || true
  #copy the sample keystore/truststore
  unzip -j domibus-msh-distribution-${DOMIBUS_VERSION}-sample-configuration-and-testing.zip "conf/domibus/keystores/$(basename $KEYSTORE_LOCATION)" -d ${keystore_path}
  unzip -j domibus-msh-distribution-${DOMIBUS_VERSION}-sample-configuration-and-testing.zip "conf/domibus/keystores/$(basename $TRUSTSTORE_LOCATION)" -d ${truststore_path}
  popd
}

# install plugins using DOMIBUS_PLUGINS env variable
function install_plugins() {
  pushd ${DOMIBUS_INSTALL}
  # remove all plugin if exists
  rm -r ${DOMIBUS_HOME}/plugins/ || true
  # create plugin directories
  mkdir -p ${DOMIBUS_HOME}/plugins/config
  mkdir -p ${DOMIBUS_HOME}/plugins/lib
	for i in $(echo ${DOMIBUS_PLUGINS} | sed "s/,/ /g")
  do
    unzip -j ${DOMIBUS_INSTALL}/domibus-msh-distribution-${DOMIBUS_VERSION}-default-${i}-plugin.zip conf/domibus/plugins/config/tomcat/* -d ${DOMIBUS_HOME}/plugins/config
    unzip -j ${DOMIBUS_INSTALL}/domibus-msh-distribution-${DOMIBUS_VERSION}-default-${i}-plugin.zip conf/domibus/plugins/lib/* -d ${DOMIBUS_HOME}/plugins/lib
	done
  popd
}


run_liquibase() {
  echo ; echo "Starting Liquibase update: liquibase  $LIQUIBASE_OPTS update"
  eval "/opt/liquibase/liquibase $LIQUIBASE_OPTS update"
  echo ; echo "Liquibase update completed"
}

update_database() {
  LIQUIBASE_OPTS="$LIQUIBASE_OPTS --defaultsFile=/liquibase.properties"
  if [[ -n "$DATABASE_SERVER" ]]; then
    DATABASE_URL="jdbc:mysql://${DATABASE_SERVER}:${DATABASE_PORT}/${DATABASE_SCHEMA}"
  fi


  echo -n > /liquibase.properties

  echo "parameter.DomibusVersion: ${DOMIBUS_VERSION}" >> /liquibase.properties
  echo "parameter.DomibuDomibusBuildTimesVersion: ${DOMIBUS_TIMESTAMP}" >> /liquibase.properties

  if [[ -n "$LIQUIBASE_LOG_LEVEL" ]]; then
    echo "logLevel: ${LIQUIBASE_LOG_LEVEL}" >> /liquibase.properties
  fi

  ## Database driver
  if [[ -n "$MYSQL_DRIVER" ]]; then
    echo "driver: ${MYSQL_DRIVER}" >> /liquibase.properties
  fi

  ## Classpath
  if [[ -n "$MYSQL_DRIVER_PATH" ]]; then
    echo "classpath: ${MYSQL_DRIVER_PATH}:/" >> /liquibase.properties
  fi

  ## Database url
  if [[ -n "$DATABASE_URL" ]]; then
    echo "url: ${DATABASE_URL}?useSSL=false" >> /liquibase.properties
  fi

  ## Database username
  if [[ -n "$DATABASE_USERNAME" ]]; then
    echo "username: ${DATABASE_USERNAME}" >> /liquibase.properties
  fi

  ## Database password
  if [[ -n "$DATABASE_PASSWORD" ]]; then
    echo "password: ${DATABASE_PASSWORD}" >> /liquibase.properties
  fi

  ## Database contexts
  if [[ -n "$LIQUIBASE_CONTEXTS" ]]; then
    echo "contexts: ${LIQUIBASE_CONTEXTS}" >> /liquibase.properties
  fi

  ## Database changelog file
  if [[ -n "$LIQUIBASE_CHANGELOG_PATH" ]]; then
    echo "changeLogFile: ${LIQUIBASE_CHANGELOG_PATH}/master.xml" >> /liquibase.properties
  fi

  run_liquibase
}



configure_domibus() {

  if [[ -n "$DATABASE_USERNAME" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.datasource.user=${DATABASE_USERNAME}"
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.datasource.xa.property.user=${DATABASE_USERNAME}"
  fi

  if [[ -n "$DATABASE_PASSWORD" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.datasource.xa.property.password=${DATABASE_PASSWORD}"
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.datasource.password=${DATABASE_PASSWORD}"
  fi

  if [[ -n "$CHECK_DEFAULT_PASSWORD" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.passwordPolicy.checkDefaultPassword=${CHECK_DEFAULT_PASSWORD}"
  fi

  if [[ -n "$CERT_ALIAS" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.security.key.private.alias=${CERT_ALIAS}"
  fi

  if [[ -n "$FOUR_CORNER_MODEL" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.fourcornermodel.enabled=${FOUR_CORNER_MODEL}"
  fi

  if [[ -n "$KEY_PRIVATE_PASSWORD" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.security.key.private.password=${KEY_PRIVATE_PASSWORD}"
  fi

  if [[ -n "$KEYSTORE_LOCATION" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.security.keystore.location=${KEYSTORE_LOCATION}"
  fi

  if [[ -n "$KEYSTORE_TYPE" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.security.keystore.type=${KEYSTORE_TYPE}"
  fi

  if [[ -n "$KEYSTORE_PASSWORD" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.security.keystore.password=${KEYSTORE_PASSWORD}"
  fi

  if [[ -n "$TRUSTSTORE_LOCATION" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.security.truststore.location=${TRUSTSTORE_LOCATION}"
  fi

  if [[ -n "TRUSTSTORE_TYPE" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.security.truststore.type=${TRUSTSTORE_TYPE}"
  fi

  if [[ -n "$TRUSTSTORE_PASSWORD" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.security.truststore.password=${TRUSTSTORE_PASSWORD}"
  fi


  if [[ -n "$QUEUE_REPLY" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Djmsplugin.queue.reply=${QUEUE_REPLY}"
  fi

  if [[ -n "$QUEUE_CONSUMER_NOTIFICATION_ERROR" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Djmsplugin.queue.consumer.notification.error=${QUEUE_NOTIFICATION_ERROR}"
  fi

  if [[ -n "$QUEUE_PRODUCER_NOTIFICATION_ERROR" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Djmsplugin.queue.producer.notification.error=${QUEUE_PRODUCER_NOTIFICATION_ERROR}"
  fi

  if [[ -n "$DEFAULT_PASSWORD_EXPIRATION" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.passwordPolicy.defaultPasswordExpiration=${DEFAULT_PASSWORD_EXPIRATION}"
  fi

  if [[ -n "$PASSWORD_EXPIRATION" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.passwordPolicy.expiration=${PASSWORD_EXPIRATION}"
  fi

  if [[ -n "$PASSWORD_AUTO" ]]; then
    DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.passwordPolicy.defaultUser.autogeneratePassword=${PASSWORD_AUTO}"
  fi



  [ -z "${DATABASE_SERVER}" ] && echo "DATABASE_SERVER is not set correctly" && exit 1;
  [ -z "${DATABASE_PORT}" ] && echo "DATABASE_PORT is not set correctly" && exit 1;
  [ -z "${DATABASE_SCHEMA}" ] && echo "DATABASE_SCHEMA is not set correctly" && exit 1;
  [ -z "${DOMIBUS_HOME}" ] && echo "DOMIBUS_HOME is not set correctly" && exit 1;
  [ -z "${ACTIVEMQ_HOST}" ] && echo "ACTIVEMQ_HOST is not set correctly" && exit 1;


  DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.database.serverName=${DATABASE_SERVER}"
  DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.database.port=${DATABASE_PORT}"
  DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.database.schema=${DATABASE_SCHEMA}"
  DOMIBUS_OPTS="${DOMIBUS_OPTS} -Ddomibus.config.location=${DOMIBUS_HOME}"
  DOMIBUS_OPTS="${DOMIBUS_OPTS} -DactiveMQ.broker.host=${ACTIVEMQ_HOST}"

  export CATALINA_OPTS="${CATALINA_OPTS} ${DOMIBUS_OPTS}"
  echo ; echo ">>> CATALINA_OPTS: $CATALINA_OPTS"
}


# check to see if this file is being run or sourced from another script
_is_sourced() {
	# https://unix.stackexchange.com/a/215279
	[ "${#FUNCNAME[@]}" -ge 2 ] \
		&& [ "${FUNCNAME[0]}" = '_is_sourced' ] \
		&& [ "${FUNCNAME[1]}" = 'source' ]
}


_main() {
	# if first arg is run
	if [[ "${1}" = 'run' ]]; then
		set -- "catalina.sh" "$@"
	fi

	if [[ "$1" = 'catalina.sh' ]]; then
    if [[ "${DOMIBUS_MODE}" = "test" ]]; then
      install_testconfiguration
    fi
    install_plugins
    configure_domibus
    update_database

    echo ; echo "Starting Tomcat: $CATALINA_HOME/bin/catalina.sh run $CATALINA_OPTS"
    set -- "$@" "-DCATALINA_OPTS=$CATALINA_OPTS"
	fi

	exec "$@"
}

if ! _is_sourced; then
	_main "$@"
fi
