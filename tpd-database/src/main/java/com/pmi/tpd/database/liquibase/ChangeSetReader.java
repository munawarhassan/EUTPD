package com.pmi.tpd.database.liquibase;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET_AUTHOR;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET_CONTEXT;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET_DBMS;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET_ID;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.COLUMN;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.COLUMN_NAME;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.COLUMN_TYPE;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.DELETE;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.INSERT;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.TABLE_NAME;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.database.liquibase.backup.processor.IChangeSetProcessor;
import com.pmi.tpd.database.liquibase.backup.xml.XmlEncoder;

import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.InsertDataChange;
import liquibase.changelog.ChangeSet;

/**
 * A SAX content handler that reads and applies each changeset from a changeset log.
 *
 * @see ChangeSetScanner
 * @author Christophe Friederich
 * @since 1.3
 */
public class ChangeSetReader extends DefaultHandler {

    /** */
    private final ICancelState cancelState;

    /** */
    private final IChangeSetProcessor processor;

    /** */
    private final XmlEncoder xmlEncoder;

    /**
     * Variables for keeping track of the elements being read. These will be null when not in context.
     */
    private ChangeSet changeSet;

    /** */
    private DeleteDataChange deleteDataChange;

    /** */
    private InsertDataChange insertDataChange;

    /** */
    private Optional<ColumnParsingContext> columnContextOption = empty();

    /**
     * Create new instance of {@link ChangeSetReader}.
     *
     * @param cancelState
     *                    a cancel state
     * @param processor
     *                    a processor used
     * @param xmlEncoder
     *                    a xml encoder used.
     */
    public ChangeSetReader(@Nonnull final ICancelState cancelState, @Nonnull final IChangeSetProcessor processor,
            @Nonnull final XmlEncoder xmlEncoder) {
        this.cancelState = checkNotNull(cancelState, "cancelState");
        this.processor = checkNotNull(processor, "processor");
        this.xmlEncoder = checkNotNull(xmlEncoder, "xmlEncoder");
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (!cancelState.isCanceled()) {
            columnContextOption.ifPresent(context -> context.append(ch, start, length));
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (cancelState.isCanceled()) {
            return;
        }
        if (changeSet != null && CHANGE_SET.equals(qName)) {
            processor.onChangesetComplete(changeSet);
            changeSet = null;
        } else if (columnContextOption.isPresent() && COLUMN.equals(qName)) {
            insertDataChange.addColumn(columnContextOption.get().asColumnConfig(xmlEncoder));
            columnContextOption = empty();
        } else if (insertDataChange != null && INSERT.equals(qName)) {
            if (insertDataChange.getColumns().size() > 0) {
                processor.onChangesetContent(insertDataChange);
                insertDataChange = null;
            }
        } else if (deleteDataChange != null && DELETE.equals(qName)) {
            processor.onChangesetContent(deleteDataChange);
            deleteDataChange = null;
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException {
        if (cancelState.isCanceled()) {
            return;
        }
        if (changeSet == null && CHANGE_SET.equals(qName)) {
            changeSet = newChangeSet(attributes);
            processor.onChangesetBegin(changeSet);
        } else if (changeSet != null && insertDataChange == null && INSERT.equals(qName)) {
            insertDataChange = new InsertDataChange();
            insertDataChange.setTableName(attributes.getValue(TABLE_NAME));
        } else if (changeSet != null && deleteDataChange == null && DELETE.equals(qName)) {
            deleteDataChange = new DeleteDataChange();
            deleteDataChange.setTableName(attributes.getValue(TABLE_NAME));
        } else if (insertDataChange != null && COLUMN.equals(qName)) {
            final String columnName = attributes.getValue(COLUMN_NAME);
            final ColumnSerialisationType columnSerialisationType = ColumnSerialisationType
                    .fromString(attributes.getValue(COLUMN_TYPE))
                    .get();
            columnContextOption = of(new ColumnParsingContext(columnName, columnSerialisationType));
        }
    }

    private ChangeSet newChangeSet(final Attributes attributes) {
        final boolean alwaysRun = false;
        final boolean runOnChange = false;
        final boolean runInTxn = false;
        final String filePath = "restore";
        final String id = attributes.getValue(CHANGE_SET_ID);
        final String author = attributes.getValue(CHANGE_SET_AUTHOR);
        final String contextString = attributes.getValue(CHANGE_SET_CONTEXT);
        final String dbms = attributes.getValue(CHANGE_SET_DBMS);
        return new ChangeSet(id, author, alwaysRun, runOnChange, filePath, contextString, dbms, runInTxn, null);
    }
}
