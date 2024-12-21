package com.pmi.tpd.database.liquibase;

import java.util.Objects;

import com.pmi.tpd.database.liquibase.backup.ILiquibaseChangeSet;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
class LiquibaseChangeSetMetaData implements ILiquibaseChangeSet {

    private final long changeCount;

    /**
     * A measure of the relative size of the change set in terms of the number of changes it contains. A value from 0 to
     * 100. i.e. a percentage.
     */
    private final int weight;

    LiquibaseChangeSetMetaData(final long changeCount, final int weight) {
        this.changeCount = changeCount;
        this.weight = weight;
    }

    @Override
    public long getChangeCount() {
        return changeCount;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LiquibaseChangeSetMetaData that = (LiquibaseChangeSetMetaData) o;
        return changeCount == that.changeCount && weight == that.weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeCount, weight);
    }

    @Override
    public String toString() {
        return "ChangeSetMetaData{" + "changeCount=" + changeCount + ", weight=" + weight + '}';
    }
}
