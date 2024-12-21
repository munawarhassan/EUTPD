package com.pmi.tpd.core.maintenance;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class LocalMaintenanceTaskStatusSupplierTest extends TestCase {

    private final LocalMaintenanceTaskStatusSupplier holder = new LocalMaintenanceTaskStatusSupplier();

    @Test
    public void testSetWhenNoExistingStatus() throws Exception {
        final IRunnableMaintenanceTaskStatus status = newStatus("status-id", 0L);
        holder.set(status);
        assertSame(status, holder.get());
    }

    @Test
    public void testSetWhenExistingWithSameId() throws Exception {
        final IRunnableMaintenanceTaskStatus status = newStatus("status-id", 0L);
        holder.set(status);
        final IRunnableMaintenanceTaskStatus newStatus = newStatus("status-id", 0L);
        holder.set(newStatus);
        assertSame(newStatus, holder.get());
    }

    @Test
    public void testSetWhenExistingWithDifferentIdBefore() throws Exception {
        final IRunnableMaintenanceTaskStatus status = newStatus("status-id", 10L);
        holder.set(status);
        final IRunnableMaintenanceTaskStatus newStatus = newStatus("other-status-id", 5L);
        holder.set(newStatus);
        assertSame(status, holder.get());
    }

    @Test
    public void testSetWhenExistingWithDifferentIdAfter() throws Exception {
        final IRunnableMaintenanceTaskStatus status = newStatus("status-id", 5L);
        holder.set(status);
        final IRunnableMaintenanceTaskStatus newStatus = newStatus("other-status-id", 10L);
        holder.set(newStatus);
        assertSame(newStatus, holder.get());
    }

    private IRunnableMaintenanceTaskStatus newStatus(final String id, final long startTime) {
        final IRunnableMaintenanceTaskStatus status = mock(IRunnableMaintenanceTaskStatus.class);
        when(status.getId()).thenReturn(id);
        when(status.getStartTime()).thenReturn(new Date(startTime));
        return status;
    }

}
