
The zip and tar.gz are constructed by the maven assembly plugin.  The main configuration file is
${application.name}-distribution/src/main/assembly/bin.xml.  The plugin downloads and unzips a tomcat installation, puts the application
war in the root directory and adds a few other config and sh/bat files.

Tomcat files:

conf/server.xml modified from the default tomcat server.xml to add application as the default webapp
start-${application.name} - ensure that  ${app.home.property} is set, run tomcat's start script
stop-${application.name} - run tomcat's stop script


To run:

```
cd ${application.name}-distribution
```

mvn clean package

The result is target/${application.name}-distribution.zip and target/${application.name}-distribution.tar.gz


To install:

Unpack distributable.  Run bin/start-${application.name}[.sh/bat].  ${application.title} is served at http://localhost:${app.http.port}/
