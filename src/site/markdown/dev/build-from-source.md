# Building from Source

The TPD Submission tool uses a maven build system. In the instructions below, ./mvnw is invoked from the root of the source tree and serves as a cross-platform, self-contained bootstrap mechanism for the build.

## Prerequisites

[Git][git] and [JDK 13][jdk]

Be sure that your maven toolchains file `${HOME}/.m2/toolchains.xml` is configured with JDK 13.

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads
[git]: http://help.github.com/set-up-git-redirect

## Check out sources

Clone the TPD project on your local machine and checkout on the branch for work:

```bash
$ git clone ssh://git@rd-bitbucket.app.pmi:7999/tpd/tpd.git
$ git checkout tpd-123-new-feature
```

## Install all jars into your local Maven cache

```shell
$ ./mvnw clean compile
```

## Before submit a Pull Request

Before create a pull request, you have some checks to do.

### Verify Conflict

Verify your branch has no conflict with following command, before add `dry` and `conflict` command in git global config.

```bash
  // add dry and conflict comand to git global config
$ git config --global alias.dry '!f() { git merge-tree `git merge-base $2 $1` $2 $1; }; f'
$ git config --global alias.conflict '!f() { git merge-tree `git merge-base $2 $1` $2 $1 | grep -A3 "changed in both"; }; f'
```

The command will show the changelog for the merge of your feature branch into develop.

```bash
$
  // check how the merge of tpd-456-new-feature into develop
$ git dry tpd-123-new-feature develop
changed in both
  base   100644 e69de29bb2d1d6434b8b29ae775ad8c2e48c5391 first_file
  our    100644 deba01fc8d98200761c46eb139f11ac244cf6eb5 first_file
  their  100644 dc1ff7f95ac4812480edad5ec13d4c1a20066377 first_file
@@ -1 +1,5 @@
+<<<<<<< .our
 something
+=======
well, something else?
+>>>>>>> .their
```

The command is just a shorthand that will limit the output to the lines related to the changed files, without the full changelog.

```bash
$ git conflict tpd-123-new-feature develop
changed in both
  base   100644 e69de29bb2d1d6434b8b29ae775ad8c2e48c5391 first_file
  our    100644 deba01fc8d98200761c46eb139f11ac244cf6eb5 first_file
  their  100644 dc1ff7f95ac4812480edad5ec13d4c1a20066377 first_file
```

If you encounter a conflit, you will must merge `develop` to your branch and resolve manually conflict.

### Run Unit and Integration Tests

Before push your work on Bitbucket, you must ensure your code compile and tests work. For this execute the following command :

```bash
$ ./mvnw clean verify -maven.javadoc.skip=true
```

This command execute all tests in `tpd-api`, `tpd-backend`, `tpd-core` and `tpd-web` modules (excluding all others modules). The system property `maven-javadoc-skip` allows disable the javadoc generation.

### Run Functional Tests

All functional tests are in `func-test` module. there are two methods to execute functional tests locally. first, using a local instance of TPD (i.e, with url `http://localhost:8080/tpd-web`). the second using docker to ship a tpd instance in container and execute.

#### Using a local instance

You must start a instance of TPD with url `http://localhost:8080/tpd-web` (url when you start tomcat in your Eclipse).

> Note:
> The feature is also available running Junit Test in your IDE for create new function test or debuging.

```bash
$ ./mvnw verify -Pfull,func-test -pl tpd-test/func-test
```

#### Using docker

For execute functional tests, docker is wonderful tools. It deploys a selenium server with two nodes, one for Firefox and other for Chrome. Once ready, it deploys TPD submission tools instance.

> Note:
> The project should be compiled once time before with command `./mvnw clean install -Pfull,skipTests -Dmaven-javadoc=true`.

```bash
$ ./mvnw verify -Pfull,docker-func-test -pl tpd-test/func-test
```

**Debugging**

In the event you wish to visually see what the browser is doing you will want to run the `debug` variant of node or standalone images. A VNC server will run on port 5900. You are free to map that to any free external port that you wish. Example: : 5900) you will only be able to run 1 node per port so if you wish to include a second node, or more, you will have to use different ports, the 5900 as the internal port will have to remain the same though as thats the VNC service on the node. The second example below shows how to run multiple nodes and with different VNC ports open:

You can acquire the port that the VNC server is exposed to by running: (In this case our port mapping looks like 49338:5900 for our node)

```bash
$ docker port <container-name|container-id> 5900
#=> 0.0.0.0:49338
```

In case you have [VNC Viewer][vnc-viewer] binary vnc in your path, you can always take a look, view only to avoid messing around your tests with an unintended mouse click or keyboard interrupt:

```bash
$ ./bin/vncview 127.0.0.1:49160
```

When you are prompted for the password it is secret.

[vnc-viewer]: https://www.realvnc.com/en/connect/download/viewer/

### Run TPD Standalone Package

TPD Submission tools project produces a standalone package on a standard Tomcat distribution. To produce the package, execute following command:

```bash
$ ./mvnw clean package -Pfull,skipTests -Dmaven.javadoc.skip=true
```

The package is available in folder `<project path>/tpd-distribution/default/target/`. To run, unflat the package and execute:

_for unix_

```bash
./bin/tpd-start.sh
```

_for windows_

```dos
bin\tpd-start.bat
```

Congratulation, you can go to [http://localhost:7880/](http://localhost:7880/)
