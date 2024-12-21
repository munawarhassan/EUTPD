package com.pmi.tpd.database.liquibase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;

/**
 * A collection of change set meta data.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ChangeLogOutline implements Iterable<LiquibaseChangeSetMetaData> {

    /** */
    private final List<LiquibaseChangeSetMetaData> changeSetMetaDatas;

    /** */
    private final int nonEmptyChangeSetCount;

    /**
     * Constructs a change log outline with the given change counts.
     * <p>
     * Care is taken to deal with the rounding of change set weights. This method calculates the 'weight' of each change
     * set as a measure of the relative size of that change set against the total of all change sets. We ensure that the
     * overall weight adds up to nearly 100 (percent) despite the rounding-down of fractional weights.
     *
     * @param changeCounts
     *            the sizes of the change sets. The size of this collection tells you the number of change sets, and the
     *            sizes themselves are the numbers of changes in each change set.
     */
    public ChangeLogOutline(final Iterable<Long> changeCounts) {
        final int size = Iterables.size(changeCounts);
        if (size > 100) {
            throw new IllegalArgumentException(
                    "The current changelog backup and restore process supports a " + "maximum of 100 changesets.");
        }

        changeSetMetaDatas = new ArrayList<>(size);

        // first pass to calculate the total number of changes and the number of non-empty changesets
        long totalChangeCount = 0;
        int nonEmptyChangeSetCount = 0;
        for (final Long changeCount : changeCounts) {
            totalChangeCount += changeCount;
            if (changeCount > 0) {
                nonEmptyChangeSetCount++;
            }
        }

        // the running total count of all changes
        long cumulativeCount = 0;

        // the running total of all changeset weights
        int cumulativeWeight = 0;

        // the number of non-empty changesets that are yet to be processed
        int nonEmptyChangeSetsRemaining = nonEmptyChangeSetCount;

        // second pass to calculate the relative weight of each changeset
        for (final Long changeCount : changeCounts) {
            cumulativeCount += changeCount;

            // the percentage that all weights thus far (including the current changeset) *should* add up to
            int desiredCumulativeWeight = 0;
            if (totalChangeCount > 0) {
                desiredCumulativeWeight = (int) (cumulativeCount * 100 / totalChangeCount);
            }

            // give the current summary a weight that gets the total weight as close to the desired cumulative weight
            // weight will always be positive for a non-empty changeset
            int weight = 0;
            if (changeCount > 0) {
                nonEmptyChangeSetsRemaining--;
                weight = desiredCumulativeWeight - cumulativeWeight;
                // non-empty changesets must have non-zero weight
                weight = Math.max(1, weight);
                // make sure there is enough unallocated weight for remaining non-empty changesets to have non-zero
                // weights (i.e. at least 1)
                weight = Math.min(weight, 100 - cumulativeWeight - nonEmptyChangeSetsRemaining);
            }

            changeSetMetaDatas.add(new LiquibaseChangeSetMetaData(changeCount, weight));
            cumulativeWeight += weight;
        }
        this.nonEmptyChangeSetCount = nonEmptyChangeSetCount;
    }

    @Override
    public Iterator<LiquibaseChangeSetMetaData> iterator() {
        return changeSetMetaDatas.iterator();
    }

    public int nonEmptyChangeSetCount() {
        return nonEmptyChangeSetCount;
    }
}
