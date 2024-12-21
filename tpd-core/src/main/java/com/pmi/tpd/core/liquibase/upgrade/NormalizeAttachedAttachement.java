package com.pmi.tpd.core.liquibase.upgrade;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

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
import com.pmi.tpd.euceg.api.Eucegs;

import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;
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
public class NormalizeAttachedAttachement implements CustomTaskChange {

  private static final Logger LOGGER = LoggerFactory.getLogger(NormalizeAttachedAttachement.class);

  private static FormattingConversionService converter = new DefaultFormattingConversionService();

  @SuppressWarnings("unused")
  private ResourceAccessor resourceAccessor;

  @Override
  public String getConfirmationMessage() {
    return "Extract attachment from xml and associate to product";
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
      dao.deleteAllRows(ProductEntity.TABLE_NAME_ATTACHMENT);
      forEachRow(dao,
          ProductEntity.TABLE_NAME,
          "SELECT p.product_number as product_number, y.payload_data as payload_data FROM t_product p, t_payload y "
              + "WHERE p.payload_product_id = y.id",
          "product_number",
          cancelState,
          new UpdateAttachmentRowEffect(dao));
    } finally {
      dao.endChangeSet();
    }
  }

  private class UpdateAttachmentRowEffect implements Consumer<Map<String, Object>> {

    private final ILiquibaseAccessor dao;

    public UpdateAttachmentRowEffect(final ILiquibaseAccessor dao) {
      this.dao = dao;
    }

    @Override
    public void accept(final Map<String, Object> row) {

      final String productNumber = converter.convert(row.get("product_number"), String.class);

      Set<String> uuids = Collections.emptySet();
      try (Reader reader = ChangeHelper.getReader(row, "payload_data")) {
        uuids = Eucegs.extractAttachementID(reader);
      } catch (IOException | SQLException e) {

      }
      for (final String uuid : uuids) {
        final InsertDataChange insert = new InsertDataChange();
        insert.setTableName(ProductEntity.TABLE_NAME_ATTACHMENT);
        insert.addColumn(new ColumnConfig().setName("product_number").setValue(productNumber));
        insert.addColumn(new ColumnConfig().setName("attachmentId").setValue(uuid));

        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("insert attachment '{}' associated to product '{}'", uuid, productNumber);
        }
        dao.insert(insert);
      }
    }
  }

  private long forEachRow(final DefaultLiquibaseAccessor dao,
      @Nonnull final String tablename,
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
    final Table table = dao.findTable(tablename);
    try {
      final Connection connection = LiquibaseUtils.getConnection(dao.getDatabaseSession().getDatabase());
      statement = connection.createStatement();
      statement.setFetchSize(100);
      resultSet = statement.executeQuery(query + dao.orderByClause(table, orderingColumn));
      while (resultSet.next() && !cancelState.isCanceled()) {
        forCurrentRow(dao, resultSet, effect);
        numberOfRows++;
        if (numberOfRows % 100 == 0) {
          LOGGER.trace("{}: {} rows processed", table.getName(), numberOfRows);
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

  private void forCurrentRow(final DefaultLiquibaseAccessor dao,
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

}
