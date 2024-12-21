package com.pmi.tpd.catalina.startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * A shim that decorates arguments to org.apache.catalina.startup.Bootstrap and appends "-config" "/path/to/server.xml"
 * if a server.xml file is found in the currently configured product home or shared home directories. This works by
 * substituting the original Bootstrap class with this class in the various Tomcat scripts used to launch the product /
 * register it as a service on Windows, Linux and OSX.
 * <p>
 * Resolution of the the product home dir is via the "app.home" system property or else the "TPD_HOME" environment
 * variable (in that order).
 * </p>
 * <p>
 * Resolution of the the product shared home dir is (in order): the "app.shared.home" system property or else the
 * "TPD_SHARED_HOME" environment variable or else the "shared" directory under the resolved application home if one was
 * resolved.
 * </p>
 * These mechanisms are analogous to how {@code HomeDirectoryResolver} resolves the two directories.
 */
public class Bootstrap {

    private static final String FALLBACK_SERVER_XML = ".default-server.xml";

    public static final String ENV_VAR_HOME = "TPD_HOME";

    public static final String ENV_VAR_SHARED_HOME = "TPD_SHARED_HOME";

    public static final String SYS_PROP_HOME = "app.home";

    public static final String SYS_PROP_SHARED_HOME = "app.shared.home";

    // Log class not used in declaration to avoid UnsupportedClassVersionError on loading of this class on Java 6
    private static Object log;

    public static void main(final String[] rawArgs) throws Exception {
        final String version = System.getProperty("java.version", "");
        final String[] vers = version.split("\\.");
        final int major = Integer.valueOf(vers[0]);
        if (major < 11) {
            System.out.println("TPD Submission Tool does not support Java '" + version
                    + "'. Please start the product with Java 11 or later");
            System.exit(16);
        }

        initLog();

        try {
            final Class<?> catalinaBootstrap = Class.forName("org.apache.catalina.startup.Bootstrap");
            catalinaBootstrap.getDeclaredMethod("main", String[].class)
                    .invoke(null, new Object[] { new ArgumentDecorator().apply(rawArgs) });
        } catch (final Exception e) {
            logException("Failed dynamically invoking org.apache.catalina.startup.Bootstrap.main(String[])", e);
            System.exit(1);
        }
    }

    // VisibleForTesting
    protected static void initLog() {
        log = LogFactory.getLog(Bootstrap.class);
    }

    private static void logDebug(final String msg) {
        ((Log) log).debug(msg);
    }

    private static void logError(final String msg) {
        ((Log) log).error(msg);
    }

    private static void logException(final String msg, final Exception e) {
        ((Log) log).error(msg, e);
    }

    /**
     * Decorates the arguments to be passed onto org.apache.catalina.startup.Bootstrap.main(String[]) with the
     * server.xml file if one is found in the product home or the product shared home
     */
    protected static class ArgumentDecorator {

        public String[] apply(final String[] rawArgs) {
            boolean isStop = false;
            for (final String arg : rawArgs) {
                if ("-config".equals(arg)) {
                    logDebug("Arguments to Tomcat Bootstrap already contain \"-config\" - not decorating");
                    return rawArgs;
                }
                if ("stop".equals(arg)) {
                    isStop = true;
                }
            }

            String[] args = rawArgs;
            final File homeDir = getHomeDir();
            File serverXml = findServerXmlFile(homeDir);
            if (serverXml == null) {
                serverXml = findServerXmlFile(getSharedHomeDir(homeDir));
            }
            if (serverXml == null) {
                serverXml = findServerXmlFile(getInstallationConfDir());
            }
            if (serverXml == null && !isStop) {
                logError("Copying fallback default-server.xml to shared home; no other server.xml file was found");
                serverXml = copyFallbackServerXmlTo(getSharedHomeDir(homeDir));
            }
            if (serverXml == null) {
                serverXml = findServerXmlFile(getInstallationConfDir(), FALLBACK_SERVER_XML);
            }

            if (serverXml == null) {
                logError("Could not find a server.xml file to use. TPD Submission Tool will fail to start.");
            } else {
                serverXml = tryGetCanonicalFile(serverXml);
                logDebug("Using " + serverXml.getAbsolutePath());
                final Collection<String> newArgs = new ArrayList<>(args.length + 2);
                newArgs.add("-config");
                newArgs.add(serverXml.getAbsolutePath());
                newArgs.addAll(Arrays.asList(args));
                args = newArgs.toArray(new String[newArgs.size()]);
            }
            return args;
        }

        // Visible for testing
        protected String getEnvVariable(final String name) {
            return System.getenv(name);
        }

        private File findServerXmlFile(final File dir) {
            return findServerXmlFile(dir, "server.xml");
        }

        private File findServerXmlFile(final File dir, final String fileName) {
            if (dir == null) {
                return null;
            }

            final File serverXml = new File(dir.getAbsolutePath(), fileName);
            final boolean exists = serverXml.exists();
            final boolean isFile = serverXml.isFile();
            final boolean canRead = serverXml.canRead();

            logDebug(serverXml.getAbsolutePath() + ": exists = " + exists + ", is file = " + isFile + ", can read = "
                    + canRead);
            if (!(exists && isFile && canRead)) {
                return null;
            }
            return serverXml;
        }

        @SuppressWarnings("resource")
        private File copyFallbackServerXmlTo(final File dir) {
            if (dir == null) {
                return null;
            }

            final File defaultServerXml = findServerXmlFile(getInstallationConfDir(), FALLBACK_SERVER_XML);
            if (defaultServerXml == null) {
                return null;
            }

            final File serverXml = new File(dir.getAbsolutePath(), "server.xml");
            FileChannel sourceChannel = null;
            FileChannel destChannel = null;
            try {
                serverXml.getParentFile().mkdirs();
                serverXml.createNewFile();
                try {
                    sourceChannel = new FileInputStream(defaultServerXml).getChannel();
                    destChannel = new FileOutputStream(serverXml).getChannel();
                    destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                } finally {
                    if (sourceChannel != null) {
                        sourceChannel.close();
                    }
                    if (destChannel != null) {
                        destChannel.close();
                    }
                }
            } catch (final IOException e) {
                logException("Failed to create new server.xml while attempting fallback to default-server.xml", e);
                return null;
            }

            return serverXml;
        }

        private File getInstallationConfDir() {
            final File installationDirectory = resolve(getPropertyValue("catalina.base", null, null));
            if (installationDirectory == null) {
                return null;
            }

            final File confDirectory = new File(installationDirectory, "conf");
            if (!confDirectory.exists() || !confDirectory.isDirectory()) {
                return null;
            }
            return confDirectory;
        }

        private File getHomeDir() {
            return resolve(getPropertyValue(SYS_PROP_HOME, ENV_VAR_HOME, null));
        }

        private String getPropertyValue(final String propertyName,
            final String environmentName,
            final UncheckedCallable<String> defaultValue) {
            String propertyValue = System.getProperty(propertyName);
            if (propertyValue != null) {
                logDebug("Found system property " + propertyName + ": " + propertyValue);
                return propertyValue;
            }

            propertyValue = getEnvVariable(environmentName);
            if (propertyValue != null) {
                logDebug("Found environment variable " + environmentName + ": " + propertyValue);
                return propertyValue;
            }

            logDebug("System property " + propertyName + " and environment variable " + environmentName + " not found");
            propertyValue = defaultValue == null ? null : defaultValue.call();
            if (propertyValue != null) {
                logDebug("Using default value " + propertyValue);
            }

            return propertyValue;
        }

        private File getSharedHomeDir(final File home) {
            return resolve(getPropertyValue(SYS_PROP_SHARED_HOME,
                ENV_VAR_SHARED_HOME,
                () -> home == null ? null : new File(home, "shared").getAbsolutePath()));
        }

        private File resolve(final String dirName) {
            if (dirName == null || dirName.trim().length() == 0) {
                return null;
            }

            final File dir = new File(dirName);
            return tryGetCanonicalFile(dir);
        }

        private File tryGetCanonicalFile(final File dir) {
            try {
                return dir.getCanonicalFile();
            } catch (final IOException e) {
                return dir.getAbsoluteFile();
            }
        }
    }

    private interface UncheckedCallable<V> extends Callable<V> {

        @Override
        V call();
    }
}
