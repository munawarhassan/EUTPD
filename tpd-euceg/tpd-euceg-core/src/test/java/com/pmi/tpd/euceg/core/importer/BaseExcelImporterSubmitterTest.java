package com.pmi.tpd.euceg.core.importer;

import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.Matchers.contains;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.PropertyException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.core.EucegSubmitter;
import com.pmi.tpd.euceg.core.excel.ColumnDescriptor;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;
import com.pmi.tpd.testing.junit5.TestCase;

public class BaseExcelImporterSubmitterTest extends TestCase {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseExcelImporterSubmitterTest.class);

    @BeforeEach
    public void forceIndent() throws PropertyException {
        Eucegs.indentMarshalling(true);
    }

    @AfterEach
    public void restoreIndentMarshalling() throws PropertyException {
        Eucegs.indentMarshalling(false);
    }

    @Test
    public void shouldSupplierGroupsHaveCorrectKeys() {
        final ListDescriptor root = BaseExcelImporterSubmitter.DESCRIPTORS;
        assertThat(transform(root.get("Submitter").orElseThrow().getForeignKeyColumns(), toColumnName()),
            contains("Submitter_ID"));

        assertThat(transform(root.get("Parent").orElseThrow().getForeignKeyColumns(), toColumnName()),
            contains("Submitter_ID"));

        assertThat(transform(root.get("Affiliate").orElseThrow().getForeignKeyColumns(), toColumnName()),
            contains("Submitter_ID"));
    }

    private Function<ColumnDescriptor<?>, String> toColumnName() {
        return c -> c.getName();
    }

    /**
     * Simple non-regression test.
     */
    @Test
    public void importOnlyOneOkSubmitter() throws IOException, Exception {

        try (InputStream in = getResourceAsStream(this.getClass(), "submitter-valid.xls")) {

            final IImporterResult<EucegSubmitter> result = new SimpleImporterSubmitter(new SimpleI18nService())
                    .importFromExcel(in, null);

            if (!result.getValidationResult().isEmpty()) {
                LOGGER.error(result.getValidationResult().toString());
                fail("the import has failed");
            }
            final List<EucegSubmitter> list = result.getResults();
            assertEquals(1, list.size(), "Only one submitter must exist");

            final EucegSubmitter info = Iterables.getFirst(list, null);

            final String xmlSubmitter = Eucegs.marshal(Eucegs.wrap(info.getSubmitter()));
            approve("submitter", xmlSubmitter);

            final String xmlSubmitterDetails = Eucegs.marshal(info.getSubmitterDetails());
            approve("details", xmlSubmitterDetails);
        }
    }

    public static class SimpleImporterSubmitter extends BaseExcelImporterSubmitter {

        public SimpleImporterSubmitter(final I18nService i18nService) {
            super(i18nService);
        }

        @Override
        protected String findAttachmentIdByFilename(final String filename) {
            return null;
        }

    }

}
