
# A Build Lifecycle is Made Up of Phases

Each of these build lifecycles is defined by a different list of build phases, wherein a build phase represents a stage in the lifecycle.

For example, the default lifecycle has the following build phases (for a complete list of the build phases, refer to the Lifecycle Reference):

* `validate` - validate the project is correct and all necessary information is available compile - compile the source code of the project
* `test` - test the compiled source code using a suitable unit testing framework. These tests should not require the code be packaged or deployed
* `package` - take the compiled code and package it in its distributable format, such as a JAR.
* `integration-test` - process and deploy the package if necessary into an environment where integration tests can be run
* `verify` - run any checks to verify the package is valid and meets quality criteria
* `install` - install the package into the local repository, for use as a dependency in other projects locally
* `deploy` - done in an integration or release environment, copies the final package to the remote repository for sharing with other developers and projects.


These build phases (plus the other build phases not shown here) are executed sequentially to complete the default lifecycle. Given the build phases above, this means that when the default lifecycle is used, Maven will first validate the project, then will try to compile the sources, run those against the tests, package the binaries (e.g. jar), run integration tests against that package, verify the package, install the verified package to the local repository, then deploy the installed package in a specified environment.

To do all those, you only need to call the last build phase to be executed, in this case, deploy:

```
mvn deploy
```

That is because if you call a build phase, it will execute not only that build phase, but also every build phase prior to the called build phase. Thus, doing

```
mvn verify
```

will do every build phase before it (validate, compile, package, etc.), before executing integration-test.

There are more commands that are part of the lifecycle, which will be discussed in the following sections.

It should also be noted that the same command can be used in a multi-module scenario (i.e. a project with one or more subprojects). For example:

```
mvn clean install
```

This command will traverse into all of the sub-projects and run clean, then install (including all of the prior steps).

## Environment Profile

|	Profile	|	Description	|
| -------------- | ------------------ |
| development (default) | This profile is used exclusively for development environment, i.e locally on computer of developer. it activate maven profile `development` (can be activate with parameter `env=dev`) and allow to deploy on local tomcat. |
| integration | This profile activate the maven profile `integration` and allow to deploy a war or distribution in general on the `integration` environment (can be activate with parameter `env=int`). |
| qa | This profile activate the maven profile `qa` and allow to deploy a war or distribution in general on the `qa` environment (can be activate with parameter `env=qa`). |
| production | this is default profile used to generate the distribution|

## Additionnel Profile

|	Profile |	Description	|
| --------------- | ------------------ |
| no-proxy |   This profile restores default **npm** registry server. During the phase `process-resources` in *tpd-web* module, the *frontend-maven-plugin* plugin downloads **npm** and **nodejs**, installs **npm** dependencies and builds the front end of application. this plugin is configured to use internal (PMI network) maven repository. |
| full | This profile allows to execute a maven command on all modules. |
| site-deploy |  Associated in general to `site:deploy` phase Activates the deployment of site. |
| skipTests | Skips `test` and `integration-test` phases. |
| release-offline | Associated in general to `release` phase, it allows to release locally (without modify the remote repository). |

## Working outside PMI network 

the front end use Nexus Repository  to retrieve npm and node packages. Use the profile `no-proxy`, if you have to work outside PMI network.

## Test Coverage in a CI environment

There are two recommended ways to utilize Clover's test coverage in a CI ([Continuous Integration](http://en.wikipedia.org/wiki/Continuous_integration)) environment, either using a Profile, or to run the goals directly.

NB. Clover Test Optimization will not work if you have added the maven-clover2-plugin to the default build section of the pom with an execution binding the `instrument` goal.

### Setting up a CI profile

1. Add a `clover` profile to the project's pom.xml.
2. Create a new '**Gateway**' build plan in your CI server. A 'Gateway' build plan is one that gets run before any others and if successful, triggers any subsequent builds.
3. The gateway plan should execute the verify phase, with the `clover` profile activated. Example:

```
clean clover2:setup verify clover2:aggregate clover2:clover -Pclover,full
```

```
mvn site -Pclover,full
```

### Clover

[Clover Best Practices](https://confluence.atlassian.com/display/CLOVER/Best+Practices+for+Maven)

Clover is a mave plugin giving the possibility to produce a coverage report according to your test. Preferred `test` phase for generate clover database that is use during the `site` phase to produce a clover report.

```bash
$ mvn clean clover2:setup test clover2:aggregate clover2:clover -Pclover,full -DskipITs=true
```

The phase `verify` execute unit and integration test and installs in your maven repository the generated modules (as war,jar,ear) with `clover` classifier.  If, instead, you want to skip only the integration tests being run by the Failsafe Plugin, you would use the `skipITs` property instead: 

```bash
$ mvn clean test -Pclover -DskipITs=true
```

Note:

```
export MAVEN_OPTS="-Xmx1024m"
```

### Generate Site and deploy

```bash
$ mvn clean verify -Pfull,clover -DskipITs=true
...
[INFO] Reactor Summary:
[INFO] 
[INFO] TPD RESTful API project ..................... SUCCESS [  2.056 s]
[INFO] TPD Code Checkers ........................... SUCCESS [  1.689 s]
[INFO] tpd-core .................................... SUCCESS [ 44.808 s]
[INFO] tpd Maven Webapp ............................ SUCCESS [01:05 min]
[INFO] Ear Module .................................. SUCCESS [  5.717 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------

$ mvn site site:deploy -Pfull,site-deploy,clover -Dsite.deploy.url=file:///Users/devacfr/site
```