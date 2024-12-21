# Set Up the Eclipse IDE for Windows

So far, you've configured your environment. On this section, you configure the Eclipse IDE.

<div class="alert alert-info">
    <p><strong>Do I have to Use Eclipse?</strong></p>
    <p>
If you already have a version of Eclipse installed you don't need to do this step. Note that the tutorial assumes a particular version of Eclipse, so you should anticipate that you may need to adjust procedures if you choose to use another version of it. If you aren't interested in using Eclipse and prefer another IDE, you are perfectly free to skip this page. However, the rest of this tutorial assumes you are using Eclipse.
    </p>
</div>

## Install the Eclipse IDE

In this step, you download and install the Eclipse IDE for Java EE Developers. This version of Eclipse comes with most of the Maven dependencies already installed. Do the following to install Eclipse in your system:

1. Download the [Eclipse IDE for Java EE Developers](https://eclipse.org/downloads/). This IDE has many of the dependencies required by the Maven Eclipse plugin.
2. Expand the ZIP file into `<tools development directory>`.

## Configure the Eclipse Plugin to start under JDK 1.8

1. Make a note of the location of your JDK 1.8 installation. Your root should be similar to: C:\development\Java\jdk1.8.0_101.
2. Navigate to the root of the Eclipse installation.
3. Make a copy of the *eclipse.ini* file.
4. Edit the *eclipse.ini* file with your favorite text editor.
5. Add a `-vm` entry to file before any `-vmargs` entry. The entry should point to the *bin* directory of your JDK.  The *eclipse.ini* file requires that you reverse the slashes from back to forward slashes.  When you are done the file will look similar to the following:

```text
-startup
plugins/org.eclipse.equinox.launcher_1.2.0.v20110502.jar
--launcher.library
plugins/org.eclipse.equinox.launcher.win32.win32.x86_1.1.100.v20110502
-product
org.eclipse.epp.package.jee.product
--launcher.defaultAction
openFile
-showsplash
org.eclipse.platform
--launcher.defaultAction
openFile
-vm
C:/development/Java/jdk1.8.0_101/bin
-vmargs
-Dosgi.requiredJavaVersion=1.5
-Xms40m
-Xmx512m
```

## Start Eclipse and update the Installed JREs

Start Eclipse and do the following:

1. Choose **Windows** > **Preferences** from the Eclipse menu bar. The system displays the preferences dialog.
2. Filter for or navigate to the **Installed JREs** page.
3. Click the **Add** button. The **Add JRE** wizard displays.
4. Make sure **Standard VM** is selected and press Next.
5. Press **Directory**. The **Browse For Folder** dialog appears.
6. Navigate to your JDK installation.
7. Press **OK**.
	
	Eclipse locates all the libraries. At this point the dialog should look similar to the following:
	
	![Add JRE](images/dev/add-jre.png)

8. Press **Finish**. The system returns you to the **Installed JREs** page.
9. Check the JDK you just added.
	
	The dialog should look similar to this:
	
	![Installed JRE](images/dev/installed-jre.png)

## Configure the Maven Plugin

You need to ensure that the M2E plugin is correctly configured:

1. Choose **Windows > Preferences** from the Eclipse menu bar. The system displays the preferences dialog.
1. Filter for or navigate to the **Maven > Installations** page.
1. Click the **Add** button. The Maven Installation dialog displays.
1. Browse to your *C:\development\tools\apache-maven* installation.
1. Press **OK**.
	
	The system sets this external repository for you. The dialog should look like the following:
	
	![Install Maven Local](images/dev/install-local-maven.png)
	
1. Ensure the **Global settings** are coming from the installation directory.
	
	![User Settings](images/dev/maven-user-settings.png)
	
1. Press **Apply**.
1. Click the Maven root.
1. Uncheck Download repository index updates on startup. This prevents Maven from updating on Eclipse startup which can be time consuming.
1. Press **OK** to close the dialog.

## Installation and Configure Lombok Eclipse Plugin

The offical lombok website can be found here: [https://projectlombok.org/](projectlombok).

Lombok is used to reduce boilerplate code for model/data objects, e.g., it can generate getters and setters for those object automatically by using Lombok annotations. The easiest way is to use the @Data annotation.

You need close Eclipse before and follow instructions in following page [https://projectlombok.org/setup/eclipse](Eclipse Lombok Installation).

## Installation and Configure CheckStyle Eclipse Plugin

After the Eclipse restarts, you need to install CheckSyle plugin:

1. Open **Eclipse Marketplace...** in menu **Help** and find in typing *checkstyle*.

	![CheckStyle MarketPlace](images/dev/checkstyle-marketplace.png)

1. Click on button *Install* and follow instruction.
1. After restart, you need configure CheckStyle preferences, **Window > Preference > CheckStyle**.
1. In CheckStyle preferences, click on button **New...** and configure as following:

	![CheckStyle new](images/dev/checkstyle-new.png)

1. Press **OK** to close the dialog.
1. Once configure, the system sets CheckStyle Plugin for you. The dialog should look like the following:

	![CheckStyle settings](images/dev/checkstyle-settings.png)

## Installation EditorConfig Eclipse Plugin

You need to install EditorConfig plugin:

1. Open **Eclipse Marketplace...** in menu **Help** and find in typing *editorconfig*.

	![Editor Config new](images/dev/editorconfig-marketplace.png)

1. Click on button *Install* and follow instruction.
1. After restart, the project will be automatically configured with `.editorconfig` file.

## Set Up a DOS Shell in Eclipse

Once you have Eclipse configured to use the project, you would still need to keep a DOS command prompt open in which to run each command. This is very handy if you want an "all in one" workspace. In this step, you create an external tool configuration that opens the DOS command prompt in an Eclipse console window. In this window, you can enter the atlas commands.

If you haven't already done so, start Eclipse and then do the following:

1. Make sure your workspace is set to your project. You can use **File > Switch Workspace > Other** to switch if you need to. You need to do this because run configurations are associated with a workspace.
1. Click to **Run > External Tools > External Tools Configuration**... from the Eclipse menu bar. The** External Tools Configuration** dialog appears.
1. Select **Programs** and press **New launch configuration**.
	
	![External Tools Configuration](images/dev/external-tools-config.png)
	
	The system creates a new configuration and places you in a configuration dialog.
	
1. Name the new configuration **DOS cmd Prompt**.
1. Click **Browse File System...**.
1. Navigate to the location of the *cmd.exe* program. This should be in the *C:\WINDOWS\system32\cmd.exe* directory.
1. Add the *${workspace_loc}* variable to the Working Directory section.
	
	When you are done, the dialog will appear as follows: 
	
	![Dos cmd Prompt](images/dev/dos-cmd.png)
	
1. Click the **Common** tab.
1. Check **External Tools** under **Display** in **Favorites** menu.
1. Make sure the **Allocate console** (necessary for input) option is checked.
1. Press **Apply** to save your configuration.
1. Press **Close** to close the dialog.

Go ahead and try your new configuration.  Launch the DOS cmd Prompt from within Eclipse:

![Run Dos cmd](images/dev/run-as-dos-cmd.png)

You should see the prompt appear in an Eclipse console:

![Prompt Dos cmd](images/dev/prompt-dos-cmd.png)

The console doesn't support everything a regular DOS window does, but it does everything you need for most of your work.

## Add Tomcat Server and locate in Eclipse

Before import the project, install locally a instance of Tomcat Server in *&lt;root development directory&gt;\server*.
In Eclipse create a new Tomcat 8 server and set the location of your Tomcat Server and used JRE.

### Tomcat Server Configuration

You are need adding access roles to the Tomcat Manager for integration testing execution and the automatic deployment.

It would be quite unsafe to ship Tomcat with default settings that allowed anyone on the Internet to execute the Manager application on your server. Therefore, the Manager application is shipped with the requirement that anyone who attempts to use it must authenticate themselves, using a username and password that have one of **manager-\*\*** roles associated with them (the role name depends on what functionality is required). Further, there is no username in the default users file (`$CATALINA_BASE/conf/tomcat-users.xml`) that is assigned to those roles. Therefore, access to the Manager application is completely disabled by default.

You can find the role names in the `web.xml` file of the Manager web application. The available roles are:

* `manager-gui` — Access to the HTML interface.
* `manager-status` — Access to the "Server Status" page only.
* `manager-script` — Access to the tools-friendly plain text interface that is described in this document, and to the "Server Status" page.
* `manager-jmx` — Access to JMX proxy interface and to the "Server Status" page.

Modify the file `$CATALINA_BASE/conf/tomcat-users.xml` like sample below:

```xml
<tomcat-users>
 ...
  <role rolename="manager-script"/>
  <role rolename="manager-jmx"/>
  <role rolename="manager-gui"/>
  <role rolename="manager-status"/>
  <user username="tomcat" password="tomcat" roles="tomcat,manager-gui,manager-jmx,manager-status,manager-script"/>
...
</tomcat-users>
```
