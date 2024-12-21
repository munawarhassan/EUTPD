package com.pmi.tpd.core.context;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.ApplicationConstants.Directories;
import com.pmi.tpd.api.Environment;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.util.FileUtils;
import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.core.util.DevModeUtils;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Singleton
@Named
public class GlobalApplicationConfiguration implements IApplicationConfiguration {

    /** */
    private Path configurationDirectory;

    /** */
    private Path homeDirectory;

    /** */
    private Path sharedHomeDirectory;

    /** */
    private Path attachmentsDirectory;

    /** */
    private Path dataDirectory;

    /** */
    private Path indexDirectory;

    /** */
    private Path applicationDirectory;

    /** */
    private Path logDirectory;

    /** */
    private Path temporaryDirectory;

    /** */
    private Path wastebasketDirectory;

    /** */
    private Path workingDirectory;

    /** */
    private Path pluginsDirectory;

    /** */
    private Path backupDirectory;

    /** */
    private Path reportDirectory;

    /** */
    private Environment env;

    /** */
    private final IBuildUtilsInfo buildUtilsInfo;

    /** */
    private final org.springframework.core.env.Environment environment;

    /**
     * @param buildUtilsInfo
     * @param environment
     */
    @Autowired
    public GlobalApplicationConfiguration(@Nonnull final IBuildUtilsInfo buildUtilsInfo,
            @Nonnull final org.springframework.core.env.Environment environment) {
        this.buildUtilsInfo = Assert.notNull(buildUtilsInfo, "buildUtilsInfo is required");
        this.environment = environment;
        final String[] profiles = this.environment.getActiveProfiles();
        if (profiles != null) {
            for (final String profile : profiles) {
                this.env = Environment.get(profile);
                if (env != null) {
                    break;
                }
            }
        }
        if (env == null) {
            this.env = Environment.PRODUCTION;
        }
    }

    /**
     * @param environment
     */
    public void setEnv(final String environment) {
        env = Environment.get(environment);
        if (env == null) {
            env = Environment.PRODUCTION;
        }
    }

    /**
     *
     */
    public void clear() {
        configurationDirectory = null;
        homeDirectory = null;
        applicationDirectory = null;
        logDirectory = null;
        temporaryDirectory = null;
        wastebasketDirectory = null;
        workingDirectory = null;
        attachmentsDirectory = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBuildUtilsInfo getBuildUtilsInfo() {
        return buildUtilsInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getLogDirectory() {
        // Create the dir if doesn't exist, throw runtime exception on failure
        if (logDirectory == null) {
            logDirectory = getHomeDirectory(Directories.LOG_DIRECTORY);
            final File f = logDirectory.toFile();
            mkdir(f);
        }
        return logDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getLogFile() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getConfigurationDirectory() {
        if (configurationDirectory == null) {
            configurationDirectory = getHomeDirectory(Directories.CONFIGURATION_DIRECTORY);
        }
        return configurationDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getHomeDirectory(final String key) {
        return getHomeDirectory().resolve(key);
    }

    public Path getSharedHomeDirectory(final String key) {
        return getSharedHomeDirectory().resolve(key).toAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getHomeDirectory() {
        // Create the dir if doesn't exist, throw runtime exception on failure
        if (homeDirectory == null) {
            final String path = environment
                    .resolvePlaceholders("${" + ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY + "}");
            homeDirectory = Paths.get(path).toAbsolutePath();
            final File f = homeDirectory.toFile();
            mkdir(f);
        }
        return homeDirectory;
    }

    @Override
    public Path getSharedHomeDirectory() {
        // Create the shared home if doesn't exist, throw runtime exception on failure
        if (sharedHomeDirectory == null) {
            sharedHomeDirectory = getHomeDirectory(Directories.SHARED_DIRECTORY).toAbsolutePath();
            final File f = sharedHomeDirectory.toFile();
            mkdir(f);
        }
        return sharedHomeDirectory;
    }

    @Override
    public Path getAttachmentsDirectory() {
        if (attachmentsDirectory == null) {
            attachmentsDirectory = getSharedHomeDirectory(Directories.ATTACHMENT_DIRECTORY);
            final File f = attachmentsDirectory.toFile();
            mkdir(f);
        }
        return attachmentsDirectory;
    }

    @Override
    public Path getDataDirectory() {
        if (dataDirectory == null) {
            dataDirectory = getSharedHomeDirectory(Directories.DATA_DIRECTORY);
            final File f = dataDirectory.toFile();
            mkdir(f);
        }
        return dataDirectory;
    }

    @Override
    public Path getIndexDirectory() {
        if (indexDirectory == null) {
            indexDirectory = getSharedHomeDirectory(Directories.INDEX_DIRECTORY);
            final File f = indexDirectory.toFile();
            mkdir(f);
        }
        return indexDirectory;
    }

    /**
     * @return
     */
    public Path getApplicationDirectory() {
        if (applicationDirectory == null) {
            final String path = environment
                    .resolvePlaceholders("${" + ApplicationConstants.PropertyKeys.APPLICATION_PATH_PROPETY + "}");
            applicationDirectory = Paths.get(path).toAbsolutePath();
        }
        return applicationDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getTemporaryDirectory() {
        if (temporaryDirectory == null) {
            temporaryDirectory = Paths.get(System.getProperty("java.io.tmpdir"));
            final File f = temporaryDirectory.toFile();
            mkdir(f);
        }
        return temporaryDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getWastebasketDirectory() {
        if (wastebasketDirectory == null) {
            wastebasketDirectory = getHomeDirectory(Directories.TRASH_DIRECTORY);
            final File f = wastebasketDirectory.toFile();
            mkdir(f);
        }
        return wastebasketDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getInstalledPluginsDirectory() {
        if (pluginsDirectory == null) {
            pluginsDirectory = getHomeDirectory(Directories.PLUGINS_DIRECTORY);
            final File f = pluginsDirectory.toFile();
            mkdir(f);
        }
        return pluginsDirectory;
    }

    @Override
    public Path getBackupDirectory() {
        if (backupDirectory == null) {
            backupDirectory = getHomeDirectory(Directories.BACKUP_DIRECTORY);
            final File f = backupDirectory.toFile();
            mkdir(f);
        }
        return backupDirectory;
    }

    @Override
    public Path getReportDirectory() {
        if (reportDirectory == null) {
            reportDirectory = getHomeDirectory(Directories.REPORT_DIRECTORY);
            final File f = reportDirectory.toFile();
            mkdir(f);
        }
        return reportDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getWorkingDirectory() {
        if (workingDirectory == null) {
            workingDirectory = getHomeDirectory(Directories.WORK_DIRECTORY);
            final File f = workingDirectory.toFile();
            mkdir(f);
        }
        return workingDirectory;
    }

    @Override
    public Environment getEnvironment() {
        return env;
    }

    @Override
    public boolean isDevMode() {
        return DevModeUtils.isEnabled(environment) /* || Environment.DEVELOPMENT.equals(getEnvironment()) */;
    }

    private void mkdir(final File file) {
        try {
            FileUtils.mkdir(file);
        } catch (final Exception e) {
            LoggerFactory.getLogger(GlobalApplicationConfiguration.class).warn(e.getMessage(), e);
        }
    }
}
