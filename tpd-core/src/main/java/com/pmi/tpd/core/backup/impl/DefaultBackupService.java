package com.pmi.tpd.core.backup.impl;

import static com.google.common.base.Preconditions.checkState;
import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.isTrue;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.spring.context.SpringAware;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.exception.ArgumentValidationException;
import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.cluster.hazelcast.NodeIdMemberSelector;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.BackupFeatures;
import com.pmi.tpd.core.backup.IBackup;
import com.pmi.tpd.core.backup.IBackupClientProgressCallback;
import com.pmi.tpd.core.backup.IBackupFeature;
import com.pmi.tpd.core.backup.IBackupService;
import com.pmi.tpd.core.backup.task.BackupPhase;
import com.pmi.tpd.core.backup.task.BackupTask;
import com.pmi.tpd.core.backup.task.BaseBackupTask;
import com.pmi.tpd.core.backup.task.IBackupTaskFactory;
import com.pmi.tpd.core.maintenance.IMaintenanceService;
import com.pmi.tpd.core.maintenance.ITaskMaintenanceMonitor;
import com.pmi.tpd.core.maintenance.MaintenanceType;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;

/**
 * A default implementation of the {@link IBackupService} which uses the {@link IMaintenanceService MaintenanceService}
 * to start backup tasks, storing backups in {@link ApplicationSettings#getBackupDir() the backup directory} under the
 * configured home directory.
 *
 * @see BackupTask
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultBackupService implements IBackupService {

    /** */
    private static final String EXTENSION_ZIP = ".zip";

    /** */
    private static final int LENGTH_SUFFIX = (new SimpleDateFormat(BackupPhase.FORMAT_UTC_TIMESTAMP).format(new Date())
            + EXTENSION_ZIP).length();

    /** */
    private static final int LENGTH_EXTENSION = EXTENSION_ZIP.length();

    /** */
    private static final Pattern PATTERN_UTC_FILE_NAME = Pattern
            .compile("backup-[^\\\\/\\.]+-([0-9]{8}-[0-9]{6}-[0-9]{3})Z\\.zip");

    /**
     * Compares the filename of backup files based on the timestamp encoded in the filename itself. Last modified date
     * is not used because it is less stable and less explicit than the timestamp we encode in the name itself.
     */
    private static final Comparator<File> FILE_NAME_TIMESTAMP_COMPARATOR = new Comparator<>() {

        @Override
        public int compare(final File left, final File right) {
            return fileNameTimestamp(right).compareTo(fileNameTimestamp(left));
        }

        /**
         * Returns the part of the filename that encodes the backup timestamp e.g. if this method is passed a file with
         * {@link java.io.File#getName() name} "backup-someuser-20130121-225321-347.zip" it will return
         * 20130121-225321-347
         */
        private String fileNameTimestamp(final File file) {
            final int length = file.getName().length();
            return file.getName().substring(length - LENGTH_SUFFIX, length - LENGTH_EXTENSION);
        }
    };

    /** */
    private static final Function<Class<?>, IBackupFeature> CLASS_TO_BACKUP_FEATURE = //
            input -> new SimpleBackupFeature("database", input.getSimpleName(), BackupFeatureMode.RESTORE);

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBackupService.class);

    /** */
    private final IExecutorService clusterExecutorService;

    /** */
    private final I18nService i18nService;

    /** */
    private final ILiquibaseAccessor liquibaseDao;

    /** */
    private final IApplicationConfiguration settings;

    /** */
    private final IMaintenanceService maintenanceService;

    /** */
    private final IBackupTaskFactory taskFactory;

    /** */
    private volatile IBackupClientProgressCallback clientProgressCallback;

    /**
     * @param clusterExecutorService
     * @param i18nService
     * @param maintenanceService
     * @param maintenanceTaskFactory
     * @param settings
     * @param liquibaseDao
     * @param taskFactory
     */
    @Inject
    public DefaultBackupService(@Nonnull final IExecutorService clusterExecutorService,
            @Nonnull final I18nService i18nService, @Nonnull final IMaintenanceService maintenanceService,
            @Nonnull final IBackupTaskFactory taskFactory, @Nonnull final IApplicationConfiguration settings,
            @Nonnull final ILiquibaseAccessor liquibaseDao) {
        this.clusterExecutorService = checkNotNull(clusterExecutorService, "clusterExecutorService");
        this.maintenanceService = checkNotNull(maintenanceService, "maintenanceService");
        this.taskFactory = checkNotNull(taskFactory, "taskFactory");
        this.i18nService = checkNotNull(i18nService, "i18nService");
        this.liquibaseDao = checkNotNull(liquibaseDao, "liquibaseDao");
        this.settings = checkNotNull(settings, "settings");
    }

    /**
     * Begins the process backing up the system, if another backup is not already in progress.
     * <p>
     * Exactly one backup is allowed to be running at a time. Because Event should be blocking access to the entire
     * application during the backup process, this should not be an issue. However, this method does perform its own
     * validation to ensure another backup is not already in progress.
     *
     * @return a {@code MaintenanceTaskMonitor task monitor} which can be used to cancel the backup and listen for its
     *         completion
     * @throws BackupException
     *                         if another backup is already in progress
     * @see com.pmi.tpd.core.backup.task.BackupTask
     * @see IMaintenanceService#start(com.pmi.tpd.core.exec.IRunnableTask, MaintenanceType)
     */
    @Nonnull
    @Override
    // @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
    public ITaskMaintenanceMonitor backup() {
        return submitBackupTask(taskFactory.backupTask());
    }

    private ITaskMaintenanceMonitor submitBackupTask(final BaseBackupTask backupTask) {
        try {
            final ITaskMaintenanceMonitor taskMonitor = maintenanceService.start(backupTask, MaintenanceType.BACKUP);
            clientProgressCallback = backupTask.getClientProgressCallback();
            taskMonitor.registerCallback(() -> {
                clientProgressCallback = null;
            });

            return taskMonitor;
        } catch (final IllegalStateException e) {
            LOGGER.error("An attempt to start a backup was blocked because maintenance is already in progress");
            throw new BackupException(i18nService.createKeyedMessage("app.backup.already.running"));
        }
    }

    @Override
    // @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
    public boolean delete(@Nonnull final IBackup backup) {
        final String name = validateName(checkNotNull(backup, "backup").getName());
        final File file = settings.getBackupDirectory().resolve(name).toFile();

        return file.isFile() && file.delete();
    }

    @Nonnull
    @Override
    // @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
    public Page<IBackup> findAll(@Nonnull final Pageable pageRequest) {
        checkNotNull(pageRequest, "pageRequest");

        final List<File> files = listBackupFiles();
        if (pageRequest.getOffset() > files.size()) {
            return PageUtils.createEmptyPage(pageRequest);
        }

        Iterable<File> page;
        if (pageRequest.getOffset() > 0) {
            page = Iterables.skip(files, (int) pageRequest.getOffset());
        } else {
            page = files;
        }

        return PageUtils.createPage(Iterables.transform(page, FileBackup.FILE_TRANSFORM), pageRequest);
    }

    @Override
    // @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
    public IBackup findByName(@Nonnull String name) {
        name = validateName(name);

        final File backup = settings.getBackupDirectory().resolve(name).toFile();
        // No sneaky escaping the backup directory
        if (backup.isFile()) {
            return new FileBackup(backup);
        }
        return null;
    }

    @Nonnull
    @Override
    // @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
    public IBackup getByName(@Nonnull final String name) {
        final IBackup backup = findByName(name);
        if (backup == null) {
            throw new NoSuchEntityException(i18nService.createKeyedMessage("app.service.backup.nosuchbackup", name));
        }
        return backup;
    }

    @Nonnull
    @Override
    // @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
    public IBackup getLatest() {
        final List<File> files = listBackupFiles();
        if (files.isEmpty()) {
            throw new NoSuchEntityException(i18nService.createKeyedMessage("app.service.backup.nobackups"));
        }

        return new FileBackup(files.get(0)); // First file in the list is the newest
    }

    /**
     * Retrieves a list of backup features, in order for the backup client to determine whether a backup or restore is
     * possible.
     *
     * @return the backup features
     */
    @Nonnull
    @Override
    // @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
    public List<IBackupFeature> getFeatures() {
        return ImmutableList.<IBackupFeature> builder()
                .addAll(BackupFeatures.getFeatures())
                .addAll(Collections2.transform(liquibaseDao.findCustomChanges(), CLASS_TO_BACKUP_FEATURE))
                .build();
    }

    @Override
    public void updateClientProgress(final int percentage) {
        isTrue(percentage >= 0 && percentage <= 100, "Progress must be between 0 and 100");

        final IBackupClientProgressCallback listener = clientProgressCallback;
        if (listener != null) {
            // backup is running on this node
            listener.onProgressUpdate(percentage);
        } else {
            // backup may be running on another node
            final ITaskMaintenanceMonitor task = maintenanceService.getRunningTask();
            if (task != null && task.getType() == MaintenanceType.BACKUP) {
                clusterExecutorService.executeOnMembers(new UpdateClientProgress(percentage),
                    new NodeIdMemberSelector(task.getOwnerNodeId()));
            } else {
                throw new NoSuchEntityException(i18nService.createKeyedMessage("app.service.backup.not.backing.up"));
            }
        }
    }

    private List<File> listBackupFiles() {
        final List<File> files = Lists.newArrayList(FileUtils.listFiles(settings.getBackupDirectory().toFile(),
            new RegexFileFilter(PATTERN_UTC_FILE_NAME),
            FalseFileFilter.INSTANCE));
        Collections.sort(files, FILE_NAME_TIMESTAMP_COMPARATOR);

        return files;
    }

    private String validateName(String name) {
        checkNotNull(name, "name");

        name = name.trim();
        if (!PATTERN_UTC_FILE_NAME.matcher(name).matches()) {
            throw new ArgumentValidationException(
                    i18nService.createKeyedMessage("app.service.backup.invalidname", name));
        }
        return name;
    }

    /**
     * Updates the backup client progress on a node.
     */
    @SpringAware
    private static final class UpdateClientProgress implements Serializable, Runnable {

        /** */
        private static final long serialVersionUID = 1L;

        /** */
        private final int progress;

        /** */
        private transient volatile IBackupService backupService;

        private UpdateClientProgress(final int progress) {
            this.progress = progress;
        }

        @Override
        public void run() {
            checkState(backupService != null, "BackupService hasn't been injected");
            backupService.updateClientProgress(progress);
        }

        @Autowired
        public void setBackupService(final IBackupService backupService) {
            this.backupService = backupService;
        }
    }

}
