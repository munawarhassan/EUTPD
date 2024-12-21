package com.pmi.tpd.startup.check;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.File;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.ApplicationConstants.Directories;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.core.startup.CompositeHomePathLocator;
import com.pmi.tpd.core.startup.HomeException;
import com.pmi.tpd.core.startup.IHomePathLocator;
import com.pmi.tpd.core.startup.IStartupCheck;

/**
 * This StartupCheck will check that there is a valid app.home configured that we can get an exclusive lock on.
 * <p>
 * <em>Note: this has the side effect that the app.home directory is created, if required, and "locked".</em> These
 * side-effects are REQUIRED in order to return valid results.
 *
 * @since 1.0
 */
@Named
public class HomeStartupCheck implements IStartupCheck {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeStartupCheck.class);

    /** */
    private final IHomePathLocator locator;

    /** */
    private volatile String faultDescription;

    /** */
    private volatile String faultDescriptionHtml;

    /** */
    private volatile File homeDirectory;

    /** */
    private volatile boolean initalised = false;

    /** */
    private static HomeStartupCheck HOME_STARTUP_CHECK;

    /** */
    private final IApplicationConfiguration applicationConfiguration;

    /** */
    private final Environment environment;

    @Inject
    public HomeStartupCheck(@Nonnull final IApplicationConfiguration applicationConfiguration,
            @Nonnull final Environment environment) {
        this.applicationConfiguration = checkNotNull(applicationConfiguration, "applicationConfiguration");
        this.environment = checkNotNull(environment, "environment");
        locator = createHomePathLocator();
        HOME_STARTUP_CHECK = this;
    }

    /**
     * @return
     */
    protected IHomePathLocator createHomePathLocator() {
        return new CompositeHomePathLocator(new SystemPropertyHomePathLocator(), new HomePathLocator(environment));
    }

    /**
     * @return
     */
    public static HomeStartupCheck getProductionCheck() {
        return HOME_STARTUP_CHECK;
    }

    @Override
    public String getName() {
        return "Initial Check";
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public boolean isOk() {
        if (!initalised) {
            try {
                // Get configured app.home
                final String portalHome = getConfiguredHome();

                // Validate the app.home, and create the directory if required
                // Note that we only save homeDirectory if everything is valid.
                homeDirectory = validateHomePath(portalHome);

            } catch (final HomeException ex) {
                faultDescriptionHtml = ex.getHtmlMessage();
                faultDescription = ex.getMessage();
            } catch (final IllegalStateException ex) {
                faultDescription = ex.getMessage();
            } finally {
                initalised = true;
            }
        }
        return homeDirectory != null;
    }

    /**
     * @return
     */
    public boolean isInitialised() {
        return initalised;
    }

    private String getConfiguredHome() throws HomeException {
        final String home = locator.getHome();
        if (StringUtils.isBlank(home)) {
            final String plainText = "No " + ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY
                    + " is configured.\nSee " + ApplicationConstants.PROPERTIES_FILENAME
                    + " for instructions on setting.";
            throw new HomeException(plainText, plainText);
        }
        return home;
    }

    private File validateHomePath(final String homepath) throws HomeException {
        final File proposedHome = new File(homepath);

        // try to show useful error messages if the user puts a single-backslash
        // in their portal-application.properties
        // for app.home. java.util.Properties does a lot of magic parsing so
        // we don't have much to work with.
        if (!proposedHome.isAbsolute()) {
            final StringBuilder plainText = new StringBuilder()
                    .append(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY)
                    .append(" must be an absolute path.\nSee ")
                    .append(ApplicationConstants.PROPERTIES_FILENAME)
                    .append("for instructions on setting.")
                    .append("\nYour current app.home is:\n")
                    .append(homepath);
            final boolean deadlyBackslash = System.getProperty("file.separator").equals("\\");
            if (deadlyBackslash) {
                plainText.append("\n");
                plainText.append("It looks like you are on Windows. ")
                        .append("This is usually caused by incorrect use of backslashes inside of ")
                        .append(ApplicationConstants.PROPERTIES_FILENAME)
                        .append('\n');
                plainText.append("Use forward slashes (/) instead.");
            }

            throw new HomeException(plainText.toString(), plainText.toString());
        }

        // Check if the home path actually exists
        if (proposedHome.exists()) {
            // Check that it is a directory
            if (!proposedHome.isDirectory()) {
                final String message = "Configured app.home '" + proposedHome.getAbsolutePath()
                        + "' is not a directory.";
                throw new HomeException(message);
            }
        } else {
            LOGGER.info(
                "Configured app.home '" + proposedHome.getAbsolutePath() + "' does not exist. We will create it.");
            // Attempt to create the directory
            try {
                if (!proposedHome.mkdirs()) {
                    final String portalHomeURL = ApplicationConstants.PROPERTIES_FILENAME;
                    throw new HomeException(
                            "Could not create app.home directory '" + proposedHome.getAbsolutePath() + "'. Please see "
                                    + portalHomeURL + " for more information on how to set up your home directory.");
                }
            } catch (final SecurityException ex) {
                final String portalHomeURL = ApplicationConstants.PROPERTIES_FILENAME;
                throw new HomeException("Could not create app.home directory '" + proposedHome.getAbsolutePath()
                        + "'. A Security Exception occured. Please see " + portalHomeURL
                        + " for more information on how to set up your home directory.");
            }
        }

        try {
            createAllHomeDirectories(proposedHome);
        } catch (final SecurityException ex) {
            final String portalHomeURL = ApplicationConstants.PROPERTIES_FILENAME;
            throw new HomeException("Could not create app.home directory '" + proposedHome.getAbsolutePath()
                    + "'. A Security Exception occured. Please see " + portalHomeURL
                    + " for more information on how to set up your home directory.");
        }

        // attempt to lock the home directory
        lockHome(proposedHome);

        // All tests passed
        LOGGER.info("The app.home directory '" + proposedHome.getAbsolutePath()
                + "' is validated and locked for exclusive use by this instance.");
        return proposedHome;
    }

    void createAllHomeDirectories(final File proposedHome) throws HomeException {
        final Set<String> subdirs = ImmutableSet.<String> builder()
                .add(Directories.LOG_DIRECTORY)
                .add(Directories.SHARED_DIRECTORY)
                .add(Directories.LIB_DIRECTORY)
                .add(Directories.TRASH_DIRECTORY)
                .add(Directories.WORK_DIRECTORY)
                .add(Directories.PLUGINS_DIRECTORY)
                .build();

        for (final String subdir : subdirs) {
            try {
                final File dir = new File(proposedHome, subdir);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        final String s = String.format("Could not create necessary subdirectory '%1$s' of app.home.",
                            subdir);
                        throw new HomeException(s);
                    }
                }
            } catch (final HomeException homeException) {
                throw homeException;
            } catch (final Exception e) {
                final String s = String.format("Could not create necessary subdirectory '%1$s' of app.home.", subdir);
                throw new HomeException(s + "\n" + e.getMessage());
            }
        }
    }

    private void lockHome(final File proposedHome) throws HomeException {
        // Look for Lock file
        final File lockFile = HomeLockUtils.getLockFile(proposedHome);
        // Try to lock the directory for ourselves
        try {
            if (HomeLockUtils.lockHome(proposedHome)) {
                // release the file lock when the JVM exits.
                lockFile.deleteOnExit();
            } else {
                if (applicationConfiguration.isDevMode()) {
                    // NOTE: this override is only here for development, this
                    // should not be used in production.
                    LOGGER.warn("app.home is locked, but app.dev.mode is set to true so we will ignore the lock.");
                } else {
                    // /CLOVER:OFF
                    if (lockFile.exists()) {
                        // /CLOVER:ON
                        final String homeURL = "Administrative Documentation";
                        final String plainText = "The app.home directory '" + proposedHome.getAbsolutePath()
                                + "' is already locked. Please see " + homeURL + " for more information on locked "
                                + ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY + " directories.";
                        throw new HomeException(plainText, plainText);
                    }
                    // /CLOVER:OFF
                    // Creation failed.
                    final String plainText = "Unable to create lock file for app.home directory '"
                            + proposedHome.getAbsolutePath() + "'.";
                    throw new HomeException(plainText);
                    // /CLOVER:ON
                }
            }
        } catch (final HomeException ex) {
            throw ex;
        } catch (final Exception ex) {
            // We log here to get the stack trace - may help the admin or
            // support. Note that there will be a fatal log message later as
            // well.
            LOGGER.error(
                "Unable to create lock file for app.home directory '" + proposedHome.getAbsolutePath() + "'. "
                        + ex.getMessage(),
                ex);
            throw new HomeException(
                    "Unable to create lock file for app.home directory '" + proposedHome.getAbsolutePath() + "'.");
        }
    }

    @Override
    public String getFaultDescription() {
        return faultDescription;
    }

    @Override
    public String getHtmlFaultDescription() {
        return faultDescriptionHtml;
    }

    public File getHome() {
        return homeDirectory;
    }
}
