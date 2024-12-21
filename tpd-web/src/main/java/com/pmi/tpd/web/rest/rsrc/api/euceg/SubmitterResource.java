package com.pmi.tpd.web.rest.rsrc.api.euceg;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.isTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.elasticsearch.common.Strings;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

import com.codahale.metrics.annotation.Timed;
import com.google.common.io.Closeables;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.core.elasticsearch.IIndexerOperations;
import com.pmi.tpd.core.elasticsearch.model.SubmitterIndexed;
import com.pmi.tpd.core.euceg.IEucegImportExportService;
import com.pmi.tpd.core.euceg.ISubmissionService;
import com.pmi.tpd.core.euceg.SubmitterRequest;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.model.euceg.SubmitterRevision;
import com.pmi.tpd.euceg.core.util.validation.ValidationResult;
import com.pmi.tpd.web.core.hateoas.BaseResourceSupport;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
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
@Path(RestApplication.API_RESOURCE_PATH + "/submitters")
@Tag(description = "Endpoint for Submitters", name = "business")
public class SubmitterResource extends BaseResourceSupport<SubmitterResource> {

    /** */
    private final ISubmitterStore store;

    /** */
    private final ISubmissionService submissionService;

    /** */
    private final IEucegImportExportService eucegImportExportService;

    /** */
    private final IIndexerOperations indexerOperations;

    /**
     * @param store
     * @param submissionService
     * @param indexerOperations
     */
    @Inject
    public SubmitterResource(final ISubmitterStore store, final ISubmissionService submissionService,
            final IEucegImportExportService eucegImportExportService, final IIndexerOperations indexerOperations) {
        this.store = store;
        this.submissionService = submissionService;
        this.eucegImportExportService = eucegImportExportService;
        this.indexerOperations = indexerOperations;
    }

    /**
     * @param page
     * @param size
     * @return
     * @throws Exception
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Timed
    @Operation(summary = "Gets list of submitters", responses = { @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = SubmitterListRequest.class))) })
    public Response findAll(
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @Parameter(description = "fiters submitter", required = false) @QueryParam("filter") final String filter,
        @Parameter(description = "search submitter with expression",
                required = false) @QueryParam("search") final String search)
            throws Exception {
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);
        return ResponseFactory.ok(
            resources(SubmitterListRequest.toResourcesForIndexed(indexerOperations.findAllSubmitter(pageRequest),
                assembler(SubmitterIndexed.class, SubmitterListRequest.class)))).build();

    }

    /**
     * @param submitterId
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets a submitter", responses = { @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = SubmitterRequest.class))) })
    public Response get(@PathParam("id") final String submitterId) {
        try {
            return ResponseFactory.ok(resource(submissionService.getSubmitter(submitterId))).build();
        } catch (final JpaObjectRetrievalFailureException | EntityNotFoundException e) {
            return ResponseFactory.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * get submitter revisions
     *
     * @param submitterId
     *                    submitter Id
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/rev")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getRevisions(@PathParam("id") final String submitterId,
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort) {

        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, null, null);
        final Page<SubmitterRevision> revisions = this.store.findRevisions(submitterId, pageRequest);
        return ResponseFactory.ok(revisions).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/rev/latest")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getCurrentRevision(@PathParam("id") final String submitterId) {
        try {
            return ResponseFactory.ok(this.store.getCurrentRevision(submitterId)).build();
        } catch (final NoSuchElementException e1) {
            return ResponseFactory.notFound().build();
        }
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/compare")
    public Response compare(@PathParam("id") final String submitterId,
        @QueryParam("originalRevision") final Integer originalRevision,
        @QueryParam("revisedRevision") final Integer revisedRevision) throws IOException {

        return ResponseFactory.ok(this.store.compare(submitterId, originalRevision, revisedRevision)).build();
    }

    /**
     * @param submitterId
     * @param submitter
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Update a submitter", responses = { @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = SubmitterRequest.class))) })
    public Response update(@PathParam("id") final String submitterId, final SubmitterRequest submitter) {
        checkNotNull(submitter, "submitter");
        isTrue(!Strings.isNullOrEmpty(submitterId));
        isTrue(submitter.getSubmitterId().equals(submitterId));
        return ResponseFactory.ok(submissionService.updateSubmitter(submitter)).build();
    }

    /**
     * @param submitter
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Create a submitter", responses = { @ApiResponse(responseCode = "201",
            content = @Content(schema = @Schema(implementation = SubmitterRequest.class))) })
    public Response create(final SubmitterRequest submitter) {
        return ResponseFactory.status(Status.CREATED).entity(submissionService.createSubmitter(submitter)).build();
    }

    /**
     * @param submitterId
     * @return
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Path("{id}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Delete a submitter", responses = { @ApiResponse(responseCode = "204",
            content = @Content(schema = @Schema(implementation = SubmitterRequest.class))) })
    public Response delete(
        @Parameter(description = "submitterId to delete", required = true) @PathParam("id") final String submitterId) {
        try {
            store.remove(submitterId);
            return ResponseFactory.status(Status.NO_CONTENT).build();
        } catch (final EmptyResultDataAccessException ex) {
            return ResponseFactory.status(Status.NOT_FOUND).build();
        }

    }

    /**
     * @param file
     * @return
     * @throws Exception
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Path("/import")
    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importFile(@FormDataParam("file") final InputStream file) throws Exception {
        try {
            final ValidationResult result = this.eucegImportExportService.importSubmitterFromExcel(file);
            if (result.hasFailures()) {
                return ResponseFactory.status(Status.NOT_ACCEPTABLE).entity(result).build();
            }
            return ResponseFactory.ok(result).build();
        } finally {
            Closeables.closeQuietly(file);
        }
    }

}
