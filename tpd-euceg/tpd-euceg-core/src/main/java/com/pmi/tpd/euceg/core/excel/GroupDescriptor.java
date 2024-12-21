package com.pmi.tpd.euceg.core.excel;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableMap.copyOf;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.concat;
import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pmi.tpd.euceg.core.excel.ExcelMapper.ObjectMapper;
import com.pmi.tpd.euceg.core.excel.ExcelMapper.ValueMapper;
import com.pmi.tpd.euceg.core.excel.ExcelSheet.ConvertType;

/**
 * Class immutable describing a group of {@link ColumnDescriptor column} in one sheet in Excel workbook.
 * <p>
 * A group can have
 * <p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Immutable
@JsonPropertyOrder({ "name", "sheetIndex", "required", "primaryKeys", "columns", "children" })
public final class GroupDescriptor {

    /** */
    @Nonnull
    private final String name;

    /** */
    @JsonIgnore
    private WeakReference<GroupDescriptor> parent = null;

    /** */
    @JsonProperty("primaryKeys")
    private Set<ColumnDescriptor<?>> primaryKeyColumns;

    /** */
    @JsonIgnore
    @Nonnull
    private Set<ColumnDescriptor<?>> foreignKeyColumns;

    /** */
    @Nonnull
    private final ExcelSheet sheet;

    /**
     *
     */
    private final Set<ColumnDescriptor<?>> columns;

    /** */
    @JsonProperty("children")
    @Nonnull
    private final Map<String, GroupDescriptor> childrenMap;

    private GroupDescriptor() {
        throw new UnsupportedOperationException();
    }

    private GroupDescriptor(@Nonnull final Builder builder) {
        this.name = checkNotNull(checkNotNull(builder, "builder").name, "name");
        this.sheet = checkNotNull(builder.sheet, "sheet");
        this.primaryKeyColumns = builder.columnKeys != null ? copyOf(builder.columnKeys) : Collections.emptySet();
        this.foreignKeyColumns = builder.columnKeys != null ? copyOf(primaryKeyColumns) : Collections.emptySet();
        this.columns = copyOf(concat(primaryKeyColumns, builder.columns));
        this.childrenMap = copyOf(builder.children);
        this.configure();
    }

    private void configure() {
        // set the parent of all children group descriptor
        childrenMap.values().forEach(g -> g.setParent(this));
        // set columns as primary keys.
        this.primaryKeyColumns.forEach(c -> c.setPrimaryKey(true));
        // set the parent of all columns
        this.columns.forEach(c -> c.setParent(this));
    }

    private void setParent(final GroupDescriptor parent) {
        this.parent = new WeakReference<>(parent);
        this.foreignKeyColumns = copyOf(Iterables.concat(parent.foreignKeyColumns, primaryKeyColumns));
        this.childrenMap.values().forEach(g -> g.setParent(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GroupDescriptor)) {
            return false;
        }
        final GroupDescriptor that = (GroupDescriptor) obj;

        return null == this.name ? false : this.name.equals(that.name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * Gets the child group for the specified name.
     *
     * @param name
     *             the name of group child (can <b>not</b> be {@code null} or empty).
     * @return Returns a {@link GroupDescriptor} for the specified name if exists, {@code null} otherwise.
     */
    @Nullable
    public GroupDescriptor getChildren(@Nonnull final String name) {
        checkHasText(name, "name");
        return childrenMap.get(name);
    }

    /**
     * @return Returns the name.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * @return Returns the sheet.
     */
    @Nonnull
    public ExcelSheet getSheet() {
        return sheet;
    }

    /**
     * @return Returns the list of {@link ColumnDescriptor} allowing identify a {@link ObjectMapper} uniquely.
     */
    @Nonnull
    @JsonIgnore
    public Set<ColumnDescriptor<?>> getForeignKeyColumns() {
        return foreignKeyColumns;
    }

    /**
     * @return Returns the list of {@link ColumnDescriptor} allowing identify a {@link ObjectMapper} uniquely.
     */

    public Set<ColumnDescriptor<?>> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    /**
     * @return Returns the list of {@link ColumnDescriptor}.
     */
    public Set<ColumnDescriptor<?>> getColumns() {
        return columns;
    }

    /**
     * @return Returns the group parent if exists, {@code null} otherwise.
     */
    @Nullable
    @CheckReturnValue
    @JsonIgnore
    public GroupDescriptor getParent() {
        if (parent == null) {
            return null;
        }
        return parent.get();
    }

    /**
     * @return Returns the root group.
     */
    @SuppressWarnings("null")
    @Nonnull
    @JsonIgnore
    public GroupDescriptor getRoot() {
        GroupDescriptor parent = this;
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        return parent;
    }

    /**
     * @return Returns a map of all children group by name associated to.
     */
    @Nonnull
    public Map<String, GroupDescriptor> getChildren() {
        return childrenMap;
    }

    /**
     * @param workbook
     * @return
     */
    Sheet getSheet(final Workbook workbook) {
        return workbook.getSheetAt(getSheet().getIndex());
    }

    /**
     * @param workbook
     */
    void checkColumnMapping(final Workbook workbook) {
        final Sheet sheet = getSheet(workbook);
        for (final ColumnDescriptor<?> column : this.columns) {
            if (!column.isPartOf(sheet)) {
                throw new ExcelMappingException(String.format("The Column '%s' does not exist in Excel sheet '%s'.",
                    column.getName(),
                    sheet.getSheetName()));
            }
        }
    }

    /**
     * @param row
     * @return
     */
    @Nonnull
    String foreignKeyToString(final Row row) {
        return buildKey(row, getForeignKeyColumns());
    }

    /**
     * @param row
     * @return
     */
    @Nonnull
    String primaryKeyToString(final Row row) {
        final String key = buildKey(row, getPrimaryKeyColumns());
        // remove separator to check if exist at least one primary key value
        String localKey = key;
        if (!isNullOrEmpty(localKey)) {
            localKey = localKey.replace("|", "");
        }
        if (isNullOrEmpty(localKey)) {
            return null;
        }
        return key;
    }

    private Map<String, ValueMapper<?>> getValues(@Nonnull final Row row) {
        final Map<String, ValueMapper<?>> values = Maps.newHashMap();
        for (final ColumnDescriptor<?> column : this.columns) {
            final ValueMapperImpl<?> value = new ValueMapperImpl<>(column, row);
            values.put(column.getName(), value);
        }
        return values;
    }

    @Nonnull
    private static String buildKey(final @Nonnull Row row, final @Nonnull Iterable<ColumnDescriptor<?>> columns) {
        final StringBuilder str = new StringBuilder();
        final int size = Iterables.size(columns);
        int count = 0;
        for (final ColumnDescriptor<?> columnDescriptor : columns) {
            final Object value = columnDescriptor.getValue(row);
            if (!columnDescriptor.getMetadata().isNullable() && value == null) {
                return null;
            }
            String key = "";
            if (value != null) {
                key = value.toString();
                key = key.toLowerCase().trim();
            }
            str.append(key);
            count++;
            if (count < size) {
                str.append("||");
            }
        }
        return str.toString();
    }

    /**
     * Create a builder for {@link GroupDescriptor}.
     *
     * @param name
     *              name of group.
     * @param sheet
     *              the index of sheet.
     * @return Returns new instance of {@link Builder}.
     */
    public static Builder builder(@Nonnull final String name, final ExcelSheet sheet) {
        return new Builder().name(name).sheet(sheet);
    }

    /**
     * Create a builder for {@link GroupDescriptor}.
     *
     * @return Returns new instance of {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("sheet", sheet)
                .add("columnKeys", foreignKeyColumns)
                .add("parent", parent)
                .toString();
    }

    /**
     * Builder of {@link GroupDescriptor} for fluent syntax and immutability.
     *
     * @author Christophe Friederich
     * @since 1.0
     */
    public static class Builder {

        /** */
        private String name;

        /** */
        private Set<ColumnDescriptor<?>> columnKeys;

        /** */
        private ExcelSheet sheet;

        /**
        *
        */
        private final Set<ColumnDescriptor<?>> columns = Sets.newLinkedHashSet();

        /** */
        private final Map<String, GroupDescriptor> children = Maps.newHashMap();

        /**
         * @param value
         *              the name
         * @return Returns fluent {@link Builder}.
         */
        public Builder name(final String value) {
            this.name = value;
            return this;
        }

        /**
         * @param value
         *              the list of column allowing identify a {@link ObjectMapper} uniquely.
         * @return Returns fluent {@link Builder}.
         */
        public Builder keys(final ColumnDescriptor<?>... value) {
            this.columnKeys = ImmutableSet.copyOf(Arrays.asList(value));
            return this;
        }

        /**
         * @param value
         *              the sheet.
         * @return Returns fluent {@link Builder}.
         */
        public Builder sheet(final ExcelSheet value) {
            this.sheet = value;
            return this;
        }

        /**
         * @param columns
         *                the list of columns.
         * @return Returns fluent {@link Builder}.
         */
        public Builder columns(final Iterable<ColumnDescriptor<?>> columns) {
            for (final ColumnDescriptor<?> column : columns) {
                column(column);
            }
            return this;
        }

        /**
         * @param columns
         *                the list of columns.
         * @return Returns fluent {@link Builder}.
         */
        public Builder columns(final ColumnDescriptor<?>... columns) {
            for (final ColumnDescriptor<?> column : columns) {
                column(column);
            }
            return this;
        }

        /**
         * @param column
         *               a Column
         * @return Returns fluent {@link Builder}.
         */
        public Builder column(final ColumnDescriptor<?> column) {
            this.columns.add(column);
            return this;
        }

        /**
         * @param groups
         *               a list of children group descriptor.
         * @return Returns fluent {@link Builder}.
         */
        public Builder children(final GroupDescriptor... groups) {
            for (final GroupDescriptor group : groups) {
                child(group);
            }
            return this;
        }

        /**
         * @param group
         *              a child group descriptor.
         * @return Returns fluent {@link Builder}.
         */
        public Builder child(final GroupDescriptor group) {
            this.children.put(group.getName(), group);
            return this;
        }

        /**
         * @return Returns new instance of {@link GroupDescriptor}.
         */
        public GroupDescriptor build() {
            return new GroupDescriptor(this);
        }
    }

    /**
     * @author Christophe Friederich
     */
    static class ObjectMapperImpl implements ObjectMapper {

        /** */
        private final Workbook workbook;

        /** */
        private final ListDescriptor root;

        /** */
        private final GroupDescriptor groupDescriptor;

        /** */
        private final Map<String, ValueMapper<?>> values;

        /** */
        @Nonnull
        private final String primaryKey;

        /** */
        @Nullable
        private final List<Integer> selectedSheets;

        ObjectMapperImpl(@Nonnull final Workbook workbook, @Nonnull final ListDescriptor root,
                @Nonnull final GroupDescriptor groupDescriptor, @Nonnull final Row row,
                @Nullable final List<Integer> selectedSheets) {

            this.workbook = checkNotNull(workbook, "workbook");
            this.root = checkNotNull(root, "root");
            this.groupDescriptor = checkNotNull(groupDescriptor, "groupDescriptor");
            this.primaryKey = groupDescriptor.foreignKeyToString(row);
            this.values = groupDescriptor.getValues(row);
            this.selectedSheets = selectedSheets;
        }

        public GroupDescriptor getGroupDescriptor() {
            return groupDescriptor;
        }

        public Workbook getWorkbook() {
            return workbook;
        }

        @SuppressWarnings("null")
        @Override
        public boolean isSelected(final String groupName) {
            final GroupDescriptor descriptor = getGroupDescriptor(checkNotNull(groupName, "groupName"))
                    .orElseThrow(() -> new RuntimeException(String.format("the group '%s' does not exist", groupName)));
            if (selectedSheets == null || selectedSheets.isEmpty()) {
                return true;
            }
            return selectedSheets.contains(descriptor.getSheet().getIndex());
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("null")
        @Override
        @Nonnull
        public ValueMapper<?> get(@Nonnull final String columnName) {
            if (!values.containsKey(checkNotNull(columnName, "columnName"))) {
                throw new RuntimeException(String.format("The column '%s' doesn't exist in a group '%s'",
                    columnName,
                    groupDescriptor.getName()));
            }
            return find(columnName).get();

        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("null")
        @Override
        @Nonnull
        public Optional<ValueMapper<?>> find(@Nonnull final String columnName) {
            if (!values.containsKey(checkNotNull(columnName, "columnName"))) {
                throw new RuntimeException(String.format("The column '%s' doesn't exist in a group '%s'",
                    columnName,
                    groupDescriptor.getName()));
            }
            return Optional.ofNullable(values.get(columnName));

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @Nonnull String getPrimaryKey() {
            return primaryKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <E> E getValue(final @Nonnull String columnName, final @Nonnull Class<E> targetType) {
            return get(columnName).getValue(targetType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <E> E getValue(final @Nonnull String columnName,
            final @Nonnull Class<E> targetType,
            final @Nullable E defaultValue) {
            return get(columnName).getValue(targetType, defaultValue);
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("null")
        @Override
        public @Nonnull Map<String, ValueMapper<?>> toMap() {
            return Collections.unmodifiableMap(values);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return values.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        public List<ObjectMapper> getObjectMappers(@Nonnull final String groupName, final boolean acceptDuplicate) {
            final GroupDescriptor descriptor = getGroupDescriptor(checkNotNull(groupName, "groupName"))
                    .orElseThrow(() -> new RuntimeException(
                            String.format("the group '%s' does not exist or can not used during import", groupName)));
            final Sheet sheet = descriptor.getSheet(workbook);

            if (selectedSheets != null && !selectedSheets.contains(descriptor.getSheet().getIndex())) {
                return Collections.emptyList();
            }

            final Set<String> primaryKeys = Sets.newHashSet();
            descriptor.checkColumnMapping(workbook);
            return FluentIterable.from(sheet).skip(1).filter(row -> {
                // select only row associated to this group.
                String key = groupDescriptor.foreignKeyToString(row);
                if (!primaryKey.equals(key)) {
                    return false;
                }
                if (Iterables.elementsEqual(groupDescriptor.getForeignKeyColumns(),
                    descriptor.getForeignKeyColumns())) {
                    return true;
                }
                // filter according to selected group keys.
                key = descriptor.primaryKeyToString(row);
                if (key == null) {
                    return false;
                }
                if (!acceptDuplicate && primaryKeys.contains(key)) {
                    return false;
                }
                primaryKeys.add(key);
                return true;
            })
                    .transform(row -> (ObjectMapper) new GroupDescriptor.ObjectMapperImpl(workbook, root, descriptor,
                            row, selectedSheets))
                    .toList();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        public List<ObjectMapper> getObjectMappers(@Nonnull final String groupName) {
            return getObjectMappers(groupName, false);
        }

        public Optional<GroupDescriptor> getGroupDescriptor(@Nonnull final String groupName) {
            return this.root.get(checkNotNull(groupName, "groupName"))
                    .filter(group -> ConvertType.importExcel.equals(group.getSheet().getConvertType())
                            || ConvertType.both.equals(group.getSheet().getConvertType()));
        }

    }

    /**
     * Default implementation of interface {@link ValueMapper}.
     *
     * @author Christophe Friederich
     * @param <T>
     */
    static class ValueMapperImpl<T> implements ValueMapper<T> {

        /** */
        @Nonnull
        private final ColumnDescriptor<T> column;

        /** */
        @Nullable
        private final T value;

        ValueMapperImpl(@Nonnull final ColumnDescriptor<T> column, @Nonnull final Row row) {
            super();
            this.column = checkNotNull(column, "column");
            this.value = column.getValue(checkNotNull(row, "row"));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        public String getColumnName() {
            return column.getName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        public Class<T> getTargetType() {
            return column.getMetadata().getTargetType();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nullable
        public T getValue() {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nullable
        public <E> E getValue(@Nonnull final Class<E> targetType) {
            return checkNotNull(targetType, "targetType is required").cast(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nullable
        public <E> E getValue(@Nonnull final Class<E> targetType, @Nullable final E defaultValue) {
            checkNotNull(targetType, "targetType is required");
            if (value == null) {
                return defaultValue;
            }
            return targetType.cast(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("columnName", getColumnName()).add("value", value).toString();
        }

    }
}
