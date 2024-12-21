package com.pmi.tpd.database.liquibase;

import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ILiquibaseXmlWriterFactory {

    /**
     * Create a new {@link ILiquibaseXmlWriter writer}.
     *
     * @param stream
     *               stream the writer will write to
     * @param author
     *               author assigned to the Liquibase changesets
     * @return a {@link ILiquibaseXmlWriter} around the given {@code stream}
     */
    @Nonnull
    ILiquibaseXmlWriter create(@Nonnull OutputStream stream, @Nonnull String author);

}
