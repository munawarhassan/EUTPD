package com.pmi.tpd.core.liquibase.upgrade;

import java.util.Map;
import java.util.function.Consumer;

import org.eu.ceg.Submission;
import org.eu.ceg.SubmissionTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.euceg.api.Eucegs;

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
public class TPD_110_SetSubmissionType implements CustomTaskChange {

    private static final Logger LOGGER = LoggerFactory.getLogger(TPD_110_SetSubmissionType.class);

    private static FormattingConversionService converter = new DefaultFormattingConversionService();

    @SuppressWarnings("unused")
    private ResourceAccessor resourceAccessor;

    @Override
    public String getConfirmationMessage() {
        return "Set Error to Receipt";
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
            dao.forEachRow(SubmissionEntity.TABLE_NAME, "id", cancelState, new SetUpdateColumn(dao));
        } finally {
            dao.endChangeSet();
        }
    }

    private class SetUpdateColumn implements Consumer<Map<String, Object>> {

        private final ILiquibaseAccessor dao;

        public SetUpdateColumn(final ILiquibaseAccessor dao) {
            this.dao = dao;
        }

        @Override
        public void accept(final Map<String, Object> row) {
            final Integer type = converter.convert(row.get("submission_type"), Integer.class);
            if (type != null) {
                return;
            }

            final String xmlSubmission = converter.convert(row.get("xml_submission"), String.class);

            final Submission submission = Eucegs.unmarshal(xmlSubmission);
            final SubmissionTypeEnum submissionType = submission.getSubmissionType().getValue();

            final Long id = converter.convert(row.get("id"), Long.class);

            final UpdateDataChange change = new UpdateDataChange();
            change.setTableName(SubmissionEntity.TABLE_NAME);
            change.setWhere("id = ?");
            change.addWhereParam(new ColumnConfig().setName("id").setValueNumeric(id));
            change.addColumn(
                new ColumnConfig().setName("submission_type").setValueNumeric(submissionType.value()).setType("int"));

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("set '{}' to error column of  '{}' table",
                    submissionType.toString(),
                    SubmissionEntity.TABLE_NAME);
            }
            dao.update(change);
        }

    }

}
