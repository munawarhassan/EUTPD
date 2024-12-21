package com.pmi.tpd.database.liquibase.backup.processor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;

import liquibase.change.ColumnConfig;
import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.InsertDataChange;
import liquibase.changelog.ChangeSet;

/**
 * {@link IChangeSetProcessor} that filters out null characters (U+0000) from the changeset data.
 *
 * @see ColumnParsingContext#asColumnConfig(XmlEncoder)
 * @author Christophe Friederich
 * @since 1.3
 */
public class NullFilteringChangeSetProcessor implements IChangeSetProcessor {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(NullFilteringChangeSetProcessor.class);

    /** */
    private static final CharMatcher NULL_CHAR_MATCHER = CharMatcher.is('\u0000');

    @Override
    public void onChangesetBegin(final ChangeSet changeSet) {
    }

    @Override
    public void onChangesetContent(final InsertDataChange change) {
        for (final ColumnConfig columnConfig : change.getColumns()) {
            final String value = columnConfig.getValue();
            if (value != null && NULL_CHAR_MATCHER.matchesAnyOf(value)) {
                columnConfig.setValue(NULL_CHAR_MATCHER.removeFrom(value));
                LOGGER.warn(
                    "Removed the null characters (U+0000) on the following row on the table '{}':\n"
                            + " - id: '{}'\n - column: '{}'\n - value: '{}'",
                    change.getTableName(),
                    findRowId(change),
                    columnConfig.getName(),
                    StringUtils.abbreviate(columnConfig.getValue(), 50));
            }
        }
    }

    @Override
    public void onChangesetContent(final DeleteDataChange change) {
    }

    @Override
    public void onChangesetComplete(final ChangeSet changeSet) {
    }

    private String findRowId(final InsertDataChange change) {
        for (final ColumnConfig column : change.getColumns()) {
            if (column.getName().equalsIgnoreCase("id")) {
                if (column.getValueNumeric() != null) {
                    return column.getValueNumeric().toString();
                } else if (column.getValueObject() != null) {
                    return column.getValueObject().toString();
                } else if (column.getValue() != null) {
                    return column.getValue();
                }
            }
        }
        return "?";
    }

}
