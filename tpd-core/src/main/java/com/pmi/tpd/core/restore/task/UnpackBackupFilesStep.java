package com.pmi.tpd.core.restore.task;

import static com.google.common.base.Preconditions.checkState;
import static com.pmi.tpd.api.util.FileUtils.createTempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.migration.IMigrationState;
import com.pmi.tpd.core.migration.MigrationException;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;

/**
 * @author Christophe Friederich
 */
public class UnpackBackupFilesStep extends AbstractRunnableTask {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(UnpackBackupFilesStep.class);

    /** */
    private final I18nService i18nService;

    /** */
    private volatile int progress;

    /** */
    private final IApplicationConfiguration settings;

    /** */
    private final IMigrationState state;

    @Inject
    public UnpackBackupFilesStep(final IMigrationState state, final I18nService i18nService,
            final IApplicationConfiguration settings) {
        this.i18nService = i18nService;
        this.settings = settings;
        this.state = state;
    }

    @Nonnull
    @Override
    public IProgress getProgress() {
        return new ProgressImpl(i18nService.getMessage("app.backup.restore.unpacking.backup"), progress);
    }

    @Override
    public void run() {
        final File backupFile = state.getBackupFile();
        checkState(backupFile != null && backupFile.exists(), "Backup file not found");

        try {
            state.setUnzippedBackupDirectory(unpackZip(backupFile));
        } catch (final IOException e) {
            throw new MigrationException(i18nService.createKeyedMessage("app.backup.restore.unpacking.backup.failed",
                backupFile.getAbsolutePath()), e);
        }
    }

    protected File unpackZip(final File zipFile) throws IOException {
        final File directory = createTempDir(zipFile.getName() + "-unpack",
            ".backup",
            settings.getTemporaryDirectory().toFile());
        LOGGER.debug("Unpacking backup files to {}", directory.getAbsolutePath());

        final ZipFile zip = new ZipFile(zipFile);
        try {
            final Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                if (isCanceled()) {
                    LOGGER.debug("Canceled while unpacking backup files");
                    return directory;
                }
                final ZipEntry entry = entries.nextElement();
                final File file = new File(directory, entry.getName());

                LOGGER.debug("Unpacking {} ({} bytes) to {}", entry.getName(), entry.getSize(), file.getAbsolutePath());
                final long bytes = IOUtils.copyLarge(zip.getInputStream(entry), new FileOutputStream(file));
                if (bytes != entry.getSize()) {
                    LOGGER.warn("{} may not have been completely extracted; unpacked {} bytes where {} were expected",
                        entry.getName(),
                        bytes,
                        entry.getSize());
                }
            }
            LOGGER.debug("All backup files have been unpacked");
        } finally {
            progress = 100;
            try {
                zip.close();
            } catch (final IOException e) {
                LOGGER.warn("Failed to close backup file " + zipFile.getAbsolutePath(), e);
            }
        }

        return directory;
    }

}
