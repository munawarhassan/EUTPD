package com.pmi.tpd.core.backup.task;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.concurrent.CountDownLatch;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.backup.IBackupClientProgressCallback;
import com.pmi.tpd.core.backup.IBackupService;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;

/**
 * Placeholder step in the overall backup process that ensures that the server
 * side backup code waits for the external
 * backup client to perform whatever operations it needs to do. This placeholder
 * step blocks the {@link BackupTask}
 * until the client has indicated that it has completed (through
 * {@link IBackupService#updateClientProgress(int)}).
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class BackupClientPlaceholderStep extends AbstractRunnableTask implements IBackupClientProgressCallback {

  /** */
  protected static final Logger LOGGER = LoggerFactory.getLogger(BackupClientPlaceholderStep.class);

  /** */
  private final CountDownLatch latch;

  /** */
  private final I18nService i18nService;

  /** */
  private volatile int percentage;

  /**
   * @param i18nService
   */
  public BackupClientPlaceholderStep(final I18nService i18nService) {
    this.i18nService = i18nService;
    this.latch = new CountDownLatch(1);
  }

  @Nonnull
  @Override
  public IProgress getProgress() {
    return new ProgressTask(i18nService.getMessage("app.backup.home.dir"), percentage);
  }

  @Override
  public void run() {
    try {
      LOGGER.debug("Waiting for client backup progress to reach 100. Current progress: {}", percentage);
      latch.await();
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void cancel() {
    super.cancel();
    latch.countDown();
  }

  @Override
  public void onProgressUpdate(final int percentage) {
    checkArgument(percentage >= 0 && percentage <= 100, "percentage must between 0 and 100");
    checkArgument(percentage >= this.percentage,
        String.format("percentage must not decrease (previous value: %d)", percentage));

    LOGGER.debug("New progress update from backup client: {}", percentage);

    this.percentage = Math.min(100, percentage);

    if (percentage == 100) {
      latch.countDown();
    }
  }
}
