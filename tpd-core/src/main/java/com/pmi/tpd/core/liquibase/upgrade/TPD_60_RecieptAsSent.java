package com.pmi.tpd.core.liquibase.upgrade;

import java.util.Map;
import java.util.function.Consumer;

import org.eu.ceg.EcigProductSubmissionResponse;
import org.eu.ceg.ResponseStatus;
import org.eu.ceg.SubmissionResponse;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoProductSubmissionResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;

import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class TPD_60_RecieptAsSent implements CustomTaskChange {

    private static final Logger LOGGER = LoggerFactory.getLogger(TPD_60_RecieptAsSent.class);

    private static FormattingConversionService converter = new DefaultFormattingConversionService();

    @SuppressWarnings("unused")
    private ResourceAccessor resourceAccessor;

    /** incremental identifier */
    private long id = 1L;

    @Override
    public String getConfirmationMessage() {
        return "extract Product from submission";
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
            dao.forEachRow(SubmissionEntity.TABLE_NAME, "id", cancelState, new UpdateSubmissionRowEffect(dao));

            dao.insert(ChangeHelper.insertGeneratorValue(TransmitReceiptEntity.GENERATOR_COLUMN_NAME, id));

        } finally {
            dao.endChangeSet();
        }
    }

    private class UpdateSubmissionRowEffect implements Consumer<Map<String, Object>> {

        private final ILiquibaseAccessor dao;

        public UpdateSubmissionRowEffect(final ILiquibaseAccessor dao) {
            this.dao = dao;
        }

        @Override
        public void accept(final Map<String, Object> row) {

            final String uuid = Eucegs.uuid();
            final Long submissionId = converter.convert(row.get("id"), Long.class);
            final String productId = converter.convert(row.get("product_id"), String.class);
            final ProductType productType = converter.convert(row.get("product_type"), ProductType.class);

            SubmissionResponse response = null;
            if (productType == ProductType.ECIGARETTE) {
                response = new EcigProductSubmissionResponse().withDate(DateTime.now())
                        .withProductID(productId)
                        .withStatus(ResponseStatus.SUCCESS)
                        .withUuid(uuid)
                        .withSubmissionType(SubmissionTypeEnum.NEW);
            } else if (productType == ProductType.TOBACCO) {
                response = new TobaccoProductSubmissionResponse().withDate(DateTime.now())
                        .withProductID(productId)
                        .withStatus(ResponseStatus.SUCCESS)
                        .withUuid(uuid)
                        .withSubmissionType(SubmissionTypeEnum.NEW);
            } else {
                return;
            }
            final InsertDataChange change = new InsertDataChange();
            change.setTableName(TransmitReceiptEntity.TABLE_NAME);
            change.addColumn(new ColumnConfig().setName("id").setValueNumeric(id++).setType("bigint"));
            change.addColumn(new ColumnConfig().setName("message_id").setValue(uuid).setType("VARCHAR(250)"));
            change.addColumn(new ColumnConfig().setName("payload_type")
                    .setValue(PayloadType.SUBMISSION.toString())
                    .setType("VARCHAR(25)"));
            change.addColumn(
                new ColumnConfig().setName("submission_id").setValueNumeric(submissionId).setType("VARCHAR(25)"));
            change.addColumn(new ColumnConfig().setName("transmit_status")
                    .setValue(TransmitStatus.RECEIVED.toString())
                    .setType("VARCHAR(25)"));
            change.addColumn(
                new ColumnConfig().setName("xml_response").setValue(Eucegs.marshal(response)).setType("CLOB(4096)"));
            // fake product id
            change.addColumn(new ColumnConfig().setName("product_id").setValue(productId).setType("VARCHAR(25)"));

            ChangeHelper.addAuditedValue(change);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Insert Receipt for product '{}'", productId);
            }
            dao.insert(change);
        }
    }

}
