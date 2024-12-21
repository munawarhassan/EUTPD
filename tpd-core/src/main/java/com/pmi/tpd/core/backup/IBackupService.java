package com.pmi.tpd.core.backup;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.exception.ArgumentValidationException;
import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.core.maintenance.ITaskMaintenanceMonitor;

/**
 * Defines a service for creating and managing system backups.
 * <p>
 * <b>Note:</b> Backups cannot be restored by the system; they must be restored externally.
 * </p>
 *
 * @since 1.1
 */
public interface IBackupService {

    /**
     * Creates a new backup containing a snapshot of the current system state.
     * <p>
     * In an attempt to strike a balance between server downtime and implementation complexity, backing up the system
     * does <i>not</i> include repository data. A standalone backup client performs the repository backup, coordinating
     * the system backup performed by this method to produce a final, unified backup which contains synchronised state
     * for the database <i>and</i> the repositories.
     *
     * @return a handle to the backup task
     * @throws BackupException
     *                         if the processing to create the backup fails
     */
    @Nonnull
    ITaskMaintenanceMonitor backup();

    /**
     * Attempts to delete the specified backup on disk and returns a flag to indicate the outcome.
     *
     * @param backup
     *               the backup to delete
     * @return {@code true} if the backup existed and was successfully deleted; otherwise, {@code false} if no such
     *         backup existed or it could not be removed
     * @throws ArgumentValidationException
     *                                     if the {@link IBackup#getName() backup name} describes a relative path, or
     *                                     does not follow the expected pattern
     * @throws NullPointerException
     *                                     if the provided {@link IBackup} or its {@link IBackup#getName() name} is
     *                                     {@code null}
     */
    boolean delete(@Nonnull IBackup backup);

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
    Page<IBackup> findAll(@Nonnull Pageable pageRequest);

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
    @Nullable
    IBackup findByName(@Nonnull String name);

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
    IBackup getByName(@Nonnull String name);

    /**
     * Retrieves a list of backup features, in order for the backup client to determine whether a backup or restore is
     * possible.
     *
     * @return the backup features
     */
    @Nonnull
    List<IBackupFeature> getFeatures();

    /**
     * Retrieves the most recent backup.
     *
     * @return the most recent backup
     * @throws NoSuchEntityException
     *                               if no backups exist
     */
    @Nonnull
    IBackup getLatest();

    /**
     * Informs the running backup task the percentage complete of the backup client.
     *
     * @param percentage
     *                   the percentage complete of the backup client
     * @throws NoSuchEntityException
     *                               if a backup is not currently in progress
     */
    void updateClientProgress(int percentage);

}
