package com.pmi.tpd.core.liquibase.upgrade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.LiquibaseUtils;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;

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
 * @since 1.0
 */
public class TPD_110_SetSubmissionStatus implements CustomTaskChange {

    private static final Logger LOGGER = LoggerFactory.getLogger(TPD_110_SetSubmissionStatus.class);

    private static FormattingConversionService converter = new DefaultFormattingConversionService();

    @SuppressWarnings("unused")
    private ResourceAccessor resourceAccessor;

    @Override
    public String getConfirmationMessage() {
        return "Set Submission status to submission";
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
            dao.forEachRow(SubmissionEntity.TABLE_NAME, "id", cancelState, new setSubmissionStatus(dao));
        } finally {
            dao.endChangeSet();
        }
    }

    private class setSubmissionStatus implements Consumer<Map<String, Object>> {

        private final ILiquibaseAccessor dao;

        public setSubmissionStatus(final ILiquibaseAccessor dao) {
            this.dao = dao;
        }

        @Override
        public void accept(final Map<String, Object> row) {
            SubmissionStatus submissionStatus = converter.convert(row.get("submission_status"), SubmissionStatus.class);
            if (submissionStatus != null) {
                return;
            }

            final Long id = converter.convert(row.get("id"), Long.class);

            if (existReceiptInError((DefaultLiquibaseAccessor) dao, id)) {
                submissionStatus = SubmissionStatus.ERROR;
            } else {
                final TransmitStatus status = getTransmitStatusFromSubmissionReceipt((DefaultLiquibaseAccessor) dao,
                    id);
                if (status == null) {
                    submissionStatus = SubmissionStatus.PENDING;
                } else {
                    submissionStatus = from(status);
                }
            }

            final UpdateDataChange change = new UpdateDataChange();
            change.setTableName(SubmissionEntity.TABLE_NAME);
            change.setWhere("id = ?");
            change.addWhereParam(new ColumnConfig().setName("id").setValueNumeric(id));
            change.addColumn(new ColumnConfig().setName("submission_status")
                    .setValue(submissionStatus.name())
                    .setType("varchar(25)"));

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("set '{}' to submission status column of  '{}' table",
                    submissionStatus,
                    SubmissionEntity.TABLE_NAME);
            }
            dao.update(change);
        }

        private boolean existReceiptInError(final DefaultLiquibaseAccessor dao, final long submissionId) {

            final Table table = dao.findTable(TransmitReceiptEntity.TABLE_NAME);
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                final Connection connection = LiquibaseUtils.getConnection(dao.getDatabaseSession().getDatabase());
                statement = connection.prepareStatement(
                    "SELECT 1 FROM " + table.getName() + " WHERE submission_id = ? and response_error = ?");
                statement.setLong(1, submissionId);
                statement.setBoolean(2, true);
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return true;
                }
                return false;
            } catch (final SQLException e) {
                throw new DataRetrievalFailureException("Could not fetch receipt in error from " + table.getName(), e);
            } finally {
                JdbcUtils.closeResultSet(resultSet);
                JdbcUtils.closeStatement(statement);
            }
        }

        private TransmitStatus getTransmitStatusFromSubmissionReceipt(final DefaultLiquibaseAccessor dao,
            final long submissionId) {

            final Table table = dao.findTable(TransmitReceiptEntity.TABLE_NAME);
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                final Connection connection = LiquibaseUtils.getConnection(dao.getDatabaseSession().getDatabase());
                statement = connection.prepareStatement(
                    "SELECT transmit_status FROM " + table.getName() + " WHERE submission_id = ? and payload_type = ?");
                statement.setLong(1, submissionId);
                statement.setString(2, PayloadType.SUBMISSION.name());
                resultSet = statement.executeQuery();
                TransmitStatus status = null;
                if (resultSet.next()) {
                    status = TransmitStatus.from(resultSet.getString(1));
                }
                return status;
            } catch (final SQLException e) {
                throw new DataRetrievalFailureException("Could not fetch receipt in error from " + table.getName(), e);
            } finally {
                JdbcUtils.closeResultSet(resultSet);
                JdbcUtils.closeStatement(statement);
            }
        }

        public SubmissionStatus from(@Nonnull final TransmitStatus transmitStatus) {
            switch (transmitStatus) {
                case AWAITING:
                    return SubmissionStatus.PENDING;
                case PENDING:
                    return SubmissionStatus.SUBMITTING;
                case DELETED:
                case REJECTED:
                    return SubmissionStatus.ERROR;
                case RECEIVED:
                    return SubmissionStatus.SUBMITTED;
                case CANCELLED:
                    return SubmissionStatus.CANCELLED;
                default:
                    break;
            }
            return SubmissionStatus.PENDING;
        }
    }

}
