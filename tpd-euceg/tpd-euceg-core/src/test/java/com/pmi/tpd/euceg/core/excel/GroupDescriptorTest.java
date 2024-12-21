package com.pmi.tpd.euceg.core.excel;

import static com.pmi.tpd.euceg.core.excel.ColumnDescriptor.createColumn;

import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class GroupDescriptorTest extends TestCase {

    @Test
    public void testNameIsRequired() {
        assertThrowsWithMessage(IllegalArgumentException.class,
            () -> GroupDescriptor.builder().build(),
            "name parameter is required");
        assertThrows(IllegalArgumentException.class, () -> GroupDescriptor.builder(null, null).build());
    }

    @Test
    public void shouldBeImmutable() {
        final GroupDescriptor grp = GroupDescriptor.builder("grp", ExcelSheet.create("sheet", 0))
                .keys(createColumn("key1", String.class), createColumn("key2", String.class))
                .columns(createColumn("col1", String.class),
                    createColumn("col2", String.class),
                    createColumn("col3", String.class),
                    createColumn("col4", String.class))
                .build();
        assertThrows(UnsupportedOperationException.class, () -> grp.getChildren().put("test", grp));
        assertThrows(UnsupportedOperationException.class,
            () -> grp.getColumns().add(createColumn("col5", String.class)));
        assertThrows(UnsupportedOperationException.class,
            () -> grp.getForeignKeyColumns().add(createColumn("col5", String.class)));
        assertThrows(UnsupportedOperationException.class,
            () -> grp.getPrimaryKeyColumns().add(createColumn("key3", String.class)));
    }

    @Test
    public void shouldColumnsKeepOrdering() {
        final GroupDescriptor grp = GroupDescriptor.builder("grp", ExcelSheet.create("sheet", 0))
                .keys(createColumn("key1", String.class), createColumn("key2", String.class))
                .columns(createColumn("col1", String.class),
                    createColumn("col2", String.class),
                    createColumn("col3", String.class),
                    createColumn("col4", String.class))
                .build();
        assertEquals(6, grp.getColumns().size());
        assertThat(grp.getColumns().stream().map(ColumnDescriptor::getName).collect(Collectors.toList()),
            Matchers.contains("key1", "key2", "col1", "col2", "col3", "col4"));
    }

    @Test
    public void shouldBuildForeignKeys() {
        final ExcelSheet sheet = ExcelSheet.create("sheet", 0);
        final GroupDescriptor subsub = GroupDescriptor.builder("subsub", sheet)
                .keys(createColumn("subsub_key1", String.class), createColumn("subsub_key2", String.class))
                .columns(createColumn("subsub_col1", String.class),
                    createColumn("subsub_col2", String.class),
                    createColumn("subsub_col3", String.class),
                    createColumn("subsub_col4", String.class))
                .build();

        final GroupDescriptor sub = GroupDescriptor.builder("sub", sheet)
                .keys(createColumn("sub_key1", String.class), createColumn("sub_key2", String.class))
                .columns(createColumn("sub_col1", String.class),
                    createColumn("sub_col2", String.class),
                    createColumn("sub_col3", String.class),
                    createColumn("sub_col4", String.class))
                .child(subsub)
                .build();
        final GroupDescriptor grp = GroupDescriptor.builder("grp", sheet)
                .keys(createColumn("key1", String.class), createColumn("key2", String.class))
                .columns(createColumn("col1", String.class),
                    createColumn("col2", String.class),
                    createColumn("col3", String.class),
                    createColumn("col4", String.class))
                .child(sub)
                .build();
        assertEquals(2, grp.getForeignKeyColumns().size());
        assertThat(grp.getForeignKeyColumns().stream().map(ColumnDescriptor::getName).collect(Collectors.toList()),
            Matchers.contains("key1", "key2"));

        assertEquals(4, sub.getForeignKeyColumns().size());
        assertThat(sub.getForeignKeyColumns().stream().map(ColumnDescriptor::getName).collect(Collectors.toList()),
            Matchers.contains("key1", "key2", "sub_key1", "sub_key2"));

        assertEquals(6, subsub.getForeignKeyColumns().size());
        assertThat(subsub.getForeignKeyColumns().stream().map(ColumnDescriptor::getName).collect(Collectors.toList()),
            Matchers.contains("key1", "key2", "sub_key1", "sub_key2", "subsub_key1", "subsub_key2"));
    }

    @Test
    public void shouldIndicateColumnsAsPrimaryKey() {
        final GroupDescriptor grp = GroupDescriptor.builder("grp", ExcelSheet.create("sheet", 0))
                .keys(createColumn("key1", String.class), createColumn("key2", String.class))
                .columns(createColumn("col1", String.class),
                    createColumn("col2", String.class),
                    createColumn("col3", String.class),
                    createColumn("col4", String.class))
                .build();
        assertEquals(true, grp.getPrimaryKeyColumns().stream().allMatch(ColumnDescriptor::isPrimaryKey));
        assertEquals(true,
            grp.getColumns()
                    .stream()
                    .filter(c -> c.getName().startsWith("key"))
                    .allMatch(ColumnDescriptor::isPrimaryKey));
        assertEquals(false,
            grp.getColumns()
                    .stream()
                    .filter(c -> c.getName().startsWith("col"))
                    .allMatch(ColumnDescriptor::isPrimaryKey));
    }

}
