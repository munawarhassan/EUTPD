package com.pmi.tpd.core.liquibase.upgrade;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.LiquibaseUtils;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;

import joptsimple.internal.Strings;
import liquibase.change.ColumnConfig;
import liquibase.change.core.UpdateDataChange;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;

/**
 * @author Christophe Friederich
 * @since 2.5
 */
public class TPD_286_SetProductPirStatus implements CustomTaskChange {

    private static final Logger LOGGER = LoggerFactory.getLogger(TPD_286_SetProductPirStatus.class);

    private static FormattingConversionService converter = new DefaultFormattingConversionService();

    @SuppressWarnings("unused")
    private ResourceAccessor resourceAccessor;

    @Override
    public String getConfirmationMessage() {
        return "Set PIR status";
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public void setFileOpener(final ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;

    }

    @Override
    public ValidationErrors validate(final Database database) {
        return new ValidationErrors();
    }

    @Override
    public void execute(final Database database) throws CustomChangeException {
        final DefaultLiquibaseAccessor dao = new DefaultLiquibaseAccessor(database, 1000);
        final ICancelState cancelState = new SimpleCancelState();
        try {
            dao.beginChangeSet();
            forEachRow(dao,
                "SELECT p.product_number as product_number, y.payload_data as payload_data FROM t_product p, t_payload y "
                        + "WHERE p.payload_product_id = y.id",
                "product_number",
                cancelState,
                new SetWithdrawalProductPirStatus(dao));
        } finally {
            dao.endChangeSet();
        }
    }

    public long forEachRow(final DefaultLiquibaseAccessor dao,
        @Nonnull final String query,
        final String orderingColumn,
        @Nonnull final ICancelState cancelState,
        @Nonnull final Consumer<Map<String, Object>> effect) {
        Preconditions.checkNotNull(query, "query");
        Preconditions.checkArgument(orderingColumn == null || StringUtils.isNotBlank(orderingColumn),
            "blank ordering column");
        Preconditions.checkNotNull(effect, "effect");

        long numberOfRows = 0;
        Statement statement = null;
        ResultSet resultSet = null;
        final Table table = dao.findTable(ProductEntity.TABLE_NAME);
        try {
            final Connection connection = LiquibaseUtils.getConnection(dao.getDatabaseSession().getDatabase());
            statement = connection.createStatement();
            statement.setFetchSize(100);
            resultSet = statement.executeQuery(query + dao.orderByClause(table, orderingColumn));
            while (resultSet.next() && !cancelState.isCanceled()) {
                forCurrentRow(dao, resultSet, effect);
                numberOfRows++;
                if (numberOfRows % 100 == 0) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("{}: {} rows processed", table.getName(), numberOfRows);
                    }
                }
            }
            return numberOfRows;
        } catch (final SQLException | IOException e) {
            throw new DataRetrievalFailureException("Could not fetch rows from " + table.getName(), e);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }
    }

    public void forCurrentRow(final DefaultLiquibaseAccessor dao,
        final ResultSet resultSet,
        final Consumer<Map<String, Object>> effect) throws SQLException, IOException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();
        final Map<String, Object> columnValues = Maps.newHashMapWithExpectedSize(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            final String columnName = dao.getColumnName(metaData, i);
            columnValues.put(columnName, resultSet.getObject(i));
        }
        effect.accept(columnValues);
    }

    private static class SetWithdrawalProductPirStatus implements Consumer<Map<String, Object>> {

        private final ILiquibaseAccessor dao;

        public SetWithdrawalProductPirStatus(final ILiquibaseAccessor dao) {
            this.dao = dao;
        }

        @Override
        public void accept(final Map<String, Object> row) {

            final String product_number = converter.convert(row.get("product_number"), String.class);

            if (isWithdrawn(row)) {
                final UpdateDataChange change = new UpdateDataChange();
                change.setTableName(ProductEntity.TABLE_NAME);
                change.setWhere("product_number = ?");
                change.addWhereParam(new ColumnConfig().setName("product_number").setValue(product_number));
                change.addColumn(new ColumnConfig().setName("product_pir_status")
                        .setValue(ProductPirStatus.WITHDRAWN.name())
                        .setType("varchar(50)"));

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("set '{}' to product_pir_status column of  '{}' table",
                        ProductPirStatus.WITHDRAWN.name(),
                        change.getTableName());
                }
                dao.update(change);
            }
        }

        private boolean isWithdrawn(final Map<String, Object> row) {
            try (Reader in = ChangeHelper.getReader(row, "payload_data")) {
                return isWithdrawn(in);
            } catch (XMLStreamException | IOException | SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        private static boolean isWithdrawn(@Nonnull final Reader xmlProduct) throws XMLStreamException {
            checkNotNull(xmlProduct, "xmlProduct");

            // Instance of the class which helps on reading tags
            final XMLInputFactory factory = XMLInputFactory.newInstance();

            // Initializing the handler to access the tags in the XML file
            final XMLEventReader eventReader = factory.createXMLEventReader(xmlProduct);
            boolean running = false;

            try {
                while (eventReader.hasNext()) {
                    final XMLEvent xmlEvent = eventReader.nextEvent();

                    if (xmlEvent.isStartElement()) {
                        final StartElement startElement = xmlEvent.asStartElement();

                        if ("Product".equals(startElement.getName().getLocalPart())) {
                            running = true;
                        }
                        if (!running) {
                            continue;
                        }
                        switch (startElement.getName().getLocalPart()) {
                            case "Presentations":
                                return parsePresentations(eventReader);
                        }
                    } else if (xmlEvent.isEndElement()) {
                        final EndElement endElement = xmlEvent.asEndElement();
                        if ("Product".equals(endElement.getName().getLocalPart())) {
                            break;
                        }
                    }
                }
            } finally {
                if (eventReader != null) {
                    eventReader.close();
                }
            }

            return false;
        }

        private static boolean parsePresentations(final XMLEventReader eventReader) throws XMLStreamException {
            boolean withdrawn = false;
            boolean isFirst = true;
            while (eventReader.hasNext()) {
                final XMLEvent xmlEvent = eventReader.nextEvent();

                if (xmlEvent.isStartElement()) {
                    final StartElement startElement = xmlEvent.asStartElement();

                    switch (startElement.getName().getLocalPart()) {
                        case "Presentation":
                            final boolean isWithdrawn = parsePresentation(eventReader);
                            if (isFirst) {
                                withdrawn = isWithdrawn;
                                isFirst = false;
                            } else {
                                withdrawn = withdrawn && isWithdrawn;
                            }
                            if (!withdrawn) {
                                return withdrawn;
                            }
                            break;
                    }
                } else if (xmlEvent.isEndElement()) {
                    final EndElement endElement = xmlEvent.asEndElement();
                    if ("Presentations".equals(endElement.getName().getLocalPart())) {
                        break;
                    }
                }
            }
            return withdrawn;
        }

        private static boolean parsePresentation(final XMLEventReader eventReader) throws XMLStreamException {

            while (eventReader.hasNext()) {
                final XMLEvent xmlEvent = eventReader.nextEvent();

                if (xmlEvent.isStartElement()) {
                    final StartElement startElement = xmlEvent.asStartElement();

                    switch (startElement.getName().getLocalPart()) {
                        case "WithdrawalDate": {
                            final Characters value = (Characters) eventReader.nextEvent();
                            final String date = value.getData();
                            if (!Strings.isNullOrEmpty(date)) {
                                return true;
                            }
                            break;
                        }
                    }
                } else if (xmlEvent.isEndElement()) {
                    final EndElement endElement = xmlEvent.asEndElement();
                    final String localPart = endElement.getName().getLocalPart();
                    if ("Presentation".equals(localPart)) {
                        break;
                    }
                }
            }
            return false;
        }

    }

}
