package com.pmi.tpd.database.liquibase;

import static com.pmi.tpd.database.liquibase.LiquibaseConstants.ENCODING;

import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.base.Preconditions;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;
import com.pmi.tpd.database.liquibase.backup.xml.DefaultXmlEncoder;
import com.pmi.tpd.database.liquibase.backup.xml.PrettyXmlWriter;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultLiquibaseXmlWriterFactory implements ILiquibaseXmlWriterFactory {

    @Override
    @Nonnull
    public ILiquibaseXmlWriter create(@Nonnull final OutputStream stream, @Nonnull final String author) {
        Preconditions.checkNotNull(stream);
        Preconditions.checkNotNull(author);

        try {
            final PrettyXmlWriter delegate = new PrettyXmlWriter(newXmlWriter(stream));
            return new DefaultLiquibaseXmlWriter(delegate, new DefaultChangeSetIdGenerator(), author,
                    new DefaultXmlEncoder());
        } catch (final XMLStreamException e) {
            throw new LiquibaseDataAccessException("An error occurred while writing to the output stream", e);
        }
    }

    /**
     * @param stream
     * @return
     * @throws XMLStreamException
     */
    protected XMLStreamWriter newXmlWriter(final OutputStream stream) throws XMLStreamException {
        return XMLOutputFactory.newInstance().createXMLStreamWriter(stream, ENCODING);
    }

}
