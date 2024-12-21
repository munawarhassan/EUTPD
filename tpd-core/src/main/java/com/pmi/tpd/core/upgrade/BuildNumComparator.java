package com.pmi.tpd.core.upgrade;

import java.util.Comparator;

/**
 * <p>
 * BuildNumComparator class.
 * </p>
 *
 * @author devacfr
 * @since 1.0
 */
public class BuildNumComparator implements Comparator<String> {

    /** {@inheritDoc} */
    @Override
    public int compare(final String o1, final String o2) {
        return Long.valueOf(o1).compareTo(Long.valueOf(o2));
    }

}
