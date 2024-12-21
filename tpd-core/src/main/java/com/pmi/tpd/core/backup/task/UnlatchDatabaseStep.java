package com.pmi.tpd.core.backup.task;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.database.spi.IDatabaseLatch;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.scheduler.exec.cluster.AbstractUnlatchTask;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class UnlatchDatabaseStep extends AbstractUnlatchTask<IDatabaseLatch> {

  /** */
  private final IDatabaseHandle target;

  /**
   * @param builder
   */
  protected UnlatchDatabaseStep(final Builder builder) {
    super(builder.i18nService, builder.databaseManager);

    this.target = builder.target;
  }

  @Nonnull
  @Override
  public IProgress getProgress() {
    final String message = target == null ? i18nService.getMessage("app.backup.restore.resuming.database")
        : i18nService.getMessage("app.backup.restore.switching.database");
    return new ProgressImpl(message, isUnlatched() ? 100 : 0);
  }

  @Override
  protected void unlatch(final IDatabaseLatch latch) {
    if (target == null) {
      // Unlatch the system using the existing DataSource and SessionFactory
      super.unlatch(latch);
    } else {
      // If a new target was provided, switch the DataSource and SessionFactory and
      // unlatch
      // the system using those
      latch.unlatchTo(target);
    }
  }

  /**
   * @author Christophe Friederich
   * @since 1.3
   */
  public static class Builder {

    /** */
    private final IDatabaseManager databaseManager;

    /** */
    private final I18nService i18nService;

    /** */
    private IDatabaseHandle target;

    /**
     * @param i18nService
     * @param databaseManager
     */
    public Builder(final I18nService i18nService, final IDatabaseManager databaseManager) {
      this.databaseManager = databaseManager;
      this.i18nService = i18nService;
    }

    /**
     * @return
     */
    @Nonnull
    public UnlatchDatabaseStep build() {
      return new UnlatchDatabaseStep(this);
    }

    /**
     * @param target
     * @return
     */
    @Nonnull
    public Builder target(@Nullable final IDatabaseHandle target) {
      this.target = target;

      return this;
    }
  }
}
