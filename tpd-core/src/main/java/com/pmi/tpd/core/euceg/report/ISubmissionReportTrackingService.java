package com.pmi.tpd.core.euceg.report;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.exception.ArgumentValidationException;
import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.core.backup.IBackup;
import com.pmi.tpd.euceg.core.exporter.submission.SubmissionReportType;
import com.pmi.tpd.scheduler.exec.ITaskMonitor;

public interface ISubmissionReportTrackingService {

    @Nonnull
    ITaskMonitor trackingReport(@Nonnull SubmissionReportType reportName, @Nonnull Pageable pageable, final long limit);

    /**
     * Attempts to delete the specified tracking report on disk and returns a flag to indicate the outcome.
     *
     * @param trackingReport
     *                       the trackingReport to delete
     * @return {@code true} if the trackingReport existed and was successfully deleted; otherwise, {@code false} if no
     *         such trackingReport existed or it could not be removed
     * @throws ArgumentValidationException
     *                                     if the {@link ITrackingReport#getName() backup name} describes a relative
     *                                     path, or does not follow the expected pattern
     * @throws NullPointerException
     *                                     if the provided {@link ITrackingReport} or its
     *                                     {@link ITrackingReport#getName() name} is {@code null}
     */
    boolean delete(@Nonnull ITrackingReport trackingReport);

    boolean delete(@Nonnull String filename);

    /**
     * Retrieves a page of {@link IBackup backups}, as described by the provided {@link Pageable request}.
     *
     * @param pageRequest
     *                    describes the start and limit of the page to retrieve
     * @return a page of backups, which may be empty but never {@code null}
     * @throws NullPointerException
     *                              if the provided {@link Page} is {@code null}
     */
    @Nonnull
    Page<ITrackingReport> findAll(@Nonnull Pageable pageRequest);

    /**
     * Finds a backup with the specified name, if one exists.
     *
     * @param name
     *             the <i>exact</i> name of the backup
     * @return the named backup, if it exists, or {@code null} if it doesn't
     * @throws ArgumentValidationException
     *                                     if the provided {@code name} describes a relative path, or does not follow
     *                                     the expected pattern
     * @throws NullPointerException
     *                                     if the provided {@code name} is {@code null}
     */
    @Nonnull
    Optional<ITrackingReport> findByName(@Nonnull String name);

    /**
     * Retrieves the backup with the specified name.
     *
     * @param name
     *             the <i>exact</i> name of the backup
     * @return the named backup
     * @throws ArgumentValidationException
     *                                     if the provided {@code name} describes a relative path, or does not follow
     *                                     the expected pattern
     * @throws NoSuchEntityException
     *                                     if no backup exists with the specified {@code name}
     * @throws NullPointerException
     *                                     if the provided {@code name} is {@code null}
     */
    @Nonnull
    ITrackingReport getByName(@Nonnull String name);

    /**
     * Retrieves the most recent backup.
     *
     * @return the most recent backup
     * @throws NoSuchEntityException
     *                               if no backups exist
     */
    @Nonnull
    ITrackingReport getLatest();

    Optional<ITrackingReport> findLatest();

    /**
     * @param name
     * @return
     */
    Optional<ITaskMonitor> getTaskMonitor(String name);

    void cancelTask(@Nonnull String cancelToken);

}
