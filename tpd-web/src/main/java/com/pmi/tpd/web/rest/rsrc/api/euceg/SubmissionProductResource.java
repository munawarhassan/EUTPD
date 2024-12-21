package com.pmi.tpd.web.rest.rsrc.api.euceg;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityNotFoundException;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.elasticsearch.IIndexerOperations;
import com.pmi.tpd.core.elasticsearch.model.SubmissionIndexed;
import com.pmi.tpd.core.euceg.IBulkProductService;
import com.pmi.tpd.core.euceg.IEucegImportExportService;
import com.pmi.tpd.core.euceg.ISubmissionService;
import com.pmi.tpd.core.euceg.SubmissionSendRequest;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.core.BulkRequest;
import com.pmi.tpd.web.core.hateoas.BaseResourceSupport;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.ProductListRequest;
import com.pmi.tpd.web.rest.model.ReceiptRequest;
import com.pmi.tpd.web.rest.model.SubmissionRequest;
import com.pmi.tpd.web.rest.model.SubmitterListRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Named
@Path(RestApplication.API_RESOURCE_PATH + "/submissions")
@Tag(description = "Endpoint for Product Submission", name = "business")
@ExposesResourceFor(SubmissionEntity.class)
public class SubmissionProductResource extends BaseResourceSupport<SubmissionProductResource> {

    /** */
    public static final String FORMAT_UTC_TIMESTAMP = "yyyyMMdd-HHmmss-SSS'Z'";

    /** */
    public static final TimeZone TIMEZONE_FOR_TIMESTAMP = TimeZone.getTimeZone("UTC");

    /** */
    private final IProductSubmissionStore store;

    /** */
    private final ISubmissionService submissionService;

    /** */
    private final ISubmitterStore submitterStore;

    /** */
    private final IIndexerOperations indexerOperations;

    /** */
    private final IEucegImportExportService eucegImportExportService;

    /** */
    private final IBulkProductService bulkProductService;

    /**
     * @param submissionService
     * @param store
     * @param submitterStore
     * @param indexerOperations
     */
    @Inject
    public SubmissionProductResource(final ISubmissionService submissionService,
            final IEucegImportExportService eucegImportExportService, final IProductSubmissionStore store,
            final ISubmitterStore submitterStore, final IIndexerOperations indexerOperations,
            final IBulkProductService bulkProductService) {
        this.store = Assert.checkNotNull(store, "store");
        this.submissionService = Assert.checkNotNull(submissionService, "submissionService");
        this.eucegImportExportService = Assert.checkNotNull(eucegImportExportService, "eucegImportExportService");
        this.submitterStore = Assert.checkNotNull(submitterStore, "submitterStore");
        this.indexerOperations = Assert.checkNotNull(indexerOperations, "indexerOperations");
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
    @Operation(summary = "Gets list of Product Submission", responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = SubmissionRequest.class))) })
    public Response findAll(
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @Parameter(description = "filters submission", required = false) @QueryParam("filter") final String filter,
        @Parameter(description = "search submission with expression",
                required = false) @QueryParam("search") final String search)
            throws Exception {
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, search);
        return Response
                .ok(resources(SubmissionRequest.toResourcesForIndexed(indexerOperations.findAllSubmission(pageRequest),
                    assembler(SubmissionIndexed.class, SubmissionRequest.class))))
                .build();
    }

    /**
     * @param productId
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets a Product Submission",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = SubmissionEntity.class))) })
    public Response get(@PathParam("id") final Long id) {
        final SubmissionEntity entity;
        try {
            entity = store.get(id);
        } catch (final JpaObjectRetrievalFailureException | EntityNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(resource(entity)).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/product")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets a Product of the Submission", responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = ProductListRequest.class))) })
    public Response getProduct(@PathParam("id") final Long id) {
        final ISubmissionEntity entity;
        try {
            entity = store.get(id);
        } catch (final JpaObjectRetrievalFailureException | EntityNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
        final EntityModel<?> rsrc = new EntityModel<>(ProductListRequest.from(entity.getProduct(),
            assembler(IProductEntity.class, ProductListRequest.class)));
        rsrc.add(linkTo(ProductResource.class).slash(entity.getId()).withSelfRel());
        return Response.ok(rsrc).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/submitter")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets the submitter of the Submission", responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = SubmitterListRequest.class))) })
    public Response getSubmitter(@PathParam("id") final Long id) {
        final ISubmissionEntity entity;
        SubmitterEntity submitterEntity;
        try {
            entity = store.get(id);
            submitterEntity = submitterStore.get(entity.getSubmitterId());
        } catch (final JpaObjectRetrievalFailureException | EntityNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
        final EntityModel<?> rsrc = new EntityModel<>(SubmitterListRequest.from(submitterEntity));
        rsrc.add(linkTo(SubmitterResource.class).slash(entity.getId()).withSelfRel());
        return Response.ok(rsrc).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/receipts")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets a Receipts of the Submission",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ReceiptRequest.class))) })
    public Response getReceipts(@PathParam("id") final Long id) {
        final ISubmissionEntity entity;
        try {
            entity = store.get(id);
        } catch (final JpaObjectRetrievalFailureException | EntityNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }

        final CollectionModel<?> rsrc = new CollectionModel<>(ReceiptRequest.map(entity.getReceipts()));
        rsrc.add(linkTo().slash(entity.getId()).slash("receipts").withSelfRel());
        return Response.ok(rsrc).build();
    }

    /**
     * @param submissionId
     * @param entity
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    @Timed
    @Operation(summary = "Update a Product Submission")
    public Response update(@PathParam("id") final Long submissionId, final SubmissionEntity entity) {
        return Response.ok(store.save(entity)).build();
    }

    /**
     * @param productId
     * @param entity
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @PUT
    @Path("{id}/cancel")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    @Timed
    @Operation(summary = "Cancel a Product Submission")
    public Response cancel(@PathParam("id") final Long submissionId) {
        this.submissionService.cancelSubmission(submissionId);
        return Response.ok().build();
    }

    /**
     * @param productId
     * @param entity
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @PUT
    @Path("{id}/reject")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    @Timed
    @Operation(summary = "Reject a Product Submission")
    public Response reject(@PathParam("id") final Long submissionId) {
        this.submissionService.rejectSubmission(submissionId);
        return Response.ok().build();
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
    @Operation(summary = "Create a Product Submission",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = SubmissionEntity.class))),
                    @ApiResponse(responseCode = "201", description = "Submission has been succefully created") })
    public Response create(final SubmissionEntity entity) {
        return Response.status(Status.CREATED).entity(submissionService.createSubmission(entity)).build();
    }

    /**
     * @param productId
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Path("{id}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Delete a Product Submission", responses = {
            @ApiResponse(responseCode = "204", description = "The submission has been succefully deleted.") })
    public Response delete(
        @Parameter(description = "Product submission id to delete", required = true) @PathParam("id") final Long id) {
        store.remove(id);
        return Response.status(Status.NO_CONTENT).build();
    }

    /**
     * @param productId
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/export")
    @Timed
    @Produces({ "application/zip", "application/octet-stream" })
    @Operation(summary = "export a Product Submission")
    public Response export(@PathParam("id") final Long id) {
        final StreamingOutput outputStream = output -> eucegImportExportService.writeZipSubmissionReport(id, output);

        return Response.ok(outputStream)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + getExportFilename(id, "export") + "\"")
                .type(MediaType.valueOf("application/zip"))
                .build();
    }

    /**
     * @param productId
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/package")
    @Timed
    @Produces({ "application/zip", "application/octet-stream" })
    @Operation(summary = "package a Product Submission")
    public Response packageSubmission(@PathParam("id") final Long id) {
        final StreamingOutput outputStream = output -> eucegImportExportService.writeZipSubmissionPackage(id, output);

        return Response.ok(outputStream)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + getExportFilename(id, "package") + "\"")
                .type(MediaType.valueOf("application/zip"))
                .build();
    }

    /**
     * @param productId
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Path("send")
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "create and send the submission of product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = SubmissionEntity.class))),
                    @ApiResponse(responseCode = "201", description = "Submission has been succefully sent.") })
    public Response send(final SubmissionSendRequest request) {
        final ISubmissionEntity entity = this.submissionService.createOrSendSubmission(request);
        return Response.status(Status.CREATED).entity(resource(entity)).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Path("{productType}/bulkSend")
    @Timed
    @Operation(summary = "create and send the submission of products")
    public Response bulkSendSubmissions(@Nonnull @PathParam("productType") final ProductType productType,
        @Nonnull final BulkRequest request) {
        this.bulkProductService.bulkSend(productType, request);
        return Response.ok().build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Path("{id}/send")
    @Timed
    @Operation(summary = "send the submission of product")
    public Response send(@PathParam("id") final Long id) throws EucegException {
        this.submissionService.sendSubmission(id);
        return Response.ok().build();
    }

    private String getExportFilename(final Long id, final String action) {
        final ISubmissionEntity entity = this.store.get(id);
        final SimpleDateFormat format = new SimpleDateFormat(FORMAT_UTC_TIMESTAMP);
        format.setTimeZone(TIMEZONE_FOR_TIMESTAMP);
        final String displayTimestamp = format.format(new Date());
        return String.format("submission-%s-%s-%s.zip", entity.getProductId(), action, displayTimestamp);
    }

    @Override
    public <T extends IIdentityEntity<?>> EntityModel<T> resource(@Nonnull final T entity) {
        final EntityModel<T> rsrc = super.resource(entity);
        rsrc.add(linkTo(methodOn().getProduct((Long) entity.getId())).withRel("product"));
        rsrc.add(linkTo(methodOn().getReceipts((Long) entity.getId())).withRel("receipts"));
        rsrc.add(linkTo(methodOn().getSubmitter((Long) entity.getId())).withRel("submitter"));
        return rsrc;
    }

}
