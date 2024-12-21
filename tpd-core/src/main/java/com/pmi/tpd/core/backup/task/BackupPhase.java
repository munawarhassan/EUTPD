package com.pmi.tpd.core.backup.task;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.core.backup.task.BackupPhase.CloseShieldZipOutputStream;
import com.pmi.tpd.scheduler.exec.CompositeRunableTask;
import com.pmi.tpd.security.IAuthenticationContext;

import de.schlichtherle.truezip.zip.ZipOutputStream;

/**
 * Wrapper around the various backup steps that ensures that.
 * <ul>
 * <li>the backup {@link java.util.zip.ZipOutputStream} is ready to be written to</li>
 * <li>the backup {@link java.util.zip.ZipOutputStream} cannot be closed by intermediate backup steps</li>
 * <li>the backup {@link java.util.zip.ZipOutputStream} is always closed after backup steps have executed</li>
 * <li>in case of an error, the partial backup file is cleaned up</li>
 * </ul>
 */
public class BackupPhase extends CompositeRunableTask {

    /** */
    public static final TimeZone TIMEZONE_FOR_TIMESTAMP = TimeZone.getTimeZone("UTC");

    /** */
    public static final String FORMAT_UTC_TIMESTAMP = "yyyyMMdd-HHmmss-SSS'Z'";

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(BackupPhase.class);

    /** */
    private final IAuthenticationContext authenticationContext;

    /** */
    private final I18nService i18nService;

    /** */
    private final IApplicationConfiguration settings;

    /** */
    private final IBackupState state;

    /**
     * @param authenticationContext
     * @param i18nService
     * @param settings
     * @param state
     * @param steps
     * @param totalWeight
     */
    protected BackupPhase(@Nonnull final IAuthenticationContext authenticationContext,
            @Nonnull final I18nService i18nService, @Nonnull final IApplicationConfiguration settings,
            @Nonnull final IBackupState state, @Nonnull final Step[] steps, final int totalWeight) {
        super(steps, totalWeight);

        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
        this.i18nService = checkNotNull(i18nService, "i18nService");
        this.settings = checkNotNull(settings, "settings");
        this.state = checkNotNull(state, "state");
    }

    @Override
    public void run() {
        boolean success = false;

        if (!settings.getBackupDirectory().toFile().canWrite()) {
            throw new BackupException(
                    i18nService.createKeyedMessage("app.backup.cant.write", settings.getBackupDirectory()));
        }

        final File backupFile = settings.getBackupDirectory().resolve(getBackupFilename()).toFile();

        CloseShieldZipOutputStream zipStream = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Writing backup to {}", backupFile.getAbsolutePath());
            }

            final FileOutputStream fileStream = new FileOutputStream(backupFile);
            zipStream = new CloseShieldZipOutputStream(fileStream);

            state.setBackupFile(backupFile);
            state.setBackupZipStream(zipStream);

            super.run();

            success = true;
        } catch (final Exception e) {
            throw new BackupException(i18nService.createKeyedMessage("app.backup.create.failed"), e);
        } finally {
            if (zipStream != null) {
                // Really close the zip stream
                try {
                    zipStream.reallyClose();
                } catch (final IOException e) {
                    // Ignore
                    LOGGER.debug("Problem closing the backup zip", e);
                }
            }

            if (!success || isCanceled()) {
                if (backupFile.delete()) {
                    LOGGER.debug("Deleted partial backup file at {} after catching an exception",
                        backupFile.getAbsolutePath());
                } else {
                    LOGGER.debug(
                        "Failed to delete partial backup file at {}; the file has been marked for deletion on "
                                + "exit",
                        backupFile.getAbsolutePath());
                    backupFile.deleteOnExit();
                }
            }
        }
    }

    /**
     * @return a filename for the migration zip of the form migration-USERNAME-DATE-TIMESTAMP.zip. The username will be
     *         sanitised to a format suitable for use as part of a filename.
     */
    private String getBackupFilename() {
        String username = authenticationContext.getCurrentUser().map(user -> user.getUsername()).orElse("anonymous");
        username = username.replaceAll("[^a-zA-Z0-9-_]", "_");
        final SimpleDateFormat format = new SimpleDateFormat(FORMAT_UTC_TIMESTAMP);
        format.setTimeZone(TIMEZONE_FOR_TIMESTAMP);
        final String displayTimestamp = format.format(new Date());
        return String.format("%s-%s-%s.zip", "backup", username, displayTimestamp);
    }

    /**
     * @author Christophe Friederich
     */
    public static class Builder extends AbstractBuilder<Builder> {

        /** */
        private final IBackupState state;

        /** */
        private final I18nService i18nService;

        /** */
        private final IApplicationConfiguration settings;

        /** */
        private final IAuthenticationContext authenticationContext;

        /**
         * @param authenticationContext
         * @param i18nService
         * @param settings
         * @param state
         */
        public Builder(@Nonnull final IAuthenticationContext authenticationContext,
                @Nonnull final I18nService i18nService, @Nonnull final IApplicationConfiguration settings,
                @Nonnull final IBackupState state) {
            super();
            this.authenticationContext = authenticationContext;
            this.i18nService = i18nService;
            this.settings = settings;
            this.state = state;
        }

        // NOT REMOVE: for mockito proxying
        @Override
        public Builder add(final @Nonnull IRunnableTask step, final int weight) {
            return super.add(step, weight);
        }

        @Nonnull
        @Override
        public BackupPhase build() {
            return new BackupPhase(authenticationContext, i18nService, settings, state,
                    steps.toArray(new Step[steps.size()]), totalWeight);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    /**
     * @author Christophe Friederich
     */
    static class CloseShieldZipOutputStream extends ZipOutputStream {

        /**
         * @param fileStream
         */
        public CloseShieldZipOutputStream(final FileOutputStream fileStream) {
            super(fileStream);
        }

        @Override
        public void close() throws IOException {
            // Do nothing
        }

        private void reallyClose() throws IOException {
            try {
                super.close();
            } finally {
                // If closing the zip outputstream fails, we should attempt to close the nested
                // OutputStream instead.
                Closeables.close(delegate, true);
            }
        }
    }
}
