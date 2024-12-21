package com.pmi.tpd.euceg.core.excel;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

/**
 * Class facility to build {@link ObjectMapper} on excel file using poi library.
 *
 * @author Christophe Friederich
 * @since 1.0
 * @see ObjectMapper
 */
public final class ExcelMapper {

    /**
     * Build the list of {@link ObjectMapper} on High level representation of a Excel workbook and using the
     * {@code root} descriptor.
     *
     * @param workbook
     *                 a representation of a Excel workbook (can <b>not</b> be {@code null}).
     * @param root
     *                 a descriptor (can <b>not</b> be {@code null}).
     * @return Returns a list of {@link ObjectMapper} representing a object representation of a excel workbook according
     *         to a {@link GroupDescriptor}.
     */
    @Nonnull
    public static List<ObjectMapper> build(@Nonnull final Workbook workbook, @Nonnull final ListDescriptor root) {
        return build(workbook, root, null);
    }

    /**
     * Build the list of {@link ObjectMapper} on High level representation of a Excel workbook and using the
     * {@code root} descriptor.
     *
     * @param workbook
     *                       a representation of a Excel workbook (can <b>not</b> be {@code null}).
     * @param root
     *                       a descriptor (can <b>not</b> be {@code null}).
     * @param selectedSheets
     *                       list of sheet names to use in the excel workbook (can be {@code null} if all are selected).
     * @return Returns a list of {@link ObjectMapper} representing a object representation of a excel workbook according
     *         to a {@link GroupDescriptor}.
     */
    @Nonnull
    public static List<ObjectMapper> build(@Nonnull final Workbook workbook,
        @Nonnull final ListDescriptor root,
        @Nullable final int[] selectedSheets) {
        final GroupDescriptor groupDescriptor = Iterables.getFirst(checkNotNull(root, "root"), null);
        final Sheet sheet = checkNotNull(groupDescriptor, "groupDescriptor")
                .getSheet(checkNotNull(workbook, "workbook"));
        // force check column on root to check the primary key
        groupDescriptor.checkColumnMapping(workbook);
        final Map<String, ObjectMapper> result = Maps.newHashMap();
        // skip the first header row
        FluentIterable.from(sheet).skip(1).forEach(row -> {
            final String key = groupDescriptor.foreignKeyToString(row);
            if (Strings.isNullOrEmpty(key)) {
                return;
            }
            // accept only first row.
            if (result.containsKey(key)) {
                return;
            } else {
                result.put(key,
                    new GroupDescriptor.ObjectMapperImpl(workbook, root, groupDescriptor, row,
                            includeRequiredSheets(workbook, root, selectedSheets)));
            }
        });
        return ImmutableList.copyOf(result.values());
    }

    /**
     * Build a map of {@link ObjectMapper} on High level representation of a Excel workbook and using the {@code root}
     * descriptor.
     *
     * @param workbook
     *                 a representation of a Excel workbook (can <b>not</b> be {@code null}).
     * @param root
     *                 a descriptor (can <b>not</b> be {@code null}).
     * @return Returns a map of {@link ObjectMapper} associated to unique key ({@link ObjectMapper#getPrimaryKey()})
     *         representing a object representation of a excel workbook according to a {@link GroupDescriptor}.
     * @see ObjectMapper#getPrimaryKey()
     */
    public static Map<String, ObjectMapper> toMap(@Nonnull final Workbook workbook,
        @Nonnull final ListDescriptor root) {
        return toMap(workbook, root, null);
    }

    /**
     * Build a map of {@link ObjectMapper} on High level representation of a Excel workbook and using the {@code root}
     * descriptor.
     *
     * @param workbook
     *                       a representation of a Excel workbook (can <b>not</b> be {@code null}).
     * @param root
     *                       a descriptor (can <b>not</b> be {@code null}).
     * @param selectedSheets
     *                       selected sheets to use (can be {@code null}).
     * @return Returns a map of {@link ObjectMapper} associated to unique key ({@link ObjectMapper#getPrimaryKey()})
     *         representing a object representation of a excel workbook according to a {@link GroupDescriptor}.
     * @see ObjectMapper#getPrimaryKey()
     */
    public static Map<String, ObjectMapper> toMap(@Nonnull final Workbook workbook,
        @Nonnull final ListDescriptor root,
        @Nullable final int[] selectedSheets) {
        return FluentIterable.from(build(workbook, root, selectedSheets)).uniqueIndex(ObjectMapper::getPrimaryKey);
    }

    @Nullable
    static List<Integer> includeRequiredSheets(@Nonnull final Workbook workbook,
        @Nonnull final ListDescriptor root,
        @Nullable final int[] selectedSheets) {
        if (selectedSheets == null || selectedSheets.length == 0) {
            return null;
        }
        final var sheets = Sets.newHashSet(Ints.asList(selectedSheets));
        sheets.addAll(root.getRequiredImportedSheets(workbook)
                .stream()
                .map(s -> (Integer) workbook.getSheetIndex(s))
                .collect(Collectors.toList()));
        return sheets.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Represents a Object (or row) mapping to one item (group of information) in a workbook excel.
     *
     * @author Christophe Friederich
     * @since 1.0
     * @see GroupDescriptor
     */
    public interface ObjectMapper {

        /**
         * Gets the generated primary key associated to.
         *
         * @return Returns a {@link String} representing a unique key for this {@link ObjectMapper}.
         */
        @Nonnull
        String getPrimaryKey();

        /**
         * Gets the {@link ValueMapper} for the specified column name.
         *
         * @param columnName
         *                   a column name to use (can <b>not</b> be {@code null}).
         * @return Returns a {@link ValueMapper} representing the value for the specified column name.
         */
        @Nonnull
        ValueMapper<?> get(@Nonnull String columnName);

        /**
         * find the {@link ValueMapper} for the specified column name.
         *
         * @param columnName
         *                   a column name to use (can <b>not</b> be {@code null}).
         * @return Returns a {@link ValueMapper} representing the value for the specified column name.
         */
        @Nonnull
        Optional<ValueMapper<?>> find(@Nonnull String columnName);

        /**
         * Gets the value of column for the specified {@code targetTape}.
         *
         * @param columnName
         *                   the name of column (can <b>not</b> be {@code null}).
         * @param targetType
         *                   the target type to use (can <b>not</b> be {@code null}).
         * @return Returns a value for the specified column and target type.
         * @param <E>
         *            the target type.
         */
        <E> E getValue(@Nonnull String columnName, @Nonnull Class<E> targetType);

        /**
         * Gets the value of column for the specified {@code targetTape}.
         *
         * @param columnName
         *                     the name of column (can <b>not</b> be {@code null}).
         * @param targetType
         *                     the target type to use (can <b>not</b> be {@code null}).
         * @param defaultValue
         *                     default value if value is {@code null}).
         * @return Returns a value for the specified column and target type.
         * @param <E>
         *            the target type.
         */
        <E> E getValue(final @Nonnull String columnName, final @Nonnull Class<E> targetType, @Nullable E defaultValue);

        /**
         * @return Returns a map representing all values for each column.
         */
        @Nonnull
        Map<String, ValueMapper<?>> toMap();

        /**
         * @return Returns the number of value.
         */
        int size();

        /**
         * Gets the list of {@link ObjectMapper} for a specified group name.
         * <p>
         * <b>Note</b>: the group name must exist in the associated {@link GroupDescriptor}
         * </p>
         *
         * @param groupName
         *                  the group name (can <b>not</b> be {@code null}).
         * @return Returns the list of {@link ObjectMapper} for a specified group name.
         * @see GroupDescriptor#getName()
         */
        @Nonnull
        List<ObjectMapper> getObjectMappers(@Nonnull String groupName);

        /**
         * Gets the list of {@link ObjectMapper} for a specified group name.
         * <p>
         * <b>Note</b>: the group name must exist in the associated {@link GroupDescriptor}
         * </p>
         *
         * @param groupName
         *                  the group name (can <b>not</b> be {@code null}).
         * @return Returns the list of {@link ObjectMapper} for a specified group name.
         * @see GroupDescriptor#getName()
         */
        @Nonnull
        List<ObjectMapper> getObjectMappers(@Nonnull String groupName, boolean acceptDuplicate);

        /**
         * @param string
         * @return
         */
        boolean isSelected(@Nonnull String groupName);

    }

    /**
     * Represents the mapping of value on workbook excel.
     *
     * @author Christophe Friederich
     * @since 1.0
     * @param <T>
     *            the target type of value.
     */
    public interface ValueMapper<T> {

        /**
         * @return Returns the column name.
         */
        @Nonnull
        String getColumnName();

        /**
         * @return Returns the type of value.
         */
        @Nonnull
        Class<T> getTargetType();

        /**
         * @return Returns the value.
         */
        @Nullable
        T getValue();

        /**
         * Gets the value for the specified {@code targetType}.
         *
         * @param targetType
         *                   the type of value to return.
         * @return Returns the value with specified {@code targetType}.
         * @param <E>
         *            the target type.
         */
        @Nullable
        <E> E getValue(@Nonnull Class<E> targetType);

        /**
         * Gets the value for the specified {@code targetTape}.
         *
         * @param targetType
         *                     the target type to use (can <b>not</b> be {@code null}).
         * @param defaultValue
         *                     the default value to use if value doesn't exist.
         * @return Returns a value for the specified target type if exists, otherwise return default value.
         * @param <E>
         *            the target type.
         */
        @Nullable
        <E> E getValue(@Nonnull Class<E> targetType, @Nullable E defaultValue);
    }

}
