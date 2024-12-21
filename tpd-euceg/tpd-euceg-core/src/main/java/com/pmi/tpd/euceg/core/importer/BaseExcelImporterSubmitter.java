package com.pmi.tpd.euceg.core.importer;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.euceg.api.Eucegs.toBoolean;
import static com.pmi.tpd.euceg.core.excel.ColumnDescriptor.createColumn;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eu.ceg.Company;
import org.eu.ceg.CountryValue;
import org.eu.ceg.Submitter;
import org.eu.ceg.Submitter.Affiliates;
import org.eu.ceg.SubmitterDetails;
import org.eu.ceg.SubmitterType;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.euceg.core.EucegSubmitter;
import com.pmi.tpd.euceg.core.excel.ExcelMapper;
import com.pmi.tpd.euceg.core.excel.ExcelMapper.ObjectMapper;
import com.pmi.tpd.euceg.core.excel.ExcelSheet;
import com.pmi.tpd.euceg.core.excel.GroupDescriptor;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;
import com.pmi.tpd.euceg.core.util.validation.SimpleValidationFailure;
import com.pmi.tpd.euceg.core.util.validation.ValidationResult;

/**
 * The Base class to import submitter.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public abstract class BaseExcelImporterSubmitter extends AbstractImporter<EucegSubmitter> {

    /** */
    private static final ExcelSheet SUBMITTER_SHEET = ExcelSheet.create("Submitter", 0);

    /** */
    private static final ExcelSheet PARENT_SHEET = ExcelSheet.create("Parent", 1);

    /** */
    private static final ExcelSheet AFFILIATE_SHEET = ExcelSheet.create("Affiliate", 2);

    static ExcelSheet[] SHEETS = { SUBMITTER_SHEET, PARENT_SHEET, AFFILIATE_SHEET };

    /** */
    static final ListDescriptor DESCRIPTORS = new ListDescriptor(Arrays.asList(
        GroupDescriptor.builder("Submitter", SUBMITTER_SHEET)
                .keys(createColumn("Submitter_ID", String.class))
                .columns(createColumn("Company_Name", String.class),
                    createColumn("Submitter_SME", Boolean.class),
                    createColumn("Submitter_Has_VAT", Boolean.class),
                    createColumn("Submitter_VAT", String.class),
                    createColumn("Submitter_Type", SubmitterType.class),
                    createColumn("Company_Address", String.class),
                    createColumn("Company_Country", CountryValue.class),
                    createColumn("Company_Phone", String.class),
                    createColumn("Company_Email", String.class),
                    createColumn("Submitter_Has_Parent_Company", Boolean.class),
                    createColumn("Submitter_Has_affiliate_company", Boolean.class),
                    createColumn("Submitter_Appoints_Enterer", Boolean.class))
                .build(),
        GroupDescriptor.builder("Parent", PARENT_SHEET)
                .keys(createColumn("Submitter_ID", String.class))
                .columns(createColumn("Submitter_Parent_ID", String.class),
                    createColumn("Company_Name", String.class),
                    createColumn("Company_Address", String.class),
                    createColumn("Company_Country", CountryValue.class),
                    createColumn("Company_Phone", String.class),
                    createColumn("Company_Email", String.class))
                .build(),
        GroupDescriptor.builder("Affiliate", AFFILIATE_SHEET)
                .keys(createColumn("Submitter_ID", String.class))
                .columns(createColumn("Submitter_Affiliate_ID", String.class),
                    createColumn("Company_Name", String.class),
                    createColumn("Company_Address", String.class),
                    createColumn("Company_Country", CountryValue.class),
                    createColumn("Company_Phone", String.class),
                    createColumn("Company_Email", String.class))
                .build()));

    /**
     * @param i18nService
     *                    a localisation service.
     */
    public BaseExcelImporterSubmitter(final @Nonnull I18nService i18nService) {
        super(i18nService);
    }

    @Override
    public @Nonnull IImporterResult<EucegSubmitter> importFromExcel(final @Nonnull InputStream excelFile,
        @Nullable final int[] sheets) {
        checkNotNull(excelFile, "excelFile");

        @SuppressWarnings("null")
        final @Nonnull List<EucegSubmitter> submitters = Lists.newArrayList();
        final ValidationResult validationResult = new ValidationResult();
        final ExcelMapper mapper = new ExcelMapper();

        try (@SuppressWarnings("null")
        @Nonnull
        Workbook workbook = WorkbookFactory.create(excelFile)) {

            final Collection<ObjectMapper> objectMappers = mapper.build(workbook, DESCRIPTORS);

            for (final ObjectMapper objectMapper : objectMappers) {
                final SubmitterDetails submitterDetails = createSubmitterDetails(objectMapper);
                final Submitter submitter = createSubmitter(objectMapper);
                // Submitters
                submitters.add(EucegSubmitter.builder()
                        .submitter(submitter)
                        .submitterDetails(submitterDetails)
                        .name(submitterDetails.getName())
                        .submitterId(submitter.getSubmitterID())
                        .build());
            }
        } catch (final Exception e) {
            logger.error("Import ecig has failed", e);
            validationResult.addFailure(new SimpleValidationFailure(this, e.getMessage()));
        }

        return new ImportResultImpl<>(submitters, validationResult);
    }

    /**
     * Create a submitter details instance.
     *
     * @param objectMapper
     *                     the root object mapper.
     * @return Returns new instance of {@link SubmitterDetails}.
     */
    protected SubmitterDetails createSubmitterDetails(final ObjectMapper objectMapper) {
        final String phoneNumber = Strings.isNullOrEmpty(objectMapper.getValue("Company_Phone", String.class)) ? null
                : objectMapper.getValue("Company_Phone", String.class).trim();
        return new SubmitterDetails().withName(objectMapper.getValue("Company_Name", String.class))
                .withAddress(objectMapper.getValue("Company_Address", String.class))
                .withCountry(objectMapper.getValue("Company_Country", CountryValue.class))
                .withPhoneNumber(phoneNumber)
                .withEmail(objectMapper.getValue("Company_Email", String.class))
                .withSme(objectMapper.getValue("Submitter_SME", Boolean.class))
                .withHasVatNumber(objectMapper.getValue("Submitter_Has_VAT", Boolean.class))
                .withVatNumber(objectMapper.getValue("Submitter_VAT", String.class));
    }

    /**
     * Create a submitter instance.
     *
     * @param objectMapper
     *                     the root object mapper.
     * @return Returns new instance of {@link Submitter}.
     */
    protected Submitter createSubmitter(final ObjectMapper objectMapper) {
        final Affiliates affiliates = createAffiliates(objectMapper);
        final Company parentCompany = createParentCompany(objectMapper);
        final Company naturalLegalRepresentative = createNaturalLegalRepresentative(objectMapper);

        return new Submitter().withConfidential(false)
                .withSubmitterID(formatSubmitterId(objectMapper.getValue("Submitter_ID", String.class)).orElse(null))
                .withSubmitterType(objectMapper.getValue("Submitter_Type", SubmitterType.class))
                .withHasParent(parentCompany != null)
                .withParent(parentCompany)
                .withHasNaturalLegalRepresentative(toBoolean(naturalLegalRepresentative != null))
                .withNaturalLegalRepresentative(naturalLegalRepresentative)
                .withHasAffiliates(affiliates.getAffiliate().size() > 0)
                .withHasEnterer(false)
                .withAffiliates(affiliates);

    }

    private Company createParentCompany(final ObjectMapper objectMapper) {
        final List<ObjectMapper> list = objectMapper.getObjectMappers("Parent");
        final ObjectMapper mapper = Iterables.getFirst(list, null);

        if (mapper == null) {
            return null;
        }

        return createCompany(mapper, "Submitter_Parent_ID");
    }

    private Company createNaturalLegalRepresentative(final ObjectMapper objectMapper) {
        final List<ObjectMapper> list = objectMapper.getObjectMappers("Parent");
        final ObjectMapper mapper = Iterables.getFirst(list, null);

        if (mapper == null) {
            return null;
        }

        return null;
    }

    private Affiliates createAffiliates(final ObjectMapper objectMapper) {
        final List<Company> allAffiliates = Lists.newArrayList();
        final List<ObjectMapper> list = objectMapper.getObjectMappers("Affiliate");

        for (final ObjectMapper mapper : list) {
            allAffiliates.add(createCompany(mapper, "Submitter_Affiliate_ID"));

        }
        return new Affiliates().withAffiliate(allAffiliates);
    }

}
