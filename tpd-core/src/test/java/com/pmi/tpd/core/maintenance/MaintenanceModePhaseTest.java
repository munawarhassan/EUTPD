package com.pmi.tpd.core.maintenance;

import static org.mockito.ArgumentMatchers.same;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class MaintenanceModePhaseTest extends MockitoTestCase {

    @Mock
    private MaintenanceApplicationEvent event;

    @Mock
    private IMaintenanceModeHelper maintenanceModeHelper;

    private MaintenanceModePhase phase;

    @Mock
    private IRunnableTask step;

    @BeforeEach
    public void setUp() {
        phase = new MaintenanceModePhase.Builder(maintenanceModeHelper).event(event).add(step, 100).build();
    }

    @Test
    public void testRun() throws Exception {
        try {
            phase.run();
        } finally { // Done in a finally block to allow testRunStepException to reuse it
            final InOrder ordered = inOrder(maintenanceModeHelper, step);
            ordered.verify(maintenanceModeHelper).lock(same(event));
            ordered.verify(step).run();
            ordered.verify(maintenanceModeHelper).unlock(same(event));
        }
    }

    @Test
    public void testRunStepException() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> {
            doThrow(new UnsupportedOperationException()).when(step).run();

            testRun();
        });
    }
}
