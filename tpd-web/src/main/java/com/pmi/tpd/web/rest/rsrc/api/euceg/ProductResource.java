package com.pmi.tpd.web.rest.rsrc.api.euceg;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.elasticsearch.common.Strings;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Throwables;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.exception.InvalidArgumentException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.elasticsearch.IIndexerOperations;
import com.pmi.tpd.core.elasticsearch.model.ProductIndexed;
import com.pmi.tpd.core.euceg.IBulkProductService;
import com.pmi.tpd.core.euceg.IEucegImportExportService;
import com.pmi.tpd.core.euceg.ISubmissionService;
import com.pmi.tpd.core.euceg.PirStatusUpdateRequest;
import com.pmi.tpd.core.euceg.ProductDiffRequest;
import com.pmi.tpd.core.euceg.ProductDiffRequest.ProductDiffRequestBuilder;
import com.pmi.tpd.core.euceg.ProductUpdateRequest;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.ProductRevision;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.core.BulkRequest;
import com.pmi.tpd.euceg.core.excel.ExcelHelper;
import com.pmi.tpd.euceg.core.excel.SheetDescriptor;
import com.pmi.tpd.euceg.core.excel.UnsupportedExcelFormatException;
import com.pmi.tpd.euceg.core.util.validation.ValidationResult;
import com.pmi.tpd.web.core.hateoas.BaseResourceSupport;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.ProductListRequest;
import com.pmi.tpd.web.rest.model.SubmissionRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Named
@Path(RestApplication.API_RESOURCE_PATH + "/products")
@Tag(description = "Endpoint for Products", name = "business")
@ExposesResourceFor(ProductEntity.class)
public class ProductResource extends BaseResourceSupport<ProductResource> {

    private static Logger LOGGER = LoggerFactory.getLogger(ProductResource.class);

    /** */
    public static final String FORMAT_UTC_TIMESTAMP = "yyyyMMdd-HHmmss-SSS'Z'";

    /** */
    public static final TimeZone TIMEZONE_FOR_TIMESTAMP = TimeZone.getTimeZone("UTC");

    /** */
    private final IProductStore store;

    /** */
    private final IProductSubmissionStore submissionStore;

    /** */
    private final ISubmissionService submissionService;

    private final IEucegImportExportService eucegImportExportService;

    /** */
    private final IIndexerOperations indexerOperations;

    /** */
    private final I18nService i18nService;

    /** */
    private final IBulkProductService bulkProductService;

    /**
     * @param submissionService
     *                          a submission service
     * @param store
     *                          a product store.
     * @param indexerOperations
     *                          a accessor to indexing operations.
     */
    @Inject
    public ProductResource(final ISubmissionService submissionService,
            final IEucegImportExportService eucegImportExportService, final IProductStore store,
            final IProductSubmissionStore submissionStore, final IIndexerOperations indexerOperations,
            final I18nService i18nService, final IBulkProductService bulkProductService) {
        this.store = Assert.checkNotNull(store, "store");
        this.submissionStore = Assert.checkNotNull(submissionStore, "submissionStore");
        this.submissionService = Assert.checkNotNull(submissionService, "submissionService");
        this.eucegImportExportService = Assert.checkNotNull(eucegImportExportService, "eucegImportExportService");
        this.indexerOperations = Assert.checkNotNull(indexerOperations, "indexerOperations");
        this.i18nService = Assert.checkNotNull(i18nService, "i18nService");
        this.bulkProductService = Assert.checkNotNull(bulkProductService, "bulkProductService");
    }

    /**
     * @param productType
     * @param page
     * @param size
     * @return
     * @throws Exception
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Timed
    @Operation(summary = "Gets list of Product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CollectionModel.class))) })
    public Response findAll(
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @Parameter(description = "filters product", required = false) @QueryParam("filter") final String filter,
        @Parameter(description = "search product with expression",
                required = false) @QueryParam("search") final String search) {
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, search);
        return ResponseFactory
                .ok(resources(ProductListRequest.toResourcesForIndexed(indexerOperations.findAllProduct(pageRequest),
                    assembler(ProductIndexed.class, ProductListRequest.class))))
                .build();
    }

    @Path("findAllNewProduct")
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Timed
    @Operation(summary = "Gets list of Product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CollectionModel.class))) })
    public Response findAllNewProduct(@QueryParam("productType") final ProductType productType) {
        final List<? extends IProductEntity> l = this.store.findAllNewProduct(productType);
        return ResponseFactory
                .ok(resources(ProductListRequest.map(l, assembler(IProductEntity.class, ProductListRequest.class))))
                .build();
    }

    @Path("whereUsed")
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET()
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Timed
    @Operation(summary = "Gets list of Product use attachment",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CollectionModel.class))) })
    public Response findAllUseAttachment(@Context final Pageable pageable, @QueryParam("uuid") final String uuid) {
        return ResponseFactory
                .ok(resources(ProductListRequest.toResources(this.store.findAllUseAttachment(pageable, uuid),
                    assembler(ProductEntity.class, ProductListRequest.class))))
                .build();
    }

    /**
     * @param productNumber
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets a Product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = EntityModel.class))) })
    public Response get(@PathParam("id") final String productNumber) {
        try {
            return ResponseFactory.ok(resource(store.get(productNumber))).build();
        } catch (final JpaObjectRetrievalFailureException | EntityNotFoundException e) {
            return ResponseFactory.notFound().build();
        }
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/rev")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets revisions of a Product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))) })
    public Response getRevisions(@PathParam("id") final String productNumber,
        @Parameter(
                description = "page to load (zero-based page index)") @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @Parameter(description = "filters product", required = false) @QueryParam("filter") final String filter) {

        final Page<ProductRevision> revisions = store.findRevisions(productNumber,
            PageUtils.newRequest(page, size, sort, filter, null));

        return ResponseFactory.ok(revisions).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/rev/latest")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets the latest revision of product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ProductRevision.class))) })
    public Response getLatestRevision(@PathParam("id") final String productNumber) {
        try {
            return ResponseFactory.ok(this.store.getCurrentRevision(productNumber)).build();
        } catch (final NoSuchElementException e1) {
            return ResponseFactory.notFound().build();
        }
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/rev/compare")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Compare two Product revisions", responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = ProductDiffRequest.class))) })
    public Response compare(@PathParam("id") final String productNumber,
        @QueryParam("revised") @DefaultValue("current") final String revisedRevision,
        @QueryParam("original") @NotNull final Integer originalRevision) throws IOException {

        Integer revised = null;
        if (revisedRevision.toLowerCase().equals("current")) {
            revised = null;
        } else {
            revised = Integer.parseInt(revisedRevision);
        }

        return ResponseFactory.ok(this.store.compareRevisions(productNumber, revised, originalRevision)).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/submissions")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets all submission associated to a product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CollectionModel.class))) })
    public Response getSubmissions(@PathParam("id") final String productNumber, @Context final Pageable pageRequest) {
        final Page<SubmissionEntity> page = this.submissionStore.findAllForProduct(productNumber, pageRequest);
        return ResponseFactory
                .ok(resources(SubmissionRequest.toResources(page,
                    assembler(SubmissionProductResource.class, SubmissionEntity.class, SubmissionRequest.class))))
                .build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/latestSubmission")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets the latest submission associated to a product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = EntityModel.class))) })
    public Response getLatestSubmission(@PathParam("id") final String productNumber) {
        final ProductEntity entity;
        SubmissionEntity latestSubmisstion;
        try {
            entity = store.get(productNumber);

        } catch (final JpaObjectRetrievalFailureException | EntityNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
        latestSubmisstion = entity.getLastestSubmission();
        if (latestSubmisstion == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        final EntityModel<?> rsrc = new EntityModel<>(
                SubmissionRequest.from(latestSubmisstion, assembler(SubmissionEntity.class, SubmissionRequest.class)));
        rsrc.add(linkTo(SubmissionProductResource.class).slash(entity.getId()).withSelfRel());
        return ResponseFactory.ok(rsrc).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/child")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets the child product associated to a product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ProductEntity.class))) })
    public Response getChild(@PathParam("id") final String productNumber) {
        final ProductEntity entity;
        ProductEntity child;
        try {
            entity = store.get(productNumber);

        } catch (final JpaObjectRetrievalFailureException | EntityNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
        child = entity.getChild();
        if (child == null) {
            return ResponseFactory.status(Status.NOT_FOUND).build();
        }
        return ResponseFactory.ok(resource(child)).build();
    }

    /**
     * @param productId
     * @param entity
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    @Timed
    @Operation(summary = "Update a Product")
    public Response update(@PathParam("id") final String productId, @Valid final ProductUpdateRequest request) {
        return ResponseFactory.ok(submissionService.saveProduct(request)).build();
    }

    /**
     * @param productId
     * @param entity
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @PUT
    @Path("{id}/pirStatus")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    @Timed
    @Operation(summary = "Update a Product")
    public Response updatePIR(@PathParam("id") final String productId, @Valid final PirStatusUpdateRequest request) {
        return ResponseFactory.ok(submissionService.updatePirStatus(request)).build();
    }

    /**
     * @param entity
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "create a Product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = IProductEntity.class))) })
    public Response create(final ProductEntity entity) {
        return ResponseFactory.status(Status.CREATED).entity(submissionService.createProduct(entity)).build();
    }

    /**
     * @param productNumber
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Path("{id}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Delete a Product")
    public Response delete(@Parameter(description = "product number to delete",
            required = true) @PathParam("id") final String productNumber) {
        store.remove(productNumber);
        return ResponseFactory.ok().build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Path("validate")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "validate a Product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ValidationResult.class))) })
    public Response validate(final ProductEntity entity) {
        final ValidationResult result = new ValidationResult();
        this.store.validate(entity, result);
        return ResponseFactory.ok(result).build();
    }

    /**
     * Gets the list of {@link SheetDescriptor} representing sheets containing in excel {@code file}.
     *
     * @param file
     *             the excel file to use.
     * @param type
     *             the string representation of {@link ProductType}.
     * @return Returns the list of {@link SheetDescriptor} representing sheets containing in excel {@code file}.
     * @throws IOException
     *                     if I/O error.
     * @since 1.6
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Path("{type}/sheets")
    @GET
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets the list of available sheets in Excel file.", responses = { @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SheetDescriptor.class)))) })
    public Response getSheets(@PathParam("type") final String type) throws IOException {
        final ProductType productType = ProductType.valueOf(type);
        try {
            return ResponseFactory.ok(ExcelHelper.getImportedSheets(productType)).build();
        } catch (final UnsupportedExcelFormatException e) {
            throw new InvalidArgumentException(i18nService
                    .createKeyedMessage("app.service.euceg.product.file.notsupported", Product.getFullName()));
        }
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Path("diff")
    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiffFromFile(@FormDataParam("file") final FormDataBodyPart file,
        @FormDataParam("file") final InputStream fileInputStream,
        @FormDataParam("product_type") final String type,
        @FormDataParam("sheets") final String sheets,
        @FormDataParam("keep_sale_history") final boolean keepSaleHistory) throws Throwable {

        final ProductType productType = ProductType.valueOf(type);

        int[] arSheets = null;
        if (!Strings.isNullOrEmpty(sheets) && !"[]".equals(sheets)) {
            arSheets = Arrays.stream(sheets.split("\\|")).mapToInt(Integer::valueOf).toArray();
        }
        final ProductDiffRequestBuilder request = ProductDiffRequest.builder();
        try {
            final ProductDiffRequest diff = this.eucegImportExportService
                    .generateProductDiffFromFile(fileInputStream, productType, arSheets, keepSaleHistory);
            if (diff.getValidationResult().hasFailures()) {
                return Response.status(Status.BAD_REQUEST).entity(diff.getValidationResult()).build();
            }
            request.merge(diff);
            return ResponseFactory.ok(request.build()).build();
        } catch (final Throwable ex) {
            LOGGER.error("unknow error on diff", ex);
            throw Throwables.getRootCause(ex);
        }
    }

    /**
     * Import a product excel file from {@link FormDataMultiPart} form.
     *
     * @param form
     *             the form to use
     * @return Returns status code OK or ACCEPTED with empty response when the request has succeeded.
     * @throws IOException
     *                     if I/O errors
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Path("import")
    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importFile(@FormDataParam("files") final List<FormDataBodyPart> files,
        @FormDataParam("product_type") final String type,
        @FormDataParam("sheets") final String sheets,
        @FormDataParam("keep_sale_history") final boolean keepSaleHistory) throws IOException {

        final ProductType productType = ProductType.valueOf(type);

        int[] arSheets = null;
        if (!Strings.isNullOrEmpty(sheets) && !"[]".equals(sheets)) {
            arSheets = Arrays.stream(sheets.split("\\|")).mapToInt(Integer::valueOf).toArray();
        }
        if (files != null) {
            ValidationResult result = null;

            for (final FormDataBodyPart bodyPart : files) {
                final BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
                try (InputStream file = bodyPartEntity.getInputStream()) {

                    result = this.eucegImportExportService.importProductFromExcel(file,
                        bodyPart.getContentDisposition().getFileName(),
                        productType,
                        arSheets,
                        keepSaleHistory);
                    if (result.hasFailures()) {
                        return ResponseFactory.status(Status.BAD_REQUEST).entity(result).build();
                    }
                }
            }

            return ResponseFactory.accepted().build();
        }
        return ResponseFactory.ok().build();
    }

    /**
     * Export list of product in excel file.
     *
     * @param productType
     *                       the product type.
     * @param productNumbers
     *                       the list of product numbers.
     * @return Returns a generate excel file containing products associated to.
     * @since 2.5
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Path("{productType}/export")
    @Timed
    @Produces({ "application/vnd.ms-excel", "application/octet-stream" })
    @Operation(summary = "export a Product")
    public Response export(@NotNull @Nonnull @PathParam("productType") final ProductType productType,
        @NotNull @Nonnull final BulkRequest request) {
        StreamingOutput outputStream = null;
        outputStream = output -> bulkProductService.exportToExcel(output, productType, request);

        return Response.ok(outputStream)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + getExportFilename() + "\"")
                .type(MediaType.valueOf("application/vnd.ms-excel"))
                .build();
    }

    private String getExportFilename() {
        final SimpleDateFormat format = new SimpleDateFormat(FORMAT_UTC_TIMESTAMP);
        format.setTimeZone(TIMEZONE_FOR_TIMESTAMP);
        final String displayTimestamp = format.format(new Date());
        return String.format("product-export-%s.xlsx", displayTimestamp);
    }

    @Override
    public <E extends IIdentityEntity<?>> EntityModel<E> resource(final @Nonnull E entity) {
        final EntityModel<E> rsrc = super.resource(entity);
        rsrc.add(linkTo(methodOn().getSubmissions((String) entity.getId(), PageUtils.newRequest(0, 20)))
                .withRel("submissions"));
        rsrc.add(linkTo(methodOn().getLatestSubmission((String) entity.getId())).withRel("latestSubmission"));
        rsrc.add(linkTo(methodOn().getChild((String) entity.getId())).withRel("child"));
        return rsrc;
    }

}
