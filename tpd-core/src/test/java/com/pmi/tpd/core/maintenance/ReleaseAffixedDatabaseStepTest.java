package com.pmi.tpd.core.maintenance;

import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import com.google.common.collect.ImmutableList;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.core.migration.MigrationException;
import com.pmi.tpd.database.spi.IDatabaseAffixed;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class ReleaseAffixedDatabaseStepTest extends MockitoTestCase {

    @Mock(name = "firstAffix")
    private IDatabaseAffixed firstAffix;

    @Spy
    private final I18nService i18nService = new SimpleI18nService();

    @Mock(name = "secondAffix")
    private IDatabaseAffixed secondAffix;

    private ReleaseAffixedDatabaseStep step;

    @BeforeEach
    public void setup() {
        step = new ReleaseAffixedDatabaseStep(i18nService, ImmutableList.of(firstAffix, secondAffix));
    }

    @Test
    public void testGetProgress() {
        final IProgress progress = step.getProgress();
        assertNotNull(progress);
        assertEquals("app.migration.releasingdatabase", progress.getMessage());
        assertEquals(0, progress.getPercentage());
    }

    @Test
    public void testRun() {
        step.run();

        final IProgress progress = step.getProgress();
        assertNotNull(progress);
        assertEquals("app.migration.releasingdatabase", progress.getMessage());
        assertEquals(100, progress.getPercentage());
    }

    @Test
    public void testRunHandlesCancellation() {
        doAnswer(invocation -> {
            step.cancel();

            return null;
        }).when(firstAffix).release();

        step.run();
        verify(firstAffix).release();
        verifyNoInteractions(secondAffix);
    }

    @Test
    public void testRunHandlesExceptions() {
        assertThrows(MigrationException.class, () -> {
            doThrow(RuntimeException.class).when(secondAffix).release();

            try {
                step.run();
            } finally {
                verify(firstAffix).release();
                verify(secondAffix).release();
                verify(i18nService).createKeyedMessage(eq("app.migration.releasedatabasefailed"));
            }
        });
    }
}
