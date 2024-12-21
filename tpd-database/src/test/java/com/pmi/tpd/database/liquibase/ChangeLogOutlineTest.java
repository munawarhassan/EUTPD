package com.pmi.tpd.database.liquibase;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests the operation of the {@link ChangeLogOutline} class.
 */
public class ChangeLogOutlineTest extends TestCase {

    @Test
    public void emptyOutlineHasNoNext() {
        final List<Long> emptyList = Lists.newArrayList();
        assertFalse(new ChangeLogOutline(emptyList).iterator().hasNext());
    }

    @Test
    public void oneEmtpyChangeSet() {
        final List<Long> changeCounts = Lists.newArrayList(0L);
        assertEquals(new LiquibaseChangeSetMetaData(0, 0), new ChangeLogOutline(changeCounts).iterator().next());
    }

    @Test
    public void oneChangeSetWithOneChange() {
        final List<Long> changeCounts = Lists.newArrayList(1L);
        assertEquals(new LiquibaseChangeSetMetaData(1, 100), new ChangeLogOutline(changeCounts).iterator().next());
    }

    @Test
    public void oneChangeSetWithTwoChanges() {
        final List<Long> changeCounts = Lists.newArrayList(2L);
        assertEquals(new LiquibaseChangeSetMetaData(2, 100), new ChangeLogOutline(changeCounts).iterator().next());
    }

    @Test
    public void twoChangeSetsWithOneChangeEach() {
        final List<Long> changeCounts = Lists.newArrayList(1L, 1L);
        final ChangeLogOutline outline = new ChangeLogOutline(changeCounts);
        final Iterator<LiquibaseChangeSetMetaData> iterator = outline.iterator();
        assertEquals(new LiquibaseChangeSetMetaData(1, 50), iterator.next());
        assertEquals(new LiquibaseChangeSetMetaData(1, 50), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void emptyChangeSetFollowedByChangeSetWithOneChange() {
        final List<Long> changeCounts = Lists.newArrayList(0L, 1L);
        final ChangeLogOutline outline = new ChangeLogOutline(changeCounts);
        final Iterator<LiquibaseChangeSetMetaData> iterator = outline.iterator();
        assertEquals(new LiquibaseChangeSetMetaData(0, 0), iterator.next());
        assertEquals(new LiquibaseChangeSetMetaData(1, 100), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void tooManyChangeSets() {
        assertThrows(IllegalArgumentException.class, () -> {
            final List<Long> changeCounts = Lists.newArrayList();

            // fill changeCounts with 200 1's
            for (int i = 0; i < 200; i++) {
                changeCounts.add(1L);
            }

            // should throw IAE since there are more than 100 changesets
            new ChangeLogOutline(changeCounts);
        });
    }

    /**
     * Test that one hundred changesets will all have a weight of one, despite their relative sizes.
     */
    @Test
    public void oneHundredChangesets() {
        final List<Long> changeCounts = Lists.newArrayList();

        // add 100 changecounts, with 1..100 changes each
        long changes = 1;
        for (int i = 0; i < 100; i++) {
            changeCounts.add(changes++);
        }

        final ChangeLogOutline outline = new ChangeLogOutline(changeCounts);
        int count = 0;
        for (final LiquibaseChangeSetMetaData metaData : outline) {
            // each changeset must have non-zero weight, so each should have a weight of 1
            assertEquals(1, metaData.getWeight());
            count++;
        }
        assertEquals(100, count);
    }

    @Test
    public void twentyEquallySizedChangesets() {
        final List<Long> changeCounts = Lists.newArrayList();

        for (int i = 0; i < 20; i++) {
            changeCounts.add(105L);
        }

        final ChangeLogOutline outline = new ChangeLogOutline(changeCounts);
        int count = 0;
        for (final LiquibaseChangeSetMetaData metaData : outline) {
            // each should have an equal weight
            assertEquals(5, metaData.getWeight());
            count++;
        }
        assertEquals(20, count);
    }

    /**
     * Test that a large change has a (roughly) proportionally larger weight than smaller changes, but that the smaller
     * changes still have a non-zero weight.
     */
    @Test
    public void oneGiantAndNineTinyChangesets() {
        final List<Long> changeCounts = Lists.newArrayList();

        changeCounts.add(1000L);
        for (int i = 0; i < 9; i++) {
            changeCounts.add(1L);
        }

        final ChangeLogOutline outline = new ChangeLogOutline(changeCounts);
        final Iterator<LiquibaseChangeSetMetaData> metaDataIterator = outline.iterator();
        assertEquals(91, metaDataIterator.next().getWeight());
        int count = 1;
        while (metaDataIterator.hasNext()) {
            final LiquibaseChangeSetMetaData metaData = metaDataIterator.next();
            assertEquals(1, metaData.getWeight());
            count++;
        }
        assertEquals(10, count);
    }

    /**
     * This is similar to the above test, but ensures that the ordering of different sized changesets doesn't effect the
     * weighting calculation.
     */
    @Test
    public void nineTinyAndOneGiantChangeset() {
        final List<Long> changeCounts = Lists.newArrayList();

        for (int i = 0; i < 9; i++) {
            changeCounts.add(1L);
        }
        changeCounts.add(1000L);

        final ChangeLogOutline outline = new ChangeLogOutline(changeCounts);
        final Iterator<LiquibaseChangeSetMetaData> metaDataIterator = outline.iterator();
        int count = 0;
        while (metaDataIterator.hasNext() && count < 9) {
            final LiquibaseChangeSetMetaData metaData = metaDataIterator.next();
            assertEquals(1, metaData.getWeight());
            count++;
        }
        assertEquals(91, metaDataIterator.next().getWeight());
        count++;
        assertEquals(10, count);
    }
}
