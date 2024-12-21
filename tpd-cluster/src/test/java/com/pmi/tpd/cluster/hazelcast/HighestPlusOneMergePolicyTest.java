package com.pmi.tpd.cluster.hazelcast;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.hazelcast.spi.merge.MergingLastUpdateTime;
import com.hazelcast.spi.merge.MergingValue;
import com.pmi.tpd.testing.junit5.TestCase;

public class HighestPlusOneMergePolicyTest extends TestCase {

    @Test
    public void testMergingSameValue() throws Exception {
        final EntryValue existing = mockEntry(1);
        final EntryValue merging = mockEntry(1);

        assertEquals(2, new HighestPlusOneMergePolicy<>().merge(merging, existing));
    }

    @Test
    public void testMergingLessThanExisting() throws Exception {
        final EntryValue existing = mockEntry(1);
        final EntryValue merging = mockEntry(0);

        assertEquals(2, new HighestPlusOneMergePolicy<>().merge(merging, existing));
    }

    @Test
    public void testMergingGreaterThanExisting() throws Exception {
        final EntryValue existing = mockEntry(0);
        final EntryValue merging = mockEntry(1);

        assertEquals(2, new HighestPlusOneMergePolicy<>().merge(merging, existing));
    }

    @Test
    public void testExistingNull() throws Exception {
        final EntryValue existing = mockEntry(null);
        final EntryValue merging = mockEntry(1);

        assertEquals(2, new HighestPlusOneMergePolicy<>().merge(merging, existing));
    }

    @Test
    public void testMergingNull() throws Exception {
        final EntryValue existing = mockEntry(1);
        final EntryValue merging = mockEntry(null);

        assertEquals(2, new HighestPlusOneMergePolicy<>().merge(merging, existing));
    }

    @Test
    public void testNonIntegersReturnsMostRecent() throws Exception {
        final EntryValue existing = mockEntry("abc");
        when(existing.getLastUpdateTime()).thenReturn(0L);
        final EntryValue merging = mockEntry("123");
        when(merging.getLastUpdateTime()).thenReturn(1L);

        assertEquals("123", new HighestPlusOneMergePolicy<>().merge(merging, existing));
    }

    private EntryValue mockEntry(final Object value) {
        final EntryValue entry = mock(EntryValue.class);
        when(entry.getValue()).thenReturn(value);
        when(entry.getRawValue()).thenReturn(value);
        return entry;
    }

    public interface EntryValue extends MergingValue<Object>, MergingLastUpdateTime {

    }
}
