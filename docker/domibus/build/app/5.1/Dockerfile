FROM tomcat:9-jdk11-temurin-jammy

ARG DOMIBUS_VERSION
ARG MYSQL_DRIVER_VERSION
ARG LIQUIBASE_VERSION
ARG DOCKERIZE_VERSION

ENV DOMIBUS_VERSION="${DOMIBUS_VERSION}" \
    # domibus build timestamp, used in liquibase parameters
    DOMIBUS_TIMESTAMP="2023-03-24 16:41" \
    # Dokerize version
    DOCKERIZE_VERSION="${DOCKERIZE_VERSION}" \
    # mysql driver version
    MYSQL_DRIVER_VERSION="${MYSQL_DRIVER_VERSION}" \
    # mysql driver used
    MYSQL_DRIVER="com.mysql.cj.jdbc.Driver" \
    # location of mysql driver used by Liquibase
    MYSQL_DRIVER_PATH="/usr/share/java/mysql.jar" \
    # Liquibase version
    LIQUIBASE_VERSION="${LIQUIBASE_VERSION}" \
    # Liquibase Location
    LIQUIBASE_HOME="/opt/liquibase" \
    # Liquibase changesets location to execute
    LIQUIBASE_CHANGELOG_PATH="/opt/liquibase/changelog" \
    # Liquibase log level
    LIQUIBASE_LOG_LEVEL="info" \
    # Liquibase contexts to use
    LIQUIBASE_CONTEXTS="" \
    # Liquibase options
    LIQUIBASE_OPTS="" \
    # Global Liquibase parameter is an integer value that sets the amount of time Liquibase waits for a locked database to become available. The default value is 5.
    LIQUIBASE_CHANGELOG_LOCK_WAIT_TIME_IN_MINUTES="2" \
    # Database server name: domibus.database.serverName
    DATABASE_SERVER="db" \
    # Database shema: domibus.database.schema
    DATABASE_SCHEMA="domibus" \
    # Database port: domibus.database.port
    DATABASE_PORT="3306" \
    # Database username: domibus.datasource.user,domibus.datasource.xa.property.user
    DATABASE_USERNAME="edelivery" \
    # Database password: domibus.datasource.password,domibus.datasource.xa.property.password
    DATABASE_PASSWORD="edelivery" \
    # enable/disable check default password on sign in: domibus.passwordPolicy.checkDefaultPassword
    CHECK_DEFAULT_PASSWORD="false" \
    # The certificate alias to use: domibus.security.key.private.alias
    CERT_ALIAS="" \
    # finalRecipient and originalSender properties are used in filtering the messages on GUI: domibus.fourcornermodel.enabled
    FOUR_CORNER_MODEL="true" \
    # Private key password: domibus.security.key.private.password
    KEY_PRIVATE_PASSWORD="test123" \
    # Keystore location: domibus.security.keystore.location
    KEYSTORE_LOCATION="/usr/share/domibus/keystore/gateway_keystore.jks" \
    # Keytore password: domibus.security.keystore.password
    KEYSTORE_PASSWORD="test123" \
    # Truststore location: domibus.security.truststore.location
    TRUSTSTORE_LOCATION="/usr/share/domibus/truststore/gateway_truststore.jks" \
    # Truststore password: domibus.security.truststore.password
    TRUSTSTORE_PASSWORD="" \
    # List of Domibus system properties
    DOMIBUS_OPTS="" \
    # Execution mode ['test'...]
    DOMIBUS_MODE="" \
    # List of plugins to install in startup [jms,ws,fs]
    DOMIBUS_PLUGINS="jms,ws" \
    # Password expiration policy in days (0 to disable)
    PASSWORD_EXPIRATION="90" \
    #Default user password generation policy enabled/disabled
    PASSWORD_AUTO="false" \
    # Default password expiration policy in days (0 to disable)
    DEFAULT_PASSWORD_EXPIRATION="15" \
    # Location of Domibus configuration: domibus.config.location
    DOMIBUS_HOME="/usr/share/domibus/home" \
    # Prerequisite location (plugins, test configuration,...)
    DOMIBUS_INSTALL="/usr/share/domibus/install" \
    # Tomcat options
    CATALINA_OPTS="" \
    # ActiveMQ host: activeMQ.broker.host
    ACTIVEMQ_HOST="0.0.0.0" \
    # Edelivery maven repository
    EDELIVERY_REPO="https://ec.europa.eu/digital-building-blocks/artifact/content/repositories/eDelivery" \
    # Maven Central repository
    MAVEN_CENTRAL="https://repo1.maven.org/maven2"


RUN set -x \
    # update and install commands
    && apt-get update \
    && apt-get install -yq --no-install-recommends wget unzip \
    # install mysql driver
    && mkdir -p /usr/share/java \
    && wget -q -O${MYSQL_DRIVER_PATH} "${MAVEN_CENTRAL}/mysql/mysql-connector-java/${MYSQL_DRIVER_VERSION}/mysql-connector-java-${MYSQL_DRIVER_VERSION}.jar" \
    && wget -q -O/tmp/liquibase.tar.gz "https://github.com/liquibase/liquibase/releases/download/v${LIQUIBASE_VERSION}/liquibase-${LIQUIBASE_VERSION}.tar.gz" \
    # install Liquibase
    && mkdir -p ${LIQUIBASE_HOME} \
    && tar -xzf /tmp/liquibase.tar.gz -C ${LIQUIBASE_HOME} \
    && rm -f /tmp/liquibase.tar.gz \
    && chmod +x ${LIQUIBASE_HOME}/liquibase \
    && ln -s ${LIQUIBASE_HOME}/liquibase /usr/local/bin/ \
    # clean up apt-get
    && apt-get clean \
    && rm -r /var/lib/apt/lists/* \
    # create directory used to store config of domibus
    && mkdir -p ${DOMIBUS_HOME} \
    # create directory used to store installation
    && mkdir -p ${DOMIBUS_INSTALL} \
    # install dockerize command line
    && wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz


# copy scripts
COPY ./scripts ${DOMIBUS_INSTALL}/scripts
WORKDIR ${DOMIBUS_INSTALL}

RUN wget -O${DOMIBUS_INSTALL}/domibus-msh-distribution.zip ${EDELIVERY_REPO}/eu/domibus/domibus-msh-distribution/${DOMIBUS_VERSION}/domibus-msh-distribution-${DOMIBUS_VERSION}-tomcat-war.zip \
    && unzip ${DOMIBUS_INSTALL}/domibus-msh-distribution.zip -d ${DOMIBUS_INSTALL}\
    && rm -f ${DOMIBUS_INSTALL}/domibus-msh-distribution.zip

RUN wget ${EDELIVERY_REPO}/eu/domibus/domibus-msh-distribution/$DOMIBUS_VERSION/domibus-msh-distribution-${DOMIBUS_VERSION}-tomcat-configuration.zip --no-check-certificate \
    #copy the domibus configuration
    && unzip domibus-msh-distribution-${DOMIBUS_VERSION}-tomcat-configuration.zip -d ${DOMIBUS_HOME} \
    && rm domibus-msh-distribution-${DOMIBUS_VERSION}-tomcat-configuration.zip

RUN unzip ${DOMIBUS_INSTALL}/domibus-MSH-tomcat-${DOMIBUS_VERSION}.war -d ${CATALINA_HOME}/webapps/ROOT \
    && rm -f ${DOMIBUS_INSTALL}/domibus-MSH-tomcat-${DOMIBUS_VERSION}.war \
    # remove /domibus context path
    && rm -r ${CATALINA_HOME}/webapps/ROOT/META-INF \
    # remove Tomcat dist webapps
    && rm -r $CATALINA_HOME/webapps.dist
    # install MySql Connector
RUN wget ${MAVEN_CENTRAL}/mysql/mysql-connector-java/${MYSQL_DRIVER_VERSION}/mysql-connector-java-${MYSQL_DRIVER_VERSION}.jar --no-check-certificate \
    &&  mv mysql-connector-java-${MYSQL_DRIVER_VERSION}.jar ${CATALINA_HOME}/lib
    # execute specific installation
RUN chmod +x ${DOMIBUS_INSTALL}/scripts/*.sh \
    && ${DOMIBUS_INSTALL}/scripts/install.sh

# copy the entrypoint
ADD ./entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# copy liquibase all changelogs files
ADD ./changelog/  ${LIQUIBASE_CHANGELOG_PATH}

WORKDIR ${CATALINA_HOME}

# Domibus app
EXPOSE 8080
# AMQ
EXPOSE 61616

HEALTHCHECK --interval=60s --timeout=30s --start-period=60s --retries=15 \
    CMD curl --silent  http://localhost:8080/rest/application/name 2>&1 | grep "Domibus"

ENTRYPOINT ["/entrypoint.sh"]
