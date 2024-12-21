package com.pmi.tpd.keystore;

import static com.pmi.tpd.api.paging.PageUtils.asIterable;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.joda.time.DateTime;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.pmi.tpd.api.exception.ApplicationException;
import com.pmi.tpd.api.paging.IPageProvider;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.scheduler.IJobRunner;
import com.pmi.tpd.api.scheduler.IJobRunnerRequest;
import com.pmi.tpd.api.scheduler.IScheduledJobSource;
import com.pmi.tpd.api.scheduler.ISchedulerService;
import com.pmi.tpd.api.scheduler.JobRunnerResponse;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.keystore.KeyStoreProperties.Notification;
import com.pmi.tpd.keystore.KeyStoreProperties.NotificationExpiration;
import com.pmi.tpd.keystore.model.KeyStoreEntry;
import com.pmi.tpd.keystore.preference.IKeyStorePreferences;
import com.pmi.tpd.keystore.preference.KeyStorePreferenceKeys;

/**
 * Certificates are used for encrypt EUCEG payload, they should be updated
 * before expiration.
 *
 * @author Christophe Friederich
 * @since 2.2
 */
public class CertificateValidityScheduler implements IScheduledJobSource {

  /** */
  private static final JobId CLEANUP_JOB_ID = JobId.of(CertificateValidityScheduler.class.getSimpleName());

  /** */
  private static final JobRunnerKey CLEANUP_JOB_RUNNER_KEY = JobRunnerKey
      .of(CertificateValidityScheduler.class.getName());

  /** */
  private volatile IKeyStoreService keyStoreService;

  /** */
  private volatile IKeyStorePreferencesManager preferencesManager;

  /** */
  private volatile ICertificateMailNotifier notifier;

  /**
   * @param applicationProperties
   *                              a global application properties.
   * @param keyStoreService
   * @param preferencesManager
   * @param notifier
   */
  @Inject
  public CertificateValidityScheduler(@Nonnull final IKeyStoreService keyStoreService,
      @Nonnull final IKeyStorePreferencesManager preferencesManager, final ICertificateMailNotifier notifier) {
    this.keyStoreService = checkNotNull(keyStoreService, "keyStoreService");
    this.preferencesManager = checkNotNull(preferencesManager, "preferencesManager");
    this.notifier = checkNotNull(notifier, "notifier");
  }

  @Override
  public void schedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
    final NotificationExpiration notification = this.keyStoreService.getConfiguration()
        .getNotification()
        .getExpiration();
    schedulerService.registerJobRunner(CLEANUP_JOB_RUNNER_KEY, createJob());
    final long intervalMillis = TimeUnit.DAYS.toMillis(notification.getInterval());
    schedulerService.scheduleJob(CLEANUP_JOB_ID,
        JobConfig.forJobRunnerKey(CLEANUP_JOB_RUNNER_KEY)
            .withRunMode(RunMode.RUN_ONCE_PER_CLUSTER)
            .withSchedule(
                Schedule.forInterval(intervalMillis, new Date(System.currentTimeMillis() + intervalMillis))));
  }

  @VisibleForTesting
  IJobRunner createJob() {
    return new NotifierJob();
  }

  @Override
  public void unschedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
    schedulerService.unregisterJobRunner(CLEANUP_JOB_RUNNER_KEY);
  }

  class NotifierJob implements IJobRunner {

    @Nullable
    @Override
    public JobRunnerResponse runJob(final IJobRunnerRequest request) {
      final Notification notification = keyStoreService.getConfiguration().getNotification();
      if (!notification.getExpiration().isEnable() || Strings.isNullOrEmpty(notification.getContact())) {
        return JobRunnerResponse.success();
      }
      final DateTime now = DateTime.now();
      for (final KeyStoreEntry entry : asIterable(
          (IPageProvider<KeyStoreEntry>) pageable -> keyStoreService.findAllWithExpireDateBefore(pageable,
              now.plusDays(notification.getExpiration().getThreshold())),
          PageUtils.newRequest(0, 20))) {

        if (hasNeedNotify(entry, now)) {
          notifier.sendExpiredCertificate(notification.getContact(), entry);
        }

      }

      return JobRunnerResponse.success();
    }

    private boolean hasNeedNotify(@Nonnull final KeyStoreEntry key, @Nonnull final DateTime now) {
      checkNotNull(key, "key");
      checkNotNull(now, "now");
      final NotificationExpiration notification = keyStoreService.getConfiguration()
          .getNotification()
          .getExpiration();
      final IKeyStorePreferences pref = preferencesManager.getPreferences(key);
      boolean needNotify = true;
      try {
        if (pref.exists(KeyStorePreferenceKeys.CERTIFICATE_LAST_NOTIFICATION)) {
          final DateTime lastNotification = pref.getDate(KeyStorePreferenceKeys.CERTIFICATE_LAST_NOTIFICATION)
              .map(DateTime::new)
              .orElse(now);
          needNotify = lastNotification.plusDays(notification.getReminder()).isBefore(now);
        }

        if (needNotify) {
          pref.setDate(KeyStorePreferenceKeys.CERTIFICATE_LAST_NOTIFICATION, now.toDate());
        }
        return needNotify;
      } catch (final ApplicationException ex) {
        throw new RuntimeException(ex.getMessage(), ex);
      }
    }

  }
}