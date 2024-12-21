package com.pmi.tpd.core.database;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.transaction.support.TransactionSynchronization;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.context.IClock;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.database.DatabaseConstants;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.IDatabaseConfigurationService;
import com.pmi.tpd.database.config.ConfigurationLineProcessor;
import com.pmi.tpd.database.config.DataSourceConfigurationAmendment;
import com.pmi.tpd.database.config.DefaultDataSourceConfiguration;
import com.pmi.tpd.database.config.FileOperationException;
import com.pmi.tpd.database.config.IConfigurationAmendment;
import com.pmi.tpd.database.config.RemovePropertiesAmendment;
import com.pmi.tpd.database.config.RemoveSetupConfigurationRequest;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.spring.transaction.ITransactionSynchronizer;

/**
 * A configuration service that updates {@ app-config.properties} with the
 * details of any data source configurations
 * that it is asked to save.
 */
public class DatabaseConnectionConfiguration implements IDatabaseConfigurationService {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionConfiguration.class);

  /** */
  private final IAuthenticationContext authenticationContext;

  /** */
  private final IClock clock;

  /** */
  private final Path sharedHomeDir;

  /** */
  private final I18nService i18nService;

  /** */
  private final ITransactionSynchronizer synchronizer;

  /**
   * Constructs a configuration service impl with the given home directory, and
   * the given clock.
   *
   * @param settings
   *                 get the shared home directory, in which we will find (or
   *                 create) the application config properties
   *                 file
   * @param clock
   *                 the clock used to generate timestamps in the configuration
   *                 file
   */
  @Inject
  public DatabaseConnectionConfiguration(@Nonnull final IApplicationConfiguration settings,
      final IAuthenticationContext authenticationContext, final IClock clock, final I18nService i18nService,
      final ITransactionSynchronizer synchronizer) {
    this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
    this.clock = checkNotNull(clock, "clock");
    this.sharedHomeDir = checkNotNull(settings, "settings").getSharedHomeDirectory();
    this.i18nService = checkNotNull(i18nService, "i18nService");
    this.synchronizer = synchronizer;
  }

  @Nonnull
  @Override
  public IDataSourceConfiguration loadDataSourceConfiguration() throws IOException {

    final Properties properties = loadCurrentConfig();
    if (Strings.isNullOrEmpty(properties.getProperty(DatabaseConstants.PROP_JDBC_DRIVER))
        || Strings.isNullOrEmpty(properties.getProperty(DatabaseConstants.PROP_JDBC_URL))) {
      throw new FileNotFoundException("doesn't exist config file");
    }
    return new DefaultDataSourceConfiguration(properties.getProperty(DatabaseConstants.PROP_JDBC_DRIVER),
        properties.getProperty(DatabaseConstants.PROP_JDBC_USER),
        properties.getProperty(DatabaseConstants.PROP_JDBC_PASSWORD),
        properties.getProperty(DatabaseConstants.PROP_JDBC_URL));
  }

  @Override
  public void saveDataSourceConfiguration(@Nonnull final IDataSourceConfiguration dataSourceConfig) {
    this.saveDataSourceConfiguration(dataSourceConfig, Optional.empty());
  }

  @Override
  public void saveDataSourceConfiguration(@Nonnull final IDataSourceConfiguration dataSourceConfig,
      @Nonnull final Optional<String> message) {
    checkNotNull(dataSourceConfig, "dataSourceConfig");
    checkNotNull(message, "message");

    // Note: During setup, for example, the context user will be null. But we still
    // need to make it possible to save
    // the configuration out, otherwise setup itself can't work.
    final IUser currentUser = authenticationContext.getCurrentUser().orElse(null);

    final DataSourceConfigurationAmendment amendment = new DataSourceConfigurationAmendment(dataSourceConfig,
        message, clock, currentUser);
    try {
      applyAmendment(amendment);
    } catch (final IOException e) {
      throw newFileOperationException("app.configuration.setup.datasourcefailed", e);
    }
  }

  @Override
  public void removeSetupProperties(@Nonnull final RemoveSetupConfigurationRequest request) {
    checkNotNull(request, "request");

    final RemovePropertiesAmendment amendment = new RemovePropertiesAmendment(clock, request.toProperties());
    try {
      applyAmendment(amendment);
    } catch (final IOException e) {
      throw newFileOperationException("app.configuration.setup.removefailed", e);
    }
  }

  @VisibleForTesting
  void applyAmendment(final IConfigurationAmendment amendment) throws IOException {
    // The current configuration properties file.
    // This may not exist; we'll check that in a moment.
    final File original = configFile();

    // We always create a draft configuration file, fill it with stuff, and then
    // rename it.
    // This ensures that the opportunity for screwing up the real configuration file
    // is minimised.
    // It works for the case where we are creating a new configuration file too.
    final File draft = createDraftConfigFile();

    File backup = null;

    try {
      try (BufferedWriter draftWriter = openFile(draft)) {
        if (original.exists()) {
          // Keep a back-up of the current configuration properties file
          backup = backUpConfigFile(original);

          final ConfigurationLineProcessor lineProcessor = new ConfigurationLineProcessor(draftWriter,
              amendment);

          // Work through the configuration properties file, and maybe inject the payload
          // (if we encounter the expected properties)
          Files.asCharSource(original, Charsets.UTF_8).readLines(lineProcessor);
        }
        amendment.finalize(draftWriter);
      }

      swapConfigFile(original, draft, backup);
    } catch (FileOperationException | IOException e) {
      // Don't keep useless files around
      deleteFile(draft);
      if (backup != null) {
        deleteFile(backup);
      }

      throw e;
    }
  }

  private void deleteFile(final File file) {
    if (!file.delete()) {
      LOGGER.warn("Failed to delete {}. Setting it to delete upon exit", file.getAbsolutePath());
      file.deleteOnExit();
    }
  }

  private FileOperationException newFileOperationException(final String i18nKey, final IOException ioe) {
    final String configurationFilePath = configFile().getAbsolutePath();
    final KeyedMessage errorMessage = i18nService.createKeyedMessage(i18nKey, configurationFilePath);
    return new FileOperationException(errorMessage, ioe);
  }

  /**
   * @param original
   *                 The original configuration file
   * @param draft
   *                 The file on which the amendments were applied
   * @param backup
   *                 The backup of the original file. Can be null.
   */
  private void swapConfigFile(final File original, final File draft, final File backup) {
    rename(draft, original);

    // Register a {@link
    // org.springframework.transaction.support.TransactionSynchronizationAdapter}
    // to restore the original configuration file if the transaction failed.
    // Note: while doing a DB migration there is no transaction
    synchronizer.register(new TransactionSynchronization() {

      @Override
      public void afterCompletion(final int status) {
        if (status == TransactionSynchronization.STATUS_ROLLED_BACK
            || status == TransactionSynchronization.STATUS_UNKNOWN) {
          LOGGER.warn(
              "Transaction was rolled back or is in an unknown state. "
                  + "Restoring original {} file (if available)",
              ApplicationConstants.CONFIG_PROPERTIES_FILE_NAME);
          if (backup != null) {
            rename(backup, original);
          }
        }
      }
    });
  }

  private void rename(final File src, final File dst) {
    try {
      Files.move(src, dst);
    } catch (final IOException e) {
      final KeyedMessage message = i18nService.createKeyedMessage("app.configuration.draft.rename", src, dst);
      throw new FileOperationException(message, e);
    }
  }

  /**
   * Makes a copy of the given file, giving it the copy the name of the original
   * plus a &quot;.bak&quot; extension.
   * <p>
   * If the backup file already exists, it is overridden.
   *
   * @param original
   *                 the file to make a backup of
   * @throws FileOperationException
   *                                if there is an IO error
   */
  @VisibleForTesting
  File backUpConfigFile(final File original) {
    final File backupFile = new File(original.getAbsolutePath() + ".bak");
    try {
      Files.copy(original, backupFile);
      return backupFile;
    } catch (final IOException e) {
      final KeyedMessage message = i18nService
          .createKeyedMessage("app.configuration.configCopyFail", original, backupFile);
      throw new FileOperationException(message, e);
    }
  }

  private File configFile() {
    return sharedHomeDir.resolve(ApplicationConstants.CONFIG_PROPERTIES_FILE_NAME).toFile();
  }

  /**
   * Creates a temporary file that will hold a draft version of the updated
   * configuration file. When it has been
   * successfully filled, we'll rename this file to app-config.properties.
   */
  private File createDraftConfigFile() {
    try {
      return File.createTempFile("draft", "properties", sharedHomeDir.toFile());
    } catch (final IOException e) {
      final KeyedMessage message = i18nService.createKeyedMessage("app.configuration.tempFile.create");
      throw new FileOperationException(message, e);
    }
  }

  private Properties loadCurrentConfig() {
    final Resource[] configResources = new Resource[] { new ClassPathResource("app-internal.properties"),
        new ClassPathResource("app-default.properties"), new FileSystemResource(configFile()) };

    final Properties properties = new Properties();
    for (final Resource resource : configResources) {
      try {
        PropertiesLoaderUtils.fillProperties(properties, resource);
      } catch (final IOException e) {
        LOGGER.warn("Problem loading properties from {}", resource.getFilename());
      }
    }

    return properties;
  }

  private BufferedWriter openFile(final File file) {
    try {
      return Files.newWriter(file, Charsets.UTF_8);
    } catch (final FileNotFoundException e) {
      final KeyedMessage message = i18nService.createKeyedMessage("app.configuration.tempFile.notFound", file);
      throw new FileOperationException(message, e);
    }
  }

}
