package com.pmi.tpd.web.rest.rsrc.api.euceg;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.paging.PageRequest;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.core.elasticsearch.IIndexerOperations;
import com.pmi.tpd.core.elasticsearch.model.AttachmentIndexed;
import com.pmi.tpd.core.euceg.AttachmentInvalidFilenaneException;
import com.pmi.tpd.core.euceg.AttachmentRequest;
import com.pmi.tpd.core.euceg.AttachmentUpdate;
import com.pmi.tpd.core.euceg.ConcurrencyAttachmentAccessException;
import com.pmi.tpd.core.euceg.DownloadableAttachment;
import com.pmi.tpd.core.euceg.IAttachmentService;
import com.pmi.tpd.core.euceg.filestorage.DirectoryUpateRequest;
import com.pmi.tpd.core.euceg.filestorage.MoveDirectoryRequest;
import com.pmi.tpd.core.euceg.filestorage.MoveFileRequest;
import com.pmi.tpd.core.model.euceg.AttachmentRevision;
import com.pmi.tpd.euceg.api.entity.IAttachmentEntity;
import com.pmi.tpd.web.core.hateoas.BaseResourceSupport;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.CreateAttachmentRequest;

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
@Path(RestApplication.API_RESOURCE_PATH + "/attachments")
@Tag(description = "Endpoint for Attachments", name = "business")
public class AttachmentResource extends BaseResourceSupport<AttachmentResource> {

    /** */
    private final IAttachmentService attachmentService;

    private final IIndexerOperations indexerOperations;

    /**
     * Default constructor.
     *
     * @param submissionService
     *                          a submission service.
     * @param attachmentStore
     *                          the attachment store.
     * @param indexerOperations
     *                          the indexer.
     */
    @Inject
    public AttachmentResource(final IAttachmentService attachmentService, final IIndexerOperations indexerOperations) {
        this.attachmentService = attachmentService;
        this.indexerOperations = indexerOperations;
    }

    /**
     * @param page
     *               zero-based page index.
     * @param size
     *               the size of the page to be returned.
     * @param sort
     *               the property to sort (can be {@literal null}).
     * @param filter
     *               the list of filter properties to use (can be {@literal null}), (see {@link PageRequest}).
     * @param search
     *               the search query.
     * @return Returns the requested page of users, which may be empty but never null.
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Timed
    @Operation(summary = "Gets list of attachments",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CollectionModel.class))) })
    public Response findAll(
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @Parameter(description = "filters attachment", required = false) @QueryParam("filter") final String filter,
        @Parameter(description = "search attachment with expression",
                required = false) @QueryParam("search") final String search) {
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, search);
        return ResponseFactory
                .ok(resources(AttachmentRequest.toResourcesForIndexed(indexerOperations.findAllAttachment(pageRequest),
                    assembler(AttachmentIndexed.class, AttachmentRequest.class))))
                .build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("ByFolder")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Timed
    @Operation(summary = "Gets files and directory for specific directory")
    public Response findAllByFolder(
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @Parameter(description = "filters attachment", required = false) @QueryParam("filter") final String filter,
        @Parameter(description = "search attachment with expression",
                required = false) @QueryParam("search") final String search,
        @Parameter(description = "current directory",
                required = false) @QueryParam("directory") final String directory) {
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, search);
        return ResponseFactory.ok(this.attachmentService.findAll(pageRequest, directory)).build();
    }

    /**
     * @param filename
     *                 the file name of attachment to find
     * @return Returns OK 200 and the list of attachments content representation, which may be empty but never null.
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("byFileName")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = AttachmentRequest.class))),
                    @ApiResponse(responseCode = "200",
                            description = "Gets list of attachment containing filename parameter"),
                    @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                            + " which prevented it from fulfilling the request.") })
    public Response findByFileName(@Parameter(description = "filename to search",
            required = false) @QueryParam("filename") final String filename) {

        return ResponseFactory.ok(AttachmentRequest.map(attachmentService.searchByFileName(filename))).build();
    }

    /**
     * @param uuid
     *             unique identifier of attachment
     * @return Returns OK 200 and the attachment content representation.
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets a attachment",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = AttachmentRequest.class))),
                    @ApiResponse(responseCode = "404", description = "The specified attachment does not exist."),
                    @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                            + " which prevented it from fulfilling the request.") })
    public Response get(@PathParam("id") final String uuid) {
        try {
            final AttachmentRequest resource = AttachmentRequest.from(attachmentService.getAttachment(uuid));
            return ResponseFactory.ok(resource).build();
        } catch (final JpaObjectRetrievalFailureException | EntityNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * @param filename
     *                 a file name of attachment to check
     * @return Returns OK status 200 if the attachment exists otherwise not found status 404.
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("exists/{filename}")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(responses = {
            @ApiResponse(responseCode = "200", description = "Gets indicating whether filename of attachment exists"),
            @ApiResponse(responseCode = "404", description = "The specified attachment does not exist."),
            @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                    + " which prevented it from fulfilling the request.") })
    public Response exists(@PathParam("filename") final String filename) {
        if (attachmentService.exists(filename)) {
            return ResponseFactory.ok().build();
        }
        return ResponseFactory.status(Status.NOT_FOUND).build();
    }

    /**
     * @param filename
     *                 the file name of attachment to get
     * @return Returns OK 200 and the attachment content representation.
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("filename/{filename}")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = AttachmentRequest.class))),
                    @ApiResponse(responseCode = "200", description = "Gets attachment with filename."),
                    @ApiResponse(responseCode = "404", description = "The specified attachment does not exist."),
                    @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                            + " which prevented it from fulfilling the request.") })
    public Response getByFileName(@PathParam("filename") final String filename) {
        try {
            final AttachmentRequest resource = AttachmentRequest
                    .from(attachmentService.getAttachmentByFilename(filename));
            return ResponseFactory.ok(resource).build();
        } catch (final JpaObjectRetrievalFailureException | EntityNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/rev")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getRevisions(@PathParam("id") final String id,
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @Parameter(description = "filters product", required = false) @QueryParam("filter") final String filter) {

        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);

        final Page<AttachmentRevision> result = this.attachmentService.findRevisions(id, pageRequest);
        return Response.ok(result).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{id}/rev/latest")
    @Timed
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Gets the latest revision of attachment",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = AttachmentRevision.class))))
    public Response getLatestRevision(@PathParam("id") final String uuid) {
        try {
            return ResponseFactory.ok(this.attachmentService.getCurrentRevision(uuid)).build();
        } catch (final NoSuchElementException e1) {
            return ResponseFactory.notFound().build();
        }
    }

    /**
     * @param uuid
     *                   the unique identifier of attachment
     * @param attachment
     *                   the attachment to update
     * @return Returns OK 200 and the updated attachment content representation.
     * @throws ConcurrencyAttachmentAccessException
     *                                              if trying update of an attachment that it is sending.
     * @throws AttachmentInvalidFilenaneException
     */
    @RolesAllowed(ApplicationConstants.Authorities.ADMIN)
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = AttachmentRequest.class))),
                    @ApiResponse(responseCode = "200", description = "Update a attachment."),
                    @ApiResponse(responseCode = "404", description = "The specified attachment does not exist."),
                    @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                            + " which prevented it from fulfilling the request.") })
    public Response update(@PathParam("id") final String uuid, @Valid final AttachmentUpdate attachment)
            throws ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        try {
            return ResponseFactory.ok(this.attachmentService.updateAttachment(attachment)).build();
        } catch (final JpaObjectRetrievalFailureException | EntityNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * @param file
     *                   a input file associated attachment to create
     * @param attachment
     *                   attachment to create.
     * @return Return the created attachment.
     * @throws ConcurrencyAttachmentAccessException
     *                                              if trying update of an attachment that it is sending.
     * @throws IOException
     *                                              if I/O errors occurs
     * @throws AttachmentInvalidFilenaneException
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = AttachmentRequest.class))),
                    @ApiResponse(responseCode = "200", description = "Create a attachment."),
                    @ApiResponse(responseCode = "404", description = "The specified attachment does not exist."),
                    @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                            + " which prevented it from fulfilling the request.") })
    public Response create(final CreateAttachmentRequest attachment, @FormDataParam("file") final InputStream file)
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        final IAttachmentEntity attachmentEntity = this.attachmentService.storeAttachment(file,
            attachment.getFilename(),
            attachment.getContentType(),
            attachment.isConfidential());
        return ResponseFactory.status(Status.CREATED).entity(AttachmentRequest.from(attachmentEntity)).build();

    }

    /**
     * @param uuid
     *             unique identifier of attachment
     * @return Returns OK status (200) without content response.
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Path("{id}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "delete a attachment")
    public Response delete(
        @Parameter(description = "attachment to delete", required = true) @PathParam("id") final String uuid) {
        this.attachmentService.deleteAttachment(uuid);
        return ResponseFactory.ok().build();
    }

    /**
     * @param uuid
     * @return
     * @throws IOException
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Timed
    @Path("download")
    @Produces(MediaType.WILDCARD)
    public Response downloadFile(@QueryParam("uuid") final String uuid) throws IOException {
        final DownloadableAttachment request = this.attachmentService.getDownloadableAttachment(uuid);
        final ResponseBuilder resp = Response.ok(request.openStream())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + request.getFilename() + "\"");
        request.getContentType().ifPresent(contentType -> resp.type(MediaType.valueOf(contentType)));

        return resp.build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Timed
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFileHttp(@FormDataParam("file") final FormDataBodyPart file,
        @FormDataParam("file") final InputStream fileInputStream)
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        final MediaType type = file.getMediaType();
        if (!"pdf".equals(type.getSubtype())) {
            return ResponseFactory.badRequest("file type no accepted").build();
        }
        return ResponseFactory.ok(AttachmentRequest.from(this.attachmentService.storeAttachment(fileInputStream,
            file.getContentDisposition().getFileName(),
            file.getMediaType().toString(),
            true))).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("/directories")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(responses = { @ApiResponse(responseCode = "200", description = "get all directories."),
            @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                    + " which prevented it from fulfilling the request.") })
    public Response getWalkTreeDirectories() {
        return ResponseFactory.ok(this.attachmentService.getTreeDirectory()).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Path("/directories/{directory:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(responses = { @ApiResponse(responseCode = "200", description = "Create a directory."),
            @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                    + " which prevented it from fulfilling the request.") })
    public Response createDirectory(@Parameter(description = "directory to create",
            required = true) @PathParam("directory") final String directory) {
        return ResponseFactory.status(Status.CREATED).entity(this.attachmentService.createDirectory(directory)).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @PUT
    @Path("/directories/{directory:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(responses = { @ApiResponse(responseCode = "200", description = "rename a directory."),
            @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                    + " which prevented it from fulfilling the request.") })
    public Response updateDirectory(
        @Parameter(description = "directory to update", required = true) @PathParam("directory") final String directory,
        final @Valid DirectoryUpateRequest request) {
        return ResponseFactory.accepted().entity(this.attachmentService.renameDirectory(directory, request)).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @DELETE
    @Path("/directories/{directory:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(responses = { @ApiResponse(responseCode = "200", description = "Delete a directory."),
            @ApiResponse(responseCode = "404", description = "The specified directory does not exist."),
            @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                    + " which prevented it from fulfilling the request.") })
    public Response deleteDirectory(@Parameter(description = "directory to delete",
            required = true) @PathParam("directory") final String directory) {
        if (this.attachmentService.deleteDirectory(directory)) {
            return ResponseFactory.accepted().build();
        }
        return ResponseFactory.notFound().build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Path("/moveFileTo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(responses = { @ApiResponse(responseCode = "200", description = "Move a file."),
            @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                    + " which prevented it from fulfilling the request.") })
    public Response moveFileTo(@Valid final MoveFileRequest request) throws IOException {
        return ResponseFactory.accepted().entity(attachmentService.moveFileTo(request)).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Path("/moveDirectoryTo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(responses = { @ApiResponse(responseCode = "200", description = "Move a directory."),
            @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
                    + " which prevented it from fulfilling the request.") })
    public Response moveDirectoryTo(@Valid final MoveDirectoryRequest request) throws IOException {
        return ResponseFactory.accepted().entity(this.attachmentService.moveDirectoryTo(request)).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("/healthy")
    @Timed
    @Operation(responses = { @ApiResponse(responseCode = "200", description = "File data is healthy."),
            @ApiResponse(responseCode = "400", description = "File data is corrupted") })
    public Response checkIntegrity(@QueryParam("filename") final String filename) throws IOException {
        try {
            this.attachmentService.checkIntegrity(filename);

        } catch (Throwable ex) {
            return ResponseFactory.badRequest(ex.getMessage()).build();
        }
        return ResponseFactory.ok().build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Path("/healthy")
    @Timed
    @Operation(responses = { @ApiResponse(responseCode = "200", description = "File data is healthy."),
            @ApiResponse(responseCode = "400", description = "File data is corrupted") })
    public Response fixIntegrity(@QueryParam("filename") final String filename) throws IOException {
        this.attachmentService.fixIntegrity(filename);
        return ResponseFactory.ok().build();
    }
}
