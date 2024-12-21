package com.pmi.tpd.euceg.core.excel;

import static com.google.common.collect.Streams.stream;
import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.lang.ref.WeakReference;
import java.util.Comparator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.pmi.tpd.euceg.core.util.validation.ValidationException;

/**
 * Class immutable describing a column in excel file.
 *
 * @author Christophe Friederich
 * @since 1.0
 * @param <T>
 *            the type contained in column.
 */
@Immutable
public final class ColumnDescriptor<T> {

    private static final Comparator<ColumnDescriptor<?>> COMPARATOR_COLUMN = (o1, o2) -> Integer.compare(o1.index,
        o2.index);

    public final static Comparator<ColumnDescriptor<?>> sorted() {
        return COMPARATOR_COLUMN;
    }

    /**
     * Create a column descriptor.
     *
     * @param <T>
     *            type accepted by the column.
     * @param name
     *            the name of column (can <b>not</b> be {@code null} or empty).
     * @param targetType
     *            the target type accepted by the column (can <b>not</b> be {@code null}).
     * @param nullable
     *            indicate if column accept {@code null} value.
     * @return Returns a new instance of {@link ColumnDescriptor}.
     */
    public static <T> ColumnDescriptor<T> createColumn(@Nonnull final String name,
        @Nonnull final Class<T> targetType,
        final boolean nullable) {
        return new ColumnDescriptor<>(name, ColumnMetadata.builder(targetType).nullable(true).build());
    }

    /**
     * Create a column descriptor.
     *
     * @param <T>
     *            type accepted by the column.
     * @param name
     *            the name of column (can <b>not</b> be {@code null} or empty).
     * @param targetType
     *            the target type accepted by the column (can <b>not</b> be {@code null}).
     * @return Returns a new instance of {@link ColumnDescriptor}.
     */
    public static <T> ColumnDescriptor<T> createColumn(@Nonnull final String name, @Nonnull final Class<T> targetType) {
        return new ColumnDescriptor<>(name, ColumnMetadata.builder(targetType).build());
    }

    /**
     * Create a column descriptor.
     *
     * @param name
     *            the name of column (can <b>not</b> be {@code null} or empty).
     * @param metadata
     *            additional information (can <b>not</b> be {@code null}).
     * @return Returns a new instance of {@link ColumnDescriptor}.
     * @param <T>
     *            type accepted by the column.
     */
    public static <T> ColumnDescriptor<T> createColumn(@Nonnull final String name,
        @Nonnull final ColumnMetadata<T> metadata) {
        return new ColumnDescriptor<>(name, metadata);
    }

    @JsonIgnore
    private WeakReference<GroupDescriptor> parent;

    /** */
    private final String name;

    /** */
    private final ColumnMetadata<T> metadata;

    private boolean primaryKey = false;

    private int index;

    private ColumnDescriptor(@Nonnull final String name, @Nonnull final ColumnMetadata<T> metadata) {
        this.name = checkHasText(name, "name");
        this.metadata = checkNotNull(metadata, "metadata");
    }

    @Override
    public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ColumnDescriptor)) {
            return false;
        }
        final ColumnDescriptor<?> that = (ColumnDescriptor<?>) obj;

        return null == this.name ? false : this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * Gets the name of column.
     *
     * @return Returns a {@link String} representing the name of column.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Set the {@link GroupDescriptor} associated to.
     *
     * @param parent
     *            the associated parent.
     */
    void setParent(final GroupDescriptor parent) {
        this.parent = new WeakReference<>(parent);
    }

    /**
     * Gets the indicating whether the column is a primary key.
     *
     * @return Returns {@code true} whether the column is a primary key otherwise {@code false}.
     */
    public boolean isPrimaryKey() {
        return this.primaryKey;
    }

    /**
     * Sets the indicating whether the column is a primary key.
     *
     * @param primaryKey
     *            {@code true} whether the column is a primary key otherwise {@code false}.
     */
    void setPrimaryKey(final boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * Gets additional information on columns.
     *
     * @return Returns a {@link ColumnDescriptor} representing additional information on column.
     * @since 2.5
     */
    @Nonnull
    public ColumnMetadata<T> getMetadata() {
        return metadata;
    }

    /**
     * Gets the value contained in @code row} and convert the given value to the target type.
     *
     * @param row
     *            a row to use (can <b>not</b> be {@code null}).
     * @return Returns a value representing the value contained in @code row}.
     * @see #getTargetType()
     */
    @Nullable
    public T getValue(@Nonnull final Row row) {
        checkNotNull(row, "row");
        final Cell cell = getCell(row);
        if (cell == null) {
            return null;
        }
        try {
            return ExcelHelper.getValue(cell, this.metadata.getTargetType());
        } catch (final Throwable e) {
            throw new ValidationException("The column '" + this.getName() + "' has wrong value in row ["
                    + row.getRowNum() + "] of sheet " + row.getSheet().getSheetName());
        }
    }

    /**
     * Gets the indicating whether column is part of the {@code sheet}.
     *
     * @param sheet
     *            the sheet to use (can <b>not</b> be {@code null}).
     * @return Returns {@code true} whether column is part of the {@code sheet}, {@code false} otherwise.
     */
    public boolean isPartOf(@Nonnull final Sheet sheet) {
        return findColumnIndex(sheet) >= 0;
    }

    private int findColumnIndex(@Nonnull final Sheet sheet) {
        checkNotNull(sheet, "sheet");
        return stream(getFirstRow(sheet)).filter(c -> {
            String cellName = ExcelHelper.getValue(c, String.class);
            if (Strings.isNullOrEmpty(cellName)) {
                return false;
            }
            cellName = cellName.trim();
            return getName().equalsIgnoreCase(cellName);
        }).findAny().map(Cell::getColumnIndex).orElse(-1);
    }

    private Cell getCell(final Row row) {
        final int columnIndex = findColumnIndex(row.getSheet());
        if (columnIndex < 0) {
            throw new IllegalArgumentException("Cell index must be >= 0");
        }
        return row.getCell(columnIndex, MissingCellPolicy.RETURN_BLANK_AS_NULL);
    }

    private Row getFirstRow(final Sheet sheet) {
        return sheet.getRow(sheet.getFirstRowNum());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("targetType", this.metadata.getTargetType())
                .toString();
    }

}
