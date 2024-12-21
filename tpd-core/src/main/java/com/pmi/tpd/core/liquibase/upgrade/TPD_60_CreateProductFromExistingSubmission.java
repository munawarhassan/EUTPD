package com.pmi.tpd.core.liquibase.upgrade;

import java.util.Map;
import java.util.function.Consumer;

import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.Product;
import org.eu.ceg.Submission;
import org.eu.ceg.TobaccoProductSubmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.ProductStatus;

import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * This task create product from existing submission.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class TPD_60_CreateProductFromExistingSubmission implements CustomTaskChange {

    @SuppressWarnings("unused")
    private ResourceAccessor resourceAccessor;

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
            dao.forEachRow(SubmissionEntity.TABLE_NAME, "product_id", cancelState, new InsertProductRowEffect(dao));
        } finally {
            dao.endChangeSet();
        }
    }

    private static class InsertProductRowEffect implements Consumer<Map<String, Object>> {

        private static final Logger LOGGER = LoggerFactory.getLogger(InsertProductRowEffect.class);

        private static FormattingConversionService converter = new DefaultFormattingConversionService();

        private final ILiquibaseAccessor dao;

        public InsertProductRowEffect(final ILiquibaseAccessor dao) {
            this.dao = dao;
        }

        @Override
        public void accept(final Map<String, Object> row) {

            final String productId = converter.convert(row.get("product_id"), String.class);
            final ProductType productType = converter.convert(row.get("product_type"), ProductType.class);
            final String productNumber = converter.convert(row.get("product_number"), String.class);
            final String submitterId = converter.convert(row.get("submitter_id"), String.class);
            final String xmlSubmission = converter.convert(row.get("xml_submission"), String.class);

            final Submission submission = Eucegs.unmarshal(xmlSubmission);
            Product product = null;
            if (submission instanceof TobaccoProductSubmission) {
                product = ((TobaccoProductSubmission) submission).getProduct();
            } else if (submission instanceof EcigProductSubmission) {
                product = ((EcigProductSubmission) submission).getProduct();
            }
            final String productXml = Eucegs.marshal(Eucegs.wrap(product, Product.class));

            final InsertDataChange change = new InsertDataChange();
            change.setTableName(ProductEntity.TABLE_NAME);
            change.addColumn(
                new ColumnConfig().setName("product_number").setValue(productNumber).setType("VARCHAR(255)"));
            change.addColumn(
                new ColumnConfig().setName("product_type").setValue(productType.toString()).setType("VARCHAR(50)"));
            change.addColumn(new ColumnConfig().setName("product_status")
                    .setValue(ProductStatus.SENT.toString())
                    .setType("VARCHAR(50)"));
            change.addColumn(
                new ColumnConfig().setName("preferred_submitter_id").setValue(submitterId).setType("VARCHAR(20)"));

            if (submission.getSubmissionType() != null && submission.getSubmissionType().getValue() != null) {
                change.addColumn(new ColumnConfig().setName("preferred_submission_type")
                        .setValue(submission.getSubmissionType().getValue().toString())
                        .setType("VARCHAR(50)"));
            }
            if (submission.getGeneralComment() != null) {
                change.addColumn(new ColumnConfig().setName("preferred_general_comment")
                        .setValue(submission.getGeneralComment().getValue())
                        .setType("VARCHAR(10000)"));
            }
            change.addColumn(
                new ColumnConfig().setName("xml_product").setValueClobFile(productXml).setType("CLOB(1024000)"));

            ChangeHelper.addAuditedValue(change);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Insert Product '{}'", productId);
            }
            dao.insert(change);
        }
    }

}
