package com.pmi.tpd.core.liquibase.upgrade;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.io.ByteSource;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.util.xml.InvalidXmlCharacterFilterReader;
import com.pmi.tpd.euceg.api.Eucegs;

import liquibase.change.ColumnConfig;
import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.InsertDataChange;
import liquibase.change.core.UpdateDataChange;

public final class ChangeHelper {

    private ChangeHelper() {
    }

    /**
     * @param generatorName
     * @param value
     * @return
     */
    @Nonnull
    public static InsertDataChange insertGeneratorValue(final String generatorName, final long value) {
        final InsertDataChange insertDataChange = new InsertDataChange();
        insertDataChange.setTableName(ApplicationConstants.Jpa.Generator.NAME);
        insertDataChange.addColumn(new ColumnConfig().setName(ApplicationConstants.Jpa.Generator.COLUMN_NAME)
                .setValue(generatorName)
                .setType("VARCHAR(255)"));
        insertDataChange.addColumn(new ColumnConfig().setName(ApplicationConstants.Jpa.Generator.COLUMN_VALUE_NAME)
                .setValueNumeric(value)
                .setType("INT"));
        return insertDataChange;
    }

    /**
     * @param generatorName
     * @param value
     * @return
     */
    @Nonnull
    public static DeleteDataChange deleteGeneratorValue(final String generatorName) {
        final DeleteDataChange change = new DeleteDataChange();
        change.setTableName(ApplicationConstants.Jpa.Generator.NAME);
        change.setWhere(ApplicationConstants.Jpa.Generator.COLUMN_NAME + " = ?");
        change.addWhereParam(
            new ColumnConfig().setName(ApplicationConstants.Jpa.Generator.COLUMN_NAME).setValue(generatorName));
        return change;
    }

    /**
     * @param generatorName
     * @param value
     * @return
     */
    @Nonnull
    public static UpdateDataChange updateGeneratorValue(final String generatorName, final long value) {
        final UpdateDataChange updateDataChange = new UpdateDataChange();
        updateDataChange.setTableName(ApplicationConstants.Jpa.Generator.NAME);
        updateDataChange.addColumn(new ColumnConfig().setName(ApplicationConstants.Jpa.Generator.COLUMN_NAME)
                .setValue(generatorName)
                .setType("VARCHAR(255)"));
        updateDataChange.addColumn(new ColumnConfig().setName(ApplicationConstants.Jpa.Generator.COLUMN_VALUE_NAME)
                .setValueNumeric(value)
                .setType("INT"));
        updateDataChange.setWhere(ApplicationConstants.Jpa.Generator.COLUMN_NAME + " = ?");
        updateDataChange.addWhereParam(new ColumnConfig().setName("id").setValue(generatorName));
        return updateDataChange;
    }

    @Nonnull
    public static InsertDataChange addAuditedValue(@Nonnull final InsertDataChange change) {
        change.addColumn(new ColumnConfig().setName("created_by").setValue("admin").setType("VARCHAR(50)"));
        change.addColumn(new ColumnConfig().setName("created_date").setValueDate(new Date()).setType("timestamp"));
        change.addColumn(new ColumnConfig().setName("last_modified_by").setValue("admin").setType("VARCHAR(50)"));
        change.addColumn(
            new ColumnConfig().setName("last_modified_date").setValueDate(new Date()).setType("timestamp"));
        return change;
    }

    @Nonnull
    public static Reader getReader(@Nonnull final Map<String, Object> row, @Nonnull final String columnName)
            throws SQLException, IOException {
        final Object value = row.get(columnName);
        Reader reader = null;
        if (value instanceof Clob) {
            final Clob xml = (Clob) value;
            reader = new InvalidXmlCharacterFilterReader(xml.getCharacterStream());
        } else if (value instanceof String) {
            final String xml = (String) value;
            reader = new InvalidXmlCharacterFilterReader(
                    ByteSource.wrap(xml.getBytes()).asCharSource(Eucegs.getDefaultCharset()).openStream());
        } else {
            throw new RuntimeException("unknow type" + value.getClass());
        }
        return reader;
    }

}
