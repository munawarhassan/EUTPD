package com.pmi.tpd.euceg.core.excel;

import static com.pmi.tpd.euceg.core.excel.ColumnDescriptor.createColumn;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class ListDescriptorTest extends MockitoTestCase {

    @Test
    public void shouldColumnsKeepOrdering() {
        final ExcelSheet sheet = ExcelSheet.create("sheet", 0);
        final GroupDescriptor grp = GroupDescriptor.builder("grp", sheet)
                .keys(createColumn("key1", String.class), createColumn("key2", String.class))
                .columns(createColumn("col1", String.class),
                    createColumn("col2", String.class),
                    createColumn("col3", String.class),
                    createColumn("col4", String.class))
                .child(GroupDescriptor.builder("sub", sheet)
                        .keys(createColumn("sub_key1", String.class), createColumn("sub_key2", String.class))
                        .columns(createColumn("sub_col1", String.class),
                            createColumn("sub_col2", String.class),
                            createColumn("sub_col3", String.class),
                            createColumn("sub_col4", String.class))
                        .build())
                .build();
        final ListDescriptor list = new ListDescriptor(Arrays.asList(grp));
        final List<ColumnDescriptor<?>> cols = list.getColumns(sheet).collect(Collectors.toList());
        assertEquals(12, cols.size());
        assertThat(cols.stream().map(ColumnDescriptor::getName).collect(Collectors.toList()),
            Matchers.contains("key1",
                "key2",
                "col1",
                "col2",
                "col3",
                "col4",
                "sub_key1",
                "sub_key2",
                "sub_col1",
                "sub_col2",
                "sub_col3",
                "sub_col4"));
    }
}
