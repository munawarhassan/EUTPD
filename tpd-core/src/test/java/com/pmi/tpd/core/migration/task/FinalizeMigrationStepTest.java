package com.pmi.tpd.core.migration.task;

import static org.mockito.ArgumentMatchers.same;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.core.backup.task.MaintenanceTaskTestHelper;
import com.pmi.tpd.core.migration.IMigrationState;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.IDatabaseConfigurationService;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class FinalizeMigrationStepTest extends MockitoTestCase {

    @Mock
    private IDatabaseConfigurationService configurationService;

    @Mock
    private IDatabaseHandle sourceHandle;

    @Mock
    private IDataSourceConfiguration targetConfiguration;

    @Mock
    private IDatabaseHandle targetHandle;

    @Spy
    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    @Mock
    private IMigrationState state;

    @InjectMocks
    private FinalizeMigrationStep step;

    @BeforeEach
    public void setUp() {
        when(state.getSourceDatabase()).thenReturn(sourceHandle);
        when(state.getTargetDatabase()).thenReturn(targetHandle);
        when(targetHandle.getConfiguration()).thenReturn(targetConfiguration);
    }

    @Test
    public void testProgress() {
        MaintenanceTaskTestHelper.assertProgress("app.migration.finalizing", 0, step.getProgress());

        step.run();

        MaintenanceTaskTestHelper.assertProgress("app.migration.finalizing", 100, step.getProgress());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRun() {
        step.run();

        verify(configurationService).saveDataSourceConfiguration(same(targetConfiguration), any(Optional.class));
        verify(sourceHandle).close();
    }

}
