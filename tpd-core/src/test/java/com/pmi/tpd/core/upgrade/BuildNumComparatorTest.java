package com.pmi.tpd.core.upgrade;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class BuildNumComparatorTest extends TestCase {

    private final BuildNumComparator versionComparator = new BuildNumComparator();

    @Test
    public void comparator() {
        assertTrue(compare("1", "1") == 0);
        assertTrue(compare("59", "6") > 0);
        assertTrue(compare("9", "66") < 0);
    }

    private int compare(final String o, final String o2) {
        return versionComparator.compare(o, o2);
    }
}
