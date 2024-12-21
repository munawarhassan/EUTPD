package com.pmi.tpd.database.liquibase;

import static com.pmi.tpd.database.liquibase.LiquibaseConstants.TABLE_NAME;

import java.util.Map;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseBackupMonitor;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;

import liquibase.change.core.InsertDataChange;

/**
 * An consumer that takes a row (a map of column names to column values) and tells a given Liquibase XML writer to
 * serialise a representation of that row to the underlying output stream.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class WriteXmlForRowConsumer implements Consumer<Map<String, Object>> {

    /** */
    private static final String CHANGE_NAME = new InsertDataChange().createChangeMetaData().getName();

    /** */
    // private static final int ROW_PROGRESS_INTERVAL = 1000;

    /** */
    private final String tableName;

    /** */
    private final Iterable<String> columnNames;

    /** */
    private final ILiquibaseBackupMonitor monitor;

    /** */
    private final ILiquibaseXmlWriter writer;

    public WriteXmlForRowConsumer(final ILiquibaseXmlWriter writer, final String tableName,
            final Iterable<String> columnNames, final ILiquibaseBackupMonitor monitor) {
        this.writer = writer;
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.monitor = monitor;
    }

    /**
     * This implementation writes an insert data change to the Liquibase XML writer the given row.
     */
    @Override
    public void accept(final Map<String, Object> row) {
        Preconditions.checkArgument(!row.keySet().contains(null), "Column names must not be null");
        try {
            writer.writeStartElement(CHANGE_NAME);
            writer.writeAttribute(TABLE_NAME, tableName.toLowerCase());
            writeRow(columnNames, row);
            writer.writeEndElement();
            monitor.rowWritten();
        } catch (final XMLStreamException e) {
            throw new LiquibaseDataAccessException("An error occurred while writing to the output stream", e);
        }
    }

    private Map<String, Object> toLowerCaseKeys(final Map<String, Object> row) {
        final Map<String, Object> result = Maps.newHashMap();
        for (final Map.Entry<String, Object> entry : row.entrySet()) {
            result.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return result;
    }

    private void writeRow(final Iterable<String> columnNames, final Map<String, Object> row) throws XMLStreamException {
        final Map<String, Object> rowWithLowerCaseKeys = toLowerCaseKeys(row);
        for (final String columnName : columnNames) {
            writer.writeColumn(columnName.toLowerCase(), rowWithLowerCaseKeys.get(columnName.toLowerCase()));
        }
    }
}
