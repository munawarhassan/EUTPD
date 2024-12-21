package com.pmi.tpd.keystore;

import static com.pmi.tpd.service.testing.mockito.PageAnswer.withPageOf;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.exception.ApplicationException;
import com.pmi.tpd.api.scheduler.IJobRunner;
import com.pmi.tpd.api.scheduler.IJobRunnerRequest;
import com.pmi.tpd.api.scheduler.JobRunnerResponse;
import com.pmi.tpd.keystore.model.KeyStoreEntry;
import com.pmi.tpd.keystore.preference.KeyStorePreferenceKeys;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class CertificateValiditySchedulerTest extends MockitoTestCase {

    @Mock
    private IKeyStoreService keyStoreService;

    @Mock
    private IKeyStorePreferencesManager preferencesManager;

    @Mock
    private ICertificateMailNotifier notifier;

    @InjectMocks
    private CertificateValidityScheduler certificateValidityScheduler;

    @Test
    public void shouldNotNotifyWithNotificationDisable() {
        final KeyStoreProperties configuration = new KeyStoreProperties();
        configuration.getNotification().getExpiration().setEnable(false);
        configuration.getNotification().setContact("john.cook@company.com");
        when(keyStoreService.getConfiguration()).thenReturn(configuration);
        final IJobRunner jobRunner = certificateValidityScheduler.createJob();
        final IJobRunnerRequest request = mock(IJobRunnerRequest.class);

        final JobRunnerResponse response = jobRunner.runJob(request);

        assertEquals(JobRunnerResponse.success(), response);
        verify(notifier, never()).sendExpiredCertificate(any(), any());
    }

    @Test
    public void shouldNotNotifyWithoutContact() {
        final KeyStoreProperties configuration = new KeyStoreProperties();
        configuration.getNotification().getExpiration().setEnable(true);
        when(keyStoreService.getConfiguration()).thenReturn(configuration);
        final IJobRunner jobRunner = certificateValidityScheduler.createJob();
        final IJobRunnerRequest request = mock(IJobRunnerRequest.class);

        final JobRunnerResponse response = jobRunner.runJob(request);

        assertEquals(JobRunnerResponse.success(), response);
        verify(notifier, never()).sendExpiredCertificate(any(), any());
    }

    @Test
    public void shouldNotifyFirstTimeExpiratedCertification() throws ApplicationException {
        final String alias = "certificate";
        when(keyStoreService.getConfiguration()).thenReturn(defaultConfiguration());

        final DateTime now = DateTime.now();
        when(keyStoreService.findAllWithExpireDateBefore(any(), any()))
                .thenAnswer(withPageOf(KeyStoreEntry.builder().alias(alias).expiredDate(now).build()));

        final MockKeyStorePreferences pref = new MockKeyStorePreferences(alias,
                ImmutableMap.<String, Object> builder().build());
        when(preferencesManager.getPreferences(any(KeyStoreEntry.class))).thenReturn(pref);

        final IJobRunner jobRunner = certificateValidityScheduler.createJob();
        final IJobRunnerRequest request = mock(IJobRunnerRequest.class);

        final JobRunnerResponse response = jobRunner.runJob(request);

        assertEquals(JobRunnerResponse.success(), response);
        assertTrue(pref.exists(KeyStorePreferenceKeys.CERTIFICATE_LAST_NOTIFICATION));
        verify(notifier, atLeastOnce()).sendExpiredCertificate(any(), any());
    }

    @Test
    public void shouldNotNotifyCertificateAlreadySent() throws ApplicationException {
        final String alias = "certificate";

        when(keyStoreService.getConfiguration()).thenReturn(defaultConfiguration());

        final DateTime now = DateTime.now();
        when(keyStoreService.findAllWithExpireDateBefore(any(), any()))
                .thenAnswer(withPageOf(KeyStoreEntry.builder().alias(alias).expiredDate(now).build()));

        // already sent now.
        final MockKeyStorePreferences pref = new MockKeyStorePreferences(alias,
                ImmutableMap.<String, Object> builder()
                        .put(KeyStorePreferenceKeys.CERTIFICATE_LAST_NOTIFICATION, now.toDate())
                        .build());
        when(preferencesManager.getPreferences(any(KeyStoreEntry.class))).thenReturn(pref);

        final IJobRunner jobRunner = certificateValidityScheduler.createJob();
        final IJobRunnerRequest request = mock(IJobRunnerRequest.class);

        final JobRunnerResponse response = jobRunner.runJob(request);

        assertEquals(JobRunnerResponse.success(), response);
        assertTrue(pref.exists(KeyStorePreferenceKeys.CERTIFICATE_LAST_NOTIFICATION));
        verify(notifier, never()).sendExpiredCertificate(any(), any());
    }

    @Test
    public void shouldRemidNotifyCertificate() throws ApplicationException {
        final String alias = "certificate";

        when(keyStoreService.getConfiguration()).thenReturn(defaultConfiguration());

        final DateTime now = DateTime.now();
        when(keyStoreService.findAllWithExpireDateBefore(any(), any()))
                .thenAnswer(withPageOf(KeyStoreEntry.builder().alias(alias).expiredDate(now).build()));

        // already sent 10 days before.
        final MockKeyStorePreferences pref = new MockKeyStorePreferences(alias,
                ImmutableMap.<String, Object> builder()
                        .put(KeyStorePreferenceKeys.CERTIFICATE_LAST_NOTIFICATION, now.minusDays(10).toDate())
                        .build());
        when(preferencesManager.getPreferences(any(KeyStoreEntry.class))).thenReturn(pref);

        final IJobRunner jobRunner = certificateValidityScheduler.createJob();
        final IJobRunnerRequest request = mock(IJobRunnerRequest.class);

        final JobRunnerResponse response = jobRunner.runJob(request);

        assertEquals(JobRunnerResponse.success(), response);
        assertTrue(pref.exists(KeyStorePreferenceKeys.CERTIFICATE_LAST_NOTIFICATION));
        verify(notifier, atLeastOnce()).sendExpiredCertificate(any(), any());
    }

    private KeyStoreProperties defaultConfiguration() {
        final KeyStoreProperties configuration = new KeyStoreProperties();
        configuration.getNotification().getExpiration().setEnable(true);
        configuration.getNotification().setContact("john.cook@company.com");
        configuration.getNotification().getExpiration().setReminder(7);
        return configuration;
    }
}
