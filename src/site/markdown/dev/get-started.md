# Getting Started

This page takes you step-by-step through creating your local development environment. The purpose of this page is to guide you through setting up your machine environment and installation of the SDK. It also teaches you about the tools, concepts, and basic processes used to maintain the project. When you complete this page, you will have built and deployed TPD Submission Tool application.

The Project requires a good programming knowledge:

* You **should** have a good understanding of the shell (command line environment) for your operating system.
* You **should** have a good level with:
  * Languages as Java, Javascript, sass, css, bash and DOS shell,
  * Developer tools as Maven (expert), Git and Eclipse,
  * Front end tools as npm or NodeJs,
* You **should** have good level in process development as `Countinuoius Integration` and the `Feature Branch Workflow`.

## Philip Morris International(PMI) requirements

Before begin to develop on TPD Submission Tools in PMI domain, you have to do the following steps:

* Request for a VDI with a minimum 8GB of memory.
* Request an administration account (a-).
* Request an administration permission on your machine with the created administration account.
* Request an Atlassian suite access with roles in IMDL :
  * CH 1002 RD Atlassian Bitbucket Users.
  * CH 1002 RD Atlassian Confluence User.
  * CH 1002 RD Atlassian Jira Users.
* Request for an extended internet access with role in IMDL:
  * Extended Internet Access for prolonged requirement.

## Supported platforms

You must make sure your Microsoft Windows system has following requirements:

* Microsoft Windows 10 x64
* Memory 8GB+ (16GB recommended),

## Set up the SDK Prerequisites on a Windows System

Before you install you software developer kit (SDK), you must make sure your Microsoft Windows system has the prerequisite software. For all actions in this document, it is recommended to have administrator access on your machine.

* You are need create a predefined structure folders on your computer. The `<root development directory>` is *C:\development* on windows and "/development* on unix, linux or Mac OS X. Here is the list folder you should create in your `<root development directory>`.

| Folder   | Description                                               |
| -------- | --------------------------------------------------------- |
| Java     | Contains all jre and jdk                                  |
| projects | Contains all projects                                     |
| tools    | Contains all development tools as Eclipse, maven, git,... |
| repo     | The Maven repository location.                            |
| servers  | Contains all server used as Tomcat, Weblogic,...          |

* Verify the Java Developer Kit 13 (JDK) is Installed and be sure that your maven toolchains file `${HOME}/.m2/toolchains.xml` is configured with JDK 13.
* Download and install  [Eclipse IDE for Java EE Developers](https://eclipse.org/downloads/), this package contains you need as maven plugin, git. See [Set Up the Eclipse IDE for Windows](#dev-setup-eclipse)
* Download and install [Download Apache Maven 3.2.x](https://maven.apache.org/download.cgi), see [Working with Maven](#dev-working-with-maven) section below for configuration.
* Download and install the last version of Git Windows client [SourceTree](https://www.sourcetreeapp.com/download/). SourceTree use a embedded Git client.
* Download and install the same [Git](https://git-scm.com/downloads) version used by `SourceTree`, See in Preferences > Git Panel. 
* Download and install [Docker CE](https://www.docker.com/community-edition#/download), Docker allow to execute functional test using Selenium Grid Hub.

<div class="alert alert-warning">
    <p><strong>Docker not supported in VDI</strong></p>
    <p>Docker does not work on Virtual Desktop Infractrusture (<a class="alert-link" href="https://www.citrix.com/virtualization/vdi.html">VDI</a>). Hyper-V and VirtualBox do not work!!!</p>
</div>

* Download and install [NodeJS](http://nodejs.org/) (with [NPM](https://www.npmjs.org/)). The command line tool npm is a package management solution for Javascript-based development. It is used to create and use node packaged modules and is built into the popular Javascript platform [Node.js](http://www.nodejs.org/), which is mostly used for server-side application development.

The npm registry at [https://www.npmjs.org/](https://www.npmjs.org/) is the default package registry, from which components can be retrieved. It contains a large number of open source packages for Node.js based server-side application development and many other packages for a variety of use cases.

In order for your npm command line client to work with Artifactory run the following command to replace the default npm registry with an Artifactory repository:

```bash
npm config set registry https://rd-artifactory.app.pmi/artifactory/api/npm/npm-registry/
```

## Clone TPD Submission Tool project

Now, you can clone the project via SourceTree Git Client. Start SourceTree and do the following:

1. **Click** on button **Clone/New**. And enter **Source Path** `ssh://git@rd-bitbucket.app.pmi:7999/tpd/tpd.git`. The Destination path must be `C:\development\projects\tpd`.

![Clone Project](images/dev/sourcetree-clone.png)

1. Click button **Clone**. Waiting a while, when finish. You can quit SourceTree.

## Import TPD Submission Tool into Eclipse

Now, import your project into the Eclipse IDE. Start Eclipse and do the following:

1. **Click right** in **Package Explorer Panel** and select **Import...**
2. Select the import source **Existing Maven Projects** and click **Next**.
3. **Browse** at root of the project ; normally the path should be *&lt;root development directory&gt;\projects\tpd*.

![Maven Import](images/dev/eclipse-maven-import.png)

1. Click **Finish**.

## Project Configuration in Eclipse

Once the project is imported (take a while.. long time). The workspace has need to be configured:

* **Suspend all validators**, *Window &gt; Preferences &gt; Validation*.

![Suspend all validators](images/dev/eclipse-suspend-validators.png)

* **Disable Spring Data validator**, *Window &gt; Preference &gt; Spring &gt; Validation*.

![Disable Spring Data validator](images/dev/eclipse-spring-data-validator.png)

* Import specific java code style formatter. 
    * Click menu *Window &gt; Preferences &gt; Java &gt; Code Style &gt; Formatter*
    * Click **Import....** button and select the file *&lt;root project&gt;/eclipse/java-code-style-formatter.xml*.
* Import specific java code style clean Up.
    * Click menu *Window &gt; Preferences &gt; Java &gt; Code Style &gt; Clean Up*
    * Click **Import....** button and select the file *&lt;root project&gt;/eclipse/java-code-style-cleanup.xml*.
* Import specific javascript code style formatter.
    * Click menu *Window &gt; Preferences &gt; JavaScript &gt; Code Style &gt; Formatter*
    * Click **Import....** button and select the file *&lt;root project&gt;/eclipse/javascript-code-style-formatter.xml*.
* Import specific javascript code style clean Up.
    * Click menu *Window &gt; Preferences &gt; JavaScript &gt; Code Style &gt; Clean Up*
    * Click **Import....** button and select the file *&lt;root project&gt;/eclipse/javascript-code-style-cleanup.xml*.
* **Add** the modules tpd-web to Tomcat Server in Eclipse, check the server option **Server modules without publishing** and modify settings for publishing at **Never**.
* Select `development` Maven profiles for all modules.

## Front end configuration

The process to generate the frontend can take long time (very very long time, depending the power of your machine). The frontend is produce in *&lt;root project&gt;/tpd-web/target/frontend* directory ; this is a configuration of `frontend-maven-plugin`.

`frontend` maven plugin execute following command:

* Install the NodeJS dependencies: `npm install`.
* Run the gulp build task: `gulp build`.

To reduce the compilation time, this plugin is executed only during the package phase. But the frontend is not yet available for wtp deployment in eclipse, you have to open a command line terminal and execute following command:

```bash
$ cd tpd-web
$ gulp build --eclipse (or -e) --debug (or -d)
```

The option `--eclipse` force `gulp` to generate the front end to directory *target/m2e-wtp/web-resources*.
The option `--debug` force `gulp` to generate the front end without minification and uglification of javascript.

Gulp will build any changes made automatically, and also run a live reload serve. For this, prefer this command:

```bash
$ gulp run --eclipse (CTRL+C to quit)
```

You can start Tomcat Server and go to [http://localhost:8080/tpd](http://localhost:8080/tpd)
