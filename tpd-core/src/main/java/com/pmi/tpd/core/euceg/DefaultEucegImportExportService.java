package com.pmi.tpd.core.euceg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.eu.ceg.Attachment;
import org.eu.ceg.EcigAnnualSalesData;
import org.eu.ceg.EcigPresentation;
import org.eu.ceg.EcigProduct;
import org.eu.ceg.Product;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoAnnualSalesData;
import org.eu.ceg.TobaccoPresentation;
import org.eu.ceg.TobaccoProduct;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.euceg.ProductDiffRequest.ProductDiffItem;
import com.pmi.tpd.core.euceg.ProductDiffRequest.ProductDiffRequestBuilder;
import com.pmi.tpd.core.euceg.event.ProductImportEvent;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ITransmitReceiptEntity;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SubmitterStatus;
import com.pmi.tpd.euceg.core.EucegProduct;
import com.pmi.tpd.euceg.core.EucegSubmitter;
import com.pmi.tpd.euceg.core.filestorage.IFileStorage;
import com.pmi.tpd.euceg.core.filestorage.IFileStorageFile;
import com.pmi.tpd.euceg.core.importer.BaseExcelImporterEcigaretteProducts;
import com.pmi.tpd.euceg.core.importer.BaseExcelImporterSubmitter;
import com.pmi.tpd.euceg.core.importer.BaseExcelmporterTobaccoProducts;
import com.pmi.tpd.euceg.core.importer.IImporterResult;
import com.pmi.tpd.euceg.core.support.EucegXmlDiff;
import com.pmi.tpd.euceg.core.support.EucegXmlDiff.DiffResult;
import com.pmi.tpd.euceg.core.util.validation.ValidationResult;

import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipOutputStream;

/**
 * @author christophe friederich
 * @since 2.5
 */
@Singleton
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class DefaultEucegImportExportService implements IEucegImportExportService {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEucegImportExportService.class);

    /** */
    @Nonnull
    private final IFileStorage fileStorage;

    /** */
    @Nonnull
    private final IAttachmentStore attachmentStore;

    /** */
    @Nonnull
    private final ISubmitterStore submitterStore;

    /** */
    @Nonnull
    private final IProductStore productStore;

    /** */
    @Nonnull
    private final IProductSubmissionStore productSubmissionStore;

    /** */
    @Nonnull
    private final I18nService i18nService;

    /** */
    @Nonnull
    private final IEventPublisher publisher;

    public DefaultEucegImportExportService(@Nonnull final IAttachmentStore attachmentStore,
            @Nonnull final IFileStorage fileStorage, @Nonnull final ISubmitterStore submitterStore,
            @Nonnull final IProductStore productStore, @Nonnull final IProductSubmissionStore productSubmissionStore,
            @Nonnull final I18nService i18nService, @Nonnull final IEventPublisher publisher) {
        super();
        this.fileStorage = Assert.checkNotNull(fileStorage, "fileStorage");
        this.attachmentStore = Assert.checkNotNull(attachmentStore, "attachmentStore");
        this.submitterStore = Assert.checkNotNull(submitterStore, "submitterStore");
        this.productStore = Assert.checkNotNull(productStore, "productStore");
        this.productSubmissionStore = Assert.checkNotNull(productSubmissionStore, "productSubmissionStore");
        this.i18nService = Assert.checkNotNull(i18nService, "i18nService");
        this.publisher = Assert.checkNotNull(publisher, "publisher");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Transactional
    public @Nonnull ValidationResult importProductFromExcel(@Nonnull final InputStream excelFile,
        @Nonnull final String sourceFileName,
        @Nonnull final ProductType fileProductType,
        @Nullable final int[] selectedSheets,
        final boolean keepSaleHistory) {
        Assert.checkNotNull(excelFile, "excelFile");
        Assert.checkNotNull(sourceFileName, "sourceFileName");
        Assert.checkNotNull(fileProductType, "fileProductType");

        final IImporterResult<EucegProduct> result = importFromExcel(excelFile, fileProductType, selectedSheets);

        if (result.getValidationResult().hasFailures()) {
            return result.getValidationResult();
        }
        final List<EucegProduct> products = result.getResults();

        if (!products.isEmpty()) {
            // Import Product
            for (final EucegProduct item : products) {
                ProductEntity child = null;
                if (Objects.equal(item.getProductNumber(), item.getPreviousProductNumber())) {
                    throw new EucegException(i18nService.createKeyedMessage(
                        "app.service.euceg.submission.import.previousProductNumber.productNumber.identical",
                        item.getPreviousProductNumber(),
                        item.getProductNumber()));
                }
                if (!Strings.isNullOrEmpty(item.getPreviousProductNumber())) {
                    child = this.productStore.find(item.getPreviousProductNumber());
                    if (child == null) {
                        throw new EucegException(i18nService.createKeyedMessage(
                            "app.service.euceg.submission.import.previousProductNumber.required",
                            item.getPreviousProductNumber(),
                            item.getProductNumber()));
                    }
                    if (this.productStore.hasChildWithAnotherProduct(item.getPreviousProductNumber(),
                        item.getProductNumber())) {
                        throw new EucegException(i18nService.createKeyedMessage(
                            "app.service.euceg.submission.import.previousProductNumber.duplicate",
                            item.getPreviousProductNumber()));
                    }
                }
                final SubmissionTypeEnum submissionType = item.getPreferredSubmissionType() == null
                        ? SubmissionTypeEnum.NEW : item.getPreferredSubmissionType();

                if (this.productStore.exists(item.getProductNumber())) {
                    // Update Product
                    ProductEntity entity = this.productStore.get(item.getProductNumber());
                    final String newGeneralComment = item.getGeneralComment();
                    final String originalGeneralComment = entity.getPreferredGeneralComment();
                    // not update if products are equal
                    if (Objects.equal(entity.getProduct(), item.getProduct())
                            && Objects.equal(entity.getPreviousProductNumber(), item.getPreviousProductNumber())
                            && Objects.equal(newGeneralComment, originalGeneralComment)
                            && Objects.equal(entity.getPreferredSubmissionType(), item.getPreferredSubmissionType())
                            && Objects.equal(entity.getInternalProductNumber(), item.getInternalProductNumber())) {
                        continue;
                    }
                    final Product product = keepSaleHistory
                            ? addSalesDataHistory(entity.getProduct(), item.getProduct()) : item.getProduct();

                    entity = productStore.save(entity.copy()
                            .generalComment(newGeneralComment)
                            .product(product)
                            .internalProductNumber(item.getInternalProductNumber())
                            .submissionType(submissionType)
                            .child(child)
                            .status(ProductStatus.IMPORTED)
                            .sourceFilename(sourceFileName)
                            .lastModifiedDate(DateTime.now())
                            .build());
                    this.productSubmissionStore.updateLastestSubmissionIfNotSend(entity);

                } else {

                    // New product
                    productStore.create(ProductEntity.builder()
                            .productNumber(item.getProductNumber())
                            .internalProductNumber(item.getInternalProductNumber())
                            .child(child)
                            .status(ProductStatus.IMPORTED)
                            .submitterId(item.getSubmitterId())
                            .submissionType(submissionType)
                            .product(item.getProduct())
                            .generalComment(item.getGeneralComment())
                            .sourceFilename(sourceFileName)
                            .build());

                }
            }
            this.publisher.publish(new ProductImportEvent(this, sourceFileName, fileProductType,
                    products.stream().map(EucegProduct::getProductNumber).collect(Collectors.toList())));
        }

        return ValidationResult.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Transactional
    public @Nonnull ValidationResult importSubmitterFromExcel(@Nonnull final InputStream excelFile) {
        Assert.checkNotNull(excelFile, "excelFile");

        List<EucegSubmitter> submitters = null;
        try {
            final IImporterResult<EucegSubmitter> result = new BaseExcelImporterSubmitter(i18nService) {

                @Override
                protected String findAttachmentIdByFilename(final String filename) {
                    return attachmentStore.findByFilename(filename).map(AttachmentEntity::getAttachmentId).orElse(null);
                }

                @Override
                protected boolean submitterExists(final String submitterId) {
                    return submitterStore.exists(submitterId);
                }
            }.importFromExcel(excelFile, null);

            if (result.getValidationResult().hasFailures()) {
                return result.getValidationResult();
            }
            submitters = result.getResults();
        } finally {
            Closeables.closeQuietly(excelFile);
        }

        if (!submitters.isEmpty()) {
            for (final EucegSubmitter submitter : submitters) {
                if (this.submitterStore.exists(submitter.getSubmitterId())) {
                    // Update Submitter
                    final SubmitterEntity entity = this.submitterStore.get(submitter.getSubmitterId());
                    this.submitterStore.save(entity.copy()
                            .submitter(submitter.getSubmitter())
                            .details(submitter.getSubmitterDetails())
                            .name(submitter.getName())
                            .status(SubmitterStatus.IMPORTED)
                            .build());
                } else {
                    // New Submitter
                    this.submitterStore.create(SubmitterEntity.builder()
                            .submitterId(submitter.getSubmitterId())
                            .submitter(submitter.getSubmitter())
                            .details(submitter.getSubmitterDetails())
                            .name(submitter.getName())
                            .status(SubmitterStatus.IMPORTED)
                            .build());
                }
            }
        }

        return ValidationResult.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Transactional(readOnly = true, noRollbackFor = { RuntimeException.class })
    public @Nonnull ProductDiffRequest generateProductDiffFromFile(@Nonnull final InputStream excelFile,
        @Nonnull final ProductType fileProductType,
        @Nullable final int[] selectedSheets,
        final boolean keepSaleHistory) throws IOException {

        Assert.checkNotNull(excelFile, "excelFile");
        Assert.checkNotNull(fileProductType, "fileProductType");

        final IImporterResult<EucegProduct> result = importFromExcel(excelFile, fileProductType, selectedSheets);

        final ProductDiffRequestBuilder productDiffRequest = ProductDiffRequest.builder();

        final List<EucegProduct> products = result.getResults();
        final List<ProductDiffItem> diffs = Lists.newLinkedList();

        if (!result.getValidationResult().hasFailures() && !products.isEmpty()) {

            // Import Product
            for (final EucegProduct item : products) {
                ProductEntity entity = null;
                if (this.productStore.exists(item.getProductNumber())) {
                    // revised Product
                    entity = this.productStore.get(item.getProductNumber());

                }
                // normalize product -> ready to submission as the existing stored product
                Product product = this.productStore.normalize(item.getProduct());
                if (entity != null && keepSaleHistory) {
                    product = addSalesDataHistory(entity.getProduct(), product);
                }

                final DiffResult diffResult = new EucegXmlDiff(item.getProductNumber(),
                        EucegXmlDiff.getXml(entity != null ? entity.getProduct() : null, Product.class, true),
                        EucegXmlDiff.getXml(product, Product.class, true)).result();
                diffs.add(ProductDiffItem.builder()
                        .change(diffResult.getChange())
                        .validationResult(this.productStore.validate(product))
                        .productNumber(item.getProductNumber())
                        .patch(diffResult.getPatch())
                        .build());
            }
        }
        return productDiffRequest.validationResult(result.getValidationResult()).diffs(diffs).build();
    }

    /**
     * {@inheritDoc}
     */
    @PreAuthorize("hasGlobalPermission('USER')")
    @Override
    public void writeZipSubmissionReport(@Nonnull final Long id, @Nonnull final OutputStream outputStream) {
        Assert.checkNotNull(id, "id");
        Assert.checkNotNull(outputStream, "outputStream");
        final ISubmissionEntity submissionEntity = this.productSubmissionStore.get(id);

        final SubmitterEntity submitter = this.submitterStore.get(submissionEntity.getSubmitterId());

        if (!submissionEntity.getSubmissionStatus().exportable()) {
            throw new EucegException(i18nService.createKeyedMessage("app.service.euceg.submission.notexportable", id));
        }
        try (ZipOutputStream zip = new ZipOutputStream(outputStream, Charsets.UTF_8)) {

            // add submitter detail
            zip.putNextEntry(new ZipEntry("submitter_detail.xml"));
            zip.write(submitter.getXmlSubmitterDetail().getBytes(Eucegs.getDefaultCharset()));
            zip.closeEntry();
            // add attachment to export
            final Set<String> uuids = submissionEntity.getAttachedAttachments();
            for (final String uuid : uuids) {
                final AttachmentEntity attachmentEntity = this.attachmentStore.get(uuid);
                final IFileStorageFile fileAttachment = this.fileStorage.getByUuid(uuid);
                // create attachment
                final Attachment attachment = attachmentEntity.toAttachment(submitter, fileAttachment.getFile());
                zip.putNextEntry(new ZipEntry("attachment-" + uuid + ".xml"));
                Eucegs.marshal(attachment, zip);
                zip.closeEntry();
            }
            // add submission;
            zip.putNextEntry(new ZipEntry("submission.xml"));
            zip.write(submissionEntity.getXmlSubmission().getBytes());
            zip.closeEntry();
            final List<? extends ITransmitReceiptEntity> receipts = submissionEntity.getReceipts();
            if (receipts != null) {
                for (final ITransmitReceiptEntity receipt : receipts) {
                    if (receipt.getResponse() == null) {
                        continue;
                    }
                    final String name = "receipt-" + receipt.getType() + "-" + receipt.getMessageId() + ".xml";
                    zip.putNextEntry(new ZipEntry(name));
                    Eucegs.marshal(receipt.getResponse(), zip);
                    zip.closeEntry();
                }
            }

        } catch (final IOException e) {
            LOGGER.error("Error during zip archive creation", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @PreAuthorize("hasGlobalPermission('USER')")
    @Override
    public void writeZipSubmissionPackage(@Nonnull final Long id, @Nonnull final OutputStream outputStream) {
        Assert.checkNotNull(id, "id");
        Assert.checkNotNull(outputStream, "outputStream");
        final ISubmissionEntity submissionEntity = this.productSubmissionStore.get(id);

        final SubmitterEntity submitter = this.submitterStore.get(submissionEntity.getSubmitterId());

        try (ZipOutputStream zip = new ZipOutputStream(outputStream, Charsets.UTF_8)) {

            // add submitter detail
            zip.putNextEntry(new ZipEntry("submitter_detail.xml"));
            zip.write(submitter.getXmlSubmitterDetail().getBytes(Eucegs.getDefaultCharset()));
            zip.closeEntry();
            // add attachment to include
            final Set<String> uuids = submissionEntity.getAttachments().keySet();
            for (final String uuid : uuids) {
                final IFileStorageFile fileAttachment = this.fileStorage.getByUuid(uuid);
                zip.putNextEntry(new ZipEntry("attachment-" + uuid + ".pdf"));
                zip.write(IOUtils.toByteArray(fileAttachment.openStream()));
                zip.closeEntry();
            }
            // add submission;
            zip.putNextEntry(new ZipEntry("submission-" + submissionEntity.getProductId() + ".xml"));
            zip.write(Eucegs.marshal(submissionEntity.getSubmission(), true).getBytes(Eucegs.getDefaultCharset()));
            zip.closeEntry();
        } catch (final IOException e) {
            LOGGER.error("Error during package creation", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private IImporterResult<EucegProduct> importFromExcel(@Nonnull final InputStream excelFile,
        @Nonnull final ProductType fileProductType,
        @Nullable final int[] selectedSheets) {
        IImporterResult<EucegProduct> result = null;
        switch (fileProductType) {
            case ECIGARETTE:
                result = new BaseExcelImporterEcigaretteProducts(i18nService) {

                    @Override
                    protected String findAttachmentIdByFilename(final String filename) {
                        return attachmentStore.findByFilename(filename)
                                .map(AttachmentEntity::getAttachmentId)
                                .orElse(null);
                    }

                    @Override
                    protected boolean submitterExists(final @Nonnull String submitterId) {
                        return submitterStore.exists(submitterId);
                    }

                    @Override
                    public EcigProduct getCurrentProduct(final @Nonnull String productNumber) {
                        final ProductEntity entity = productStore.find(productNumber);
                        if (entity != null) {
                            return (EcigProduct) entity.getProduct();
                        }
                        return null;
                    }
                }.importFromExcel(excelFile, selectedSheets);
                break;
            case TOBACCO:
                result = new BaseExcelmporterTobaccoProducts(i18nService) {

                    @Override
                    protected String findAttachmentIdByFilename(final String filename) {
                        return attachmentStore.findByFilename(filename)
                                .map(AttachmentEntity::getAttachmentId)
                                .orElse(null);
                    }

                    @Override
                    protected boolean submitterExists(final @Nonnull String submitterId) {
                        return submitterStore.exists(submitterId);
                    }

                    @Override
                    public TobaccoProduct getCurrentProduct(final @Nonnull String productNumber) {
                        final ProductEntity entity = productStore.find(productNumber);
                        if (entity != null) {
                            return (TobaccoProduct) entity.getProduct();
                        }
                        return null;
                    }
                }.importFromExcel(excelFile, selectedSheets);
                break;

            default:
                throw new RuntimeException("unknown product type '" + fileProductType + "'");
        }
        return result;
    }

    private static Product addSalesDataHistory(final Product currentProduct, final Product newProduct) {

        if (currentProduct instanceof TobaccoProduct) {
            final TobaccoProduct currentTobacco = (TobaccoProduct) currentProduct;
            final TobaccoProduct newTobacco = (TobaccoProduct) newProduct;

            Map<String, TobaccoPresentation> currentPresentations = Collections.emptyMap();
            if (currentTobacco.getPresentations() != null
                    && currentTobacco.getPresentations().getPresentation() != null) {
                currentPresentations = currentTobacco.getPresentations()
                        .getPresentation()
                        .stream()
                        .collect(Collectors.toMap(p -> p.getProductNumber().getValue(), Function.identity()));
            }
            Map<String, TobaccoPresentation> newPresentations = Collections.emptyMap();
            if (newTobacco.getPresentations() != null && newTobacco.getPresentations().getPresentation() != null) {
                newPresentations = newTobacco.getPresentations()
                        .getPresentation()
                        .stream()
                        .collect(Collectors.toMap(p -> p.getProductNumber().getValue(), Function.identity()));
            }
            for (final String productNumber : currentPresentations.keySet()) {
                final TobaccoPresentation currentPresentation = currentPresentations.get(productNumber);
                final TobaccoPresentation newPresentation = newPresentations.get(productNumber);

                if (newPresentation == null) {
                    continue;
                }
                Map<Integer, TobaccoAnnualSalesData> currentSales = Collections.emptyMap();
                if (currentPresentation.getAnnualSalesDataList() != null
                        && currentPresentation.getAnnualSalesDataList().getAnnualSalesData() != null) {
                    currentSales = currentPresentation.getAnnualSalesDataList()
                            .getAnnualSalesData()
                            .stream()
                            .collect(Collectors.toMap(s -> s.getYear().getValue(), Function.identity()));
                }
                Map<Integer, TobaccoAnnualSalesData> newSales;
                if (newPresentation.getAnnualSalesDataList() != null
                        && newPresentation.getAnnualSalesDataList().getAnnualSalesData() != null) {
                    newSales = newPresentation.getAnnualSalesDataList()
                            .getAnnualSalesData()
                            .stream()
                            .collect(Collectors.toMap(s -> s.getYear().getValue(), Function.identity()));
                } else {
                    newSales = Maps.newHashMap();
                }
                for (final Integer year : currentSales.keySet()) {
                    final TobaccoAnnualSalesData currentSale = currentSales.get(year);
                    final TobaccoAnnualSalesData newSale = newSales.get(year);

                    if (currentSale != null && newSale == null) {
                        newSales.put(year, currentSale);
                    }
                }
                if (!newSales.isEmpty()) {
                    newPresentation.withAnnualSalesDataList(
                        new TobaccoPresentation.AnnualSalesDataList().withAnnualSalesData(newSales.values()
                                .stream()
                                .sorted(Comparator.comparingInt(s -> s.getYear().getValue()))
                                .collect(Collectors.toList())));
                } else {
                    newPresentation.withAnnualSalesDataList(null);
                }
            }
        } else if (currentProduct instanceof EcigProduct) {
            final EcigProduct currentEcig = (EcigProduct) currentProduct;
            final EcigProduct newEcig = (EcigProduct) newProduct;

            Map<String, EcigPresentation> currentPresentations = Collections.emptyMap();
            if (currentEcig.getPresentations() != null && currentEcig.getPresentations().getPresentation() != null) {
                currentPresentations = currentEcig.getPresentations()
                        .getPresentation()
                        .stream()
                        .collect(Collectors.toMap(p -> p.getProductNumber().getValue(), Function.identity()));
            }
            Map<String, EcigPresentation> newPresentations = Collections.emptyMap();
            if (newEcig.getPresentations() != null && newEcig.getPresentations().getPresentation() != null) {
                newPresentations = newEcig.getPresentations()
                        .getPresentation()
                        .stream()
                        .collect(Collectors.toMap(p -> p.getProductNumber().getValue(), Function.identity()));
            }
            for (final String productNumber : currentPresentations.keySet()) {
                final EcigPresentation currentPresentation = currentPresentations.get(productNumber);
                final EcigPresentation newPresentation = newPresentations.get(productNumber);

                if (newPresentation == null) {
                    continue;
                }

                Map<Integer, EcigAnnualSalesData> currentSales = Collections.emptyMap();
                if (currentPresentation.getAnnualSalesDataList() != null
                        && currentPresentation.getAnnualSalesDataList().getAnnualSalesData() != null) {
                    currentSales = currentPresentation.getAnnualSalesDataList()
                            .getAnnualSalesData()
                            .stream()
                            .collect(Collectors.toMap(s -> s.getYear().getValue(), Function.identity()));
                }
                Map<Integer, EcigAnnualSalesData> newSales;
                if (newPresentation.getAnnualSalesDataList() != null
                        && newPresentation.getAnnualSalesDataList().getAnnualSalesData() != null) {
                    newSales = newPresentation.getAnnualSalesDataList()
                            .getAnnualSalesData()
                            .stream()
                            .collect(Collectors.toMap(s -> s.getYear().getValue(), Function.identity()));
                } else {
                    newSales = Maps.newHashMap();
                }
                for (final Integer year : currentSales.keySet()) {
                    final EcigAnnualSalesData currentSale = currentSales.get(year);
                    final EcigAnnualSalesData newSale = newSales.get(year);

                    if (currentSale != null && newSale == null) {
                        newSales.put(year, currentSale);
                    }
                }
                if (!newSales.isEmpty()) {
                    newPresentation.withAnnualSalesDataList(
                        new EcigPresentation.AnnualSalesDataList().withAnnualSalesData(newSales.values()
                                .stream()
                                .sorted(Comparator.comparingInt(s -> s.getYear().getValue()))
                                .collect(Collectors.toList())));
                } else {
                    newPresentation.withAnnualSalesDataList(null);
                }
            }
        }
        return newProduct;
    }

}
