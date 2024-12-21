package com.pmi.tpd.euceg.core.excel;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pmi.tpd.euceg.core.internal.EucegExcelSchema;

@Immutable
public final class ListDescriptor extends ArrayList<GroupDescriptor> {

    /**     */
    private static final long serialVersionUID = 1L;

    /** */
    @Nonnull
    private final Map<String, GroupDescriptor> children;

    public ListDescriptor(final @Nonnull Collection<GroupDescriptor> children) {
        super(children);
        this.children = Collections.unmodifiableMap(Maps.uniqueIndex(flatten(children), GroupDescriptor::getName));
    }

    @Nonnull
    public Map<String, GroupDescriptor> getChildren() {
        return children;
    }

    /**
     * Gets the child group for the specified name.
     *
     * @param name
     *             the name of group child (can <b>not</b> be {@code null}).
     * @return Returns a {@link GroupDescriptor} for the specified name if exists, {@code null} otherwise.
     */
    @Nonnull
    public Optional<GroupDescriptor> get(@Nonnull final String name) {
        checkHasText(name, "name");
        return ofNullable(children.get(name));
    }

    @Nonnull
    public Stream<ColumnDescriptor<?>> getColumns(@Nonnull final ExcelSheet sheet) {
        checkNotNull(sheet, "sheet");
        return getGroupDescriptors(sheet).flatMap(g -> g.getColumns().stream());
    }

    @Nonnull
    public Stream<GroupDescriptor> getGroupDescriptors(@Nonnull final ExcelSheet sheet) {
        checkNotNull(sheet, "sheet");
        return this.children.values().stream().filter(i -> i.getSheet().getIndex() == sheet.getIndex());
    }

    @Override
    public boolean add(final GroupDescriptor e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(final int index, final GroupDescriptor element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends GroupDescriptor> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends GroupDescriptor> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GroupDescriptor remove(final int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void ensureCapacity(final int minCapacity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(final Predicate<? super GroupDescriptor> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void removeRange(final int fromIndex, final int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(final UnaryOperator<GroupDescriptor> operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GroupDescriptor set(final int index, final GroupDescriptor element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sort(final Comparator<? super GroupDescriptor> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param workbook
     * @return
     */
    @Nonnull
    Set<Sheet> getRequiredImportedSheets(@Nonnull final Workbook workbook) {
        return this.stream()
                .filter(g -> g.getSheet().isRequired())
                .map(g -> g.getSheet(workbook))
                .collect(Collectors.toSet());
    }

    @Nonnull
    Set<Sheet> getAvailableImportedSheets(@Nonnull final Workbook workbook) {
        return this.stream()
                .filter(g -> EucegExcelSchema.isImportedSheets(g.getSheet()))
                .map(g -> g.getSheet(workbook))
                .collect(Collectors.toSet());
    }

    private static Iterable<GroupDescriptor> flatten(final Collection<GroupDescriptor> elements) {
        final List<GroupDescriptor> l = Lists.newArrayList();
        elements.forEach(i -> aggregator(i, l));
        return l;
    }

    private static void aggregator(final GroupDescriptor group, final List<GroupDescriptor> list) {
        list.add(group);
        if (!group.getChildren().isEmpty()) {
            group.getChildren().values().forEach(i -> aggregator(i, list));
        }
    }
}
