package com.pmi.tpd.database.liquibase;

import static com.pmi.tpd.database.DatabaseTableAttribute.PREPOPULATED;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.ENCODING;
import static com.pmi.tpd.database.security.xml.SecureXmlParserFactory.emptyEntityResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Flushables;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseBackupMonitor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseMigrationDao;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseRestoreMonitor;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;
import com.pmi.tpd.database.liquibase.backup.processor.CompositeChangeSetProcessor;
import com.pmi.tpd.database.liquibase.backup.processor.IChangeSetProcessor;
import com.pmi.tpd.database.liquibase.backup.processor.NullFilteringChangeSetProcessor;
import com.pmi.tpd.database.liquibase.backup.xml.DefaultXmlEncoder;
import com.pmi.tpd.database.security.xml.SecureXmlParserFactory;
import com.pmi.tpd.database.spi.IDatabaseTable;

/**
 * Default implementation for {@link ILiquibaseMigrationDao}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultLiquibaseMigrationDao implements ILiquibaseMigrationDao {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLiquibaseMigrationDao.class);

    /** factory generating the {@link ILiquibaseXmlWriter} to write the Liquibase changelogs. */
    private final ILiquibaseXmlWriterFactory writerFactory;

    /**
     * @param dao
     * @param writerFactory
     */
    @Inject
    public DefaultLiquibaseMigrationDao(final ILiquibaseXmlWriterFactory writerFactory) {
        this.writerFactory = writerFactory;
    }

    /*
     * Implementation note: to reduce the memory footprint, the output is 'streamed' to the output stream; that is,
     * writes occur as rows are read from the underlying database, rather than being read into an object graph first.
     */
    @Override
    public void backup(final ILiquibaseAccessor dao,
        final OutputStream stream,
        final String author,
        final ILiquibaseBackupMonitor monitor,
        final ICancelState cancelState) throws LiquibaseDataAccessException {
        LOGGER.info("Backup process started");

        final ILiquibaseXmlWriter writer = writerFactory.create(stream, author);
        try {
            // notify we started
            monitor.started(countTotalRows(dao));

            // write headers
            writer.writeStartDocument(ENCODING, "1.0");
            writer.writeDatabaseChangeLogStartElement();

            // serialize each table as a Liquibase changeset
            for (final IDatabaseTable prePopulatedTable : dao.getDatabaseTables()
                    .orderingforDeletion()
                    .with(PREPOPULATED)) {
                if (cancelState.isCanceled()) {
                    return;
                }
                writer.writeChangeSetToDeleteRowsFromTable(prePopulatedTable.getTableName());
            }
            writeXmlForTables(dao, monitor, writer, dao.getDatabaseTables(), cancelState);

            // finalise the backup
            writer.writeEndDocument();
            writer.flush();
            LOGGER.info("Backup process completed");

        } catch (final XMLStreamException e) {
            throw new LiquibaseDataAccessException("An error occurred while writing to the output stream", e);
        } finally {
            // release the resources associated with the writer (which does not close the underlying output stream)
            try {
                writer.close();
            } catch (final XMLStreamException e) {
                LOGGER.error("An error occurred while closing the output stream", e);
            }
        }
    }

    private long countTotalRows(final ILiquibaseAccessor dao) {
        long totalRows = 0;
        for (final IDatabaseTable table : dao.getDatabaseTables()) {
            totalRows += dao.countRows(table.getTableName());
        }
        return totalRows;
    }

    /**
     * Writes a Liquibase changelog (conforming to the Liquibase change log DTD) for each table.
     */
    private void writeXmlForTables(final ILiquibaseAccessor dao,
        final ILiquibaseBackupMonitor backupMonitor,
        final ILiquibaseXmlWriter writer,
        final Iterable<IDatabaseTable> tables,
        final ICancelState cancelState) throws XMLStreamException {
        final int tableCount = Iterables.size(tables);
        long rowsWritten = 0;

        LOGGER.info("There are {} tables to back up", tableCount);
        for (final IDatabaseTable table : tables) {
            if (cancelState.isCanceled()) {
                return;
            }
            final String tableName = table.getTableName();
            final String orderingColumn = table.getOrderingColumn();
            LOGGER.info("Backing up {} table", tableName);

            // write all the table rows
            final long numberOfRows = writeXmlForTable(dao,
                writer,
                tableName,
                orderingColumn,
                backupMonitor,
                cancelState);
            rowsWritten += numberOfRows;
            LOGGER.info("{} rows from the {} table were backed up", numberOfRows, tableName);
        }

        LOGGER.info("A total of {} rows in {} tables were backed up", rowsWritten, tableCount);
    }

    /**
     * Fetches the rows of the given table and serialises them as XML (conforming to the Liquibase change log DTD).
     */
    private long writeXmlForTable(final ILiquibaseAccessor dao,
        final ILiquibaseXmlWriter writer,
        final String tableName,
        final String orderingColumn,
        final ILiquibaseBackupMonitor monitor,
        final ICancelState cancelState) throws XMLStreamException {
        writer.writeChangeSetStartElement(tableName);
        final Iterable<String> columnNames = dao.getColumnNames(tableName);
        final long numberOfRows = dao.forEachRow(tableName,
            orderingColumn,
            cancelState,
            new WriteXmlForRowConsumer(writer, tableName, columnNames, monitor));
        writer.writeEndElement(); // end of change set
        return numberOfRows;
    }

    /**
     * Implementation notes: - this reads the contents of the given input stream in two phases: - the first is a quick
     * scan to obtain the changeset summaries (which include computing their relative 'weights'), - the second causes
     * changes to be executed on a database connection; - all referential integrity constraint checking is turned off
     * while changes are being made. This method does not guarantee that constraints are retrospectively checked when
     * constraint-checking is turned on again.
     */
    @Override
    public void restore(final ILiquibaseAccessor dao,
        final InputStream stream,
        final File tempDir,
        final ILiquibaseRestoreMonitor monitor,
        final ICancelState cancelState) {
        LOGGER.info("Restore process started");

        // create a temporary file to hold the contents of the input stream and then restore the database from it
        final File tempFile = createTempFile(tempDir);
        try {
            LOGGER.info("Examining backup data");
            LOGGER.debug("Saving stream contents to temporary file : {}", tempFile.getAbsolutePath());
            saveInputToFile(stream, tempFile);

            LOGGER.debug("Performing first pass through temporary file");
            final ChangeLogOutline changeSetMetaData = scan(tempFile);
            LOGGER.info("Found {} non-empty change sets to apply to the target database",
                changeSetMetaData.nonEmptyChangeSetCount());

            LOGGER.info("Connecting to target database");
            dao.withLock(dao1 -> {
                LOGGER.debug("Beginning second pass through temporary file");
                final IChangeSetProcessor processor = createProcessor(dao1, monitor, changeSetMetaData);
                final ChangeSetReader handler = new ChangeSetReader(cancelState, processor, new DefaultXmlEncoder());
                parse(tempFile, buildXmlReader(handler));
                LOGGER.info("Restore process completed");
            });
        } finally {
            LOGGER.debug("Deleting temporary file");
            deleteFile(tempFile);
        }
    }

    /**
     * Create a processor for the Liquibase changesets read from the backup.
     */
    private IChangeSetProcessor createProcessor(final ILiquibaseAccessor dao,
        final ILiquibaseRestoreMonitor monitor,
        final ChangeLogOutline changeSetMetaData) {
        final DatabaseUpdater databaseUpdater = new DatabaseUpdater(dao, monitor, changeSetMetaData);
        final boolean isPostgres = StringUtils.containsIgnoreCase(dao.getDatabaseType(), "postgres");
        if (isPostgres) {
            return new CompositeChangeSetProcessor(new NullFilteringChangeSetProcessor(), databaseUpdater);
        }
        return databaseUpdater;
    }

    /**
     * Scans the given XML file to find out how many change sets there are, and how many changes are in each change set.
     * This is used later for progress reporting.
     *
     * @param xmlFile
     *            the file in which the change sets are specified
     * @return an iterable of change set meta data
     */
    private ChangeLogOutline scan(final File xmlFile) {
        final ChangeSetScanner scanner = new ChangeSetScanner();
        parse(xmlFile, buildXmlReader(scanner));
        return new ChangeLogOutline(scanner.getChangeCounts());
    }

    private void parse(final File xmlFile, final XMLReader xmlReader) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(xmlFile);
            final InputSource inputSource = new InputSource(inputStream);
            inputSource.setEncoding(ENCODING);
            xmlReader.parse(inputSource);
        } catch (final FileNotFoundException e) {
            throw new LiquibaseDataAccessException("Failed to open backup file in order to parse its contents", e);
        } catch (final IOException e) {
            throw new LiquibaseDataAccessException("An error occurred while parsing the backup file", e);
        } catch (final SAXException e) {
            throw new LiquibaseDataAccessException("SAX parsing error while parsing backup file", e);
        } finally {
            Closeables.closeQuietly(inputStream);
        }
    }

    /**
     * Creates a temporary file with prefix &quot;app_&quot; and a &quot;.xml&quot; extension.
     *
     * @param directory
     *            directory where to create the file
     */
    private File createTempFile(final File directory) {
        try {
            return File.createTempFile("app_", ".xml", directory);
        } catch (final IOException e) {
            throw new LiquibaseDataAccessException("Failed to create a temporary file to hold the change log", e);
        }
    }

    private void deleteFile(final File file) {
        if (file != null) {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
    }

    /**
     * Saves the contents of the given input stream into the given destination file, which is overwritten if it exists.
     * <p>
     * <i>Note: This method does not close the input stream, but does close the output stream that it uses to write to
     * the destination file.</i>
     *
     * @param inputStream
     *            a stream of data to be saved
     * @param destination
     *            a file into which the data will be written. The file may, but needs not, exist when this method is
     *            called, and it will exist once this method returns.
     */
    private void saveInputToFile(final InputStream inputStream, final File destination) {
        try (FileOutputStream to = new FileOutputStream(destination)) {

            ByteStreams.copy(inputStream, to);
            Flushables.flushQuietly(to);
        } catch (final IOException e) {
            throw new LiquibaseDataAccessException(
                    "Failed to save the contents of the input stream to a temporary file", e);
        }
    }

    private XMLReader buildXmlReader(final ContentHandler handler) {
        try {
            final XMLReader xmlReader = SecureXmlParserFactory.newNamespaceAwareXmlReader();
            xmlReader.setErrorHandler(errorHandler);
            xmlReader.setContentHandler(handler);
            xmlReader.setEntityResolver(emptyEntityResolver());
            xmlReader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
            return xmlReader;
        } catch (final SAXNotRecognizedException e) {
            throw new LiquibaseDataAccessException("Could not associate the XML schema language with the backup file",
                    e);
        } catch (final SAXNotSupportedException e) {
            throw new LiquibaseDataAccessException(
                    "Could not configure the XML schema language for parsing the backup file", e);
        }
    }

    private final ErrorHandler errorHandler = new ErrorHandler() {

        @Override
        public void warning(final SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void error(final SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void fatalError(final SAXParseException exception) throws SAXException {
            throw exception;
        }
    };
}
