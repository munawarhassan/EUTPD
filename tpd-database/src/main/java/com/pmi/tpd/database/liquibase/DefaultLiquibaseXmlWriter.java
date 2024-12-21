package com.pmi.tpd.database.liquibase;

import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET_AUTHOR;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET_ID;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.COLUMN;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.COLUMN_NAME;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.COLUMN_TYPE;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.DATABASE_CHANGE_LOG;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.DELETE;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.TABLE_NAME;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.WHERE;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.pmi.tpd.database.liquibase.backup.xml.DelegatingXmlStreamWriter;
import com.pmi.tpd.database.liquibase.backup.xml.XmlEncoder;

/**
 * This implementation creates elements of the output XML document directly. You might think that we could use
 * Liquibase's {@link liquibase.serializer.core.xml.XMLChangeLogSerializer} class, but it uses DOM-based document
 * construction, which carries with it an overhead that we avoid by employing a purely streaming approach.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
class DefaultLiquibaseXmlWriter extends DelegatingXmlStreamWriter<XMLStreamWriter> implements ILiquibaseXmlWriter {

    /** */
    private final IChangeSetIdGenerator idGenerator;

    /** */
    private final String changeSetAuthor;

    /** */
    private final XmlEncoder xmlEncoder;

    /**
     * @param delegate
     * @param idGenerator
     * @param changeSetAuthor
     * @param xmlEncoder
     */
    DefaultLiquibaseXmlWriter(final XMLStreamWriter delegate, final IChangeSetIdGenerator idGenerator,
            final String changeSetAuthor, final XmlEncoder xmlEncoder) {
        super(delegate);
        this.idGenerator = idGenerator;
        this.changeSetAuthor = changeSetAuthor;
        this.xmlEncoder = xmlEncoder;
    }

    @Override
    public void writeDatabaseChangeLogStartElement() throws XMLStreamException {
        writeStartElement(DATABASE_CHANGE_LOG);
    }

    @Override
    public void writeChangeSetToDeleteRowsFromTable(@Nonnull final String tableName) throws XMLStreamException {
        writeChangeSetStartElement(tableName);
        writeDeleteAll(tableName);
        writeEndElement();
    }

    @Override
    public void writeChangeSetStartElement(@Nonnull final String tableName) throws XMLStreamException {
        writeStartElement(CHANGE_SET);
        writeAttribute(CHANGE_SET_ID, idGenerator.next(tableName));
        writeAttribute(CHANGE_SET_AUTHOR, changeSetAuthor);
    }

    private void writeDeleteAll(final String tableName) throws XMLStreamException {
        writeStartElement(DELETE);
        writeAttribute(TABLE_NAME, tableName.toLowerCase());
        writeEmptyElement(WHERE);
        writeEndElement();
    }

    @Override
    public void writeColumn(@Nonnull final String columnName, @Nullable final Object value) throws XMLStreamException {
        if (value == null) {
            return;
        }
        writeStartElement(COLUMN);
        writeAttribute(COLUMN_NAME, columnName);
        final ColumnSerialisationType columnSerialisationType = ColumnSerialisationType.ofValue(value);
        writeAttribute(COLUMN_TYPE, columnSerialisationType.toString());
        writeCData(xmlEncoder.encode(columnSerialisationType.toColumnString(value)));
        writeEndElement();
    }

    IChangeSetIdGenerator getIdGenerator() {
        return idGenerator;
    }

    String getChangeSetAuthor() {
        return changeSetAuthor;
    }
}
