package com.pmi.tpd.core.audit;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Sets;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.AuditEntryConverter;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.audit.annotation.Audited;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.core.event.audit.AuditEvent;
import com.pmi.tpd.testing.junit5.MockitoTestCase;
import com.pmi.tpd.web.core.request.IRequestManager;

public class AuditedAnnotatedEventListenerTest extends MockitoTestCase {

    static IAuditEntry lastEntry;

    @Mock
    private IAuditEntryLoggingService auditLoggingService;

    @InjectMocks
    private AuditedAnnotatedEventListener listener;

    @Mock(lenient = true)
    private IEventPublisher publisher;

    @Mock
    private IRequestManager requestManager;

    private AuditEvent lastPublished;

    private boolean publisherThrowEx;

    @BeforeEach
    public void setup() {
        lastPublished = null;
        publisherThrowEx = false;

        final Answer<Void> answer = invocation -> {
            if (publisherThrowEx) {
                throw new NullPointerException();
            }

            lastPublished = (AuditEvent) invocation.getArguments()[0];
            return null;
        };
        doAnswer(answer).when(publisher).publish(any());
    }

    @Test
    public void testListenAudited() throws Exception {
        listener.onEvent(new NotNaughty());
        assertEquals(lastPublished.getEntry(), lastEntry);
        assertEquals(Sets.newHashSet("1", "2"), lastPublished.getChannels());
        assertEquals(Priority.MEDIUM, lastPublished.getPriority());
    }

    @Test
    public void testListenAuditedPriorityOnChildren() throws Exception {
        listener.onEvent(new NotNaughty2());
        assertEquals(lastPublished.getEntry(), lastEntry);
        assertEquals(Sets.newHashSet("1"), lastPublished.getChannels());
        assertEquals(Priority.HIGH, lastPublished.getPriority());
    }

    @Test
    public void testListenAuditedAuditEvent() throws Exception {
        listener.onEvent(new Naughty(this, mock(IAuditEntry.class), Collections.<String> emptySet(), Priority.MEDIUM));
        assertTrue(lastPublished == null);
    }

    @Test
    public void testListenNonAudited() throws Exception {
        listener.onEvent(new Object());
        assertTrue(lastPublished == null);
    }

    @Test
    public void testListenNaughtyTranslatorEvent() throws Exception {
        assertThrows(NoSuchMethodException.class, () -> {
            try {
                listener.onEvent(new NaughtyTranslatorEvent());
            } finally {
                assertTrue(this.lastPublished == null);
                verify(auditLoggingService).log(any(IAuditEntry.class));
                verifyNoMoreInteractions(auditLoggingService);
            }
        });
    }

    @Test
    public void testListenExceptionalNaughtyTranslatorEvent() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                listener.onEvent(new ExceptionalNaughtyTranslatorEvent());
            } finally {
                assertTrue(this.lastPublished == null);
                verify(auditLoggingService).log(any(IAuditEntry.class));
                verifyNoMoreInteractions(auditLoggingService);
            }
        });

    }

    @Test
    public void testListenAuditedBadPublish() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            publisherThrowEx = true;
            try {
                listener.onEvent(new NotNaughty());
            } finally {
                assertTrue(this.lastPublished == null);
                verify(auditLoggingService, times(2)).log(any(IAuditEntry.class));
                verifyNoMoreInteractions(auditLoggingService);
            }
        });

    }

    @Audited(converter = NotNaughtyTranslator.class, channels = { "1", "2" }, priority = Priority.MEDIUM)
    private static class NotNaughty {
    }

    @Audited(converter = NotNaughtyTranslator.class, channels = { "1" }, priority = Priority.HIGH)
    private static class NotNaughty2 extends NotNaughty {
    }

    public static class NotNaughtyTranslator implements AuditEntryConverter<NotNaughty> {

        public NotNaughtyTranslator() {
        }

        @Nonnull
        @Override
        public IAuditEntry convert(@Nonnull final NotNaughty event, final AuditEntryBuilder builder) {
            lastEntry = mock(IAuditEntry.class);
            return lastEntry;
        }
    }

    @Audited(converter = BasicEventTranslator.class, priority = Priority.MEDIUM)
    public static class Naughty extends AuditEvent {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public Naughty(@Nonnull final Object source, @Nonnull final IAuditEntry entry,
                @Nonnull final Set<String> channels, final Priority priority) {
            super(source, entry, channels, priority);
        }
    }

    public static class BasicEventTranslator implements AuditEntryConverter<Object> {

        @Nonnull
        @Override
        public IAuditEntry convert(@Nonnull final Object event, final AuditEntryBuilder builder) {
            return mock(IAuditEntry.class);
        }
    }

    public static class NaughtyTranslator implements AuditEntryConverter<Object> {

        public NaughtyTranslator(final Object feedME) {
        }

        @Nonnull
        @Override
        public IAuditEntry convert(@Nonnull final Object event, final AuditEntryBuilder builder) {
            return mock(IAuditEntry.class);
        }
    }

    @Audited(converter = NaughtyTranslator.class, priority = Priority.MEDIUM)
    public static class NaughtyTranslatorEvent {
    }

    public static class ExceptionalNaughtyTranslator implements AuditEntryConverter<Object> {

        @Nonnull
        @Override
        public IAuditEntry convert(@Nonnull final Object event, final AuditEntryBuilder builder) {
            throw new IllegalArgumentException("I am naughty!");
        }
    }

    @Audited(converter = ExceptionalNaughtyTranslator.class, priority = Priority.MEDIUM)
    public static class ExceptionalNaughtyTranslatorEvent {
    }

}
