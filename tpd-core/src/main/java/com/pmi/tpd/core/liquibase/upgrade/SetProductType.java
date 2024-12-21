package com.pmi.tpd.core.liquibase.upgrade;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;

import liquibase.change.ColumnConfig;
import liquibase.change.core.UpdateDataChange;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * @author Christophe Friederich
 * @since 2.5
 */
public class SetProductType implements CustomTaskChange {

    private static FormattingConversionService converter = new DefaultFormattingConversionService();

    @SuppressWarnings("unused")
    private ResourceAccessor resourceAccessor;

    @Override
    public String getConfirmationMessage() {
        return "Set product type";
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
            dao.forEachRow(
                "SELECT p.product_number as product_number, y.payload_data as payload_data FROM t_product p, t_payload y "
                        + "WHERE p.payload_product_id = y.id",
                cancelState,
                new SetProductTypeRowEffect(dao));
        } finally {
            dao.endChangeSet();
        }
    }

    private static class SetProductTypeRowEffect implements Consumer<Map<String, Object>> {

        private final ILiquibaseAccessor dao;

        public SetProductTypeRowEffect(final ILiquibaseAccessor dao) {
            this.dao = dao;
        }

        @Override
        public void accept(final Map<String, Object> row) {

            final String product_number = converter.convert(row.get("product_number"), String.class);

            final UpdateDataChange change = new UpdateDataChange();
            change.setTableName(ProductEntity.TABLE_NAME);
            change.setWhere("product_number = ?");
            change.addWhereParam(new ColumnConfig().setName("product_number").setValue(product_number));
            change.addColumn(
                new ColumnConfig().setName("type").setValueNumeric(getProductType(row)).setType("integer"));

            dao.update(change);
        }

        private Integer getProductType(final Map<String, Object> row) {
            Integer productType = null;
            try (Reader in = ChangeHelper.getReader(row, "payload_data")) {
                // Instance of the class which helps on reading tags
                final XMLInputFactory factory = XMLInputFactory.newInstance();

                // Initializing the handler to access the tags in the XML file
                final XMLEventReader eventReader = factory.createXMLEventReader(in);

                try {
                    while (eventReader.hasNext()) {
                        final XMLEvent xmlEvent = eventReader.nextEvent();

                        if (xmlEvent.isStartElement()) {
                            final StartElement startElement = xmlEvent.asStartElement();

                            if ("ProductType".equals(startElement.getName().getLocalPart())) {
                                final Characters productTypeEvent = (Characters) eventReader.nextEvent();
                                productType = Integer.valueOf(productTypeEvent.getData());
                                break;
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
            } catch (XMLStreamException | IOException | SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            return productType;
        }

    }

}
