package com.pmi.tpd.euceg.core.task;

import static java.util.Optional.of;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.core.exporter.submission.SubmissionReportType;
import com.pmi.tpd.euceg.core.task.TrackingReportPhase.Builder;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class TrackingReportPhaseTest extends MockitoTestCase {

    @Mock
    private IAuthenticationContext authenticationContext;

    @Mock
    private IApplicationConfiguration applicationSettings;

    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    @Mock
    private IRunnableTask nested;

    private TrackingReportPhase phase;

    private TrackingReportPhase.Builder phaseBuilder;

    @Mock
    private ITrackingReportState state;

    @Captor
    private ArgumentCaptor<File> fileCaptor;

    public Path temporaryFolder;

    @BeforeEach
    public void forEach(@TempDir final Path path) {
        phaseBuilder = new Builder(authenticationContext, i18nService, applicationSettings, state);
        when(state.getReportType()).thenReturn(SubmissionReportType.submission);
        phase = phaseBuilder.add(nested, 100).build();
        this.temporaryFolder = path;
        when(applicationSettings.getReportDirectory()).thenReturn(path);
    }

    @Test
    public void testReportDeletedWhenNestedFailsWithException() {
        doThrow(new NullPointerException("gotcha")).when(nested).run();

        try {
            phase.run();
        } catch (final EucegException e) {
            // expected. Now verify that the partially written file has been deleted
            verify(state).setReportFile(fileCaptor.capture());
            assertFalse(fileCaptor.getValue().exists(), "partial zip file should have been deleted!");
        }
    }

    @Test
    public void testReportSurvivesWhenNestedSucceeds() {
        phase.run();

        verify(state).setReportFile(fileCaptor.capture());
        verify(state).setReportOutputStream(any(OutputStream.class));

        assertTrue(fileCaptor.getValue().exists(), "partial zip file shouldn't have been deleted!");
    }

    @Test
    public void testReportFilenameIncludesAnonymousWhenNotAuthenticated() {
        phase.run();

        verify(state).setReportFile(fileCaptor.capture());
        assertTrue(fileCaptor.getValue().getName().contains("anonymous"));
    }

    @Test
    public void testBackupFilenameIncludesUsernameWhenAuthenticated() {
        final IUser user = mock(IUser.class);
        when(user.getUsername()).thenReturn("mr_retention");
        when(authenticationContext.getCurrentUser()).thenReturn(of(user));

        phase.run();

        verify(state).setReportFile(fileCaptor.capture());
        assertTrue(fileCaptor.getValue().getName().contains("mr_retention"));
    }

    @Test
    public void testReportFilenameRemovesDots() {
        // the backup service ignores anything with '.', '/' and '\' to protected against directory escaping
        // plus forward and back slashes will cause trouble when creating the file
        final IUser user = mock(IUser.class);
        when(user.getUsername()).thenReturn("mr..foo/bar\\baz");
        when(authenticationContext.getCurrentUser()).thenReturn(of(user));

        phase.run();

        verify(state).setReportFile(fileCaptor.capture());
        assertTrue(fileCaptor.getValue().getName().contains("mr__foo_bar_baz"));
    }

}
