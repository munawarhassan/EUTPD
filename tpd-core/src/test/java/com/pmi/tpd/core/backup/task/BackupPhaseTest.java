package com.pmi.tpd.core.backup.task;

import static java.util.Optional.of;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class BackupPhaseTest extends MockitoTestCase {

    @Mock
    private IAuthenticationContext authenticationContext;

    @Mock
    private IApplicationConfiguration applicationSettings;

    @Spy
    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    @Mock
    private IRunnableTask nested;

    private BackupPhase phase;

    @InjectMocks
    private BackupPhase.Builder phaseBuilder;

    @Mock
    private IBackupState state;

    @Captor
    private ArgumentCaptor<File> fileCaptor;

    public Path temporaryFolder;

    @BeforeEach
    public void forEach(@TempDir final Path path) {
        phase = phaseBuilder.add(nested, 100).build();
        this.temporaryFolder = path;
        when(applicationSettings.getBackupDirectory()).thenReturn(path);
    }

    @Test
    public void testBackupDeletedWhenNestedFailsWithException() {
        doThrow(new NullPointerException("gotcha")).when(nested).run();

        try {
            phase.run();
        } catch (final BackupException e) {
            // expected. Now verify that the partially written file has been deleted
            verify(state).setBackupFile(fileCaptor.capture());
            assertFalse(fileCaptor.getValue().exists(), "partial zip file should have been deleted!");
        }
    }

    @Test
    public void testBackupSurvivesWhenNestedSucceeds() {
        phase.run();

        verify(state).setBackupFile(fileCaptor.capture());
        verify(state).setBackupZipStream(any(BackupPhase.CloseShieldZipOutputStream.class));

        assertTrue(fileCaptor.getValue().exists(), "partial zip file shouldn't have been deleted!");
    }

    @Test
    public void testBackupFilenameIncludesAnonymousWhenNotAuthenticated() {
        phase.run();

        verify(state).setBackupFile(fileCaptor.capture());
        assertTrue(fileCaptor.getValue().getName().contains("anonymous"));
    }

    @Test
    public void testBackupFilenameIncludesUsernameWhenAuthenticated() {
        final IUser user = mock(IUser.class);
        when(user.getUsername()).thenReturn("mr_retention");
        when(authenticationContext.getCurrentUser()).thenReturn(of(user));

        phase.run();

        verify(state).setBackupFile(fileCaptor.capture());
        assertTrue(fileCaptor.getValue().getName().contains("mr_retention"));
    }

    @Test
    public void testBackupFilenameRemovesDots() {
        // the backup service ignores anything with '.', '/' and '\' to protected against directory escaping
        // plus forward and back slashes will cause trouble when creating the file
        final IUser user = mock(IUser.class);
        when(user.getUsername()).thenReturn("mr..foo/bar\\baz");
        when(authenticationContext.getCurrentUser()).thenReturn(of(user));

        phase.run();

        verify(state).setBackupFile(fileCaptor.capture());
        assertTrue(fileCaptor.getValue().getName().contains("mr__foo_bar_baz"));
    }

}
