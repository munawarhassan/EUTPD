package com.pmi.tpd.core.user.permission;

import static java.util.Arrays.asList;

import org.springframework.data.domain.Page;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.model.user.UserEntity;

abstract class AbstractPermissionTest extends BaseDaoTestIT {

    static final Function<UserEntity, Long> USER_ID = user -> user.getId();

    static void assertGroups(final Page<String> page, final String... expected) {
        assertGroups(page, expected == null || expected.length < 10, expected);
    }

    static void assertGroups(final Page<String> page, final boolean last, final String... expected) {
        assertNotNull(page);
        if (expected == null || expected.length == 0) {
            assertEquals(0, page.getSize());
            assertTrue(page.isLast());
        } else {
            assertEquals(last, page.isLast());
            assertEquals(ImmutableList.copyOf(expected), ImmutableList.copyOf(page.getContent()));
        }
    }

    @SafeVarargs
    static <T, N extends Number> void assertIdentities(final Page<T> page,
        final Function<T, N> identity,
        final boolean last,
        final N... expected) {
        assertNotNull(page);
        if (expected == null || expected.length == 0) {
            assertEquals(0, page.getSize());
            assertTrue(page.isLast());
        } else {
            assertEquals(last, page.isLast());
            assertEquals(ImmutableList.copyOf(expected),
                ImmutableList.copyOf(Iterables.transform(page.getContent(), identity)));
        }
    }

    static void assertUsers(final Page<UserEntity> page, final Long... expected) {
        assertUsers(page, true, expected);
    }

    static void assertUsers(final Page<UserEntity> page, final boolean last, final Long... expected) {
        assertIdentities(page, USER_ID, last, expected);
    }

    static PartitionedGroups createGroups(final String... groups) {
        return createSplitGroups(0, groups);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static PartitionedGroups createSplitGroups(final int partitionSize, final String... groups) {
        return () -> (partitionSize == 0 ? asList(asList(groups))
                : (Iterable) Lists.partition(asList(groups), partitionSize)).iterator();
    }

}
