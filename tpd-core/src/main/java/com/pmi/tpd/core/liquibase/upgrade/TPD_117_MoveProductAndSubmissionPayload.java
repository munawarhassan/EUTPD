package com.pmi.tpd.core.liquibase.upgrade;

import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.core.model.euceg.PayloadEntity;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;

import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;
import liquibase.change.core.UpdateDataChange;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

public class TPD_117_MoveProductAndSubmissionPayload implements CustomTaskChange {

    private static final Logger LOGGER = LoggerFactory.getLogger(TPD_117_MoveProductAndSubmissionPayload.class);

    private final FormattingConversionService converter = new DefaultFormattingConversionService();

    @SuppressWarnings("unused")
    private ResourceAccessor resourceAccessor;

    private long idCounter = 0L;

    @Override
    public String getConfirmationMessage() {
        return "Move Submission and Product payload to payload table";
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
            dao.forEachRow(ProductEntity.TABLE_NAME,
                "product_number",
                cancelState,
                new InsertPayloadProductRowEffect(dao));
            dao.forEachRow(SubmissionEntity.TABLE_NAME, "id", cancelState, new InsertPayloadSubmissionRowEffect(dao));

            dao.insert(ChangeHelper.insertGeneratorValue(PayloadEntity.GENERATOR_COLUMN_NAME, idCounter));
        } finally {
            dao.endChangeSet();
        }
    }

    private class InsertPayloadProductRowEffect implements Consumer<Map<String, Object>> {

        private final ILiquibaseAccessor dao;

        public InsertPayloadProductRowEffect(final ILiquibaseAccessor dao) {
            this.dao = dao;
        }

        @Override
        public void accept(final Map<String, Object> row) {
            final String xmlProduct = converter.convert(row.get("xml_product"), String.class);
            final String id = converter.convert(row.get("product_number"), String.class);

            // increment the next payload_id value
            idCounter++;

            final InsertDataChange change = new InsertDataChange();
            change.setTableName(PayloadEntity.TABLE_NAME);
            change.addColumn(new ColumnConfig().setName("id").setValueNumeric(idCounter).setType("bigint"));
            change.addColumn(new ColumnConfig().setName("payload_data").setValue(xmlProduct).setType("clob(1024000)"));
            ChangeHelper.addAuditedValue(change);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Insert Payload '{}'", idCounter);
            }
            dao.insert(change);

            final UpdateDataChange updateChange = new UpdateDataChange();
            updateChange.setTableName(ProductEntity.TABLE_NAME);
            updateChange.setWhere("product_number = ?");
            updateChange.addWhereParam(new ColumnConfig().setName("product_number").setValue(id));
            updateChange.addColumn(
                new ColumnConfig().setName("payload_product_id").setValueNumeric(idCounter).setType("bigint"));
            dao.update(updateChange);
        }

    }

    private class InsertPayloadSubmissionRowEffect implements Consumer<Map<String, Object>> {

        private final ILiquibaseAccessor dao;

        public InsertPayloadSubmissionRowEffect(final ILiquibaseAccessor dao) {
            this.dao = dao;
        }

        @Override
        public void accept(final Map<String, Object> row) {

            final String xmlSubmission = converter.convert(row.get("xml_submission"), String.class);
            final Long id = converter.convert(row.get("id"), Long.class);

            // increment the next payload_id value
            idCounter++;

            final InsertDataChange change = new InsertDataChange();
            change.setTableName(PayloadEntity.TABLE_NAME);
            change.addColumn(new ColumnConfig().setName("id").setValueNumeric(idCounter).setType("bigint"));
            change.addColumn(
                new ColumnConfig().setName("payload_data").setValue(xmlSubmission).setType("clob(1024000)"));
            ChangeHelper.addAuditedValue(change);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Insert Payload '{}'", idCounter);
            }
            dao.insert(change);

            final UpdateDataChange updateChange = new UpdateDataChange();
            updateChange.setTableName(SubmissionEntity.TABLE_NAME);
            updateChange.setWhere("id = ?");
            updateChange.addWhereParam(new ColumnConfig().setName("id").setValueNumeric(id));
            updateChange.addColumn(
                new ColumnConfig().setName("payload_submission_id").setValueNumeric(idCounter).setType("bigint"));
            dao.update(updateChange);
        }
    }

}
