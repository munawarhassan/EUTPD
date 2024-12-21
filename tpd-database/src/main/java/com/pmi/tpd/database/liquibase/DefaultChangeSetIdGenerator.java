package com.pmi.tpd.database.liquibase;

import javax.annotation.Nonnull;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
class DefaultChangeSetIdGenerator implements IChangeSetIdGenerator {

    /** */
    private int changeNumber = 0;

    @Override
    @Nonnull
    public String next(@Nonnull final String prefix) {
        return prefix + "-" + ++changeNumber;
    }
}
