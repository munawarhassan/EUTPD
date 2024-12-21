package com.pmi.tpd.euceg.core.task;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.security.IAuthenticationContext;

public class TrackingReportPhase extends com.pmi.tpd.scheduler.exec.CompositeRunableTask {

    /** */
    public static final TimeZone TIMEZONE_FOR_TIMESTAMP = TimeZone.getTimeZone("UTC");

    /** */
    public static final String FORMAT_UTC_TIMESTAMP = "yyyyMMdd-HHmmss-SSS'Z'";

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackingReportPhase.class);

    /** */
    private final IAuthenticationContext authenticationContext;

    /** */
    private final I18nService i18nService;

    /** */
    private final IApplicationConfiguration settings;

    /** */
    private final ITrackingReportState state;

    /**
     * @param i18nService
     * @param steps
     * @param totalWeight
     */
    protected TrackingReportPhase(@Nonnull final IAuthenticationContext authenticationContext,
            @Nonnull final I18nService i18nService, @Nonnull final IApplicationConfiguration settings,
            @Nonnull final ITrackingReportState state, @Nonnull final Step[] steps, final int totalWeight) {
        super(steps, totalWeight);
        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
        this.i18nService = checkNotNull(i18nService, "i18nService");
        this.settings = checkNotNull(settings, "settings");
        this.state = checkNotNull(state, "state");
    }

    @Override
    public void run() {
        boolean success = false;
        if (!settings.getReportDirectory().toFile().canWrite()) {
            throw new EucegException(
                    i18nService.createKeyedMessage("app.euceg.report.cant.write", settings.getBackupDirectory()));
        }
        final File reportFile = settings.getReportDirectory().resolve(getReportFilename()).toFile();

        try (OutputStream stream = new FileOutputStream(reportFile)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Writing report to {}", reportFile.getAbsolutePath());
            }
            state.setReportFile(reportFile);
            state.setReportOutputStream(stream);
            super.run();
            success = true;
        } catch (final Exception e) {
            throw new EucegException(i18nService.createKeyedMessage("app.euceg.report.create.failed"), e);
        } finally {
            if (!success || isCanceled()) {
                if (reportFile.delete()) {
                    LOGGER.debug("Deleted partial report file at {} after catching an exception",
                        reportFile.getAbsolutePath());
                } else {
                    LOGGER.debug(
                        "Failed to delete partial report file at {}; the file has been marked for deletion on "
                                + "exit",
                        reportFile.getAbsolutePath());
                    reportFile.deleteOnExit();
                }
            }
        }
    }

    /**
     * @return a filename for the migration zip of the form migration-USERNAME-DATE-TIMESTAMP.zip. The username will be
     *         sanitised to a format suitable for use as part of a filename.
     */
    private String getReportFilename() {
        String username = authenticationContext.getCurrentUser().map(IUser::getUsername).orElse("anonymous");
        username = username.replaceAll("[^a-zA-Z0-9-_]", "_");
        final SimpleDateFormat format = new SimpleDateFormat(FORMAT_UTC_TIMESTAMP);
        format.setTimeZone(TIMEZONE_FOR_TIMESTAMP);
        final String displayTimestamp = format.format(new Date());
        final String reportType = state.getReportType().name();
        return String.format("%s-%s-%s-%s.xlsx", "report", reportType, username, displayTimestamp);
    }

    /**
     * @author Christophe Friederich
     */
    public static class Builder extends AbstractBuilder<Builder> {

        /** */
        private final ITrackingReportState state;

        /** */
        @Nonnull
        private final I18nService i18nService;

        /** */
        private final IApplicationConfiguration settings;

        /** */
        private final IAuthenticationContext authenticationContext;

        /**
         * @param i18nService
         */
        public Builder(@Nonnull final IAuthenticationContext authenticationContext,
                @Nonnull final I18nService i18nService, @Nonnull final IApplicationConfiguration settings,
                @Nonnull final ITrackingReportState state) {
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

        @SuppressWarnings("null")
        @Nonnull
        @Override
        public TrackingReportPhase build() {
            return new TrackingReportPhase(authenticationContext, i18nService, settings, state,
                    steps.toArray(Step[]::new), totalWeight);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

}
