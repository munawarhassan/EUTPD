
# Set Up Maven

## Install Apache Maven

In this step, you download and install the Apache Maven.

1. Download the latest release of [Apache Maven][maven-download].
2. Expand the ZIP file into `<tools development directory>`.

[maven-download]: https://maven.apache.org/download.cgi

## Verifying Your Maven Settings

The Maven settings file, *settings.xml*, controls some of the general behavior of Maven. For instance, the file identifies the repositories used to resolve plugin dependencies. It also specifies development environment properties. For example, the *settings.xml* file is where you configure the PMI proxy settings if you need to connect to the Internet through a web proxy.

We are using Artifactory (Maven Central Repository Server) for internal development configured with two groups which contains releases and snapshots. To use it, add proxies, add repositories, and add the following mirror configuration to your Maven settings in `~/.m2/settings.xml/` or `.m2/settings.xml`.
You are **need** access right to Artifactory.

```xml
<servers>
    <server>
        <id>libs-release</id>
        <username>artifactory_ro</username>
        <password>AP3U1Et3NZN4L8wt56Lry64Uj6m</password>
    </server>
    <server>
        <id>libs-snapshot</id>
        <username>artifactory_ro</username>
        <password>AP3U1Et3NZN4L8wt56Lry64Uj6m</password>
    </server>
    <server>
        <id>plugins-release</id>
        <username>artifactory_ro</username>
        <password>AP3U1Et3NZN4L8wt56Lry64Uj6m</password>
    </server>
    <server>
        <id>plugins-snapshot</id>
        <username>artifactory_ro</username>
        <password>AP3U1Et3NZN4L8wt56Lry64Uj6m</password>
    </server>
    <server>
        <id>central</id>
        <username>artifactory_ro</username>
        <password>AP3U1Et3NZN4L8wt56Lry64Uj6m</password>
    </server>
    <server>
        <id>thirdparty</id>
        <username>artifactory_ro</username>
        <password>AP3U1Et3NZN4L8wt56Lry64Uj6m</password>
        </server>
    <server>
        <id>npm-registry-server</id>
        <username>artifactory_ro</username>
        <password>AP3U1Et3NZN4L8wt56Lry64Uj6m</password>
    </server>
</servers>
<mirrors>
    <mirror>
        <id>central</id>
        <mirrorOf>central</mirrorOf>
        <name>central</name>
        <url>https://rd-artifactory.app.pmi/artifactory/maven-virtual</url>
    </mirror>
</mirrors>
<profiles>
    <profile>
        <id>artifactory</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <repositories>
            <repository>
                <id>libs-release</id>
                <name>libs-release</name>
                <url>https://rd-artifactory.app.pmi/artifactory/libs-release</url>
                <snapshots>
                    <enabled>false</enabled>
                </snapshots>
            </repository>
            <repository>
                <snapshots />
                <id>libs-snapshot</id>
                <name>libs-snapshot</name>
                <url>https://rd-artifactory.app.pmi/artifactory/plugins-snapshot</url>
            </repository>
            <repository>
                <id>thirdparty</id>
                <name>rd-artifactory.app.pmi-thirdparty</name>
                <url>https://rd-artifactory.app.pmi/artifactory/ThirdParty</url>
            </repository>
        </repositories>
        <pluginRepositories>
            <pluginRepository>
                <id>plugins-release</id>
                <name>plugins-release</name>
                <url>https://rd-artifactory.app.pmi/artifactory/plugins-release</url>
                <snapshots>
                    <enabled>false</enabled>
                </snapshots>
            </pluginRepository>
            <pluginRepository>
                <snapshots />
                <id>plugins-snapshot</id>
                <name>plugins-snapshot</name>
                <url>https://rd-artifactory.app.pmi/artifactory/plugins-snapshot</url>
            </pluginRepository>
        </pluginRepositories>
    </profile>
</profiles>
<!-- activeProfiles | List of profiles that are active for all builds. -->
<activeProfiles>
    <activeProfile>artifactory</activeProfile>
</activeProfiles>
```

Now, in same file `settings.xml`, you are need configure the location of your maven repository. Add it.

```xml
<localRepository>C:\development\repo</localRepository>
```
