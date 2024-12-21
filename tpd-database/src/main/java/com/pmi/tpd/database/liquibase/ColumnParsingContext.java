package com.pmi.tpd.database.liquibase;

import com.pmi.tpd.database.liquibase.backup.xml.XmlEncoder;

import liquibase.change.ColumnConfig;
import liquibase.exception.DateParseException;

// CHECKSTYLE:OFF
/**
 * Encapsulates the state relevant to parsing of a {@code <column>...</column>} tag.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
// CHECKSTYLE:ON
final class ColumnParsingContext {

    /** Stores the portion of the column value read so far. */
    private final StringBuilder buffer;

    /** The name of the column being parsed. */
    private final String columnName;

    /** The type of the column being parsed. */
    private final ColumnSerialisationType columnSerialisationType;

    ColumnParsingContext(final String columnName, final ColumnSerialisationType columnSerialisationType) {
        this.columnName = columnName;
        this.columnSerialisationType = columnSerialisationType;
        this.buffer = new StringBuilder();
    }

    void append(final char[] ch, final int start, final int len) {
        buffer.append(ch, start, len);
    }

    /**
     * Produces a {@link liquibase.change.ColumnConfig column config} the value of which is the decoded contents of the
     * internal buffer filled by calls to the {@link #append} method.
     *
     * @param xmlEncoder
     *            used to decode the buffer contents
     * @return a new column config
     */
    ColumnConfig asColumnConfig(final XmlEncoder xmlEncoder) {
        final ColumnConfig column = new ColumnConfig();
        column.setName(columnName);
        final String decoded = xmlEncoder.decode(buffer.toString());
        switch (columnSerialisationType) {
            case BOOLEAN:
                column.setType(ColumnSerialisationType.BOOLEAN.toString());
                column.setValueBoolean(Boolean.parseBoolean(decoded));
                break;
            case INTEGER:
                column.setType(ColumnSerialisationType.INTEGER.toString());
                column.setValueNumeric(decoded);
                break;
            case BIGINT:
                column.setType(ColumnSerialisationType.BIGINT.toString());
                column.setValueNumeric(decoded);
                break;
            case NUMERIC:
                column.setType(ColumnSerialisationType.NUMERIC.toString());
                column.setValueNumeric(decoded);
                break;
            case DATE:
                column.setType(ColumnSerialisationType.DATE.toString());
                try {
                    column.setValueDate(decoded);
                } catch (final DateParseException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                break;
            default:
                column.setValue(decoded);
        }
        return column;
    }
}
