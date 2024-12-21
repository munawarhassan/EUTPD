package com.pmi.tpd.database.liquibase;

import javax.annotation.Nonnull;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
interface IChangeSetIdGenerator {

    @Nonnull
    String next(@Nonnull String prefix);
}
