package com.pmi.tpd.database.liquibase;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class DefaultChangeSetIdGeneratorTest extends TestCase {

    @Test
    public void testIdsChange() {
        final DefaultChangeSetIdGenerator generator = new DefaultChangeSetIdGenerator();
        final String id1 = generator.next("test");
        final String id2 = generator.next("test");
        assertThat(id1, not(equalTo(id2)));
    }

    @Test
    public void testPrefixing() {
        final DefaultChangeSetIdGenerator generator = new DefaultChangeSetIdGenerator();
        assertTrue(generator.next("blah").startsWith("blah"));
    }
}
