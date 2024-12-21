package com.pmi.tpd.database.liquibase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * An XML stream writer that knows how to write a limited set of elements of Liquibase backup documents.
 * <p>
 * All of these methods throw XMLStreamException if there is an error in the underlying stream writer
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ILiquibaseXmlWriter extends AutoCloseable, XMLStreamWriter {

    /** Writes elements that define the outer-most change log container. */
    void writeDatabaseChangeLogStartElement() throws XMLStreamException;

    /** Writes an element that represents the deletion of all rows of the given table. */
    void writeChangeSetToDeleteRowsFromTable(@Nonnull String tableName) throws XMLStreamException;

    /** Creates a change set element to act as the container of a number of changes to the given table. */
    void writeChangeSetStartElement(@Nonnull String tableName) throws XMLStreamException;

    /**
     * Writes out a 'column' element with a 'name' attribute set to the given column name, and a value attribute set to
     * the given value. The value attribute will be one of:
     * <ul>
     * <li>'valueDate' - for date values</li>
     * <li>'valueBoolean' - for boolean values</li>
     * <li>'valueNumeric' - for numbers</li>
     * <li>'value' - for all values not covered by the above types</li>
     * </ul>
     * Liquibase does not have an interface for explicitly creating null values in the database, so this method has no
     * effect when passed a null value. That's right, we don't put null-valued columns into the XML document. This might
     * give you a fright because a column that is nullable, and which has a non-null default, would be given its
     * non-null default value on the restore, rather than the null value it had in the source database. True. However,
     * we don't have any columns of that kind, and we'll just have to remember not to create one.
     *
     * @param columnName
     *                   the name of the column to be written
     * @param value
     *                   the value of the column to be written
     */
    void writeColumn(@Nonnull String columnName, @Nullable Object value) throws XMLStreamException;
}
