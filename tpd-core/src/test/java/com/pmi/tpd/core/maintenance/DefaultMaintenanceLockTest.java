package com.pmi.tpd.core.maintenance;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.advisor.event.AddEvent;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.RemoveEvent;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.maintenance.event.SystemMaintenanceEvent;
import com.pmi.tpd.scheduler.exec.IncorrectTokenException;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultMaintenanceLockTest extends MockitoTestCase {

    private static final String TOKEN = "token";

    @Mock
    private IEventPublisher eventPublisher;

    private final I18nService i18nService = new SimpleI18nService(SimpleI18nService.Mode.RETURN_KEYS);

    private DefaultMaintenanceLock lock;

    @Mock
    private IUser owner;

    @Mock
    private Runnable listener1;

    @Mock
    private Runnable listener2;

    private static IEventAdvisorService<?> eventAdvisorService;

    @BeforeAll
    public static void setUpJohnson() throws IOException {
        eventAdvisorService = EventAdvisorService
                .initialize(getResourceAsStream(DefaultMaintenanceLockTest.class, "minimal-event-config.xml"));
    }

    @AfterAll
    public static void tearDownJohnson() {
        eventAdvisorService.terminate();
    }

    @BeforeEach
    public void setUp() {
        lock = new DefaultMaintenanceLock(eventPublisher, i18nService, eventAdvisorService, owner, TOKEN);
        lock.addListener(listener1);
        lock.addListener(listener2);
    }

    @Test
    public void testGetUnlockToken() {
        assertEquals(TOKEN, lock.getUnlockToken());
    }

    @Test
    public void testGetOwner() {
        assertSame(owner, lock.getOwner());
    }

    @Test
    public void testLockRaisesEvent() {
        lock.lock();

        // verify that an AddEvent has been raised
        final ArgumentCaptor<AddEvent> eventCaptor = ArgumentCaptor.forClass(AddEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        // verify the SystemMaintenanceEvent contained in the AddEvent
        verifySystemMaintenanceEvent(eventCaptor.getValue().getEvent());
    }

    @Test
    public void testLockWhileLocked() {
        assertThrows(IllegalStateException.class, () -> {
            lock.lock();
            lock.lock();
        });
    }

    @Test
    public void testUnlockCorrectTokenWhenLocked() {
        lock.lock();

        reset(eventPublisher);
        lock.unlock(TOKEN);

        inOrder(eventPublisher, listener1, listener2);

        // verify that the RemoveEvent was published
        final ArgumentCaptor<RemoveEvent> eventCaptor = ArgumentCaptor.forClass(RemoveEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        verifySystemMaintenanceEvent(eventCaptor.getValue().getEvent());

        // verify that the listeners have been called
        verify(listener1).run();
        verify(listener2).run();
    }

    @Test
    public void testUnlockCorrectTokenWhenNotLocked() {
        assertThrows(IllegalStateException.class, () -> {
            lock.unlock(TOKEN);
        });
    }

    @Test
    public void testUnlockIncorrectToken() {
        assertThrows(IncorrectTokenException.class, () -> {
            lock.lock();
            lock.unlock("wrong");
        });
    }

    private void verifySystemMaintenanceEvent(final Event event) {
        assertTrue(event instanceof SystemMaintenanceEvent);
        final SystemMaintenanceEvent maintenanceEvent = (SystemMaintenanceEvent) event;
        assertTrue(maintenanceEvent.isToken(TOKEN));
        assertEquals(eventAdvisorService.getEventType("performing-maintenance").orElseThrow(),
            maintenanceEvent.getKey());
        assertEquals(eventAdvisorService.getEventLevel("system-maintenance").orElseThrow(),
            maintenanceEvent.getLevel());
    }
}
