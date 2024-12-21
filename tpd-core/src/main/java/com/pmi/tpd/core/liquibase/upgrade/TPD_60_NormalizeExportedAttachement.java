package com.pmi.tpd.core.liquibase.upgrade;

import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.core.euceg.impl.SubmissionProductIdGenerator;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
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
 * @since 1.0
 */
public class TPD_60_NormalizeExportedAttachement implements CustomTaskChange {

    private static final Logger LOGGER = LoggerFactory.getLogger(TPD_60_NormalizeExportedAttachement.class);

    private static FormattingConversionService converter = new DefaultFormattingConversionService();

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
            dao.forEachRow(SubmissionEntity.TABLE_NAME_EXPORTED_ATTACHMENT,
                "productid",
                cancelState,
                new UpdateExportedAttachmentIdRowEffect(dao));
        } finally {
            dao.endChangeSet();
        }
    }

    private class UpdateExportedAttachmentIdRowEffect implements Consumer<Map<String, Object>> {

        private final ILiquibaseAccessor dao;

        public UpdateExportedAttachmentIdRowEffect(final ILiquibaseAccessor dao) {
            this.dao = dao;
        }

        @Override
        public void accept(final Map<String, Object> row) {

            final String productId = converter.convert(row.get("productid"), String.class);

            final UpdateDataChange change = new UpdateDataChange();
            change.setTableName(SubmissionEntity.TABLE_NAME_EXPORTED_ATTACHMENT);
            change.setWhere("productid = ?");
            change.addWhereParam(new ColumnConfig().setName("productid").setValue(productId));

            change.addColumn(new ColumnConfig().setName("id")
                    .setValueNumeric(SubmissionProductIdGenerator.toId(productId))
                    .setType("bigint"));

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("update sent attachment associated to product '{}'", productId);
            }
            dao.update(change);
        }
    }

}
