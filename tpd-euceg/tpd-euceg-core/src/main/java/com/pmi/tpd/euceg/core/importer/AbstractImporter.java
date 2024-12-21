package com.pmi.tpd.euceg.core.importer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.AttachmentRef;
import org.eu.ceg.Company;
import org.eu.ceg.CountryValue;
import org.eu.ceg.Manufacturer;
import org.eu.ceg.Manufacturer.ProductionSiteAddresses;
import org.eu.ceg.Product.Manufacturers;
import org.eu.ceg.ProductionSiteAddress;
import org.eu.ceg.ToxicologicalDataAvailableEnum;
import org.eu.ceg.ToxicologicalDetails;
import org.eu.ceg.ToxicologicalDetails.ToxAddictiveFiles;
import org.eu.ceg.ToxicologicalDetails.ToxCardioPulmonaryFiles;
import org.eu.ceg.ToxicologicalDetails.ToxCmrFiles;
import org.eu.ceg.ToxicologicalDetails.ToxEmissionFiles;
import org.eu.ceg.ToxicologicalDetails.ToxOtherFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.core.excel.ExcelMapper.ObjectMapper;

/**
 * @author Christophe Friederich
 * @since 1.0
 * @param <T>
 *            the type of result objects of import.
 */
public abstract class AbstractImporter<T> implements IImporter<T> {

    /** */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *
     */
    private final I18nService i18nService;

    /**
     * @param i18nService
     *                    a localisation service.
     */
    public AbstractImporter(@Nonnull final I18nService i18nService) {
        this.i18nService = Preconditions.checkNotNull(i18nService);
    }

    protected I18nService getI18nService() {
        return i18nService;
    }

    /**
     * @param submitterId
     * @throws EucegImportException
     */
    @SuppressWarnings("null")
    protected void checkSubmitterExists(@Nullable final String submitterId) throws EucegImportException {
        if (Strings.isNullOrEmpty(submitterId) || !submitterExists(submitterId)) {
            throw new EucegImportException(
                    i18nService.createKeyedMessage("app.euceg.import.submitter.required", submitterId));
        }
    }

    /**
     * @param submitterId
     * @return
     */
    protected boolean submitterExists(@Nonnull final String submitterId) {
        return true;
    }

    /**
     * @param objectMapper
     * @return
     */
    @SuppressWarnings("null")
    @Nonnull
    protected String getProductNumber(final ObjectMapper objectMapper) {

        final String productNumber = objectMapper.getValue("TPD_Product_Number", String.class);
        if (Strings.isNullOrEmpty(productNumber)) {
            throw new EucegImportException(i18nService.createKeyedMessage("app.euceg.import.productnumber.required"));
        }
        return productNumber.trim();
    }

    /**
     * @param objectMapper
     * @return
     */
    protected String getPreviousProductNumber(final ObjectMapper objectMapper) {
        final String value = objectMapper.getValue("Previous_TPD_Product_Number", String.class);
        if (value != null) {
            return value.trim();
        }
        return value;
    }

    /**
     * @param mapper
     * @param companyIdKey
     * @param confidential
     * @return
     */
    protected Company createCompany(@Nonnull final ObjectMapper mapper,
        @Nonnull final String companyIdKey,
        final boolean confidential) {
        return new Company().withConfidential(confidential)
                .withName(trim(mapper.get("Company_Name").getValue(String.class)))
                .withAddress(removeLinefeed(trim(mapper.get("Company_Address").getValue(String.class))))
                .withCountry(mapper.get("Company_Country").getValue(CountryValue.class))
                .withPhoneNumber(trim(mapper.get("Company_Phone").getValue(String.class)))
                .withEmail(trim(mapper.get("Company_Email").getValue(String.class)))
                .withSubmitterID(mapper.get(companyIdKey).getValue(String.class));
    }

    protected Company createCompany(@Nonnull final ObjectMapper mapper, @Nonnull final String companyIdKey) {
        return createCompany(mapper, companyIdKey, false);
    }

    @Nullable
    @CheckForNull
    protected Manufacturers createManufacturers(@Nonnull final ObjectMapper objectMapper) {
        final List<ObjectMapper> mapperManufacturers = objectMapper.getObjectMappers("Manufacturer");
        if (mapperManufacturers.isEmpty()) {
            return null;
        }
        final List<Manufacturer> list = Lists.newArrayList();
        for (final ObjectMapper mapperManufacturer : mapperManufacturers) {

            list.add(new Manufacturer()
                    .withAddress(removeLinefeed(trim(mapperManufacturer.get("Company_Address").getValue(String.class))))
                    .withConfidential(false)
                    .withCountry(mapperManufacturer.get("Company_Country").getValue(CountryValue.class))
                    .withEmail(trim(mapperManufacturer.get("Company_Email").getValue(String.class)))
                    .withName(trim(mapperManufacturer.get("Company_Name").getValue(String.class)))
                    .withPhoneNumber(trim(mapperManufacturer.get("Company_Phone").getValue(String.class)))
                    .withSubmitterID(mapperManufacturer.get("Manufacturer_ID").getValue(String.class))
                    .withProductionSiteAddresses(createProductionSiteAddresses(mapperManufacturer)));
        }
        return new Manufacturers().withManufacturer(list);
    }

    @Nullable
    @CheckForNull
    protected ProductionSiteAddresses createProductionSiteAddresses(@Nonnull final ObjectMapper objectMapper) {
        final List<ObjectMapper> mapperSites = objectMapper.getObjectMappers("ProductionSite");
        if (mapperSites.isEmpty()) {
            return null;
        }
        final List<ProductionSiteAddress> list = Lists.newArrayList();
        for (final ObjectMapper mapperSite : mapperSites) {
            list.add(new ProductionSiteAddress()
                    .withAddress(removeLinefeed(trim(mapperSite.get("Production_Site_Address").getValue(String.class))))
                    .withConfidential(false)
                    .withCountry(mapperSite.get("Production_Site_Country").getValue(CountryValue.class))
                    .withEmail(trim(mapperSite.get("Production_Site_Email").getValue(String.class)))
                    .withPhoneNumber(trim(mapperSite.get("Production_Site_Phone").getValue(String.class))));
        }
        return new ProductionSiteAddresses().withProductionSiteAddress(list);
    }

    @Nullable
    @CheckForNull
    protected ToxicologicalDetails createToxicologicalDetails(@Nonnull final ObjectMapper mapper)
            throws EucegImportException {
        final boolean confidential = mapper.getValue("Confidential", Boolean.class);
        final List<AttachmentRef> emissionFiles = appendAttachments(mapper, "Ingredient_Tox_Emission_File");
        final List<AttachmentRef> cmrFiles = appendAttachments(mapper, "Ingredient_Tox_CMR_File");
        final List<AttachmentRef> cardioPulmonaryFiles = appendAttachments(mapper,
            "Ingredient_Tox_CardioPulmonary_File");
        final List<AttachmentRef> toxAddictiveFiles = appendAttachments(mapper, "Ingredient_Tox_Addictive_File");
        final List<AttachmentRef> toxOthers = appendAttachments(mapper, "Ingredient_Tox_Other_File");
        return new ToxicologicalDetails().withToxAddictive(Eucegs.toBoolean(!toxAddictiveFiles.isEmpty(), confidential))
                .withToxAddictiveFiles(
                    !toxAddictiveFiles.isEmpty() ? new ToxAddictiveFiles().withAttachment(toxAddictiveFiles) : null)
                .withToxCardioPulmonary(Eucegs.toBoolean(!cardioPulmonaryFiles.isEmpty(), confidential))
                .withToxCardioPulmonaryFiles(!cardioPulmonaryFiles.isEmpty()
                        ? new ToxCardioPulmonaryFiles().withAttachment(cardioPulmonaryFiles) : null)
                .withToxCmr(Eucegs.toBoolean(!cmrFiles.isEmpty(), confidential))
                .withToxCmrFiles(!cmrFiles.isEmpty() ? new ToxCmrFiles().withAttachment(cmrFiles) : null)
                .withToxEmission(Eucegs.toBoolean(!emissionFiles.isEmpty(), confidential))
                .withToxEmissionFiles(
                    !emissionFiles.isEmpty() ? new ToxEmissionFiles().withAttachment(emissionFiles) : null)
                .withToxicologicalDataAvailable(Eucegs.toxicologicalDataAvailable(
                    mapper.get("Ingredient_Tox_Data").getValue(ToxicologicalDataAvailableEnum.class),
                    confidential))
                .withToxOther(Eucegs.toBoolean(!toxOthers.isEmpty(), confidential))
                .withToxOtherFiles(!toxOthers.isEmpty() ? new ToxOtherFiles().withAttachment(toxOthers) : null);
    }

    /**
     * @param mapper
     * @param columnName
     * @return
     * @throws EucegImportException
     */
    @SuppressWarnings("null")
    @Nullable
    @CheckForNull
    protected AttachmentRef appendAttachment(@Nonnull final ObjectMapper mapper, @Nonnull final String columnName)
            throws EucegImportException {
        Preconditions.checkNotNull(mapper, "mappers is required");
        return Iterables.getFirst(appendAttachments(Arrays.asList(mapper), columnName), null);
    }

    /**
     * @param mapper
     * @param columnName
     * @return
     * @throws EucegImportException
     */
    @SuppressWarnings("null")
    @Nonnull
    protected List<AttachmentRef> appendAttachments(@Nonnull final ObjectMapper mapper,
        @Nonnull final String columnName) throws EucegImportException {
        Preconditions.checkNotNull(mapper, "mappers is required");
        final List<AttachmentRef> list = Lists.newArrayList();
        final String value = mapper.getValue(columnName, String.class);
        if (Strings.isNullOrEmpty(value)) {
            return list;
        }
        final String[] filenames = value.split(";");
        for (final String filename : filenames) {
            final String f = filename.trim();
            addAttachment(list, f);
        }
        return list;
    }

    /**
     * @param mappers
     * @param columnName
     * @return
     * @throws EucegImportException
     */
    @SuppressWarnings("null")
    @Nonnull
    protected List<AttachmentRef> appendAttachments(@Nonnull final List<ObjectMapper> mappers,
        @Nonnull final String columnName) throws EucegImportException {
        Preconditions.checkNotNull(mappers, "mappers");
        Preconditions.checkState(!Strings.isNullOrEmpty(columnName), "columnName can not be null or empty");
        final List<AttachmentRef> list = Lists.newArrayList();
        for (final ObjectMapper mapper : mappers) {
            addAttachment(list, mapper.get(columnName).getValue(String.class));
        }
        return list;
    }

    /**
     * @param attachmentRefs
     * @param filename
     * @throws EucegImportException
     */
    protected void addAttachment(@Nonnull final List<AttachmentRef> attachmentRefs, @Nullable final String filename)
            throws EucegImportException {
        if (Strings.isNullOrEmpty(filename)) {
            return;
        }
        final String uuid = getAttachmentId(filename);
        attachmentRefs.add(Eucegs.attachmentRef(uuid));
    }

    /**
     * @param filename
     * @return
     * @throws EucegImportException
     */
    protected String getAttachmentId(final String filename) throws EucegImportException {
        final String attachmentid = findAttachmentIdByFilename(filename);
        if (attachmentid == null) {
            throw new EucegImportException(
                    i18nService.createKeyedMessage("app.euceg.import.attachment.required", filename));
        }
        return attachmentid;
    }

    /**
     * @param filename
     * @return
     */
    protected abstract String findAttachmentIdByFilename(final String filename);

    /**
     * @param value
     * @return
     */
    @SuppressWarnings("null")
    @Nullable
    protected static String trim(@Nullable final String value) {
        return Strings.isNullOrEmpty(value) ? null : value.trim();
    }

    /**
     * @param value
     * @return
     */
    protected static String removeLinefeed(final String value) {
        return Strings.isNullOrEmpty(value) ? null : value.replaceAll("(\\r|\\n)", " ");
    }

    /**
     * @param submitterId
     * @return
     */
    protected static Optional<String> formatSubmitterId(final String submitterId) {
        if (Strings.isNullOrEmpty(submitterId)) {
            return Optional.empty();
        }
        return Optional.of(String.format("%05d", Integer.parseInt(submitterId)));
    }
}
