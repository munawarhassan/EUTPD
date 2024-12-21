package com.pmi.tpd.euceg.core.excel;

import static com.pmi.tpd.euceg.core.excel.ColumnDescriptor.createColumn;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eu.ceg.SubmitterType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.io.Closeables;
import com.pmi.tpd.euceg.core.excel.ExcelMapper.ObjectMapper;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class ExcelMapperTest extends MockitoTestCase {

    private static final ExcelSheet SUBMITTER_SHEET = ExcelSheet.create("Submitter", 0, true);

    private static final ExcelSheet PARENT_SHEET = ExcelSheet.create("Parent", 1);

    private static final ExcelSheet AFFILIATE_SHEET = ExcelSheet.create("Affiliate", 2);

    private static final ListDescriptor ROOT = new ListDescriptor(Arrays.asList(
        GroupDescriptor.builder("Submitter", SUBMITTER_SHEET)
                .keys(createColumn("Submitter_ID", String.class))
                .columns(createColumn("Company_Name", String.class),
                    createColumn("Submitter_SME", Boolean.class),
                    createColumn("Submitter_Has_VAT", Boolean.class),
                    createColumn("Submitter_VAT", String.class),
                    createColumn("Submitter_Type", SubmitterType.class),
                    createColumn("Company_Address", String.class),
                    createColumn("Company_Country", String.class),
                    createColumn("Company_Phone", String.class),
                    createColumn("Company_Email", String.class),
                    createColumn("Submitter_Has_Parent_Company", Boolean.class),
                    createColumn("Submitter_Has_affiliate_company", Boolean.class),
                    createColumn("Submitter_Appoints_Enterer", Boolean.class))
                .build(),
        GroupDescriptor.builder("Parent", PARENT_SHEET)
                .keys(createColumn("Submitter_ID", String.class))
                .columns(createColumn("Submitter_Parent_Has_ID", Boolean.class),
                    createColumn("Submitter_Parent_ID", String.class),
                    createColumn("Company_Name", String.class),
                    createColumn("Company_Address", String.class),
                    createColumn("Company_Country", String.class),
                    createColumn("Company_Phone", String.class),
                    createColumn("Company_Email", String.class))
                .build(),
        GroupDescriptor.builder("Affiliate", AFFILIATE_SHEET)
                .keys(createColumn("Submitter_ID", String.class))
                .columns(createColumn("Submitter_Affiliate_Has_ID", Boolean.class),
                    createColumn("Submitter_Affiliate_ID", String.class),
                    createColumn("Company_Name", String.class),
                    createColumn("Company_Address", String.class),
                    createColumn("Company_Country", String.class),
                    createColumn("Company_Phone", String.class),
                    createColumn("Company_Email", String.class))
                .build()));

    @Test
    public void shouldGroupsHaveCorrectKeys() {
        final GroupDescriptor root = GroupDescriptor.builder("Group", ExcelSheet.create("noname", 0))
                .keys(createColumn("KEY", String.class))
                .columns(createColumn("COL1", String.class), createColumn("COL2", String.class))
                .child(GroupDescriptor.builder("Sub", ExcelSheet.create("noname", 0))
                        .keys(createColumn("KEY_SUB", String.class))
                        .columns(createColumn("KEY2", String.class),
                            createColumn("COL2_1", String.class),
                            createColumn("COL2_2", String.class),
                            createColumn("COL2_3", String.class))
                        .child(GroupDescriptor.builder("CHILD", ExcelSheet.create("noname", 0))
                                .keys(createColumn("KEY_CHILD_1", String.class),
                                    createColumn("KEY_CHILD_2", String.class))
                                .build())
                        .build())
                .build();
        assertThat(Iterables.transform(root.getForeignKeyColumns(), toColumnName()), Matchers.contains("KEY"));
        assertThat(Iterables.transform(root.getChildren("Sub").getForeignKeyColumns(), toColumnName()),
            Matchers.contains("KEY", "KEY_SUB"));
        assertThat(
            Iterables.transform(root.getChildren("Sub").getChildren("CHILD").getForeignKeyColumns(), toColumnName()),
            Matchers.contains("KEY", "KEY_SUB", "KEY_CHILD_1", "KEY_CHILD_2"));
    }

    private Function<ColumnDescriptor<?>, String> toColumnName() {
        return ColumnDescriptor::getName;
    }

    @Test
    public void mapSingleRowWithMultipleReferencesHSSF() throws Exception {

        final InputStream in = getResourceAsStream(this.getClass(), "excel-simple.xls");
        mapSingleRowWithMultipleReferences(in);
    }

    @Test
    public void mapSingleRowWithMultipleReferencesXSSF() throws Exception {

        final InputStream in = getResourceAsStream(this.getClass(), "excel-simple.xlsx");
        mapSingleRowWithMultipleReferences(in);
    }

    @Test
    public void mapMultipleObjects() throws Exception {

        final InputStream in = getResourceAsStream(this.getClass(), "excel-more.xls");
        try (final Workbook workbook = WorkbookFactory.create(in)) {

            final ExcelMapper excelMapper = new ExcelMapper();

            final Map<String, ObjectMapper> submitterValues = excelMapper.toMap(workbook, ROOT);
            assertEquals(2, submitterValues.size());

            final ObjectMapper submitter1 = submitterValues.get("99962");
            assertEquals(13, submitter1.size(), "the number od columns");

            assertEquals("99962", submitter1.get("Submitter_ID").getValue());

            final List<ObjectMapper> parent1List = submitter1.getObjectMappers("Parent");
            assertEquals(1, parent1List.size(), "only one parent");

            final ObjectMapper parent1 = Iterables.getFirst(parent1List, null);
            assertEquals(8, parent1.size(), "the number od columns");

            final ObjectMapper submitter2 = submitterValues.get("12345");
            assertEquals(13, submitter2.size(), "the number od columns");

            assertEquals("12345", submitter2.get("Submitter_ID").getValue());

            final List<ObjectMapper> parent2List = submitter1.getObjectMappers("Parent");
            assertEquals(1, parent2List.size(), "only one parent");

            final ObjectMapper parent2 = Iterables.getFirst(parent2List, null);
            assertEquals(8, parent2.size(), "the number od columns");

            final List<ObjectMapper> affiliate1List = submitter1.getObjectMappers("Affiliate");
            assertEquals(23, affiliate1List.size(), "only 23 affiliate");

            final List<ObjectMapper> affiliate2List = submitter2.getObjectMappers("Affiliate");
            assertEquals(5, affiliate2List.size(), "only 5 affiliate");

        } finally {
            Closeables.closeQuietly(in);
        }
    }

    @Test
    public void subGroupWithOwnKey() throws Exception {
        final InputStream in = getResourceAsStream(this.getClass(), "sub-group.xls");
        assumeTrue(in != null, "File should exist");
        try (final Workbook workbook = WorkbookFactory.create(in)) {

            final ExcelMapper excelMapper = new ExcelMapper();

            final ListDescriptor root = new ListDescriptor(Arrays.asList(
                GroupDescriptor.builder("Group", ExcelSheet.create("noname", 0))
                        .keys(createColumn("KEY", String.class))
                        .columns(createColumn("COL1", String.class), createColumn("COL2", String.class))
                        .build(),
                GroupDescriptor.builder("Sub", ExcelSheet.create("noname", 0))
                        .keys(createColumn("KEY", String.class))
                        .columns(createColumn("KEY2", String.class),
                            createColumn("COL2_1", String.class),
                            createColumn("COL2_2", String.class),
                            createColumn("COL2_3", String.class))
                        .build()));
            final Collection<ObjectMapper> values = excelMapper.build(workbook, root);
            assertEquals(2, values.size());

            final ObjectMapper mapper1 = Iterables.getFirst(values, null);
            final List<ObjectMapper> subValues = mapper1.getObjectMappers("Sub");
            assertEquals(3, subValues.size());

            assertEquals("COL2_1_1", subValues.get(0).get("COL2_1").getValue());
            assertEquals("COL2_1_2", subValues.get(1).get("COL2_1").getValue());
            assertEquals("COL2_1_3", subValues.get(2).get("COL2_1").getValue());

        } finally {
            Closeables.closeQuietly(in);
        }
    }

    @Test
    public void mapWithWrongColumnName() throws Exception {
        assertThrows(ExcelMappingException.class, () -> {
            final InputStream in = getResourceAsStream(this.getClass(), "excel-simple.xls");
            try (final Workbook workbook = WorkbookFactory.create(in)) {

                final ExcelMapper excelMapper = new ExcelMapper();

                final ListDescriptor root = new ListDescriptor(
                        Arrays.asList(GroupDescriptor.builder("Submitter", SUBMITTER_SHEET)
                                .keys(createColumn("Wrong_Submitter_ID", String.class))
                                .build()));
                excelMapper.build(workbook, root);
            } finally {
                Closeables.closeQuietly(in);
            }
        });
    }

    @Test
    public void getObjectMapperListWithWrongGroupName() throws Exception {
        assertThrows(RuntimeException.class, () -> {
            final InputStream in = getResourceAsStream(this.getClass(), "excel-simple.xls");
            try (final Workbook workbook = WorkbookFactory.create(in)) {

                final ExcelMapper excelMapper = new ExcelMapper();

                final ListDescriptor root = new ListDescriptor(
                        Arrays.asList(GroupDescriptor.builder("Submitter", SUBMITTER_SHEET)
                                .keys(createColumn("Submitter_ID", String.class))
                                .build()));
                final Collection<ObjectMapper> submitterValues = excelMapper.build(workbook, root);
                assertEquals(1, submitterValues.size());

                final ObjectMapper submitter = Iterables.getFirst(submitterValues, null);

                submitter.getObjectMappers("wrong group name");
            } finally {
                Closeables.closeQuietly(in);
            }
        });
    }

    @Test
    public void shouldReturnsNullRequiredSheets() {
        final Workbook workbook = mock(Workbook.class);
        final var actual = ExcelMapper.includeRequiredSheets(workbook, ROOT, null);
        assertNull(actual);
    }

    @Test
    public void shouldReturnsSelectedAndRequiredSheets() {
        final Workbook workbook = mock(Workbook.class);
        // required submitter sheet
        final Sheet sheet = mock(Sheet.class);

        when(workbook.getSheetAt(eq(SUBMITTER_SHEET.getIndex()))).thenReturn(sheet);

        var actual = ExcelMapper.includeRequiredSheets(workbook, ROOT, new int[] { 1, 2 });
        assertThat(actual, Matchers.contains(0, 1, 2));

        actual = ExcelMapper.includeRequiredSheets(workbook, ROOT, new int[] { 0, 2 });
        assertThat(actual, Matchers.contains(0, 2));
    }

    private void mapSingleRowWithMultipleReferences(final InputStream in) throws Exception {
        try (final Workbook workbook = WorkbookFactory.create(in)) {

            final ExcelMapper excelMapper = new ExcelMapper();

            final Collection<ObjectMapper> submitterValues = excelMapper.build(workbook, ROOT);
            assertEquals(1, submitterValues.size());

            final ObjectMapper submitter = Iterables.getFirst(submitterValues, null);
            assertEquals(13, submitter.size(), "the number od columns");

            assertEquals("99962", submitter.get("Submitter_ID").getValue());
            assertEquals("Philip Morris Products S.A.", submitter.get("Company_Name").getValue());
            assertEquals(false, submitter.get("Submitter_SME").getValue());
            assertEquals(true, submitter.get("Submitter_Has_VAT").getValue());
            assertEquals("CHE116276488TVA", submitter.get("Submitter_VAT").getValue());
            assertEquals(SubmitterType.MANUFACTURER, submitter.get("Submitter_Type").getValue());
            assertEquals("Quai Jeanrenaud 3, 2000 Neuchâtel", submitter.get("Company_Address").getValue());
            assertEquals("CH", submitter.get("Company_Country").getValue());
            assertEquals(" +41 (58) 242 3384", submitter.get("Company_Phone").getValue());
            assertEquals("ProductInformationReporting.PIR@pmi.com", submitter.get("Company_Email").getValue());
            assertEquals(true, submitter.get("Submitter_Has_Parent_Company").getValue());
            assertEquals(true, submitter.get("Submitter_Has_affiliate_company").getValue());
            assertEquals(false, submitter.get("Submitter_Appoints_Enterer").getValue());

            final List<ObjectMapper> parentList = submitter.getObjectMappers("Parent");
            assertEquals(1, parentList.size(), "only one parent");

            final ObjectMapper parent = Iterables.getFirst(parentList, null);
            assertEquals(8, parent.size(), "the number od columns");

            assertEquals("99962", parent.get("Submitter_ID").getValue());
            assertEquals(false, parent.get("Submitter_Parent_Has_ID").getValue());
            assertNull(parent.get("Submitter_Parent_ID").getValue());
            assertEquals("PHILIP MORRIS PRODUCTS S.A.", parent.get("Company_Name").getValue());
            assertEquals("Quai Jeanrenaud 3, 2000 Neuchâtel", parent.get("Company_Address").getValue());
            assertEquals("CH", parent.get("Company_Country").getValue());
            assertEquals(" +41 (58) 242 3384", parent.get("Company_Phone").getValue());
            assertEquals("ProductInformationReporting.PIR@pmi.com", parent.get("Company_Email").getValue());

            final List<ObjectMapper> affiliateList = submitter.getObjectMappers("Affiliate");
            assertEquals(23, affiliateList.size(), "only 23 affiliate");

        } finally {
            Closeables.closeQuietly(in);
        }
    }
}
