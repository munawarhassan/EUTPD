FROM tomcat:9-jdk11-temurin

LABEL PROJECT_NAME="${application.fullname}"


# Enabling JMX counters for performance monitoring
# Controls whether registers its management interfaces and those of its libraries with JMX.
# Note: Some libraries used by application may register themselves with JMX regardless of this setting.
# GRAPHITE_ENABLED: allows to connect application with graphite monitoring tool to http://graphite:2003
ENV DOCKERIZE_VERSION="v0.6.1" \
    TPD_HOME="/usr/share/tpd/home" \
    TPD_PROFILES="production" \
    APP_LOG_LOGLEVEL="WARN" \
    JMX_ENABLED="false" \
    METRICS_GRAPHITE_ENABLED="false" \
    JDK_JAVA_OPTIONS="${jdk.jdkoptions}"

RUN curl -LJO https://github.com/jwilder/dockerize/releases/download/${DOCKERIZE_VERSION}/dockerize-linux-amd64-${DOCKERIZE_VERSION}.tar.gz && \
    tar -C /usr/local/bin -xzvf dockerize-linux-amd64-${DOCKERIZE_VERSION}.tar.gz && \
    rm dockerize-linux-amd64-${DOCKERIZE_VERSION}.tar.gz


# replace the root context
COPY maven/ /usr/local/tomcat/webapps/

VOLUME /usr/share/tpd/home

EXPOSE 8080

# Production health check, doesn't work in dev, SMTP not working on local PMI machine.
# HEALTHCHECK --interval=30s --timeout=30s --start-period=60s --retries=6\
#  CMD curl --silent http://localhost:8080/rest/endpoint/health 2>&1 | grep UP

HEALTHCHECK --interval=30s --timeout=30s --start-period=60s --retries=6 \
  CMD curl --silent  http://localhost:8080/rest/api/status 2>&1 | grep RUNNING

CMD ["catalina.sh", "run"]
