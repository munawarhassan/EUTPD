package com.pmi.tpd.core.liquibase.upgrade;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.core.euceg.impl.SubmissionProductIdGenerator;
import com.pmi.tpd.core.model.euceg.ProductIdEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.LiquibaseUtils;
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
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class TPD_60_NormalizeSubmissionKey implements CustomTaskChange {

    private static final Logger LOGGER = LoggerFactory.getLogger(TPD_60_NormalizeSubmissionKey.class);

    private static FormattingConversionService converter = new DefaultFormattingConversionService();

    @SuppressWarnings("unused")
    private ResourceAccessor resourceAccessor;

    private long maxId = 0L;

    @Override
    public String getConfirmationMessage() {
        return "Set Id from ProductId";
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
            dao.forEachRow(SubmissionEntity.TABLE_NAME, "product_id", cancelState, new SetIdFromProductIdRowEffect(dao));

            // increment the next tpd_id value
            maxId++;

            final String submitterId = selectSubmitter(dao);
            if (submitterId != null) {
                final InsertDataChange insertDataChange = new InsertDataChange();
                insertDataChange.setTableName(ProductIdEntity.TABLE_NAME);
                insertDataChange.addColumn(
                    new ColumnConfig().setName("submitter_id").setValue(submitterId).setType("VARCHAR(20)"));
                insertDataChange
                        .addColumn(new ColumnConfig().setName("current_value").setValueNumeric(maxId).setType("int"));
                dao.insert(insertDataChange);

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Insert new TPD_ID generator with value: '{}'", maxId);
                }
            }

        } finally {
            dao.endChangeSet();
        }
    }

    private String selectSubmitter(final DefaultLiquibaseAccessor dao) {

        final Table table = dao.findTable(SubmitterEntity.TABLE_NAME);
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            final Connection connection = LiquibaseUtils.getConnection(dao.getDatabaseSession().getDatabase());
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT submitter_id FROM " + table.getName());
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
            return null;
        } catch (final SQLException e) {
            throw new DataRetrievalFailureException("Could not fetch submitterId from " + table.getName(), e);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }
    }

    private class SetIdFromProductIdRowEffect implements Consumer<Map<String, Object>> {

        private final ILiquibaseAccessor dao;

        public SetIdFromProductIdRowEffect(final ILiquibaseAccessor dao) {
            this.dao = dao;
        }

        @Override
        public void accept(final Map<String, Object> row) {

            final String productId = converter.convert(row.get("product_id"), String.class);

            final long id = SubmissionProductIdGenerator.toId(productId);
            maxId = Math.max(maxId, id);
            final UpdateDataChange change = new UpdateDataChange();
            change.setTableName(SubmissionEntity.TABLE_NAME);
            change.setWhere("product_id = ?");
            change.addWhereParam(new ColumnConfig().setName("product_id").setValue(productId));

            change.addColumn(new ColumnConfig().setName("id").setValueNumeric(id).setType("bigint"));

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Update Submission '{}'", productId);
            }
            dao.update(change);

        }
    }

}
