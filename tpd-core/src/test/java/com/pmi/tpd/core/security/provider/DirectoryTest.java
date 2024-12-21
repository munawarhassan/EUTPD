package com.pmi.tpd.core.security.provider;

import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.pmi.tpd.core.security.OperationType;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DirectoryTest extends MockitoTestCase {

    /**
     * Directory with empty {@link OperationType operations} is readonly.
     */
    @Test
    public void testDirectoryWithEmptyOperations() {
        final DefaultDirectory directory = create(Collections.emptyList());

        assertEquals("test", directory.getName());
        assertEquals(true, directory.isActive());
        assertThat(directory.getAllowedOperations(), empty());

        assertOperations(directory, false, false, false, false, false, false);
    }

    @Test
    public void testDirectoryWithEachOperation() {

        assertOperations(create(
            Lists.newArrayList(OperationType.CREATE_GROUP)), true, false, false, false, false, false);
        assertOperations(create(
            Lists.newArrayList(OperationType.DELETE_GROUP)), false, true, false, false, false, false);
        assertOperations(create(
            Lists.newArrayList(OperationType.UPDATE_GROUP)), false, false, true, false, false, false);
        assertOperations(create(
            Lists.newArrayList(OperationType.CREATE_USER)), false, false, false, true, false, false);
        assertOperations(create(
            Lists.newArrayList(OperationType.DELETE_USER)), false, false, false, false, true, false);
        assertOperations(create(
            Lists.newArrayList(OperationType.UPDATE_USER)), false, false, false, false, false, true);

        assertOperations(create(Lists.newArrayList(OperationType.values())), true, true, true, true, true, true);
    }

    private DefaultDirectory create(final List<OperationType> operations) {
        return new DefaultDirectory("test", operations, true);
    }

    private void assertOperations(final DefaultDirectory directory,
        final boolean groupCreatable,
        final boolean groupDeletable,
        final boolean groupUpdatable,
        final boolean userCreatable,
        final boolean userDeletable,
        final boolean userUpdatable) {

        assertEquals(groupCreatable, directory.isGroupCreatable());
        assertEquals(groupDeletable, directory.isGroupDeletable());
        assertEquals(groupUpdatable, directory.isGroupUpdatable());
        assertEquals(userCreatable, directory.isUserCreatable());
        assertEquals(userDeletable, directory.isUserDeletable());
        assertEquals(userUpdatable, directory.isUserUpdatable());
    }

}
