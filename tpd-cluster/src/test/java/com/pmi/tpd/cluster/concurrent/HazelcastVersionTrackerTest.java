package com.pmi.tpd.cluster.concurrent;

import static org.mockito.ArgumentMatchers.anyInt;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.IMap;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class HazelcastVersionTrackerTest extends MockitoTestCase {

    @Mock(lenient = true)
    private IMap<Integer, Integer> map;

    private HazelcastVersionTracker<Integer> versionTracker;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @BeforeEach
    public void setUp() throws Exception {
        final Map.Entry entry = mock(Map.Entry.class, withSettings().lenient());
        when(entry.getKey()).thenReturn(0);
        when(entry.getValue()).thenReturn(null);

        when(map.get(0)).thenReturn(0);
        when(map.executeOnKey(anyInt(), any(EntryProcessor.class)))
                .thenAnswer(invocation -> ((EntryProcessor) invocation.getArguments()[1]).process(entry));

        versionTracker = new HazelcastVersionTracker(map);
    }

    @Test
    public void testGetInitial() throws Exception {
        assertEquals(0, versionTracker.get(0));
        verify(map).putIfAbsent(0, 0);
    }

    @Test
    public void testIncrementAndGet() throws Exception {
        assertEquals(1, versionTracker.incrementAndGet(0));
        verify(map).executeOnKey(eq(0), any(EntryProcessor.class));
    }

    @Test
    public void testIncrementAll() throws Exception {
        versionTracker.incrementAll();
        verify(map).executeOnEntries(any(EntryProcessor.class));
    }
}
