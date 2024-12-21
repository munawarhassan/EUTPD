package com.pmi.tpd.core.liquibase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;

import liquibase.change.ColumnConfig;

/**
 * Enumeration of all data types used in database columns in Stash. Used by
 * {@link DefaultLiquibaseDaoTest#testLiquibaseInsertedDataMatchesOutput()} to check that liquibase doesn't adversely
 * effect data when inserting into the database.
 */
public enum ColumnType {

    BOOLEANT("BOOLEAN", true),
    BOOLEANF("BOOLEAN", false),
    DATE("DATE", testDate(Date.class)),
    DATETIME("DATETIME", testDate(Timestamp.class)),
    TIMESTAMP("TIMESTAMP", testDate(Timestamp.class)),
    BIGINT("BIGINT", Long.MAX_VALUE), // 2^63 is max in HSQL
    INT("INT", Integer.MAX_VALUE),
    INTEGER("INTEGER", Integer.MAX_VALUE),
    NUMERIC("NUMERIC", 1234567890L),
    SMALLINT("SMALLINT", Short.MAX_VALUE),
    VARCHAR_20("VARCHAR(20)", testString(20)),
    VARCHAR_32("VARCHAR(32)", testString(32)),
    VARCHAR_40("VARCHAR(40)", testString(40)),
    VARCHAR_50("VARCHAR(50)", testString(50)),
    VARCHAR_64("VARCHAR(64)", testString(64)),
    VARCHAR_127("VARCHAR(127)", testString(127)),
    VARCHAR_128("VARCHAR(128)", testString(128)),
    VARCHAR_255("VARCHAR(255)", testString(255)),
    VARCHAR_1024("VARCHAR(1024)", testString(1024)),
    VARCHAR_2000("VARCHAR(2000)", testString(2000)),
    // VARCHAR_4000("VARCHAR(4000)", testString(4000)),
    VARCHAR_4000_W_2000CH("VARCHAR(4000)", testString(2000)),
    CHAR_1("CHAR(1)", testString(1)),
    CLOB("CLOB", testString(10000));

    public static boolean isKnownDataType(final String dataType) {
        return dataTypeSet.contains(dataType.toUpperCase());
    }

    /**
     * Create a java.sql.Time, Timestamp or Date depending on the supplied class. The following precision is used:
     * <table>
     * <tr>
     * <td>Time</td>
     * <td>Time only (date set to epoch)</td>
     * </tr>
     * <tr>
     * <td>Date</td>
     * <td>Date only (time zeroed out)</td>
     * </tr>
     * <tr>
     * <td>Timestamp</td>
     * <td>Date + Time to second precision</td>
     * </tr>
     * </tr>
     * </table>
     */
    @SuppressWarnings("unchecked")
    private static <T extends java.util.Date> T testDate(final Class<T> clazz) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(1997, Calendar.AUGUST, 29, 2, 14, 13);
        calendar.set(Calendar.MILLISECOND, 0);

        if (Time.class.equals(clazz)) {
            // create a java.sql.Time object (with no date set - or more accurately set to epoch)
            calendar.set(Calendar.YEAR, 1970);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DATE, 1);
            return (T) new Time(calendar.getTimeInMillis());
        } else if (Timestamp.class.equals(clazz)) {
            // create a java.sql.Timestamp object
            return (T) new Timestamp(calendar.getTimeInMillis());
        } else if (Date.class.equals(clazz)) {
            // create a java.sql.Date object for testing (with no h/m/s set)
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            return (T) new java.sql.Date(calendar.getTimeInMillis());
        } else {
            throw new IllegalArgumentException("Unsupported type " + clazz.getName());
        }
    }

    /**
     * Generate a test string of the desired length
     */
    private static String testString(final int length) {
        // create a test String with some CJK characters
        final String str = "\u5718Testing testing 1-\u96c6";
        return StringUtils.repeat(str, length / str.length() + 1).substring(0, length);
    }

    private static final Set<String> dataTypeSet;

    static {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (final ColumnType columnType : values()) {
            builder.add(columnType.dataType);
        }
        dataTypeSet = builder.build();
    }

    private final String dataType;

    private final Object testValue;

    private final Object expectedValue;

    ColumnType(final String dataType, final Object testValue) {
        this(dataType, testValue, testValue);
    }

    ColumnType(final String dataType, final Object testValue, final Object expectedValue) {
        this.dataType = dataType;
        this.testValue = testValue;
        this.expectedValue = expectedValue;
    }

    public String getDataType() {
        return dataType;
    }

    public Object getTestValue() {
        return testValue;
    }

    /**
     * Create a {@link ColumnConfig} for use with Liquibase CREATE or INSERT changes.
     */
    public ColumnConfig asColumnConfig() {
        final ColumnConfig column = new ColumnConfig();
        column.setName(generateColumnName());
        column.setType(dataType);
        if (testValue instanceof Boolean) {
            column.setValueBoolean((Boolean) testValue);
        } else if (testValue instanceof Date) {
            column.setValueDate((Date) testValue);
        } else if (testValue instanceof Number) {
            column.setValueNumeric((Number) testValue);
        } else {
            column.setValue(testValue.toString());
        }
        return column;
    }

    /**
     * Generate a name safe for use as a database identifier.
     */
    private String generateColumnName() {
        return "col_" + name().replaceAll("[\\(\\)]", "_").toLowerCase() + ordinal();
    }

    /**
     * Check that the supplied ResultSet contains the test value associated with this column. Performs a relaxed check
     * for numeric types that checks that the values are equal, but not necessarily the types.
     */
    public void assertColumnPresentWithCorrectValue(final Map<String, Object> columnValueMap) throws Exception {
        Object storedValue = columnValueMap.get(generateColumnName());

        if (storedValue == null && expectedValue != null) {
            fail("Value for column " + generateColumnName() + " was not found in column value map (expected: "
                    + expectedValue + ")");
        }

        if (storedValue instanceof Clob) {
            storedValue = CharStreams.toString(((Clob) storedValue).getCharacterStream());
        } else if (storedValue instanceof Blob) {
            storedValue = CharStreams
                    .toString(new InputStreamReader(((Blob) storedValue).getBinaryStream(), Charsets.UTF_8));
        }
        if (expectedValue instanceof Number && storedValue instanceof Number) {
            assertEquals(numericToLongBits((Number) expectedValue),
                numericToLongBits((Number) storedValue),
                "Value for column " + generateColumnName() + " was mutated by Liquibase");
        } else {
            assertEquals(expectedValue,
                storedValue,
                "Value for column " + generateColumnName() + " was mutated by Liquibase");
        }
    }

    private static long numericToLongBits(final Number number) {
        return Double.doubleToLongBits(number.doubleValue());
    }
}