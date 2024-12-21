package com.pmi.tpd.web.logback;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.pmi.tpd.api.LoggingConstants;
import com.pmi.tpd.testing.junit5.TestCase;
import com.pmi.tpd.web.logback.pattern.DefaultingMDCConverter;
import com.pmi.tpd.web.logback.pattern.EnhancedThrowableConverter;
import com.pmi.tpd.web.logback.pattern.RequestContextConverter;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;

public class LogFormatPropertyDefinerTest extends TestCase {

    @Test
    public void testGetPropertyValue() {
        assertEquals(LoggingConstants.LOG_FORMAT, new LogFormatPropertyDefiner().getPropertyValue());
    }

    @Test
    public void testSetContextCreatingMap() {
        final Context context = mock(Context.class);

        new LogFormatPropertyDefiner().setContext(context);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        final ArgumentCaptor<Map<String, String>> mapCaptor = (ArgumentCaptor) ArgumentCaptor.forClass(Map.class);

        verify(context).getObject(eq(CoreConstants.PATTERN_RULE_REGISTRY));
        verify(context).putObject(eq(CoreConstants.PATTERN_RULE_REGISTRY), mapCaptor.capture());

        final Map<String, String> map = mapCaptor.getValue();
        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals(DefaultingMDCConverter.class.getName(), map.get(LoggingConstants.FORMAT_DEFAULTING_MDC));
        assertEquals(RequestContextConverter.class.getName(), map.get(LoggingConstants.FORMAT_REQUEST_CONTEXT));
        assertEquals(EnhancedThrowableConverter.class.getName(), map.get(LoggingConstants.FORMAT_ENHANCED_THROWABLE));
    }

    @Test
    public void testSetContextMergingMap() {
        @SuppressWarnings("unchecked")
        final Map<String, String> map = mock(Map.class);

        final Context context = mock(Context.class);
        when(context.getObject(eq(CoreConstants.PATTERN_RULE_REGISTRY))).thenReturn(map);

        new LogFormatPropertyDefiner().setContext(context);

        verify(context).getObject(eq(CoreConstants.PATTERN_RULE_REGISTRY));
        verifyNoMoreInteractions(context);

        verify(map).put(eq(LoggingConstants.FORMAT_DEFAULTING_MDC), eq(DefaultingMDCConverter.class.getName()));
        verify(map).put(eq(LoggingConstants.FORMAT_REQUEST_CONTEXT), eq(RequestContextConverter.class.getName()));
        verify(map).put(eq(LoggingConstants.FORMAT_ENHANCED_THROWABLE), eq(EnhancedThrowableConverter.class.getName()));
        verifyNoMoreInteractions(map);
    }
}
